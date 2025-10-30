package barang;

import page.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Tambah / Edit detail barang — desain persis seperti yang kamu minta.
 * - Jika DetailContext.editingDetailId != null -> mode edit (muat data)
 * - Jika DetailContext.parentBarangId / BarangContext.editingId tersedia -> prefill kode barang
 * - Menggunakan DetailBarangDAO untuk insert/update/delete
 */
public class tambahdetailbarang extends JPanel {

    private RoundedTextField fldKodeBarang, fldBarcode, fldStok, fldHarga, fldExpired;
    private DetailBarangDAO detailDao;

    public tambahdetailbarang() {
        try { detailDao = new DetailBarangDAO(); } catch (Exception ex) { detailDao = null; }

        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));
        setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("Tambah Detail Barang", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 24));
        title.setForeground(new Color(40, 40, 40));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setIcon(new ImageIcon(getClass().getResource("/Icon/tambahbarang.png"))); // ganti sesuai path

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(imageLabel, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(40, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,15,10,15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        fldKodeBarang = new RoundedTextField(12); fldKodeBarang.setEditable(false);
        fldBarcode = new RoundedTextField(12);
        fldStok = new RoundedTextField(12);
        fldHarga = new RoundedTextField(12);
        fldExpired = new RoundedTextField(12);

        addField(formPanel, gbc, 0, "Kode Barang:", fldKodeBarang);
        addField(formPanel, gbc, 1, "Barcode:", fldBarcode);
        addField(formPanel, gbc, 2, "Stok:", fldStok);
        addField(formPanel, gbc, 3, "Harga Jual:", fldHarga);
        addField(formPanel, gbc, 4, "Expired:", fldExpired);

        RoundedButton btnKembali = new RoundedButton("Kembali", new Color(235,235,235), new Color(60,60,60));
        RoundedButton btnSimpan = new RoundedButton("Simpan", new Color(46,204,113), Color.WHITE);
        RoundedButton btnHapus = new RoundedButton("Hapus", new Color(220,20,60), Color.WHITE);
        btnKembali.setPreferredSize(new Dimension(140,45));
        btnSimpan.setPreferredSize(new Dimension(140,45));
        btnHapus.setPreferredSize(new Dimension(120,45));

        btnKembali.addActionListener(e -> {
            DetailContext.parentBarangId = null;
            DetailContext.editingDetailId = null;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataBarangPanel();
        });

        btnSimpan.addActionListener(e -> onSave());
        btnHapus.addActionListener(e -> onDelete());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnKembali);
        bottomPanel.add(btnHapus);
        bottomPanel.add(btnSimpan);

        add(topPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // load when shown
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED)!=0 && isShowing()) {
                SwingUtilities.invokeLater(() -> loadContext());
            }
        });
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int gridx, String labelText, JComponent comp) {
        int row = gridx / 3;
        int col = gridx % 3;
        gbc.gridx = col; gbc.gridy = row;
        JPanel fieldPanel = new JPanel(new BorderLayout(5,5));
        fieldPanel.setOpaque(false);
        JLabel label = new JLabel(labelText); label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(comp, BorderLayout.CENTER);
        panel.add(fieldPanel, gbc);
    }

    private void loadContext() {
        // determine parent barang id from DetailContext or BarangContext
        Integer parentId = DetailContext.parentBarangId;
        if (parentId == null && BarangContext.editingId != null) parentId = BarangContext.editingId;
        if (parentId == null) fldKodeBarang.setText("");
        else fldKodeBarang.setText(String.valueOf(parentId));

        if (DetailContext.editingDetailId != null && detailDao != null) {
            // load detail for edit
            try {
                DetailBarang d = detailDao.findById(DetailContext.editingDetailId);
                if (d != null) {
                    fldKodeBarang.setText(String.valueOf(d.getIdBarang()));
                    fldBarcode.setText(d.getBarcode());
                    fldStok.setText(String.valueOf(d.getStok()));
                    fldHarga.setText(d.getHargaJual() == null ? "" : d.getHargaJual().toPlainString());
                    fldExpired.setText(d.getTanggalExp());
                } else {
                    JOptionPane.showMessageDialog(this, "Detail barang tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
                    DetailContext.editingDetailId = null;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal memuat detail:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // new detail
            fldBarcode.setText("");
            fldStok.setText("0");
            fldHarga.setText("");
            fldExpired.setText("");
        }
    }

    private void onSave() {
        if (detailDao == null) { JOptionPane.showMessageDialog(this, "Database tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        Integer idBarang = null;
        try { idBarang = Integer.parseInt(fldKodeBarang.getText().trim()); } catch (Exception ex) {}
        if (idBarang == null) { JOptionPane.showMessageDialog(this, "ID barang tidak valid. Pastikan kamu membuka form dari daftar barang.", "Validasi", JOptionPane.WARNING_MESSAGE); return; }

        String barcode = fldBarcode.getText().trim();
        int stok = 0;
        try { stok = Integer.parseInt(fldStok.getText().trim()); } catch (Exception ex) { stok = 0; }
        BigDecimal harga = BigDecimal.ZERO;
        try {
            String h = fldHarga.getText().trim();
            if (!h.isEmpty()) {
                // accept thousand separators like 1.000.000 or 1,000,000
                String cleaned = h.replaceAll("[.,]", "");
                harga = new BigDecimal(cleaned);
            }
        } catch (Exception ex) { harga = BigDecimal.ZERO; }
        String exp = fldExpired.getText().trim();

        try {
            if (DetailContext.editingDetailId == null) {
                DetailBarang d = new DetailBarang();
                d.setIdBarang(idBarang);
                d.setBarcode(barcode);
                d.setStok(stok);
                d.setHargaJual(harga);
                d.setTanggalExp(exp);
                d.setIdSupplier(null);
                int newId = detailDao.insert(d);
                if (newId > 0) {
                    JOptionPane.showMessageDialog(this, "Detail barang berhasil ditambahkan (id=" + newId + ").", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    DetailContext.parentBarangId = null;
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataBarangPanel();
                } else JOptionPane.showMessageDialog(this, "Insert gagal (tidak mendapatkan id).", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                DetailBarang d = new DetailBarang();
                d.setId(DetailContext.editingDetailId);
                d.setIdBarang(idBarang);
                d.setBarcode(barcode);
                d.setStok(stok);
                d.setHargaJual(harga);
                d.setTanggalExp(exp);
                detailDao.update(d);
                JOptionPane.showMessageDialog(this, "Detail barang berhasil diupdate.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                DetailContext.editingDetailId = null;
                DetailContext.parentBarangId = null;
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataBarangPanel();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan detail:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (detailDao == null) { JOptionPane.showMessageDialog(this, "Database tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        if (DetailContext.editingDetailId == null) {
            JOptionPane.showMessageDialog(this, "Tidak ada detail yang dipilih untuk dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Hapus detail barang (ID: " + DetailContext.editingDetailId + ") ?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            detailDao.delete(DetailContext.editingDetailId);
            JOptionPane.showMessageDialog(this, "Detail berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            DetailContext.editingDetailId = null;
            DetailContext.parentBarangId = null;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataBarangPanel();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus detail:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // === Rounded TextField & Button sama persis desainnya ===
    class RoundedButton extends JButton {
        private final Color backgroundColor;
        private final Color textColor;
        private int radius = 25;
        public RoundedButton(String text, Color bg, Color fg) { super(text); this.backgroundColor = bg; this.textColor = fg; setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false); setFont(new Font("Segoe UI", Font.BOLD, 15)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < 6; i++) { g2.setColor(new Color(0, 0, 0, 10 - i)); g2.fillRoundRect(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, radius, radius); }
            g2.setColor(backgroundColor); g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, radius, radius);
            g2.setColor(textColor); FontMetrics fm = g2.getFontMetrics(); int textX = (getWidth() - fm.stringWidth(getText())) / 2; int textY = (getHeight() + fm.getAscent()) / 2 - 3; g2.drawString(getText(), textX, textY); g2.dispose();
        }
    }

    class RoundedTextField extends JTextField {
        private int radius = 15;
        public RoundedTextField(int size) {
            super(size);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
