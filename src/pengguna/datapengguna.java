package pengguna;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

/**
 * datapengguna - menampilkan data dari PenggunaDAO.
 * Desain tidak diubah — hanya menambahkan fungsional CRUD & search.
 */
public class datapengguna extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    private PenggunaDAO penggunaDao;

    public datapengguna() {
        try {
            penggunaDao = new PenggunaDAO();
        } catch (Exception ex) {
            penggunaDao = null;
            JOptionPane.showMessageDialog(this, "Gagal inisialisasi PenggunaDAO:\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
        initUI();
        // load data saat panel muncul
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(() -> loadData());
            }
        });
        // initial load if already visible
        if (isShowing()) loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(236,236,236));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // =============================
        // PANEL ATAS (SEARCH & BUTTON)
        // =============================
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        // Search field
        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Cari nama pengguna...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.setBorder(new RoundedBorder(15));

        // Tombol-tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnTambah = createButton("Tambah", new Color(46, 204, 113));
        btnTambah.addActionListener(e -> {
            PenggunaContext.editingId = null; // add mode
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) {
                ((uiresponsive.Mainmenu) frame).showTambahDataPengguna();
            }
        });

        JButton btnEdit = createButton("Edit", new Color(52, 152, 219));
        btnEdit.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) { JOptionPane.showMessageDialog(this, "Pilih pengguna yang akan diedit.", "Validasi", JOptionPane.WARNING_MESSAGE); return; }
            String id = String.valueOf(model.getValueAt(sel, 0));
            PenggunaContext.editingId = id;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) {
                ((uiresponsive.Mainmenu) frame).showEditDataPengguna();
            }
        });

        JButton btnHapus = createButton("Hapus", new Color(231, 76, 60));
        btnHapus.addActionListener(e -> {
            if (penggunaDao == null) { JOptionPane.showMessageDialog(this, "Database tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            int sel = table.getSelectedRow();
            if (sel < 0) { JOptionPane.showMessageDialog(this, "Pilih pengguna yang akan dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE); return; }
            String id = String.valueOf(model.getValueAt(sel, 0));
            String nama = String.valueOf(model.getValueAt(sel, 4)); // Nama Lengkap
            int ok = JOptionPane.showConfirmDialog(this, "Hapus pengguna \"" + nama + "\" (ID: " + id + ") ?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            try {
                penggunaDao.delete(id);
                JOptionPane.showMessageDialog(this, "Pengguna berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus pengguna:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnRefresh = createButton("Refresh", new Color(155, 89, 182));
        btnRefresh.addActionListener(e -> loadData());

        buttonPanel.add(btnTambah);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnRefresh);

        topPanel.add(searchField, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // =============================
        // TABEL DATA PENGGUNA
        // =============================
        String[] columns = {"ID Pengguna", "Username", "Alamat", "Jabatan", "Nama Lengkap", "Email", "No. Telp"};
        model = new DefaultTableModel(columns, 0);
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

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // interactions
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.rowAtPoint(e.getPoint());
                    if (r >= 0) {
                        String id = String.valueOf(model.getValueAt(r, 0));
                        PenggunaContext.editingId = id;
                        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(datapengguna.this);
                        if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showEditDataPengguna();
                    }
                }
            }
        });

        // realtime search
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                filterTable(searchField.getText().trim());
            }
        });
    }

    private void loadData() {
        model.setRowCount(0);
        if (penggunaDao == null) return;
        try {
            List<Pengguna> list = penggunaDao.findAll();
            for (Pengguna p : list) {
                model.addRow(new Object[]{
                    p.getIdPengguna(),
                    p.getUsername(),
                    p.getAlamat(),
                    p.getJabatan(),
                    p.getNamaLengkap(),
                    p.getEmail(),
                    p.getNotelpPengguna()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data pengguna:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(String q) {
        model.setRowCount(0);
        if (penggunaDao == null) return;
        try {
            List<Pengguna> list = penggunaDao.findAll();
            if (q.isEmpty()) {
                for (Pengguna p : list) {
                    model.addRow(new Object[]{
                        p.getIdPengguna(), p.getUsername(), p.getAlamat(), p.getJabatan(),
                        p.getNamaLengkap(), p.getEmail(), p.getNotelpPengguna()
                    });
                }
            } else {
                String lower = q.toLowerCase();
                for (Pengguna p : list) {
                    boolean matched = (p.getNamaLengkap() != null && p.getNamaLengkap().toLowerCase().contains(lower))
                                   || (p.getUsername() != null && p.getUsername().toLowerCase().contains(lower))
                                   || (p.getEmail() != null && p.getEmail().toLowerCase().contains(lower));
                    if (matched) {
                        model.addRow(new Object[]{
                            p.getIdPengguna(), p.getUsername(), p.getAlamat(), p.getJabatan(),
                            p.getNamaLengkap(), p.getEmail(), p.getNotelpPengguna()
                        });
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencari data pengguna:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =============================
    // METHOD BUAT TOMBOL STYLISH (tidak diubah desainnya)
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
            @Override public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(color.brighter()); }
            @Override public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(color); }
        });

        return btn;
    }

    // =============================
    // BORDER ROUNDED UNTUK TEXTFIELD (tidak diubah)
    // =============================
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        public RoundedBorder(int radius) { this.radius = radius; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(radius / 2, radius / 2, radius / 2, radius / 2); }
        @Override public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius / 2;
            return insets;
        }
    }
}
