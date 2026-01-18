package barang;

import page.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.io.File; // [PENTING] Tambahan import untuk cek file
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.toedter.calendar.JDateChooser;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import Helper.DatabaseHelper;

/**
 * editdatabarang - layout fixed:
 * Row0: Kode | Barcode | Stok
 * Row1: Harga Jual | Expired | Harga Beli
 * Row2: Nama Barang | Kategori | Supplier
 * * Pemanggilan gambar sudah diperbaiki (Hybrid) agar jalan di EXE.
 */
public class editdatabarang extends JPanel {
    private RoundedTextField txtKode;
    private RoundedTextField txtBarcode;
    private RoundedTextField txtStok;
    private RoundedTextField txtHarga;
    private RoundedTextField txtHargaBeli; // <-- TAMBAHKAN INI
    private JDateChooser dateExpired;
    private RoundedTextField txtNama;
    private RoundedTextField txtKategori;
    private RoundedTextField txtSupplier;

    private String selectedKategoriId = null;
    private String selectedSupplierId = null;

    private BarangDAO barangDao;
    private DetailBarangDAO detailDao;

    public editdatabarang() {
        try {
            barangDao = new BarangDAO();
            detailDao = new DetailBarangDAO();
        } catch (Exception ex) {
            barangDao = null;
            detailDao = null;
            JOptionPane.showMessageDialog(this, "Gagal inisialisasi DAO:\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));
        setBorder(new EmptyBorder(20, 40, 20, 40));

        JLabel title = new JLabel("Edit Data Barang", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        title.setForeground(new Color(40, 40, 40));

        // Tambahkan icon/gambar tanpa mengubah UI lain
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // [UBAH DI SINI] Gunakan helper method hybrid
        imageLabel.setIcon(loadTopImage("tambahbarang.png"));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(imageLabel, BorderLayout.CENTER);

        // Form with GridBag: explicit placement for each cell (comfortable to read)
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 0, 10, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Row 0: Kode | Barcode | Stok
        gbc.gridx = 0; gbc.gridy = 0;
        txtKode = createField(form, gbc, "Kode Barang:");
        gbc.gridx = 1;
        txtBarcode = createField(form, gbc, "Barcode:");
        gbc.gridx = 2;
        txtStok = createField(form, gbc, "Stok:");

        // Row 1: Harga Jual | Expired | Harga Beli
        gbc.gridx = 0; gbc.gridy = 1;
        txtHarga = createField(form, gbc, "Harga Jual:");
        gbc.gridx = 1;
        JPanel expirePanel = new JPanel(new BorderLayout(4,4)); expirePanel.setOpaque(false);
        JLabel lblExp = new JLabel("Expired:"); lblExp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateExpired = new JDateChooser(); dateExpired.setDateFormatString("yyyy-MM-dd"); dateExpired.setPreferredSize(new Dimension(0, 36));
        expirePanel.add(lblExp, BorderLayout.NORTH);
        expirePanel.add(dateExpired, BorderLayout.CENTER);
        form.add(expirePanel, gbc);
        gbc.gridx = 2;
        txtHargaBeli = createField(form, gbc, "Harga Beli:");

        // Row 2: Nama Barang | Kategori | Supplier
        gbc.gridx = 0; gbc.gridy = 2;
        txtNama = createField(form, gbc, "Nama Barang:");
        gbc.gridx = 1;
        txtKategori = createField(form, gbc, "Kategori Barang:");
        gbc.gridx = 2;
        txtSupplier = createField(form, gbc, "Supplier:");

        // kategori & supplier behave as pickers (click) but only enabled in per-detail
        txtKategori.setEditable(false);
        txtKategori.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtKategori.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (txtKategori.isEnabled()) new PilihKategoriFrame(txtKategori);
            }
        });

        txtSupplier.setEditable(false);
        txtSupplier.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtSupplier.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (txtSupplier.isEnabled()) new PilihSupplierFrame(txtSupplier);
            }
        });

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12));
        btnPanel.setOpaque(false);
        RoundedButton btnBack = new RoundedButton("Kembali", new Color(230,230,230), new Color(60,60,60));
        RoundedButton btnSave = new RoundedButton("Simpan", new Color(46,204,113), Color.WHITE);
        btnBack.setPreferredSize(new Dimension(140,42));
        btnSave.setPreferredSize(new Dimension(140,42));
        btnBack.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataBarangPanel();
        });
        btnSave.addActionListener(e -> saveAction());
        btnPanel.add(btnBack); btnPanel.add(btnSave);

        add(topPanel, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // load on show
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(() -> loadFromContext());
            }
        });
    }

    private RoundedTextField createField(JPanel parent, GridBagConstraints gbc, String labelText) {
        JPanel p = new JPanel(new BorderLayout(6,6)); p.setOpaque(false);
        JLabel lbl = new JLabel(labelText); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        RoundedTextField tf = new RoundedTextField(12);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(lbl, BorderLayout.NORTH);
        p.add(tf, BorderLayout.CENTER);
        parent.add(p, gbc);
        return tf;
    }

    private void saveAction() {
        Integer editingId = null;
        try { editingId = BarangContext.editingId; } catch (Throwable t) { editingId = null; }
        Integer editingDetailId = null;
        try { editingDetailId = DetailContext.editingDetailId; } catch (Throwable t) { editingDetailId = null; }

        if (editingId == null && editingDetailId == null) {
            JOptionPane.showMessageDialog(this, "Tidak ada ID yang sedang diedit.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (barangDao == null || detailDao == null) {
            JOptionPane.showMessageDialog(this, "DAO belum terinisialisasi.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nama = txtNama.getText().trim();

        // resolve kategori & supplier ids if text provided
        String kategoriId = selectedKategoriId;
        if ((kategoriId == null || kategoriId.trim().isEmpty()) && txtKategori.getText() != null && !txtKategori.getText().trim().isEmpty()) {
            kategoriId = getKategoriIdByName(txtKategori.getText().trim());
        }
        String supplierId = selectedSupplierId;
        if ((supplierId == null || supplierId.trim().isEmpty()) && txtSupplier.getText() != null && !txtSupplier.getText().trim().isEmpty()) {
            supplierId = getSupplierIdByName(txtSupplier.getText().trim());
        }

        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama barang tidak boleh kosong.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // update barang if editingId present (group mode or per-detail updating category)
            if (editingId != null) {
                Barang b = new Barang();
                b.setId(editingId);
                b.setNama(nama);
                // update kategori only if user selected / in per-detail mode we will update anyway
                if (kategoriId != null && !kategoriId.trim().isEmpty()) b.setIdKategori(kategoriId);
                barangDao.update(b);
            }

            // determine target detail
            DetailBarang target = null;
            if (editingDetailId != null) target = detailDao.findById(editingDetailId);
            else if (editingId != null) {
                List<DetailBarang> dets = detailDao.findByBarangId(editingId);
                if (dets != null && !dets.isEmpty()) target = dets.get(0);
            }

            if (target != null) {
                target.setBarcode(txtBarcode.getText().trim());
                try { target.setStok(Integer.parseInt(txtStok.getText().trim())); } catch (Exception ex) { /* keep */ }

                String h = txtHarga.getText().trim();
                if (!h.isEmpty()) {
                    String cleaned = cleanNumberString(h);
                    try { target.setHargaJual(new BigDecimal(cleaned)); } catch (Exception ex) {}
                }

                // ----- simpan harga beli (jika diisi) -----
                String hb = txtHargaBeli.getText().trim();
                if (!hb.isEmpty()) {
                    String cleanedHb = cleanNumberString(hb);
                    try {
                        java.math.BigDecimal hargaBeliNew = new java.math.BigDecimal(cleanedHb);
                        // hanya simpan ke table detail_pembelian jika target punya id_detail_pembelian
                        Integer idDetPemb = target.getIdDetailPembelian();
                        if (idDetPemb != null) {
                            updateDetailPembelianHargaBeli(idDetPemb, hargaBeliNew); // method helper di bawah
                        } else {
                            // jika tidak ada id_detail_pembelian, abaikan atau beri notifikasi jika mau
                        }
                    } catch (NumberFormatException ex) {
                        // abaikan jika parsing gagal (harga jual tetap disimpan)
                    }
                }

                Date d = dateExpired.getDate();
                if (d != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    target.setTanggalExp(sdf.format(d));
                } else target.setTanggalExp(null);

                // supplier only saved in per-detail mode
                if (editingDetailId != null) {
                    if (supplierId != null && !supplierId.trim().isEmpty()) {
                        try { target.setIdSupplier(Integer.valueOf(supplierId)); } catch (Exception ex) {}
                    } else {
                        target.setIdSupplier(null);
                    }
                }

                detailDao.update(target);

                // if user changed kategori in per-detail mode, update parent barang (ensure id obtained)
                if (editingDetailId != null && kategoriId != null && !kategoriId.trim().isEmpty()) {
                    Barang parent = new Barang();
                    parent.setId(target.getIdBarang());
                    parent.setNama(nama);
                    parent.setIdKategori(kategoriId);
                    barangDao.update(parent);
                }
            }

            JOptionPane.showMessageDialog(this, "Perubahan disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataBarangPanel();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan perubahan:\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadFromContext() {
        Integer editingId = null;
        try { editingId = BarangContext.editingId; } catch (Throwable t) { editingId = null; }
        Integer editingDetailId = null;
        try { editingDetailId = DetailContext.editingDetailId; } catch (Throwable t) { editingDetailId = null; }

        clearFields();
        selectedKategoriId = null;
        selectedSupplierId = null;

        if (editingId == null && editingDetailId == null) return;

        if (barangDao == null || detailDao == null) {
            JOptionPane.showMessageDialog(this, "DAO null", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Barang parent = null;
            if (editingId != null) {
                List<Barang> all = barangDao.findAll();
                if (all != null) {
                    for (Barang b: all) if (b.getId() == editingId) { parent = b; break; }
                }
            }

            DetailBarang detail = null;
            if (editingDetailId != null) {
                detail = detailDao.findById(editingDetailId);
                if (detail != null && parent == null) {
                    List<Barang> all = barangDao.findAll();
                    if (all != null) {
                        for (Barang b: all) if (b.getId() == detail.getIdBarang()) { parent = b; break; }
                    }
                }
            } else if (editingId != null) {
                List<DetailBarang> dets = detailDao.findByBarangId(editingId);
                if (dets != null && !dets.isEmpty()) detail = dets.get(0);
            }

            if (parent != null) {
                txtKode.setText(String.valueOf(parent.getId()));
                txtNama.setText(parent.getNama() == null ? "" : parent.getNama());
                selectedKategoriId = parent.getIdKategori();
                txtKategori.setText(parent.getNamaKategori() == null ? "" : parent.getNamaKategori());
            }

            if (detail != null) {
                txtBarcode.setText(detail.getBarcode() == null ? "" : detail.getBarcode());
                txtStok.setText(String.valueOf(detail.getStok()));
                if (detail.getHargaJual() != null) {
                    NumberFormat nf = NumberFormat.getInstance(new Locale("in","ID"));
                    txtHarga.setText(nf.format(detail.getHargaJual()));
                } else txtHarga.setText("");

                if (detail.getHargaBeli() != null) {
                    NumberFormat nf2 = NumberFormat.getInstance(new Locale("in","ID"));
                    txtHargaBeli.setText(nf2.format(detail.getHargaBeli()));
                } else {
                    txtHargaBeli.setText("");
                }

                if (detail.getTanggalExp() != null && !detail.getTanggalExp().trim().isEmpty()) {
                    try { dateExpired.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(detail.getTanggalExp())); } catch (ParseException ex) { dateExpired.setDate(null); }
                } else dateExpired.setDate(null);

                if (detail.getIdSupplier() != null) selectedSupplierId = String.valueOf(detail.getIdSupplier());
                txtSupplier.setText(detail.getNamaSupplier() == null ? "" : detail.getNamaSupplier());
            }

            boolean perDetail = (editingDetailId != null);

            // enable category & supplier only in per-detail
            txtKategori.setEnabled(perDetail);
            txtSupplier.setEnabled(perDetail);

            txtKategori.setToolTipText(perDetail ? "Klik untuk pilih kategori" : "Kategori dapat diubah di mode per-detail");
            txtSupplier.setToolTipText(perDetail ? "Klik untuk pilih supplier" : "Supplier dapat diubah di mode per-detail");

            // detail fields enabled if there's a detail OR per-detail mode
            boolean enableDetailFields = perDetail || (detail != null);
            txtBarcode.setEnabled(enableDetailFields);
            txtStok.setEnabled(enableDetailFields);
            txtHarga.setEnabled(enableDetailFields);
            txtHargaBeli.setEnabled(enableDetailFields);
            dateExpired.setEnabled(enableDetailFields);

            revalidate(); repaint();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal load:\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        txtKode.setText("");
        txtBarcode.setText("");
        txtStok.setText("");
        txtHarga.setText("");
        txtHargaBeli.setText("");
        dateExpired.setDate(null);
        txtNama.setText("");
        txtKategori.setText("");
        txtSupplier.setText("");
        txtKategori.setEnabled(false);
        txtSupplier.setEnabled(false);
    }

    // ============================================================
    // Update helper: tulis harga_beli ke detail_pembelian
    // ============================================================
    private void updateDetailPembelianHargaBeli(int idDetailPembelian, BigDecimal hargaBeli) throws SQLException {
        String sql = "UPDATE detail_pembelian SET harga_beli = ? WHERE id_detail_pembelian = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hargaBeli == null ? BigDecimal.ZERO.toPlainString() : hargaBeli.toPlainString());
            ps.setInt(2, idDetailPembelian);
            ps.executeUpdate();
        }
    }

    // ============================================================
    // [BARU] HELPER METHOD UNTUK IMAGE (HYBRID EXE/NETBEANS)
    // ============================================================
    private ImageIcon loadTopImage(String fileName) {
        // 1. CARA EXE: Cek folder luar "icon/"
        String pathDisk = "icon/" + fileName;
        File f = new File(pathDisk);

        if (f.exists()) {
            return new ImageIcon(pathDisk);
        }

        // 2. CARA NETBEANS: Cek resource internal "/Icon/"
        java.net.URL url = getClass().getResource("/Icon/" + fileName);
        if (url != null) {
            return new ImageIcon(url);
        }

        // 3. Gagal total
        System.err.println("Gambar header tidak ditemukan (Disk/Res): " + fileName);
        return null;
    }
    // ============================================================

    // helper UI classes follow (RoundedTextField, RoundedButton)
    class RoundedTextField extends JTextField {
        private int radius = 12;
        public RoundedTextField(int size) {
            super(size);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);
            g2.setColor(new Color(200,200,200));
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    class RoundedButton extends JButton {
        private final Color bg; private final Color fg;
        public RoundedButton(String txt, Color bg, Color fg) { super(txt); this.bg = bg; this.fg = fg; setOpaque(false); setFocusPainted(false); setBorderPainted(false); setFont(new Font("Segoe UI", Font.BOLD, 14)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,18,18);
            g2.setColor(fg);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth()-fm.stringWidth(getText()))/2;
            int y = (getHeight()+fm.getAscent())/2-3;
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    // PilihKategoriFrame & PilihSupplierFrame (sama implementasi seperti sebelumnya)
    class PilihKategoriFrame extends JFrame {
        public PilihKategoriFrame(JTextField targetField) {
            setTitle("Pilih Kategori Barang");
            setSize(600,450);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel(new BorderLayout(10,10));
            panel.setBorder(new EmptyBorder(12,12,12,12));
            panel.setBackground(new Color(250,250,250));

            JPanel searchPanel = new JPanel(new BorderLayout(8,8));
            searchPanel.setOpaque(false);
            JTextField txtSearch = new JTextField();
            JButton btnSearch = new JButton("Cari");
            styleBtn(btnSearch, new Color(255,140,0));
            searchPanel.add(new JLabel("Cari Kategori:"), BorderLayout.WEST);
            searchPanel.add(txtSearch, BorderLayout.CENTER);
            searchPanel.add(btnSearch, BorderLayout.EAST);
            panel.add(searchPanel, BorderLayout.NORTH);

            String[] cols = {"ID Kategori","Nama Kategori"};
            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols,0);
            JTable t = new JTable(model);
            t.setRowHeight(26);
            t.setFont(new Font("Segoe UI", Font.PLAIN,13));
            panel.add(new JScrollPane(t), BorderLayout.CENTER);

            loadKategori(model, "");

            JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnP.setOpaque(false);
            JButton pilih = new JButton("Pilih"), cancel = new JButton("Cancel");
            styleBtn(pilih, new Color(0,180,0)); styleBtn(cancel, new Color(220,0,0));
            btnP.add(pilih); btnP.add(cancel);
            panel.add(btnP, BorderLayout.SOUTH);

            cancel.addActionListener(e -> dispose());
            pilih.addActionListener(e -> {
                int r = t.getSelectedRow();
                if (r != -1) {
                    String id = String.valueOf(t.getValueAt(r,0));
                    String nama = String.valueOf(t.getValueAt(r,1));
                    targetField.setText(nama);
                    selectedKategoriId = id;
                    dispose();
                } else JOptionPane.showMessageDialog(this, "Pilih kategori dulu!");
            });

            btnSearch.addActionListener(e -> loadKategori(model, txtSearch.getText().trim()));
            add(panel);
            setVisible(true);
        }
        private void loadKategori(javax.swing.table.DefaultTableModel model, String q) {
            model.setRowCount(0);
            String sql = (q==null||q.isEmpty()) ? "SELECT id_kategori,nama_kategori FROM data_kategori ORDER BY nama_kategori"
                    : "SELECT id_kategori,nama_kategori FROM data_kategori WHERE LOWER(nama_kategori) LIKE ? ORDER BY nama_kategori";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                if (q!=null && !q.isEmpty()) ps.setString(1, "%" + q.toLowerCase() + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) model.addRow(new Object[]{ rs.getString("id_kategori"), rs.getString("nama_kategori") });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal memuat kategori:\n"+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        private void styleBtn(JButton b, Color c){ b.setBackground(c); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setPreferredSize(new Dimension(110,36)); b.setFont(new Font("Segoe UI", Font.BOLD, 13)); }
    }

    class PilihSupplierFrame extends JFrame {
        public PilihSupplierFrame(JTextField targetField) {
            setTitle("Pilih Supplier");
            setSize(600,450);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel(new BorderLayout(10,10));
            panel.setBorder(new EmptyBorder(12,12,12,12));
            panel.setBackground(new Color(250,250,250));

            JPanel searchPanel = new JPanel(new BorderLayout(8,8));
            searchPanel.setOpaque(false);
            JTextField txtSearch = new JTextField();
            JButton btnSearch = new JButton("Cari");
            styleBtn(btnSearch, new Color(255,140,0));
            searchPanel.add(new JLabel("Cari Supplier:"), BorderLayout.WEST);
            searchPanel.add(txtSearch, BorderLayout.CENTER);
            searchPanel.add(btnSearch, BorderLayout.EAST);
            panel.add(searchPanel, BorderLayout.NORTH);

            String[] cols = {"ID Supplier","Nama Supplier"};
            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols,0);
            JTable t = new JTable(model);
            t.setRowHeight(26);
            t.setFont(new Font("Segoe UI", Font.PLAIN,13));
            panel.add(new JScrollPane(t), BorderLayout.CENTER);

            loadSupplier(model, "");

            JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnP.setOpaque(false);
            JButton pilih = new JButton("Pilih"), cancel = new JButton("Cancel");
            styleBtn(pilih, new Color(0,180,0)); styleBtn(cancel, new Color(220,0,0));
            btnP.add(pilih); btnP.add(cancel);
            panel.add(btnP, BorderLayout.SOUTH);

            cancel.addActionListener(e -> dispose());
            pilih.addActionListener(e -> {
                int r = t.getSelectedRow();
                if (r != -1) {
                    String id = String.valueOf(t.getValueAt(r,0));
                    String nama = String.valueOf(t.getValueAt(r,1));
                    targetField.setText(nama);
                    selectedSupplierId = id;
                    dispose();
                } else JOptionPane.showMessageDialog(this, "Pilih supplier dulu!");
            });

            btnSearch.addActionListener(e -> loadSupplier(model, txtSearch.getText().trim()));
            add(panel);
            setVisible(true);
        }
        private void loadSupplier(javax.swing.table.DefaultTableModel model, String q) {
            model.setRowCount(0);
            String sql = (q==null||q.isEmpty()) ? "SELECT id_supplier,nama_supplier FROM data_supplier ORDER BY nama_supplier"
                    : "SELECT id_supplier,nama_supplier FROM data_supplier WHERE LOWER(nama_supplier) LIKE ? ORDER BY nama_supplier";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                if (q!=null && !q.isEmpty()) ps.setString(1, "%" + q.toLowerCase() + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) model.addRow(new Object[]{ rs.getString("id_supplier"), rs.getString("nama_supplier") });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal memuat supplier:\n"+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        private void styleBtn(JButton b, Color c){ b.setBackground(c); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setPreferredSize(new Dimension(110,36)); b.setFont(new Font("Segoe UI", Font.BOLD, 13)); }
    }

    // utility
    private String cleanNumberString(String s) {
        if (s == null) return "0";
        s = s.trim();
        String keep = s.replaceAll("[^0-9\\.,]", "");
        if (keep.contains(",")) { keep = keep.replaceAll("\\.", ""); keep = keep.replace(',', '.'); }
        else keep = keep.replaceAll("\\.", "");
        if (keep.isEmpty()) return "0";
        return keep;
    }

    private String getKategoriIdByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String id = null;
        String sql = "SELECT id_kategori FROM data_kategori WHERE nama_kategori = ? LIMIT 1";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) id = rs.getString("id_kategori"); }
        } catch (SQLException ex) {}
        return id;
    }

    private String getSupplierIdByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String id = null;
        String sql = "SELECT id_supplier FROM data_supplier WHERE nama_supplier = ? LIMIT 1";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) id = rs.getString("id_supplier"); }
        } catch (SQLException ex) {}
        return id;
    }
}
