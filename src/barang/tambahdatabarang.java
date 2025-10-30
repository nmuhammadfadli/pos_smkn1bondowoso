package barang;

import page.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Helper.DatabaseHelper;

/**
 * Desain persis seperti yang kamu minta.
 * Fungsional: terhubung ke BarangDAO, support tambah + edit (via BarangContext.editingId),
 * PilihKategoriFrame membaca data_kategori dari DB, onSave insert/update lewat BarangDAO.
 */
public class tambahdatabarang extends JPanel {
    private JTextField txtId, txtNama, txtKategori;
    private BarangDAO barangDao;

    public tambahdatabarang() {
        try {
            barangDao = new BarangDAO();
        } catch (Exception ex) {
            barangDao = null;
            // jangan ganggu desain — hanya log/alert
            JOptionPane.showMessageDialog(this, "Peringatan: gagal inisialisasi BarangDAO:\n" + ex.getMessage(), "DB Warning", JOptionPane.WARNING_MESSAGE);
        }

        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));
        setBorder(new EmptyBorder(30, 50, 30, 50));

        // ===== Bagian atas =====
        JLabel title = new JLabel("Tambah Data Barang", SwingConstants.CENTER);
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

        // buat fields (menggunakan RoundedTextField sesuai desain)
        txtId = new RoundedTextField(12);
        txtId.setEditable(false);
        txtId.setOpaque(false);
        txtId.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        addFieldWithComponent(formPanel, gbc, 0, "Kode Barang:", txtId);

        txtNama = new RoundedTextField(12);
        txtNama.setOpaque(false);
        txtNama.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        addFieldWithComponent(formPanel, gbc, 1, "Nama Barang:", txtNama);

        txtKategori = new RoundedTextField(12);
        txtKategori.setEditable(false);
        txtKategori.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtKategori.setOpaque(false);
        txtKategori.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        txtKategori.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                new PilihKategoriFrame(txtKategori);
            }
        });
        addFieldWithComponent(formPanel, gbc, 2, "Kategori Barang:", txtKategori);

        // ===== Tombol =====
        RoundedButton btnKembali = new RoundedButton("Kembali", new Color(235, 235, 235), new Color(60, 60, 60));
        RoundedButton btnSimpan = new RoundedButton("Simpan", new Color(46, 204, 113), Color.WHITE);

        btnKembali.setPreferredSize(new Dimension(140, 45));
        btnSimpan.setPreferredSize(new Dimension(140, 45));

        btnSimpan.addActionListener(e -> onSave());
        btnKembali.addActionListener(e -> {
            BarangContext.editingId = null;
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

        // load when shown (edit mode)
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(() -> loadIfEdit());
            }
        });
    }

    // helper untuk menambahkan field (preserve desain)
    private void addFieldWithComponent(JPanel panel, GridBagConstraints gbc, int gridx, String labelText, JComponent comp) {
        int row = gridx / 3;
        int col = gridx % 3;

        gbc.gridx = col;
        gbc.gridy = row;
        gbc.gridwidth = 1;

        JPanel fieldPanel = new JPanel(new BorderLayout(5,5));
        fieldPanel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(comp, BorderLayout.CENTER);
        panel.add(fieldPanel, gbc);
    }

    private void loadIfEdit() {
        if (BarangContext.editingId == null || barangDao == null) {
            txtId.setText("");
            txtNama.setText("");
            txtKategori.setText("");
            return;
        }
        try {
            java.util.List<Barang> list = barangDao.findAll();
            for (Barang b : list) {
                if (Objects.equals(b.getId(), BarangContext.editingId)) {
                    txtId.setText(String.valueOf(b.getId()));
                    txtNama.setText(b.getNama());
                    txtKategori.setText(b.getNamaKategori());
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Data barang tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data barang:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        if (barangDao == null) {
            JOptionPane.showMessageDialog(this, "Database tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String nama = txtNama.getText().trim();
        String kategoriNama = txtKategori.getText().trim();

        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama barang harus diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert kategoriNama -> id_kategori by querying DB (data_kategori)
        String idKategori = null;
        if (!kategoriNama.isEmpty()) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT id_kategori FROM data_kategori WHERE nama_kategori = ? LIMIT 1")) {
                ps.setString(1, kategoriNama);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) idKategori = rs.getString("id_kategori");
                }
            } catch (SQLException ex) {
                // biarkan idKategori null jika error; proses tetap lanjut
            }
        }

        try {
            if (BarangContext.editingId == null) {
                Barang b = new Barang();
                b.setNama(nama);
                b.setIdKategori(idKategori);
                barangDao.insert(b);
                JOptionPane.showMessageDialog(this, "Barang berhasil disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Barang b = new Barang();
                b.setId(BarangContext.editingId);
                b.setNama(nama);
                b.setIdKategori(idKategori);
                barangDao.update(b);
                JOptionPane.showMessageDialog(this, "Barang berhasil diupdate.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                BarangContext.editingId = null;
            }
            // kembali ke halaman daftar barang
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataBarangPanel();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan barang:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // PilihKategoriFrame membaca kategori dari DB (preserve desain)
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
                    String nama = tabel.getValueAt(row, 1).toString();
                    targetField.setText(nama); // isi otomatis ke textfield utama
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
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         q == null || q.isEmpty()
                                 ? "SELECT id_kategori, nama_kategori FROM data_kategori ORDER BY nama_kategori"
                                 : "SELECT id_kategori, nama_kategori FROM data_kategori WHERE LOWER(nama_kategori) LIKE ? ORDER BY nama_kategori"
                 )) {
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

    // === Rounded TextField & Button (preserve style) ===
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
            g2.dispose();
            super.paintComponent(g);
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
        @Override
        protected void paintComponent(Graphics g) {
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
