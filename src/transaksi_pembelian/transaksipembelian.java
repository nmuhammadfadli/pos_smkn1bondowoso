package transaksi_pembelian;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

import Helper.DatabaseHelper;
import barang.DetailBarangDAO;
import barang.BarangDAO;
import barang.DetailBarang;
import barang.Barang;
import java.text.NumberFormat;

/**
 * transaksipembelian - UI asli dipertahankan, tambahan fungsional pembelian:
 * - Pilih barang/supplier dari DB
 * - Keranjang internal (List<DetailPembelian>)
 * - Simpan transaksi via PembelianDAO.insertPembelianWithDetails(...)
 */
public class transaksipembelian extends JPanel {

    // cart store
    private final List<DetailPembelian> cart = new ArrayList<>();

    public transaksipembelian() {
        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));

        // ========== HEADER ==========
        JLabel tanggal = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), SwingConstants.RIGHT);
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

       JLabel lblKode = makeLabel("ID Pembelian:", x, y);
        JTextField txtKode = makeField(x, y + 22, fieldW, fieldH);

        // tampilkan ID awal (generate jika memungkinkan)
        try {
            String initialId = generateNewIdPembelianLocal();
            txtKode.setText(initialId);
        } catch (Exception ex) {
            // jika gagal (DB unreachable dsb) biarkan kosong — user bisa memasukkan manual
            txtKode.setText("");
        }
        leftPanel.add(lblKode);
        leftPanel.add(txtKode);

        leftPanel.add(lblKode);
        leftPanel.add(txtKode);

        y += gapY;
        JLabel lblNama = makeLabel("Nama Barang:", x, y);
        JTextField txtNama = makeField(x, y + 22, fieldW - 90, fieldH);
        JButton btnPilih = createButton("Pilih", new Color(26, 97, 145), 90, 46);
        btnPilih.setBounds(x + fieldW - 80, y + 22, 90, 46);
        btnPilih.addActionListener(e -> new PilihBarangFrame(txtNama));
        leftPanel.add(lblNama);
        leftPanel.add(txtNama);
        leftPanel.add(btnPilih);

        y += gapY;
        JLabel lblNamasup = makeLabel("Nama Supplier:", x, y);
        JTextField txtNamasup = makeField(x, y + 22, fieldW - 90, fieldH);
        JButton btnPilihsup = createButton("Pilih", new Color(26, 97, 145), 90, 46);
        btnPilihsup.setBounds(x + fieldW - 80, y + 22, 90, 46);
        btnPilihsup.addActionListener(e -> new PilihSupplierFrame(txtNamasup));
        leftPanel.add(lblNamasup);
        leftPanel.add(txtNamasup);
        leftPanel.add(btnPilihsup);

        y += gapY;
        JLabel lblHarga = makeLabel("Harga Beli:", x, y);
        JTextField txtHarga = makeField(x, y + 22, fieldW, fieldH);
        leftPanel.add(lblHarga);
        leftPanel.add(txtHarga);

        y += gapY;
        JLabel lblJumlah = makeLabel("Jumlah:", x, y);
        JTextField txtJumlah = makeField(x, y + 22, fieldW, fieldH);
        leftPanel.add(lblJumlah);
        leftPanel.add(txtJumlah);

        // Tombol Masukkan Keranjang di bawah langsung
        JButton btnKeranjang = createButton("Masukkan Keranjang", new Color(26, 97, 145), fieldW + 10, 55);
        btnKeranjang.setBounds(x, y + gapY + 15, fieldW + 10, 55);
        leftPanel.add(btnKeranjang);

        // ========== PANEL KANAN ==========
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        // ---- Tabel Keranjang ----
        String[] kolom = {"Kode", "Nama Barang", "Jumlah", "Harga", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);
        JTable tabel = new JTable(model);
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabel.setRowHeight(28);
        tabel.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        tabel.getTableHeader().setBackground(new Color(60, 80, 120));
        tabel.getTableHeader().setForeground(Color.WHITE);
        tabel.setGridColor(new Color(230, 230, 230));
        tabel.setSelectionBackground(new Color(93, 173, 226));
        tabel.setSelectionForeground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        scroll.setPreferredSize(new Dimension(520, 260));
        scroll.getViewport().setBackground(new Color(230, 230, 230));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210), 2, true));
        rightPanel.add(scroll);

        // ---- Tombol Cancel & Reset ----
        JPanel tombolPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        tombolPanel.setOpaque(false);
        JButton btnCancel = createButton("Cancel", new Color(155, 89, 182), 120, 45);
        JButton btnReset = createButton("Reset", new Color(231, 76, 60), 120, 45);
        tombolPanel.add(btnCancel);
        tombolPanel.add(btnReset);
        tombolPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tombolPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
        rightPanel.add(tombolPanel);

        // Jika mau: tambahkan aksi btnCancel / btnReset
        btnCancel.addActionListener(e -> {
            model.setRowCount(0);
            cart.clear();
        });
            btnReset.addActionListener(e -> {
            // generate id baru untuk field kode (fall back ke kosong jika error)
            try {
                txtKode.setText(generateNewIdPembelianLocal());
            } catch (Exception ex) {
                txtKode.setText("");
            }
            txtNama.setText("");
            txtHarga.setText("");
            txtJumlah.setText("");
        });


        // ---- Panel Pembayaran (4x2) dengan dropdown metode bayar ----
        JPanel bayarPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        bayarPanel.setOpaque(false);
        bayarPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Labels
        JLabel lblMetode = new JLabel("Metode Bayar:");
        lblMetode.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblMetode.setForeground(Color.WHITE);
        JLabel lblTotal = new JLabel("Total Harga:");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblTotal.setForeground(Color.WHITE);
        JLabel lblJumlahBayar = new JLabel("Jumlah Bayar:");
        lblJumlahBayar.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblJumlahBayar.setForeground(Color.WHITE);
        JLabel lblKembali = new JLabel("Kembali:");
        lblKembali.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblKembali.setForeground(Color.WHITE);

        // Dropdown Metode Bayar
        String[] metodeBayarOptions = {"Faktur", "Cash", "Belum Bayar"};
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
        bayarWrapper.setBackground(new Color(53, 67, 77));
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
        JButton btnSelesai = createButton("Selesaikan Transaksi", new Color(26, 97, 145), 520, 55);
        btnSelesai.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel selesaiPanel = new JPanel();
        selesaiPanel.setOpaque(false);
        selesaiPanel.add(btnSelesai);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(selesaiPanel);

        // contoh aksi: masukkan keranjang ke tabel
        btnKeranjang.addActionListener(e -> {
            String kodeHeader = txtKode.getText().trim();
            String nama = txtNama.getText().trim();
            String jumlahStr = txtJumlah.getText().trim();
            String hargaStr = txtHarga.getText().trim();

            if (nama.isEmpty() || jumlahStr.isEmpty() || hargaStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lengkapi semua data (barang, jumlah, harga) terlebih dahulu!");
                return;
            }

            Object selectedBarangIdObj = txtNama.getClientProperty("selectedBarangId");
            Object selectedDetailIdObj = txtNama.getClientProperty("selectedDetailId");
            if (selectedBarangIdObj == null) {
                JOptionPane.showMessageDialog(this, "Pilih barang dari daftar (klik Pilih) agar ID barang tersimpan.");
                return;
            }
            Integer idBarang = null;
            Integer idDetailBarang = null;
            try {
                if (selectedBarangIdObj instanceof Integer) idBarang = (Integer) selectedBarangIdObj;
                else idBarang = Integer.parseInt(String.valueOf(selectedBarangIdObj));
            } catch (Exception ex) {}
            try {
                if (selectedDetailIdObj instanceof Integer) idDetailBarang = (Integer) selectedDetailIdObj;
                else idDetailBarang = Integer.parseInt(String.valueOf(selectedDetailIdObj));
            } catch (Exception ex) {}

            // supplier id jika dipilih
            Object selectedSupplierIdObj = txtNamasup.getClientProperty("selectedSupplierId");
            Integer idSupplier = null;
            if (selectedSupplierIdObj != null) {
                try {
                    if (selectedSupplierIdObj instanceof Integer) idSupplier = (Integer) selectedSupplierIdObj;
                    else idSupplier = Integer.parseInt(String.valueOf(selectedSupplierIdObj));
                } catch (Exception ex) { idSupplier = null; }
            }

            int jumlah = 0;
            int harga = 0;
            try { jumlah = Integer.parseInt(jumlahStr); } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka bulat.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try { harga = Integer.parseInt(hargaStr.replaceAll("[^0-9]", "")); } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Harga harus berupa angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int subtotal = jumlah * harga;
            // tambahkan ke tabel UI
            model.addRow(new Object[]{ Objects.toString(idBarang, ""), nama, jumlah, harga, subtotal });

            // tambahkan ke cart list (DetailPembelian class dari paket transaksi_pembelian)
            DetailPembelian d = new DetailPembelian();
            d.setIdBarang(idBarang);
            d.setIdSupplier(idSupplier);
            d.setHargaBeli(harga);
            d.setStok(jumlah);
            d.setSubtotal(subtotal);
            // id_pembelian akan di-set saat create header sebelum insert
            cart.add(d);

            // kosongkan field setelah ditambahkan
            txtNama.setText("");
            txtNama.putClientProperty("selectedBarangId", null);
            txtNama.putClientProperty("selectedDetailId", null);
            txtHarga.setText("");
            txtJumlah.setText("");

            // update total di UI
            int tot = cart.stream().mapToInt(DetailPembelian::getSubtotal).sum();
            txtTotalHarga.setText(String.valueOf(tot));
        });

        // aksi selesai: buat Pembelian object dan simpan via PembelianDAO
        btnSelesai.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Keranjang kosong. Tambahkan barang terlebih dahulu.");
                return;
            }

            String idPembelian = txtKode.getText().trim();
            try {
                // jika kosong, generate new id (mirip DAO)
                if (idPembelian.isEmpty()) {
                    idPembelian = generateNewIdPembelianLocal();
                }

                Pembelian p = new Pembelian();
                p.setIdPembelian(idPembelian);
                p.setTglPembelian(LocalDate.now().toString());
                p.setPaymentMethod((String) cmbMetodeBayar.getSelectedItem());
                int totalHarga = cart.stream().mapToInt(DetailPembelian::getSubtotal).sum();
                p.setTotalHarga(totalHarga);
                p.setDetails(new ArrayList<>(cart));

                PembelianDAO dao = new PembelianDAO();
                dao.insertPembelianWithDetails(p);

                JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan! ID: " + idPembelian, "Sukses", JOptionPane.INFORMATION_MESSAGE);
                // bersihkan
                // bersihkan
            model.setRowCount(0);
            cart.clear();

            // set new generated id agar siap untuk transaksi berikutnya
            try {
                txtKode.setText(generateNewIdPembelianLocal());
            } catch (Exception ex) {
                txtKode.setText("");
            }

            txtTotalHarga.setText("");
            txtJumlahBayar.setText("");
            txtKembali.setText("");

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi:\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
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

    // generate new id pembelian local (mengikuti logika di PembelianDAO)
    private String generateNewIdPembelianLocal() throws SQLException {
        String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
        String defaultId = today + "0001";
        String sql = "SELECT id_pembelian FROM data_pembelian WHERE id_pembelian LIKE ? ORDER BY id_pembelian DESC LIMIT 1";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, today + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String lastId = rs.getString(1);
                    if (lastId != null && lastId.length() >= 12) {
                        String seq = lastId.substring(8);
                        try {
                            int lastNum = Integer.parseInt(seq);
                            return today + String.format("%04d", lastNum + 1);
                        } catch (NumberFormatException ignore) {}
                    }
                }
            }
        } catch (Exception e) {
            // ignore, fallback to default
        }
        return defaultId;
    }

    // ===== TESTING =====
    public static void main(String[] args) {
        JFrame f = new JFrame("Transaksi Pembelian");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1300, 750);
        f.setLocationRelativeTo(null);
        f.setContentPane(new transaksipembelian());
        f.setVisible(true);
    }

    // ============================
    // FRAME PILIH BARANG (membaca dari DB)
    // ============================
    class PilihBarangFrame extends JFrame {
        public PilihBarangFrame(JTextField targetField) {
            setTitle("Pilih Barang");
            setSize(800, 500);
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

            // Tabel Barang (ambil dari DB: gabungan barang + detail-only)
            String[] kolom = {"ID Detail", "ID Barang", "Nama Barang", "Harga", "Stok"};
            DefaultTableModel model = new DefaultTableModel(kolom, 0);
            JTable tabel = new JTable(model);
            tabel.setRowHeight(26);
            tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JScrollPane scroll = new JScrollPane(tabel);
            panel.add(scroll, BorderLayout.CENTER);

            // load awal
            loadBarangIntoModel(model, "");

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
                    Object idDetailObj = model.getValueAt(row, 0);
                    Object idBarangObj = model.getValueAt(row, 1);
                    Object namaObj = model.getValueAt(row, 2);
                    Object hargaObj = model.getValueAt(row, 3);

                    // convert to Integer when possible
                    Integer idDetail = null;
                    Integer idBarang = null;
                    try { if (idDetailObj != null && !"".equals(String.valueOf(idDetailObj))) idDetail = Integer.parseInt(String.valueOf(idDetailObj)); } catch (Exception ignore) {}
                    try { if (idBarangObj != null && !"".equals(String.valueOf(idBarangObj))) idBarang = Integer.parseInt(String.valueOf(idBarangObj)); } catch (Exception ignore) {}

                    String nama = namaObj == null ? "" : String.valueOf(namaObj);
                    String harga = hargaObj == null ? "" : String.valueOf(hargaObj);

                    // set ke target field dan simpan client properties (id)
                    targetField.setText(nama);
                    targetField.putClientProperty("selectedBarangId", idBarang);
                    targetField.putClientProperty("selectedDetailId", idDetail);
                    targetField.putClientProperty("selectedHarga", harga);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Pilih dulu barangnya!");
                }
            });

            btnSearch.addActionListener(ev -> {
                String q = txtSearch.getText().trim();
                loadBarangIntoModel(model, q);
            });

            add(panel);
            setVisible(true);
        }

        /**
         * Load model with:
         * 1) All Barang (one row per barang) with aggregated stok and representative harga (first detail)
         * 2) All detail_barang entries that do not belong to any row above (id_barang NULL or pointing to non-existing barang)
         *
         * Search q matches barang.nama / barang.nama_kategori OR detail.barcode
         */
        private void loadBarangIntoModel(DefaultTableModel model, String q) {
            model.setRowCount(0);
            String lowerQ = q == null ? "" : q.trim().toLowerCase();
            try {
                BarangDAO bdao = new BarangDAO();
                DetailBarangDAO ddao = new DetailBarangDAO();

                List<Barang> barangList = bdao.findAll();
                Set<Integer> seenBarangIds = new HashSet<>();
                // 1) barang rows
                for (Barang b : barangList) {
                    boolean matches = lowerQ.isEmpty()
                            || (b.getNama() != null && b.getNama().toLowerCase().contains(lowerQ))
                            || (b.getNamaKategori() != null && b.getNamaKategori().toLowerCase().contains(lowerQ));

                    if (!matches) continue;

                    int stokTotal = 0;
                    String hargaStr = "";
                    try {
                        List<DetailBarang> dets = ddao.findByBarangId(b.getId());
                        if (dets != null && !dets.isEmpty()) {
                            for (DetailBarang d : dets) {
                                if (d != null && d.getStok() > 0) stokTotal += d.getStok();
                            }
                            if (dets.get(0).getHargaJual() != null) {
                                NumberFormat nf = NumberFormat.getInstance(new Locale("in","ID"));
                                hargaStr = nf.format(dets.get(0).getHargaJual());
                            }
                        }
                    } catch (SQLException ex) {
                        // ignore — tampilkan barang tanpa detail
                    }

                    // add row: mark idDetail empty because row represents barang aggregate
                    model.addRow(new Object[]{ "", b.getId(), b.getNama(), hargaStr, stokTotal });
                    seenBarangIds.add(b.getId());
                }

                // 2) detail-only rows (detail entries whose id_barang is NULL or not present in barangList)
                // We'll query all detail_barang and include those whose id_barang is null or missing
                String sql = "SELECT id_detail_barang, id_barang, barcode, stok, harga_jual FROM detail_barang ORDER BY id_detail_barang";
                try (Connection conn = DatabaseHelper.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Integer idBarang = null;
                        int ib = rs.getInt("id_barang");
                        if (!rs.wasNull()) idBarang = ib;

                        // if idBarang present and we already showed the parent barang, skip this detail (we already represent via aggregate)
                        if (idBarang != null && seenBarangIds.contains(idBarang)) continue;

                        Integer idDetail = rs.getInt("id_detail_barang");
                        if (rs.wasNull()) idDetail = null;
                        String barcode = rs.getString("barcode");
                        Object stokObj = rs.getObject("stok");
                        String stokStr = stokObj == null ? "" : String.valueOf(stokObj);
                        String harga = rs.getString("harga_jual");
                        if (harga == null) harga = "";

                        // search/filter: if q provided, require match on barcode (for detail-only) or on harga etc.
                        boolean matchesDetail = lowerQ.isEmpty()
                                || (barcode != null && barcode.toLowerCase().contains(lowerQ))
                                || (harga != null && harga.toLowerCase().contains(lowerQ));

                        if (!matchesDetail) continue;

                        // display name: barcode (since no barang record)
                        String displayName = (barcode == null || barcode.trim().isEmpty()) ? "(detail tanpa nama)" : "(no-name) " + barcode;

                        model.addRow(new Object[]{ idDetail == null ? "" : idDetail, idBarang == null ? "" : idBarang, displayName, harga, stokStr });
                    }
                } catch (SQLException ex) {
                    // ignore detail-only load error — at least barang rows are shown
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal memuat barang:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        private String generatePembelianId() {
    String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
    int nextNumber = 1;

    // ambil ID terakhir dari tabel pembelian yang sesuai tanggal hari ini
    try (java.sql.Connection conn = DatabaseHelper.getConnection();
         java.sql.PreparedStatement ps = conn.prepareStatement(
                 "SELECT id_pembelian FROM data_pembelian WHERE id_pembelian LIKE ? ORDER BY id_pembelian DESC LIMIT 1")) {
        ps.setString(1, today + "%");
        try (java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString(1); // contoh: 221020250005
                String seqStr = lastId.substring(8); // ambil 0005
                try {
                    nextNumber = Integer.parseInt(seqStr) + 1;
                } catch (NumberFormatException ignored) {}
            }
        }
    } catch (Exception e) {
        System.err.println("Gagal cek ID terakhir: " + e.getMessage());
    }

    // format ke 4 digit (misal 0001)
    String newSeq = String.format("%04d", nextNumber);
    return today + newSeq;
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
    // FRAME PILIH SUPPLIER (membaca dari DB)
    // ============================
    class PilihSupplierFrame extends JFrame {
        public PilihSupplierFrame(JTextField targetField) {
            setTitle("Pilih Supplier");
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
            searchPanel.add(new JLabel("Cari Supplier:"), BorderLayout.WEST);
            searchPanel.add(txtSearch, BorderLayout.CENTER);
            searchPanel.add(btnSearch, BorderLayout.EAST);
            panel.add(searchPanel, BorderLayout.NORTH);

            // Tabel Supplier
            String[] kolom = {"ID", "Nama Supplier", "No. Telp"};
            DefaultTableModel model = new DefaultTableModel(kolom, 0);
            JTable tabel = new JTable(model);
            tabel.setRowHeight(26);
            tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JScrollPane scroll = new JScrollPane(tabel);
            panel.add(scroll, BorderLayout.CENTER);

            loadSupplierIntoModel(model, "");

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
                    String id = String.valueOf(model.getValueAt(row, 0));
                    String nama = model.getValueAt(row, 1).toString();
                    targetField.setText(nama); // isi otomatis ke textfield utama
                    // simpan id ke client property agar main panel dapat baca (Integer)
                    try {
                        targetField.putClientProperty("selectedSupplierId", Integer.valueOf(id));
                    } catch (Exception ex) {
                        targetField.putClientProperty("selectedSupplierId", id);
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Pilih dulu suppliernya!");
                }
            });

            btnSearch.addActionListener(ev -> {
                String q = txtSearch.getText().trim();
                loadSupplierIntoModel(model, q);
            });

            add(panel);
            setVisible(true);
        }

        private void loadSupplierIntoModel(DefaultTableModel model, String q) {
            model.setRowCount(0);
            String sql;
            if (q == null || q.isEmpty()) {
                sql = "SELECT id_supplier, nama_supplier, notelp_supplier FROM data_supplier ORDER BY nama_supplier";
            } else {
                sql = "SELECT id_supplier, nama_supplier, notelp_supplier FROM data_supplier WHERE LOWER(nama_supplier) LIKE ? ORDER BY nama_supplier";
            }
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                if (q != null && !q.isEmpty()) ps.setString(1, "%" + q.toLowerCase() + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{ rs.getInt("id_supplier"), rs.getString("nama_supplier"), rs.getString("notelp_supplier") });
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal memuat supplier:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

}
