package voucher;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

public class datavoucher extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    private VoucherDAO dao;
    // map row index -> id_voucher (so we don't show id column)
    private final List<Integer> rowIds = new ArrayList<>();

    public datavoucher() {
        try { dao = new VoucherDAO(); } catch (Exception ex) { dao = null; }
        initUI();
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(() -> loadData());
            }
        });
        if (isShowing()) loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(236,236,236));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Cari nama Voucher...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.setBorder(new RoundedBorder(15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnTambah = createButton("Tambah", new Color(46, 204, 113));
        JButton btnEdit = createButton("Edit", new Color(52, 152, 219));
        JButton btnHapus = createButton("Hapus", new Color(231, 76, 60));
        JButton btnRefresh = createButton("Refresh", new Color(155, 89, 182));

        buttonPanel.add(btnTambah);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnRefresh);

        topPanel.add(searchField, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Table
        String[] columns = {"Kode Voucher", "Nominal", "Bulan", "Nama Guru"};
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

        // dummy row (will be replaced when loadData runs)
        model.addRow(new Object[]{"VR0023", "20000", "20 Oktober 2025", "Mamba"});

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Actions
        btnTambah.addActionListener(e -> {
            VoucherContext.editingId = null;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showTambahDataVoucher();
        });

        btnEdit.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) { JOptionPane.showMessageDialog(this, "Pilih voucher yang akan diedit.", "Validasi", JOptionPane.WARNING_MESSAGE); return; }
            if (sel >= rowIds.size()) { JOptionPane.showMessageDialog(this, "ID voucher tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            Integer id = rowIds.get(sel);
            if (id == null) { JOptionPane.showMessageDialog(this, "ID voucher tidak valid.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            VoucherContext.editingId = id;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showEditDataVoucher();
        });

        btnHapus.addActionListener(e -> {
            if (dao == null) { JOptionPane.showMessageDialog(this, "Database tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            int sel = table.getSelectedRow();
            if (sel < 0) { JOptionPane.showMessageDialog(this, "Pilih voucher yang akan dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE); return; }
            if (sel >= rowIds.size()) { JOptionPane.showMessageDialog(this, "ID voucher tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            Integer id = rowIds.get(sel);
            String kode = String.valueOf(model.getValueAt(sel, 0));
            int ok = JOptionPane.showConfirmDialog(this, "Hapus voucher \"" + kode + "\" (ID: " + id + ") ?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            try {
                dao.delete(id);
                JOptionPane.showMessageDialog(this, "Voucher berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus voucher:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRefresh.addActionListener(e -> loadData());

        // double-click => edit
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < rowIds.size()) {
                        VoucherContext.editingId = rowIds.get(r);
                        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(datavoucher.this);
                        if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showEditDataVoucher();
                    }
                }
            }
        });

        // search realtime
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                filterTable(searchField.getText().trim());
            }
        });
    }

    private void loadData() {
        model.setRowCount(0);
        rowIds.clear();
        if (dao == null) return;
        try {
            java.util.List<Voucher> list = dao.findAll();
            if (list == null) return;
            for (Voucher v : list) {
                String nominal = v.getCurrentBalance() == null ? "" : v.getCurrentBalance().toPlainString();
                model.addRow(new Object[]{ v.getKode(), nominal, v.getBulan(), v.getNamaGuru() });
                rowIds.add(v.getIdVoucher());
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data voucher:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(String q) {
        model.setRowCount(0);
        rowIds.clear();
        if (dao == null) return;
        try {
            java.util.List<Voucher> list = dao.findAll();
            if (list == null) return;
            if (q.isEmpty()) {
                for (Voucher v : list) {
                    model.addRow(new Object[]{v.getKode(), v.getCurrentBalance()==null?"":v.getCurrentBalance().toPlainString(), v.getBulan(), v.getNamaGuru()});
                    rowIds.add(v.getIdVoucher());
                }
                return;
            }
            String lower = q.toLowerCase();
            for (Voucher v : list) {
                if ((v.getKode()!=null && v.getKode().toLowerCase().contains(lower)) ||
                    (v.getNamaGuru()!=null && v.getNamaGuru().toLowerCase().contains(lower)) ||
                    (v.getBulan()!=null && v.getBulan().toLowerCase().contains(lower)) ||
                    (v.getCurrentBalance()!=null && v.getCurrentBalance().toPlainString().toLowerCase().contains(lower))) {
                    model.addRow(new Object[]{v.getKode(), v.getCurrentBalance()==null?"":v.getCurrentBalance().toPlainString(), v.getBulan(), v.getNamaGuru()});
                    rowIds.add(v.getIdVoucher());
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencari data voucher:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // createButton and RoundedBorder identical to your design (unchanged)
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int width = getWidth(); int height = getHeight(); int arc = 22;
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
        btn.setFocusPainted(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setOpaque(false);
        btn.setForeground(Color.WHITE); btn.setBackground(color);
        btn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(125, 44));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(color.brighter()); }
            @Override public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(color); }
        });
        return btn;
    }

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
        @Override public Insets getBorderInsets(Component c, Insets insets) { insets.left = insets.right = insets.top = insets.bottom = radius / 2; return insets; }
    }
}
