package supplier;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * editdatasupplier - desain persis seperti yang kamu minta.
 * Tambahan: HierarchyListener untuk memastikan loadFromContext() terpanggil
 * ketika panel benar-benar ditampilkan (robust terhadap cara Mainmenu menambahkan panel).
 */
public class editdatasupplier extends JPanel {

    private RoundedTextField fldKode;
    private RoundedTextField fldNama;
    private RoundedTextField fldAlamat;
    private RoundedTextField fldTelp;

    private SupplierDAO dao;

    public editdatasupplier() {
        try {
            dao = new SupplierDAO();
        } catch (Exception ex) {
            dao = null;
            JOptionPane.showMessageDialog(this, "Gagal inisialisasi SupplierDAO:\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
        initUI();

        // saat panel ditampilkan, muat data supplier dari SupplierContext jika ada
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> loadFromContext());
            }
        });

        // tambahan: HierarchyListener untuk mendeteksi perubahan "showing" (lebih andal)
        this.addHierarchyListener(e -> {
            // cek apakah flag SHOWING_CHANGED berubah dan panel sekarang showing
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(() -> loadFromContext());
            }
        });
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));
        setBorder(new EmptyBorder(30, 50, 30, 50));

        // ===== Bagian atas =====
        JLabel title = new JLabel("Edit Data Supplier", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 24));
        title.setForeground(new Color(40, 40, 40));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setIcon(new ImageIcon(getClass().getResource("/Icon/tambahbarang.png"))); // ganti sesuai path

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(imageLabel, BorderLayout.CENTER);

        // ===== Form input (tengah) =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(40, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Baris input
        addField(formPanel, gbc, 0, "Kode Supplier:");
        addField(formPanel, gbc, 1, "Nama Supplier:");
        addField(formPanel, gbc, 2, "Alamat Supplier:");
        addField(formPanel, gbc, 3, "No. Telp:");

        // ===== Tombol =====
        RoundedButton btnKembali = new RoundedButton("Kembali", new Color(235, 235, 235), new Color(60, 60, 60));
        RoundedButton btnSimpan = new RoundedButton("Simpan", new Color(46, 204, 113), Color.WHITE);

        btnKembali.setPreferredSize(new Dimension(140, 45));
        btnSimpan.setPreferredSize(new Dimension(140, 45));

        btnSimpan.addActionListener(e -> onSave());
        btnKembali.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) {
                ((uiresponsive.Mainmenu) frame).showDataSupplier();
            }
            SupplierContext.editingId = null;
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnKembali);
        bottomPanel.add(btnSimpan);

        // ===== Tambahkan ke panel utama =====
        add(topPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // === Helper: Field input ===
    private void addField(JPanel panel, GridBagConstraints gbc, int gridx, String labelText) {
        int row = gridx / 3;
        int col = gridx % 3;

        gbc.gridx = col;
        gbc.gridy = row;

        JPanel fieldPanel = new JPanel(new BorderLayout(5, 5));
        fieldPanel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        RoundedTextField field = new RoundedTextField(12);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(field, BorderLayout.CENTER);

        panel.add(fieldPanel, gbc);

        String key = labelText.toLowerCase();
        if (key.contains("kode")) {
            fldKode = field;
            fldKode.setEditable(false);
        } else if (key.contains("nama")) fldNama = field;
        else if (key.contains("alamat")) fldAlamat = field;
        else if (key.contains("telp") || key.contains("no.")) fldTelp = field;
    }

    private void loadFromContext() {
        if (SupplierContext.editingId == null) return;
        if (dao == null) return;
        try {
            Supplier s = dao.findById(SupplierContext.editingId);
            if (s == null) {
                JOptionPane.showMessageDialog(this, "Data supplier tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            fldKode.setText(s.getIdSupplier() == null ? "" : String.valueOf(s.getIdSupplier()));
            fldNama.setText(s.getNamaSupplier());
            fldAlamat.setText(s.getAlamatSupplier());
            fldTelp.setText(s.getNotelpSupplier());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data supplier:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        if (dao == null) {
            JOptionPane.showMessageDialog(this, "Database tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String nama = fldNama.getText().trim();
        String alamat = fldAlamat.getText().trim();
        String telp = fldTelp.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama supplier harus diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer id = null;
        try {
            if (fldKode.getText() != null && !fldKode.getText().trim().isEmpty()) {
                id = Integer.parseInt(fldKode.getText().trim());
            }
        } catch (NumberFormatException ignored) {}
        if (id == null) {
            JOptionPane.showMessageDialog(this, "ID supplier tidak valid untuk update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Supplier s = new Supplier(id, nama, alamat, telp);
            dao.update(s);
            JOptionPane.showMessageDialog(this, "✅ Data supplier berhasil diedit!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            SupplierContext.editingId = null;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) {
                ((uiresponsive.Mainmenu) frame).showDataSupplier();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan perubahan:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // === Rounded TextField & Button sama persis desainnya ===
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
            super.paintComponent(g);
            g2.dispose();
        }
    }

    class RoundedButton extends JButton {
        private final Color backgroundColor;
        private final Color textColor;
        private int radius = 25;
        public RoundedButton(String text, Color bg, Color fg) {
            super(text);
            this.backgroundColor = bg;
            this.textColor = fg;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < 6; i++) {
                g2.setColor(new Color(0, 0, 0, 10 - i));
                g2.fillRoundRect(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, radius, radius);
            }
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, radius, radius);
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(getText())) / 2;
            int textY = (getHeight() + fm.getAscent()) / 2 - 3;
            g2.drawString(getText(), textX, textY);
            g2.dispose();
        }
    }
}
