package transaksi_penjualan;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

public class transaksipenjualan extends JPanel {

    public transaksipenjualan() {
        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));

        // ========== HEADER ==========
        JLabel tanggal = new JLabel("19 Oktober 2025", SwingConstants.RIGHT);
        tanggal.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 30, 0, 30));
        header.add(tanggal, BorderLayout.EAST);

        // ========== CONTENT ==========
        JPanel content = new JPanel(new GridLayout(1, 2, 40, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 50, 20, 50));

        // ========== PANEL KIRI ==========
        JPanel leftPanel = new JPanel(null);
        leftPanel.setOpaque(false);

        int x = 0, y = 0, fieldW = 450, fieldH = 38, gapY = 80;

        JLabel lblKode = makeLabel("Kode Transaksi:", x, y);
        JTextField txtKode = makeField(x, y + 22, fieldW, fieldH);
        leftPanel.add(lblKode);
        leftPanel.add(txtKode);

        y += gapY;
        JLabel lblNama = makeLabel("Nama Barang:", x, y);
        JTextField txtNama = makeField(x, y + 22, fieldW - 90, fieldH);
        JButton btnPilih = createButton("Pilih", new Color(255, 140, 0), 90, 46);
        btnPilih.setBounds(x + fieldW - 80, y + 22, 90, 46);
        btnPilih.addActionListener(e -> new PilihBarangFrame(txtNama)); // popup akan mengisi txtNama setelah pilih

        leftPanel.add(lblNama);
        leftPanel.add(txtNama);
        leftPanel.add(btnPilih);

        y += gapY;
        JLabel lblHarga = makeLabel("Harga:", x, y);
        JTextField txtHarga = makeField(x, y + 22, fieldW, fieldH);
        leftPanel.add(lblHarga);
        leftPanel.add(txtHarga);

        y += gapY;
        JLabel lblVoucher = makeLabel("Voucher (jika ada):", x, y);
        JTextField txtVoucher = makeField(x, y + 22, fieldW - 90, fieldH);
        JButton btnPilihVoucher = createButton("Pilih", new Color(255, 140, 0), 90, 46);
        btnPilihVoucher.setBounds(x + fieldW - 80, y + 22, 90, 46);
        btnPilihVoucher.addActionListener(e -> new PilihVoucherFrame(txtVoucher)); // buka popup

        leftPanel.add(lblVoucher);
        leftPanel.add(txtVoucher);
        leftPanel.add(btnPilihVoucher);


        y += gapY;
        JLabel lblJumlah = makeLabel("Jumlah:", x, y);
        JTextField txtJumlah = makeField(x, y + 22, fieldW, fieldH);
        leftPanel.add(lblJumlah);
        leftPanel.add(txtJumlah);

        // Tombol Masukkan Keranjang di bawah langsung
        JButton btnKeranjang = createButton("Masukkan Keranjang", new Color(0, 180, 0), fieldW + 10, 55);
        btnKeranjang.setBounds(x, y + gapY + 15, fieldW + 10, 55);
        leftPanel.add(btnKeranjang);

        // ========== PANEL KANAN ==========
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        // ---- Tabel Keranjang ----
        String[] kolom = {"Kode", "Nama Barang", "Jumlah", "Harga"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);
        JTable tabel = new JTable(model);
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabel.setRowHeight(28);
        tabel.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        scroll.setPreferredSize(new Dimension(520, 260));
        scroll.getViewport().setBackground(new Color(230, 230, 230));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210), 2, true));
        rightPanel.add(scroll);

        // ---- Tombol Cancel & Reset ----
        JPanel tombolPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        tombolPanel.setOpaque(false);
        JButton btnCancel = createButton("Cancel", new Color(218, 165, 32), 120, 45);
        JButton btnReset = createButton("Reset", new Color(255, 0, 0), 120, 45);
        tombolPanel.add(btnCancel);
        tombolPanel.add(btnReset);
        tombolPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tombolPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
        rightPanel.add(tombolPanel);

        // Jika mau: tambahkan aksi btnCancel / btnReset
        btnCancel.addActionListener(e -> {
            // contoh: kosongkan keranjang
            model.setRowCount(0);
        });
        btnReset.addActionListener(e -> {
            // contoh reset fields (jika mau)
            txtKode.setText("");
            txtNama.setText("");
            txtHarga.setText("");
            txtVoucher.setText("");
            txtJumlah.setText("");
        });

        // ---- Panel Pembayaran (4x2) dengan dropdown metode bayar ----
        JPanel bayarPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        bayarPanel.setOpaque(false);
        bayarPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Labels
        JLabel lblMetode = new JLabel("Metode Bayar:");
        lblMetode.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMetode.setForeground(Color.WHITE);
        JLabel lblTotal = new JLabel("Total Harga:");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTotal.setForeground(Color.WHITE);
        JLabel lblJumlahBayar = new JLabel("Jumlah Bayar:");
        lblJumlahBayar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblJumlahBayar.setForeground(Color.WHITE);
        JLabel lblKembali = new JLabel("Kembali:");
        lblKembali.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblKembali.setForeground(Color.WHITE);

        // Dropdown Metode Bayar
        String[] metodeBayarOptions = {"Tunai", "Transfer Bank", "QRIS", "Debit"};
        JComboBox<String> cmbMetodeBayar = new JComboBox<>(metodeBayarOptions);
        cmbMetodeBayar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbMetodeBayar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(5, 10, 5, 10)));
        cmbMetodeBayar.setBackground(Color.WHITE);
        cmbMetodeBayar.setPreferredSize(new Dimension(200, 35));
        cmbMetodeBayar.setOpaque(true);
        cmbMetodeBayar.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                if (isSelected) {
                    c.setBackground(new Color(0, 120, 215));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        // Rounded text fields
        JTextField txtTotalHarga = createRoundedField();
        JTextField txtJumlahBayar = createRoundedField();
        JTextField txtKembali = createRoundedField();

        // Add to bayarPanel (4x2)
        bayarPanel.add(lblMetode);
        bayarPanel.add(cmbMetodeBayar);
        bayarPanel.add(lblTotal);
        bayarPanel.add(txtTotalHarga);
        bayarPanel.add(lblJumlahBayar);
        bayarPanel.add(txtJumlahBayar);
        bayarPanel.add(lblKembali);
        bayarPanel.add(txtKembali);

        // Wrapper with subtle border (gives feeling of card/shadow)
        JPanel bayarWrapper = new JPanel();
        bayarWrapper.setLayout(new BoxLayout(bayarWrapper, BoxLayout.Y_AXIS));
        bayarWrapper.setBackground(new Color(255, 140, 0));
        bayarWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(6, 6, 6, 6),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 1, 3, 3, new Color(230, 230, 230)),
                        new EmptyBorder(12, 12, 12, 12)
                )
        ));
        bayarWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        bayarWrapper.add(bayarPanel);
        bayarWrapper.setMaximumSize(new Dimension(520, 220));
        rightPanel.add(bayarWrapper);

        // ==== TOMBOL SELESAIKAN TRANSAKSI ====
        JButton btnSelesai = createButton("Selesaikan Transaksi", new Color(0, 200, 0), 520, 55);
        btnSelesai.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel selesaiPanel = new JPanel();
        selesaiPanel.setOpaque(false);
        selesaiPanel.add(btnSelesai);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(selesaiPanel);

        // contoh aksi: masukkan keranjang ke tabel
        btnKeranjang.addActionListener(e -> {
            String kode = txtKode.getText().trim();
            String nama = txtNama.getText().trim();
            String jumlah = txtJumlah.getText().trim();
            String harga = txtHarga.getText().trim();

            if (kode.isEmpty() || nama.isEmpty() || jumlah.isEmpty() || harga.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lengkapi semua data terlebih dahulu!");
                return;
            }

            model.addRow(new Object[]{kode, nama, jumlah, harga});
            // kosongkan field setelah ditambahkan
            txtKode.setText("");
            txtNama.setText("");
            txtJumlah.setText("");
            txtHarga.setText("");
        });

        // aksi selesai: contoh validasi sederhana
        btnSelesai.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            model.setRowCount(0); // kosongkan keranjang setelah sukses (opsional)
        });

        // ======== Gabungkan Semua ========
        content.add(leftPanel);
        content.add(rightPanel);

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    // =================== HELPERS ===================

    private JLabel makeLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setBounds(x, y, 200, 20);
        return lbl;
    }

    private JTextField makeField(int x, int y, int w, int h) {
        JTextField field = createRoundedField();
        field.setBounds(x, y, w, h);
        return field;
    }

    private JPanel createSmallField(String labelText) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField field = createRoundedField();
        field.setPreferredSize(new Dimension(100, 35));
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JTextField createRoundedField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.setColor(new Color(190, 190, 190));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    private JButton createButton(String text, Color bgColor, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // smooth shadow lembut
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 20, 20);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(width, height));
        return button;
    }

    // ===== TESTING =====
    public static void main(String[] args) {
        JFrame f = new JFrame("Transaksi Penjualan");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1300, 750);
        f.setLocationRelativeTo(null);
        f.setContentPane(new transaksipenjualan());
        f.setVisible(true);
    }
    // ============================
// FRAME PILIH BARANG
// ============================
class PilihBarangFrame extends JFrame {
    public PilihBarangFrame(JTextField targetField) {
        setTitle("Pilih Barang");
        setSize(700, 500);
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
        searchPanel.add(new JLabel("Cari Barang:"), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Tabel Barang
        String[] kolom = {"Kode", "Nama Barang", "Harga", "Stok"};
        Object[][] data = {
            {"B001", "Beras Premium", 12000, 50},
            {"B002", "Minyak Goreng", 15000, 30},
            {"B003", "Gula Pasir", 14000, 40},
            {"B004", "Tepung Terigu", 11000, 25}
        };
        DefaultTableModel model = new DefaultTableModel(data, kolom);
        JTable tabel = new JTable(model);
        tabel.setRowHeight(26);
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(tabel);
        panel.add(scroll, BorderLayout.CENTER);

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
                targetField.setText(nama); // langsung isi ke textfield utama
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Pilih dulu barangnya!");
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
// ============================
// FRAME PILIH VOUCHER
// ============================
class PilihVoucherFrame extends JFrame {
    public PilihVoucherFrame(JTextField targetField) {
        setTitle("Pilih Voucher");
        setSize(650, 450);
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
        searchPanel.add(new JLabel("Cari Voucher:"), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Tabel Voucher
        String[] kolom = {"Kode Voucher", "Nominal", "Nama Guru"};
        Object[][] data = {
            {"V001", "50.000", "Pak Mursid"},
            {"V002", "100.000", "Bu Sinta"},
            {"V003", "25.000", "Pak Andi"}
        };
        DefaultTableModel model = new DefaultTableModel(data, kolom);
        JTable tabel = new JTable(model);
        tabel.setRowHeight(26);
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(tabel);
        panel.add(scroll, BorderLayout.CENTER);

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
                String kode = tabel.getValueAt(row, 0).toString(); // ambil kolom pertama (kode voucher)
                targetField.setText(kode);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Pilih dulu vouchernya!");
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
