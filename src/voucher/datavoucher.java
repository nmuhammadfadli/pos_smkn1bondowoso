package voucher;


import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

public class datavoucher extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    public datavoucher() {
        initUI();
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
        searchField.putClientProperty("JTextField.placeholderText", "Cari nama Voucher...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.setBorder(new RoundedBorder(15));

        // Tombol-tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        
        JButton btnTambah = createButton("Tambah", new Color(46, 204, 113));
        btnTambah.addActionListener(e -> {
    // ambil frame induk (Mainmenu)
    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
    if (frame instanceof uiresponsive.Mainmenu) {
        ((uiresponsive.Mainmenu) frame).showTambahDataVoucher(); // panggil method di Mainmenu
    }
});

        
        JButton btnEdit = createButton("Edit", new Color(52, 152, 219));
        btnEdit.addActionListener(e -> {
    // ambil frame induk (Mainmenu)
    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
    if (frame instanceof uiresponsive.Mainmenu) {
        ((uiresponsive.Mainmenu) frame).showEditDataVoucher();// panggil method di Mainmenu
    }
});
        
        JButton btnHapus = createButton("Hapus", new Color(231, 76, 60));
        JButton btnRefresh = createButton("Refresh", new Color(155, 89, 182));
    
        buttonPanel.add(btnTambah);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnRefresh);

        topPanel.add(searchField, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // =============================
        // TABEL DATA BARANG
        // =============================
        String[] columns = {"Kode Voucher", "Nominal", "Tanggal", "Nama Guru"};
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

        // Dummy data
        model.addRow(new Object[]{"VR0023", "20000", "20 Oktober 2025", "Mamba"});
        

        // =============================
        // TAMBAHKAN SEMUA KE PANEL
        // =============================
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    
    // =============================
    // METHOD BUAT TOMBOL STYLISH
    // =============================
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

            // ====== SOFT SHADOW (smooth gradation) ======
            for (int i = 8; i >= 1; i--) {
                float opacity = 0.03f * i; // makin lembut
                g2.setColor(new Color(0, 0, 0, (int) (opacity * 255)));
                g2.fillRoundRect(i, i + 3, width - (i * 2), height - (i * 2), arc, arc);
            }

            // ====== BUTTON BACKGROUND ======
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, width - 4, height - 4, arc, arc);

            // ====== TEKS ======
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

    // ====== Efek hover lembut ======
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
    // === Frame popup untuk pilih guru ===
class PilihGuruFrame extends JFrame {
    public PilihGuruFrame(JTextField targetField) {
        setTitle("Pilih Guru");
        setSize(600, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(250, 250, 250));

        // ====== Search bar ======
        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.setOpaque(false);
        JTextField txtSearch = new JTextField();
        JButton btnSearch = new JButton("Cari");
        styleButton(btnSearch, new Color(255, 140, 0));
        searchPanel.add(new JLabel("Cari Guru:"), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        panel.add(searchPanel, BorderLayout.NORTH);

        // ====== Tabel Data Guru ======
        String[] kolom = {"ID Guru", "Nama Guru", "Jabatan"};
        Object[][] data = {
            {"G001", "Mamba", "Guru TKJ"},
            {"G002", "Lina", "Guru AKP"},
            {"G003", "Rafi", "Guru RPL"},
            {"G004", "Siti", "Teknisi"}
        };

        DefaultTableModel model = new DefaultTableModel(data, kolom);
        JTable tabel = new JTable(model);
        tabel.setRowHeight(26);
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(tabel);
        panel.add(scroll, BorderLayout.CENTER);

        // ====== Tombol bawah ======
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);
        JButton btnPilih = new JButton("Pilih");
        JButton btnCancel = new JButton("Batal");
        styleButton(btnPilih, new Color(0, 180, 0));
        styleButton(btnCancel, new Color(220, 0, 0));
        btnPanel.add(btnPilih);
        btnPanel.add(btnCancel);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // ====== Aksi tombol ======
        btnCancel.addActionListener(e -> dispose());
        btnPilih.addActionListener(e -> {
            int row = tabel.getSelectedRow();
            if (row != -1) {
                String nama = tabel.getValueAt(row, 1).toString();
                targetField.setText(nama);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Pilih dulu gurunya!");
            }
        });

        add(panel);
        setVisible(true);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(120, 40));
    }
}

}

