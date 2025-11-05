package kategori;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

/**
 * Panel Data Kategori yang sudah terhubung dengan KategoriDAO (CRUD).
 */
public class datakategori extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtIdKategori, txtNamaKategori, searchField;
    private JButton btnTambah, btnEdit, btnHapus, btnRefresh;

    private KategoriDAO dao;

    public datakategori() {
        try {
            dao = new KategoriDAO();
        } catch (SQLException ex) {
            // jika DAO gagal dibuat, tampilkan error dan tetap buat UI readonly
            JOptionPane.showMessageDialog(this,
                    "Gagal inisialisasi database:\n" + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
            dao = null;
        }
        initUI();
        addActionListeners();
        if (dao != null) loadData(); // hanya load jika dao tersedia
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(236, 236, 236));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // =============================
        // PANEL ATAS (FORM + BUTTON)
        // =============================
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        // =============================
        // FORM INPUT (LABEL DI ATAS TEXTFIELD)
        // =============================
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // ID Kategori
        JLabel lblId = new JLabel("ID Kategori");
        lblId.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lblId.setBorder(new EmptyBorder(0, 5, 5, 0));

        txtIdKategori = new JTextField();
        txtIdKategori.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtIdKategori.setPreferredSize(new Dimension(250, 36));
        txtIdKategori.setMaximumSize(new Dimension(250, 36));
        txtIdKategori.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtIdKategori.setBorder(new RoundedBorder(15));

        // Nama Kategori
        JLabel lblNama = new JLabel("Nama Kategori");
        lblNama.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lblNama.setBorder(new EmptyBorder(10, 5, 5, 0));

        txtNamaKategori = new JTextField();
        txtNamaKategori.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNamaKategori.setPreferredSize(new Dimension(250, 36));
        txtNamaKategori.setMaximumSize(new Dimension(250, 36));
        txtNamaKategori.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtNamaKategori.setBorder(new RoundedBorder(15));

        formPanel.add(lblId);
        formPanel.add(txtIdKategori);
        formPanel.add(lblNama);
        formPanel.add(txtNamaKategori);

        // =============================
        // TOMBOL-TOMBOL + SEARCH (1 BARIS SEJAJAR)
        // =============================
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel tombol di kiri
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        btnTambah = createButton("Tambah Guru", new Color(46, 204, 113));
        btnEdit = createButton("Edit", new Color(52, 152, 219));
        btnHapus = createButton("Hapus", new Color(231, 76, 60));
        btnRefresh = createButton("Refresh", new Color(155, 89, 182));

        buttonPanel.add(btnTambah);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnRefresh);

        // Panel search di kanan
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(0, 0, 0, 0)); // biar posisi pas sejajar

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Cari data kategori...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.setBorder(new RoundedBorder(15));

        searchPanel.add(searchField);

        actionPanel.add(buttonPanel, BorderLayout.WEST);
        actionPanel.add(searchPanel, BorderLayout.EAST);

        // Gabungkan form dan action bar
        topPanel.add(formPanel);
        topPanel.add(actionPanel);

        // =============================
        // PANEL TABEL DATA
        // =============================
        JPanel tablePanel = new JPanel(new BorderLayout(10, 0));
        tablePanel.setOpaque(false);

        String[] columns = {"ID Kategori", "Nama Kategori"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // non-editable langsung di tabel
            }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        table.getTableHeader().setBackground(new Color(60, 80, 120));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(93, 173, 226));
        table.setSelectionForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setOpaque(true);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // =============================
        // TAMBAHKAN SEMUA KE FRAME
        // =============================
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        // disable tombol jika dao null
        boolean enabled = (dao != null);
        btnTambah.setEnabled(enabled);
        btnEdit.setEnabled(enabled);
        btnHapus.setEnabled(enabled);
        btnRefresh.setEnabled(enabled);
        searchField.setEnabled(enabled);
    }

    private void addActionListeners() {
        // tombol Tambah
        btnTambah.addActionListener(e -> {
            String id = txtIdKategori.getText().trim();
            String nama = txtNamaKategori.getText().trim();
            if (id.isEmpty() || nama.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID dan Nama kategori harus diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                // cek apakah id sudah ada
                Kategori existing = dao.findById(id);
                if (existing != null) {
                    JOptionPane.showMessageDialog(this, "ID kategori sudah ada. Gunakan ID lain atau edit baris yang ada.", "Duplikasi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Kategori k = new Kategori(id, nama);
                dao.insert(k);
                loadData();
                clearForm();
                JOptionPane.showMessageDialog(this, "Kategori berhasil ditambahkan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan kategori:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // tombol Edit
        btnEdit.addActionListener(e -> {
            String id = txtIdKategori.getText().trim();
            String nama = txtNamaKategori.getText().trim();
            if (id.isEmpty() || nama.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pilih baris lalu ubah isian. ID dan Nama tidak boleh kosong.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                // pastikan id ada
                Kategori existing = dao.findById(id);
                if (existing == null) {
                    JOptionPane.showMessageDialog(this, "ID kategori tidak ditemukan. Pastikan ID benar.", "Tidak ada", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Kategori k = new Kategori(id, nama);
                dao.update(k);
                loadData();
                clearForm();
                JOptionPane.showMessageDialog(this, "Kategori berhasil diupdate.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal mengupdate kategori:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // tombol Hapus
        btnHapus.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Pilih baris yang akan dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = (String) model.getValueAt(sel, 0);
            int ok = JOptionPane.showConfirmDialog(this,
                    "Hapus kategori dengan ID: " + id + " ?",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            try {
                dao.delete(id);
                loadData();
                clearForm();
                JOptionPane.showMessageDialog(this, "Kategori berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus kategori:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // tombol Refresh
        btnRefresh.addActionListener(e -> {
            loadData();
            clearForm();
        });

        // klik baris table -> isi form
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int sel = table.getSelectedRow();
                if (sel >= 0) {
                    String id = (String) model.getValueAt(sel, 0);
                    String nama = (String) model.getValueAt(sel, 1);
                    txtIdKategori.setText(id);
                    txtNamaKategori.setText(nama);
                }
            }
        });

        // search realtime (filter hasil dari DAO.findAll())
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTable(searchField.getText().trim());
            }
        });

        // double click clear form (opsional)
        searchField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // double click untuk bersihkan search
                if (e.getClickCount() == 2) {
                    searchField.setText("");
                    loadData();
                }
            }
        });
    }

    /**
     * Load semua data dari database dan tampilkan di tabel.
     */
    private void loadData() {
        if (dao == null) return;
        try {
            List<Kategori> list = dao.findAll();
            model.setRowCount(0);
            for (Kategori k : list) {
                model.addRow(new Object[]{k.getIdKategori(), k.getNamaKategori()});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data kategori:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Filter tabel berdasarkan teks (pencarian sederhana pada id/nama)
     */
    private void filterTable(String q) {
        if (dao == null) return;
        try {
            List<Kategori> list = dao.findAll();
            model.setRowCount(0);
            if (q.isEmpty()) {
                for (Kategori k : list) model.addRow(new Object[]{k.getIdKategori(), k.getNamaKategori()});
            } else {
                String lower = q.toLowerCase();
                for (Kategori k : list) {
                    if ((k.getIdKategori() != null && k.getIdKategori().toLowerCase().contains(lower))
                            || (k.getNamaKategori() != null && k.getNamaKategori().toLowerCase().contains(lower))) {
                        model.addRow(new Object[]{k.getIdKategori(), k.getNamaKategori()});
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencari data kategori:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtIdKategori.setText("");
        txtNamaKategori.setText("");
        table.clearSelection();
    }

    // =============================
    // METHOD BUAT TOMBOL STYLISH
    // =============================
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int arc = 22;

                for (int i = 8; i >= 1; i--) {
                    float opacity = 0.03f * i;
                    g2.setColor(new Color(0, 0, 0, (int) (opacity * 255)));
                    g2.fillRoundRect(i, i + 3, width - (i * 2), height - (i * 2), arc, arc);
                }

                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, width - 4, height - 4, arc, arc);

                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getAscent();
                g2.setColor(getForeground());
                g2.drawString(getText(), (width - textWidth) / 2, (height + textHeight) / 2 - 3);

                g2.dispose();
            }
        };

        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(125, 44));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });

        return btn;
    }

    // =============================
    // BORDER ROUNDED UNTUK TEXTFIELD
    // =============================
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius / 2;
            return insets;
        }
    }

    // =============================
    // TESTING FRAME
    // =============================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Data Kategori");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new datakategori());
            frame.setVisible(true);
        });
    }
}
