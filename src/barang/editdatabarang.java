package barang;

import page.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import Helper.DatabaseHelper;

/**
 * editdatabarang - versi yang mengisi field otomatis dan menyimpan perubahan ke DB
 * Fokus perbaikan: PilihKategoriFrame membaca kategori dari DB dan selectedKategoriId di-set.
 */
public class editdatabarang extends JPanel {
    // input fields (digunakan ulang di seluruh class)
    private RoundedTextField txtKode;
    private RoundedTextField txtNama;
    private RoundedTextField txtKategori;
    private RoundedTextField txtBarcode;
    private RoundedTextField txtStok;
    private RoundedTextField txtHarga;
    private RoundedTextField txtExpired;

    // menyimpan id kategori yang dipilih (penting untuk update)
    private String selectedKategoriId = null;

    // DAO
    private BarangDAO barangDao;
    private DetailBarangDAO detailDao;

    public editdatabarang() {
        // init DAO
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
        setBorder(new EmptyBorder(30, 50, 30, 50));

        // ===== Bagian atas =====
        JLabel title = new JLabel("Edit Data Barang", SwingConstants.CENTER);
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

        // Baris input - sekarang menyimpan referensi field agar dapat diisi
        txtKode = addField(formPanel, gbc, 0, "Kode Barang:");
        txtNama = addField(formPanel, gbc, 1, "Nama Barang:");
        txtKategori = addField(formPanel, gbc, 2, "Kategori Barang:");
        txtBarcode = addField(formPanel, gbc, 3, "Barcode:");
        txtStok = addField(formPanel, gbc, 4, "Stok:");
        txtHarga = addField(formPanel, gbc, 5, "Harga Jual:");
        txtExpired = addField(formPanel, gbc, 6, "Expired:");

        // Kategori clickable: panggil PilihKategoriFrame yang juga set selectedKategoriId
        txtKategori.setEditable(false);
        txtKategori.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtKategori.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new PilihKategoriFrame(txtKategori);
            }
        });

        // ===== Tombol =====
        RoundedButton btnKembali = new RoundedButton("Kembali", new Color(235, 235, 235), new Color(60, 60, 60));
        RoundedButton btnSimpan = new RoundedButton("Simpan", new Color(46, 204, 113), Color.WHITE);

        btnKembali.setPreferredSize(new Dimension(140, 45));
        btnSimpan.setPreferredSize(new Dimension(140, 45));

        btnSimpan.addActionListener(e -> {
            // lakukan update barang + first detail
            Integer editingId = null;
            try {
                editingId = BarangContext.editingId;
            } catch (Throwable t) { editingId = null; }

            if (editingId == null) {
                JOptionPane.showMessageDialog(this, "Tidak ada ID barang yang sedang diedit.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (barangDao == null || detailDao == null) {
                JOptionPane.showMessageDialog(this, "DAO belum terinisialisasi.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ambil nilai dari form
            String nama = txtNama.getText().trim();
            String kategoriId = selectedKategoriId;

            // Jika selectedKategoriId belum di-set (mis. user ketik/diisi dari load),
            // cari id_kategori dari nama kategori di DB (sama seperti di tambahdatabarang)
            if ((kategoriId == null || kategoriId.trim().isEmpty()) && txtKategori.getText() != null && !txtKategori.getText().trim().isEmpty()) {
                kategoriId = getKategoriIdByName(txtKategori.getText().trim());
            }

            if (nama.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama barang tidak boleh kosong.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // 1) update barang
                Barang b = new Barang();
                b.setId(editingId);
                b.setNama(nama);
                b.setIdKategori(kategoriId); // bisa null
                barangDao.update(b);

                // 2) update first detail jika ada
                List<DetailBarang> dets = detailDao.findByBarangId(editingId);
                if (dets != null && !dets.isEmpty()) {
                    DetailBarang d = dets.get(0); // first detail
                    d.setBarcode(txtBarcode.getText().trim());

                    // stok
                    int stok = 0;
                    try { stok = Integer.parseInt(txtStok.getText().trim()); } catch (Exception ex) { stok = d.getStok(); }
                    d.setStok(stok);

                    // harga: parse dari format lokal, jika kosong biarkan nilai lama
                    String hargaText = txtHarga.getText().trim();
                    if (!hargaText.isEmpty()) {
                        String cleaned = cleanNumberString(hargaText);
                        try {
                            d.setHargaJual(new BigDecimal(cleaned));
                        } catch (Exception ex) {
                            // jika parsing gagal, jangan override
                        }
                    }

                    d.setTanggalExp(txtExpired.getText().trim());
                    // lakukan update
                    detailDao.update(d);
                }

                JOptionPane.showMessageDialog(this, "✅ Perubahan disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);

                // kembali ke daftar barang (juga idealnya refresh di sana)
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame instanceof uiresponsive.Mainmenu) {
                    ((uiresponsive.Mainmenu) frame).showDataBarangPanel();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan perubahan:\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnKembali.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) {
                ((uiresponsive.Mainmenu) frame).showDataBarangPanel();
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnKembali);
        bottomPanel.add(btnSimpan);

        // ===== Tambahkan ke panel utama =====
        add(topPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // === penting: pas panel tampil, load data dari context
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(() -> loadFromContext());
            }
        });
    }

    /**
     * Memuat data barang + detail ke field ketika panel ditampilkan.
     * Menggunakan BarangContext.editingId (harus berupa Integer).
     */
    private void loadFromContext() {
        Integer editingId = null;
        try {
            editingId = BarangContext.editingId;
        } catch (Throwable t) {
            editingId = null;
        }

        // kosongkan form terlebih dahulu
        clearFormFields();
        selectedKategoriId = null;

        if (editingId == null) {
            // mode tambah (atau tidak ada data) -> biarkan kosong
            return;
        }

        if (barangDao == null) {
            JOptionPane.showMessageDialog(this, "Database tidak tersedia (barangDao null).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // karena BarangDAO yang diberikan hanya findAll(), kita cari di list
            List<Barang> all = barangDao.findAll();
            Barang target = null;
            if (all != null) {
                for (Barang b : all) {
                    if (b.getId() == editingId) {
                        target = b;
                        break;
                    }
                }
            }

            if (target == null) {
                JOptionPane.showMessageDialog(this, "Data barang (ID: " + editingId + ") tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // isi field dari objek Barang
            txtKode.setText(String.valueOf(target.getId()));
            txtNama.setText(target.getNama() == null ? "" : target.getNama());
            // simpan kategori id agar saat save kita tahu id_kategori
            selectedKategoriId = target.getIdKategori();
            // tampilkan nama kategori (jika ada)
            txtKategori.setText(target.getNamaKategori() == null ? "" : target.getNamaKategori());

            // Ambil detail (pakai detailDao.findByBarangId), gunakan detail pertama sebagai contoh:
            if (detailDao != null) {
                List<DetailBarang> dets = detailDao.findByBarangId(target.getId());
                if (dets != null && !dets.isEmpty()) {
                    DetailBarang d = dets.get(0); // representative
                    txtBarcode.setText(d.getBarcode() == null ? "" : d.getBarcode());
                    txtStok.setText(String.valueOf(d.getStok()));
                    if (d.getHargaJual() != null) {
                        NumberFormat nf = NumberFormat.getInstance(new Locale("in","ID"));
                        txtHarga.setText(nf.format(d.getHargaJual()));
                    } else {
                        txtHarga.setText("");
                    }
                    txtExpired.setText(d.getTanggalExp() == null ? "" : d.getTanggalExp());
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data:\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFormFields() {
        txtKode.setText("");
        txtNama.setText("");
        txtKategori.setText("");
        txtBarcode.setText("");
        txtStok.setText("");
        txtHarga.setText("");
        txtExpired.setText("");
    }

    // === Helper: membuat field (mengembalikan referensi field) ===
    private RoundedTextField addField(JPanel panel, GridBagConstraints gbc, int gridx, String labelText) {
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

        return field;
    }

    // === Rounded TextField ===
    class RoundedTextField extends JTextField {
        private int radius = 15;

        public RoundedTextField(int size) {
            super(size);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
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

    // === Rounded Button (smooth shadow & besar) ===
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

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Shadow halus (lebih smooth dan lembut)
            for (int i = 0; i < 6; i++) {
                g2.setColor(new Color(0, 0, 0, 10 - i)); // semakin redup di luar
                g2.fillRoundRect(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, radius, radius);
            }

            // Warna tombol
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, radius, radius);

            // Teks tombol
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(getText())) / 2;
            int textY = (getHeight() + fm.getAscent()) / 2 - 3;
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }

    // PilihKategoriFrame membaca kategori dari DB (mirip tambahdatabarang)
    class PilihKategoriFrame extends JFrame {
        public PilihKategoriFrame(JTextField targetField) {
            setTitle("Pilih Kategori Barang");
            setSize(600, 450);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(new EmptyBorder(15, 15, 15, 15));
            panel.setBackground(new Color(250, 250, 250));

            // Search bar
            JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
            searchPanel.setOpaque(false);
            JTextField txtSearch = new JTextField();
            JButton btnSearch = new JButton("Cari");
            styleButton(btnSearch, new Color(255, 140, 0));
            searchPanel.add(new JLabel("Cari Kategori:"), BorderLayout.WEST);
            searchPanel.add(txtSearch, BorderLayout.CENTER);
            searchPanel.add(btnSearch, BorderLayout.EAST);
            panel.add(searchPanel, BorderLayout.NORTH);

            // Tabel Kategori
            String[] kolom = {"ID Kategori", "Nama Kategori"};
            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(kolom, 0);
            JTable tabel = new JTable(model);
            tabel.setRowHeight(26);
            tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JScrollPane scroll = new JScrollPane(tabel);
            panel.add(scroll, BorderLayout.CENTER);

            // load from DB
            loadKategoriIntoModel(model, "");

            // Tombol bawah
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            btnPanel.setOpaque(false);
            JButton btnPilih = new JButton("Pilih");
            JButton btnCancel = new JButton("Cancel");
            styleButton(btnPilih, new Color(0, 180, 0));
            styleButton(btnCancel, new Color(220, 0, 0));
            btnPanel.add(btnPilih);
            btnPanel.add(btnCancel);
            panel.add(btnPanel, BorderLayout.SOUTH);

            // Aksi tombol
            btnCancel.addActionListener(e -> dispose());
            btnPilih.addActionListener(e -> {
                int row = tabel.getSelectedRow();
                if (row != -1) {
                    String id = String.valueOf(tabel.getValueAt(row, 0));
                    String nama = tabel.getValueAt(row, 1).toString();
                    targetField.setText(nama); // isi otomatis ke textfield utama
                    // set selectedKategoriId pada outer class
                    selectedKategoriId = id;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Pilih dulu kategorinya!");
                }
            });

            btnSearch.addActionListener(e -> {
                String q = txtSearch.getText().trim();
                loadKategoriIntoModel(model, q);
            });

            add(panel);
            setVisible(true);
        }

        private void loadKategoriIntoModel(javax.swing.table.DefaultTableModel model, String q) {
            model.setRowCount(0);
            String sql;
            if (q == null || q.isEmpty()) {
                sql = "SELECT id_kategori, nama_kategori FROM data_kategori ORDER BY nama_kategori";
            } else {
                sql = "SELECT id_kategori, nama_kategori FROM data_kategori WHERE LOWER(nama_kategori) LIKE ? ORDER BY nama_kategori";
            }
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                if (q != null && !q.isEmpty()) ps.setString(1, "%" + q.toLowerCase() + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{ rs.getString("id_kategori"), rs.getString("nama_kategori") });
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal memuat kategori:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void styleButton(JButton btn, Color color) {
            btn.setBackground(color);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(120, 40));
        }
    }

    // ====== utility ======
    // membersihkan string harga "1.234.567" atau "1.234.567,89" -> "1234567" atau "1234567.89"
    private String cleanNumberString(String s) {
        if (s == null) return "0";
        s = s.trim();
        // hapus semua karakter kecuali digit, '.' dan ','
        String keep = s.replaceAll("[^0-9\\.,]", "");
        // jika ada titik sebagai thousand sep dan koma sebagai decimal: ubah titik kosong dan ganti koma -> dot
        // heuristik: jika ada ',' maka treat ',' sebagai decimal separator
        if (keep.contains(",")) {
            keep = keep.replaceAll("\\.", ""); // hapus thousand sep
            keep = keep.replace(',', '.');
        } else {
            // tidak ada koma -> hapus titik sebagai thousand sep
            keep = keep.replaceAll("\\.", "");
        }
        if (keep.isEmpty()) return "0";
        return keep;
    }

    // Cari id_kategori dari nama kategori di DB (dipakai saat saving jika selectedKategoriId null)
    private String getKategoriIdByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String id = null;
        String sql = "SELECT id_kategori FROM data_kategori WHERE nama_kategori = ? LIMIT 1";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) id = rs.getString("id_kategori");
            }
        } catch (SQLException ex) {
            // ignore, return null
        }
        return id;
    }

    // (tetap sediakan fallback mapping jika perlu, tapi DB lookup lebih andal)
   
}
