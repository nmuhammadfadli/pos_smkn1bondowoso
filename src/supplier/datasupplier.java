package supplier;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

public class datasupplier extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    private SupplierDAO dao;

    public datasupplier() {
        try {
            dao = new SupplierDAO();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal inisialisasi SupplierDAO:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
            dao = null;
        }
        initUI();
        if (dao != null) loadData();
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
        searchField.putClientProperty("JTextField.placeholderText", "Cari nama supplier...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.setBorder(new RoundedBorder(15));

        // Tombol-tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnTambah = createButton("Tambah Supplier", new Color(30, 58, 128));
        btnTambah.addActionListener(e -> {
            // ambil frame induk (Mainmenu)
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) {
                ((uiresponsive.Mainmenu) frame).showTambahDataSupplier(); // panggil method di Mainmenu (full page)
            }
        });

        JButton btnEdit = createButton("Edit", new Color(217, 119, 6));
        btnEdit.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Pilih baris yang akan diedit.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object idObj = model.getValueAt(sel, 0);
            Integer id = null;
            if (idObj instanceof Integer) id = (Integer) idObj;
            else {
                try { id = Integer.parseInt(String.valueOf(idObj)); } catch (Exception ignored) {}
            }
            if (id == null) {
                JOptionPane.showMessageDialog(this, "ID supplier tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // tandai di context, kemudian minta Mainmenu menampilkan halaman edit
            SupplierContext.editingId = id;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) {
                ((uiresponsive.Mainmenu) frame).showEditDataSupplier();
            }
        });

        JButton btnHapus = createButton("Hapus", new Color(236, 28, 44));
        JButton btnRefresh = createButton("Refresh", new Color(194, 65, 12));

        buttonPanel.add(btnTambah);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnRefresh);

        topPanel.add(searchField, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // =============================
        // TABEL DATA SUPPLIER
        // =============================
        String[] columns = {"Kode", "Nama Supplier", "Alamat Supplier", "No. Telp"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        table.getTableHeader().setBackground(new Color(236, 28, 44));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(30, 58, 138));
        table.setSelectionForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setOpaque(true);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Dummy initially removed once loadData runs; but keep a fallback sample if no DB
        if (dao == null) model.addRow(new Object[]{"SP1001", "Mamba", "Badean", "082233236128"});

        // action listeners
        btnTambah.addActionListener(e -> {}); // already set above
        btnEdit.addActionListener(e -> {});   // already set above

        btnHapus.addActionListener(e -> {
            if (dao == null) {
                JOptionPane.showMessageDialog(this, "Database tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Pilih baris yang akan dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object idObj = model.getValueAt(sel, 0);
            Integer id = null;
            if (idObj instanceof Integer) id = (Integer) idObj;
            else {
                try { id = Integer.parseInt(String.valueOf(idObj)); } catch (Exception ignored) {}
            }
            if (id == null) {
                JOptionPane.showMessageDialog(this, "ID supplier tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String nama = String.valueOf(model.getValueAt(sel, 1));
            int ok = JOptionPane.showConfirmDialog(this,
                    "Hapus supplier \"" + nama + "\" (ID: " + id + ") ?",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            try {
                dao.delete(id);
                JOptionPane.showMessageDialog(this, "Supplier berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus supplier:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRefresh.addActionListener(e -> loadData());

        // search realtime
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                filterTable(searchField.getText().trim());
            }
        });

        // double-click row => edit
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // reuse edit action
                    for (ActionListener al : btnEdit.getActionListeners()) {
                        al.actionPerformed(new ActionEvent(btnEdit, ActionEvent.ACTION_PERFORMED, null));
                    }
                }
            }
        });

        // disable controls if dao null
        boolean enabled = (dao != null);
        btnTambah.setEnabled(enabled);
        btnEdit.setEnabled(enabled);
        btnHapus.setEnabled(enabled);
        btnRefresh.setEnabled(enabled);
        searchField.setEnabled(enabled);
    }

    private void loadData() {
        if (dao == null) return;
        try {
            List<Supplier> list = dao.findAll();
            model.setRowCount(0);
            for (Supplier s : list) {
                model.addRow(new Object[]{
                        s.getIdSupplier(),
                        s.getNamaSupplier(),
                        s.getAlamatSupplier(),
                        s.getNotelpSupplier()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data supplier:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(String q) {
        if (dao == null) return;
        try {
            List<Supplier> list = dao.findAll();
            model.setRowCount(0);
            if (q.isEmpty()) {
                for (Supplier s : list) model.addRow(new Object[]{s.getIdSupplier(), s.getNamaSupplier(), s.getAlamatSupplier(), s.getNotelpSupplier()});
            } else {
                String lower = q.toLowerCase();
                for (Supplier s : list) {
                    if ((s.getNamaSupplier()!=null && s.getNamaSupplier().toLowerCase().contains(lower)) ||
                        (s.getAlamatSupplier()!=null && s.getAlamatSupplier().toLowerCase().contains(lower)) ||
                        (s.getNotelpSupplier()!=null && s.getNotelpSupplier().toLowerCase().contains(lower))) {
                        model.addRow(new Object[]{s.getIdSupplier(), s.getNamaSupplier(), s.getAlamatSupplier(), s.getNotelpSupplier()});
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencari data supplier:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
            @Override public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(color.brighter()); }
            @Override public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(color); }
        });

        return btn;
    }

    // =============================
    // BORDER ROUNDED UNTUK TEXTFIELD
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
