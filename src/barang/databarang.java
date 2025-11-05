package barang;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

public class databarang extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    private BarangDAO barangDao;
    private DetailBarangDAO detailDao;

    public databarang() {
        try {
            barangDao = new BarangDAO();
            detailDao = new DetailBarangDAO();
        } catch (Exception ex) {
            barangDao = null;
            detailDao = null;
            JOptionPane.showMessageDialog(this, "Gagal inisialisasi DAO:\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
        initUI();

        // reload saat panel menjadi visible (robust terhadap Mainmenu show)
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(() -> loadData());
            }
        });

        // initial load (if created and already connected)
        if (isShowing()) loadData();
    }
    
    private void showDetailSelector(final int barangId) {
    if (detailDao == null) {
        JOptionPane.showMessageDialog(this, "Database detail tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    final JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Detail Barang - ID: " + barangId, Dialog.ModalityType.APPLICATION_MODAL);
    d.setSize(700, 400);
    d.setLocationRelativeTo(this);

    JPanel p = new JPanel(new BorderLayout(10,10));
    p.setBorder(new EmptyBorder(10,10,10,10));
    p.setBackground(Color.WHITE);

    // table untuk daftar detail
    String[] cols = {"ID Detail", "Barcode", "Stok", "Harga Jual", "Expired", "Supplier"};
    final DefaultTableModel m = new DefaultTableModel(cols, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    final JTable t = new JTable(m);
    t.setRowHeight(26);
    JScrollPane sp = new JScrollPane(t);
    p.add(sp, BorderLayout.CENTER);

    // tombol bawah: Tambah, Edit, Hapus, Close
    JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
    JButton bTambah = new JButton("Tambah");
    JButton bEdit   = new JButton("Edit");
    JButton bHapus  = new JButton("Hapus");
    JButton bClose  = new JButton("Close");
    btns.add(bTambah); btns.add(bEdit); btns.add(bHapus); btns.add(bClose);
    p.add(btns, BorderLayout.SOUTH);

    // load data detail
    Runnable load = () -> {
        try {
            m.setRowCount(0);
            java.util.List<DetailBarang> dets = detailDao.findByBarangId(barangId);
            if (dets != null) {
                for (DetailBarang db : dets) {
                    String harga = db.getHargaJual() == null ? "" : db.getHargaJual().toPlainString();
                    m.addRow(new Object[]{ db.getId(), db.getBarcode(), db.getStok(), harga, db.getTanggalExp(), db.getNamaSupplier() });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat detail:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    };

    load.run();

    // action: tambah => set context dan buka halaman full
    bTambah.addActionListener(ev -> {
        DetailContext.parentBarangId = barangId;
        DetailContext.editingDetailId = null;
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showTambahDetailBarang();
        d.dispose();
    });

    // edit => harus pilih baris
    bEdit.addActionListener(ev -> {
        int sel = t.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(d, "Pilih detail yang akan diedit.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer idDetail = null;
        Object o = m.getValueAt(sel, 0);
        try { idDetail = Integer.parseInt(String.valueOf(o)); } catch (Exception ex) {}
        if (idDetail == null) {
            JOptionPane.showMessageDialog(d, "ID detail tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        DetailContext.parentBarangId = barangId;
        DetailContext.editingDetailId = idDetail;
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showTambahDetailBarang();
        d.dispose();
    });

    // hapus
    bHapus.addActionListener(ev -> {
        int sel = t.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(d, "Pilih detail yang akan dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE); return; }
        Integer idDetail = null;
        Object o = m.getValueAt(sel, 0);
        try { idDetail = Integer.parseInt(String.valueOf(o)); } catch (Exception ex) {}
        if (idDetail == null) { JOptionPane.showMessageDialog(d, "ID detail tidak valid.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        int ok = JOptionPane.showConfirmDialog(d, "Hapus detail ID: " + idDetail + " ?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            detailDao.delete(idDetail);
            JOptionPane.showMessageDialog(d, "Detail berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            load.run();
            // reload main barang table stok/harga
            SwingUtilities.invokeLater(() -> loadData());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(d, "Gagal menghapus detail:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    bClose.addActionListener(ev -> d.dispose());

    d.getContentPane().add(p);
    d.setVisible(true);
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
        searchField.putClientProperty("JTextField.placeholderText", "Cari nama barang...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.setBorder(new RoundedBorder(15));

        // Tombol-tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnTambahDetail = createButton("Tambah Detail", new Color(53, 104, 89));
        JButton btnTambah = createButton("Tambah Barang", new Color(46, 204, 113));
        JButton btnEdit = createButton("Edit", new Color(52, 152, 219));
        JButton btnHapus = createButton("Hapus", new Color(231, 76, 60));
        JButton btnRefresh = createButton("Refresh", new Color(155, 89, 182));
        
        

//        buttonPanel.add(btnTambahDetail);
        buttonPanel.add(btnTambah);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnRefresh);

        topPanel.add(searchField, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // =============================
        // TABEL DATA BARANG
        // =============================
        String[] columns = {"ID", "Nama Barang", "Kategori", "Harga", "Stok"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
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

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // actions
        btnTambah.addActionListener(e -> {
            BarangContext.editingId = null;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showTambahDataBarang();
        });

        btnEdit.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) { JOptionPane.showMessageDialog(this, "Pilih barang yang akan diedit.", "Validasi", JOptionPane.WARNING_MESSAGE); return; }
            Object idObj = model.getValueAt(sel, 0);
            Integer id = parseIntSafe(idObj);
            if (id == null) { JOptionPane.showMessageDialog(this, "ID barang tidak valid.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            BarangContext.editingId = id;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showEditDataBarang();
        });

        btnHapus.addActionListener(e -> {
            if (barangDao == null) { JOptionPane.showMessageDialog(this, "Database tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            int sel = table.getSelectedRow();
            if (sel < 0) { JOptionPane.showMessageDialog(this, "Pilih barang yang akan dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE); return; }
            Integer id = parseIntSafe(model.getValueAt(sel, 0));
            String nama = String.valueOf(model.getValueAt(sel, 1));
            if (id == null) { JOptionPane.showMessageDialog(this, "ID tidak valid.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            int ok = JOptionPane.showConfirmDialog(this, "Hapus barang \"" + nama + "\" (ID: " + id + ") ?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            try {
                barangDao.delete(id);
                JOptionPane.showMessageDialog(this, "Barang berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus barang:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRefresh.addActionListener(e -> loadData());

          btnTambahDetail.addActionListener(e -> {
    int sel = table.getSelectedRow();
    if (sel < 0) {
        JOptionPane.showMessageDialog(this, "Pilih barang terlebih dahulu untuk mengelola detail.", "Validasi", JOptionPane.WARNING_MESSAGE);
        return;
    }
    Integer id = parseIntSafe(model.getValueAt(sel, 0));
    if (id == null) {
        JOptionPane.showMessageDialog(this, "ID barang tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // buka halaman full-page tambah/edit detail untuk barang ini
    DetailContext.parentBarangId = id;
    DetailContext.editingDetailId = null; // mode tambah
    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
    if (frame instanceof uiresponsive.Mainmenu) {
        ((uiresponsive.Mainmenu) frame).showTambahDetailBarang();
    }
});


        // double-click => edit barang
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.rowAtPoint(e.getPoint());
                    if (r >= 0) {
                        Integer id = parseIntSafe(model.getValueAt(r, 0));
                        if (id != null) {
                            BarangContext.editingId = id;
                            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(databarang.this);
                            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showEditDataBarang();
                        }
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

    private Integer parseIntSafe(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer)o;
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception ex) { return null; }
    }

    private void loadData() {
        if (barangDao == null) return;
        try {
            List<Barang> list = barangDao.findAll();
            model.setRowCount(0);
            NumberFormat nf = NumberFormat.getInstance(new Locale("in","ID"));
            for (Barang b : list) {
                // compute stok total & sample harga (ambil harga dari first detail if exists)
                int stokTotal = 0;
                String hargaStr = "";
                try {
                    if (detailDao != null) {
                        java.util.List<DetailBarang> dets = detailDao.findByBarangId(b.getId());
                        if (dets != null && !dets.isEmpty()) {
                            // sum stok
                            for (DetailBarang d : dets) stokTotal += d.getStok();
                            // use first harga_jual as representative
                            if (dets.get(0).getHargaJual() != null) hargaStr = nf.format(dets.get(0).getHargaJual());
                        }
                    }
                } catch (SQLException ignore) {}

                model.addRow(new Object[]{
                        b.getId(),
                        b.getNama(),
                        b.getNamaKategori(),
                        (hargaStr==null||hargaStr.isEmpty()) ? "" : hargaStr,
                        stokTotal
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data barang:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(String q) {
        if (barangDao == null) return;
        try {
            List<Barang> list = barangDao.findAll();
            model.setRowCount(0);
            NumberFormat nf = NumberFormat.getInstance(new Locale("in","ID"));
            if (q.isEmpty()) {
                for (Barang b : list) {
                    int stok = 0; String harga="";
                    if (detailDao != null) {
                        try {
                            java.util.List<DetailBarang> dets = detailDao.findByBarangId(b.getId());
                            for (DetailBarang d: dets) stok += d.getStok();
                            if (!dets.isEmpty() && dets.get(0).getHargaJual()!=null) harga = nf.format(dets.get(0).getHargaJual());
                        } catch (SQLException ignore) {}
                    }
                    model.addRow(new Object[]{b.getId(), b.getNama(), b.getNamaKategori(), harga, stok});
                }
            } else {
                String lower = q.toLowerCase();
                for (Barang b : list) {
                    if ((b.getNama()!=null && b.getNama().toLowerCase().contains(lower)) ||
                        (b.getNamaKategori()!=null && b.getNamaKategori().toLowerCase().contains(lower))) {
                        int stok = 0; String harga="";
                        if (detailDao != null) {
                            try {
                                java.util.List<DetailBarang> dets = detailDao.findByBarangId(b.getId());
                                for (DetailBarang d: dets) stok += d.getStok();
                                if (!dets.isEmpty() && dets.get(0).getHargaJual()!=null) harga = nf.format(dets.get(0).getHargaJual());
                            } catch (SQLException ignore) {}
                        }
                        model.addRow(new Object[]{b.getId(), b.getNama(), b.getNamaKategori(), harga, stok});
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencari data barang:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =============================
    // METHOD BUAT TOMBOL STYLISH
    // (tidak diubah)
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
    // (tidak diubah)
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
