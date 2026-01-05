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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Helper.DatabaseHelper;
import barang.Barang;
import barang.BarangDAO;
import barang.DetailBarangDAO;
import barang.DetailBarang;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class transaksipembelian extends JPanel {

    // ==== Logic Store ====
    private final List<DetailPembelian> cart = new ArrayList<>();

    // ==== UI Components ====
    private JTextField txtKode, txtTanggal, txtNama, txtNamasup, txtHarga, txtJumlah;
    
    // Dashboard Besar (JLabel Angka)
    private JLabel lblBigTotal, lblBigBayar, lblBigKembali;

    // Tabel
    private DefaultTableModel model;
    private JTable tabel;

    // Footer Inputs
    private JTextField txtFooterTotal, txtFooterBayar, txtFooterKembali;
    private JComboBox<String> cmbMetodeBayar;
    
    // Tombol & Checkbox
    private JButton btnHapus, btnSimpan;
   

    // ==== PALETTE WARNA ====
    private final Color BG_MAIN = new Color(242, 245, 255); 
    private final Color BOX_RED = new Color(255, 215, 215);
    private final Color BOX_YELLOW = new Color(255, 253, 208);
    private final Color BOX_GREEN = new Color(210, 255, 210);
    private final Color BTN_RED = new Color(220, 53, 69);
    private final Color BTN_GREEN = new Color(40, 167, 69);
    private final Color BTN_PURPLE = new Color(111, 66, 193);

    public transaksipembelian() {
        setLayout(new BorderLayout(25, 25)); 
        setBackground(BG_MAIN);
        setBorder(new EmptyBorder(25, 35, 25, 35)); 

        initUI();
        initLogicListeners();
        resetFields();
    }

    private void initUI() {
        // ================= TOP SECTION =================
        JPanel topPanel = new JPanel(new BorderLayout(30, 0));
        topPanel.setOpaque(false);

        // --- Left Side (Inputs) ---
        JPanel topLeftContainer = new JPanel();
        topLeftContainer.setLayout(new BoxLayout(topLeftContainer, BoxLayout.Y_AXIS));
        topLeftContainer.setOpaque(false);

        JLabel lblTitle = new JLabel("Transaksi Pembelian");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(new Color(50, 50, 70));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        topLeftContainer.add(lblTitle);
        topLeftContainer.add(Box.createVerticalStrut(20));

        // Row 1: ID & Tanggal
        JPanel row1 = new JPanel(new GridLayout(1, 2, 20, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(1200, 70)); 
        
        txtKode = createModernField(row1, "ID Pembelian");
        txtTanggal = createModernField(row1, "Tanggal");
        txtTanggal.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        txtKode.setEditable(false);
        txtTanggal.setEditable(false);
        
        topLeftContainer.add(row1);
        topLeftContainer.add(Box.createVerticalStrut(15));

        // Row 2: Supplier & Barang
        JPanel row2 = new JPanel(new GridLayout(1, 2, 20, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(1200, 70));

        txtNamasup = createModernField(row2, "Supplier");
        txtNamasup.setEditable(false);
        txtNamasup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        txtNama = createModernField(row2, "Barang");
        txtNama.setEditable(false);
        txtNama.setCursor(new Cursor(Cursor.HAND_CURSOR));

        topLeftContainer.add(row2);
        topLeftContainer.add(Box.createVerticalStrut(15));

        // Row 3: Harga & Jumlah
        JPanel row3 = new JPanel(new GridLayout(1, 2, 20, 0));
        row3.setOpaque(false);
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3.setMaximumSize(new Dimension(1200, 70));

        txtHarga = createModernField(row3, "Harga Beli");
        txtJumlah = createModernField(row3, "Jumlah");

        topLeftContainer.add(row3);

        // --- Right Side (Dashboard) ---
        JPanel topRightDashboard = new JPanel(new GridLayout(3, 1, 0, 10)); 
        topRightDashboard.setOpaque(false);
        topRightDashboard.setPreferredSize(new Dimension(320, 180)); 

        lblBigTotal = new JLabel("0", SwingConstants.RIGHT);
        lblBigBayar = new JLabel("0", SwingConstants.RIGHT);
        lblBigKembali = new JLabel("0", SwingConstants.RIGHT);

        topRightDashboard.add(createDashboardBox("Total", lblBigTotal, BOX_RED));
        topRightDashboard.add(createDashboardBox("Bayar", lblBigBayar, BOX_YELLOW));
        topRightDashboard.add(createDashboardBox("Kembali", lblBigKembali, BOX_GREEN));

        topPanel.add(topLeftContainer, BorderLayout.CENTER);
        topPanel.add(topRightDashboard, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // ================= CENTER SECTION (TABLE) =================
        String[] columns = {"ID", "Nama Barang", "Jumlah", "Harga", "Subtotal"};
        model = new DefaultTableModel(columns, 0) {
             @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        tabel = new JTable(model);
        styleModernTable();
        
        JScrollPane scrollPane = new JScrollPane(tabel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200,200,200), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(scrollPane);
        tablePanel.setBorder(new EmptyBorder(10,0,10,0));
        tablePanel.setOpaque(false);
        
        add(tablePanel, BorderLayout.CENTER);

        // ================= BOTTOM SECTION =================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // -- Bottom Left (Totals) --
        JPanel bottomLeft = new JPanel(new GridLayout(3, 1, 5, 15));
        bottomLeft.setOpaque(false);
        bottomLeft.setPreferredSize(new Dimension(380, 150));

        txtFooterTotal = createFooterField();
        txtFooterTotal.setEditable(false);
        bottomLeft.add(createFooterRow("Total", txtFooterTotal));

        JPanel bayarContainer = new JPanel(new GridLayout(1, 2, 10, 0));
        bayarContainer.setOpaque(false);
        
        txtFooterBayar = createFooterField(); 
        txtFooterBayar.setBackground(Color.WHITE);
        
        String[] metode = {"Cash", "Faktur", "Belum Bayar"};
        cmbMetodeBayar = new JComboBox<>(metode);
        cmbMetodeBayar.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        bayarContainer.add(txtFooterBayar);
        bayarContainer.add(cmbMetodeBayar); 
        bottomLeft.add(createFooterRow("Bayar", bayarContainer));

        txtFooterKembali = createFooterField();
        txtFooterKembali.setEditable(false);
        bottomLeft.add(createFooterRow("Kembali", txtFooterKembali));

        // -- Bottom Right (Buttons + Checkbox) --
        JPanel bottomRight = new JPanel();
        bottomRight.setLayout(new BoxLayout(bottomRight, BoxLayout.Y_AXIS)); // Stack vertikal
        bottomRight.setOpaque(false);
        
        // Panel Tombol (FlowLayout Right)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        btnSimpan = createModernButton("Simpan", BTN_GREEN);
        btnHapus = createModernButton("Hapus", BTN_RED);

        btnPanel.add(btnSimpan);
        btnPanel.add(btnHapus);

 

        // Gabung
        bottomRight.add(btnPanel);

        bottomPanel.add(bottomLeft, BorderLayout.WEST);
        bottomPanel.add(bottomRight, BorderLayout.EAST); 

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ================= HELPER UI =================
    
    private JTextField createModernField(JPanel parent, String labelText) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel l = new JLabel(labelText);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(new Color(80,80,80));
        
        JTextField f = new RoundedTextField(15);
        f.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        f.setFont(new Font("SansSerif", Font.PLAIN, 15));
        f.setPreferredSize(new Dimension(100, 45)); 
        
        p.add(l, BorderLayout.NORTH);
        p.add(f, BorderLayout.CENTER);
        parent.add(p);
        return f;
    }

    private JPanel createDashboardBox(String title, JLabel valLabel, Color bg) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); 
                g2.setColor(new Color(0,0,0,30));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(5, 15, 5, 15)); 
        
        JLabel tLabel = new JLabel(title);
        tLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        tLabel.setForeground(new Color(60,60,60));
        
        valLabel.setFont(new Font("Consolas", Font.BOLD, 26));
        valLabel.setForeground(Color.BLACK);
        
        p.add(tLabel, BorderLayout.WEST);
        p.add(valLabel, BorderLayout.CENTER);
        return p;
    }

    private JPanel createFooterRow(String label, Component field) {
        JPanel p = new JPanel(new BorderLayout(15, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        l.setPreferredSize(new Dimension(90, 30));
        p.add(l, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField createFooterField() {
        JTextField f = new RoundedTextField(10);
        f.setBackground(new Color(245, 245, 245)); 
        f.setBorder(BorderFactory.createCompoundBorder(
            f.getBorder(), 
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        f.setFont(new Font("SansSerif", Font.BOLD, 18));
        f.setHorizontalAlignment(SwingConstants.RIGHT);
        return f;
    }

    private JButton createModernButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(baseColor.darker());
                else if (getModel().isRollover()) g2.setColor(baseColor.brighter());
                else g2.setColor(baseColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(new Dimension(110, 45)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleModernTable() {
        tabel.setRowHeight(35); 
        tabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tabel.setGridColor(new Color(230, 230, 230));
        tabel.setIntercellSpacing(new Dimension(0, 0));
        tabel.setSelectionBackground(new Color(220, 240, 255));
        tabel.setSelectionForeground(Color.BLACK);
        JTableHeader header = tabel.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(0, 40)); 
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200,200,200)));
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }

    private class RoundedTextField extends JTextField {
        private int radius;
        public RoundedTextField(int radius) {
            this.radius = radius;
            setOpaque(false); 
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            g2.setColor(new Color(180, 180, 180));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    // ================= LOGIC HANDLERS =================

    private void initLogicListeners() {
        // 1. Popup Listeners
        txtNama.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { new PilihBarangFrame(txtNama); }
        });
        txtNamasup.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { new PilihSupplierFrame(txtNamasup); }
        });

        // 2. Auto-format Harga
        txtHarga.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    String raw = txtHarga.getText().replace(".", "").replace(",", "");
                    if(raw.isEmpty()) return;
                    long val = Long.parseLong(raw);
                    txtHarga.setText(formatThousand(val));
                } catch(Exception ex){}
            }
        });

        // 3. Enter di Jumlah
        txtJumlah.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onTambah();
            }
        });

        // 4. Enter di Bayar
        txtFooterBayar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onSimpan();
            }
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) return;
                try {
                    String raw = txtFooterBayar.getText().replace(".", "").replace(",", "");
                    if(raw.isEmpty()) return;
                    long val = Long.parseLong(raw);
                    txtFooterBayar.setText(formatThousand(val));
                } catch(Exception ex){}
            }
        });

        // 5. Update Kembalian
        DocumentListener calcListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateKembalian(); }
            @Override public void removeUpdate(DocumentEvent e) { updateKembalian(); }
            @Override public void changedUpdate(DocumentEvent e) { updateKembalian(); }
        };
        txtFooterBayar.getDocument().addDocumentListener(calcListener);
        txtFooterTotal.getDocument().addDocumentListener(calcListener);

        // 6. Buttons
        btnHapus.addActionListener(e -> onHapus());
        btnSimpan.addActionListener(e -> onSimpan());
    }

    private void onTambah() {
        String nama = txtNama.getText().trim();
        String jumlahStr = txtJumlah.getText().trim();
        String hargaStr = txtHarga.getText().trim();

        if (nama.isEmpty() || jumlahStr.isEmpty() || hargaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lengkapi data barang (Nama, Harga, Jumlah)!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object selectedBarangIdObj = txtNama.getClientProperty("selectedBarangId");
        if (selectedBarangIdObj == null) {
            JOptionPane.showMessageDialog(this, "Pilih barang dari popup!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Integer idBarang = Integer.parseInt(selectedBarangIdObj.toString());
            Object selectedSupplierIdObj = txtNamasup.getClientProperty("selectedSupplierId");
            Integer idSupplier = null;
            if (selectedSupplierIdObj != null) idSupplier = Integer.parseInt(selectedSupplierIdObj.toString());

            int jumlah = Integer.parseInt(jumlahStr);
            int harga = Integer.parseInt(hargaStr.replaceAll("[^0-9]", ""));
            int subtotal = jumlah * harga;

            model.addRow(new Object[]{ idBarang, nama, jumlah, formatThousand(harga), formatThousand(subtotal) });

            DetailPembelian d = new DetailPembelian();
            d.setIdBarang(idBarang);
            d.setIdSupplier(idSupplier);
            d.setHargaBeli(harga);
            d.setStok(jumlah);
            d.setSubtotal(subtotal);
            cart.add(d);

            updateTotalUI();

            txtNama.setText("");
            txtNama.putClientProperty("selectedBarangId", null);
            txtNama.putClientProperty("selectedDetailId", null);
            txtHarga.setText("");
            txtJumlah.setText("");
            txtNama.requestFocus(); 

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Input tidak valid: " + ex.getMessage());
        }
    }

    private void onSimpan() {
        if (cart.isEmpty()) { JOptionPane.showMessageDialog(this, "Keranjang kosong!"); return; }

        String idPembelian = txtKode.getText().trim();
        if(idPembelian.isEmpty()) {
            try { idPembelian = generateNewIdPembelianLocal(); } catch(Exception ex){}
        }

        int totalHarga = cart.stream().mapToInt(DetailPembelian::getSubtotal).sum();
        String metodeBayar = String.valueOf(cmbMetodeBayar.getSelectedItem());
        int jumlahBayar = parseCurrency(txtFooterBayar.getText());

        if ("Cash".equalsIgnoreCase(metodeBayar) && jumlahBayar < totalHarga) {
            JOptionPane.showMessageDialog(this, "Uang tunai kurang!");
            return;
        }

        try {
            Pembelian p = new Pembelian();
            p.setIdPembelian(idPembelian);
            p.setTglPembelian(LocalDate.now().toString());
            p.setPaymentMethod(metodeBayar);
            p.setTotalHarga(totalHarga);
            p.setDetails(new ArrayList<>(cart));

            PembelianDAO dao = new PembelianDAO();
            dao.insertPembelianWithDetails(p);

            JOptionPane.showMessageDialog(this, "Transaksi Berhasil! ID: " + idPembelian);
            
         
            
            resetFields();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void onHapus() {
        int r = tabel.getSelectedRow();
        if(r >= 0) {
            model.removeRow(r);
            cart.remove(r);
            updateTotalUI();
        } else {
             if(JOptionPane.showConfirmDialog(this, "Reset Transaksi?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) resetFields();
        }
    }


    private void updateTotalUI() {
        int tot = cart.stream().mapToInt(DetailPembelian::getSubtotal).sum();
        String fmt = formatThousand(tot);
        txtFooterTotal.setText(fmt);
        lblBigTotal.setText(fmt);
        updateKembalian();
    }

    private void updateKembalian() {
        int total = parseCurrency(txtFooterTotal.getText());
        int bayar = parseCurrency(txtFooterBayar.getText());
        lblBigBayar.setText(formatThousand(bayar));
        
        int kembali = bayar - total;
        String kStr = (kembali > 0) ? formatThousand(kembali) : "0";
        txtFooterKembali.setText(kStr);
        lblBigKembali.setText(kStr);
    }

    private void resetFields() {
        model.setRowCount(0);
        cart.clear();
        txtNama.setText(""); txtNamasup.setText(""); txtHarga.setText(""); txtJumlah.setText("");
        txtFooterBayar.setText(""); 
        lblBigBayar.setText("0"); lblBigTotal.setText("0"); lblBigKembali.setText("0");
        updateTotalUI();
        try { txtKode.setText(generateNewIdPembelianLocal()); } catch(Exception e){}
    }

    // ================= UTILS =================
    
    private int parseCurrency(String text) {
        if(text == null || text.isEmpty()) return 0;
        try { return Integer.parseInt(text.replace(".", "").replace(",", "")); } catch(Exception e){ return 0; }
    }

    // [MODIFIKASI] Hapus 00 dibelakang koma
    private String formatThousand(long val) {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
        DecimalFormatSymbols sym = df.getDecimalFormatSymbols();
        sym.setGroupingSeparator('.');
        df.setDecimalFormatSymbols(sym);
        return df.format(val);
    }

    private String generateNewIdPembelianLocal() throws SQLException {
        String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
        String defaultId = today + "0001";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id_pembelian FROM data_pembelian WHERE id_pembelian LIKE ? ORDER BY id_pembelian DESC LIMIT 1")) {
            ps.setString(1, today + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String lastId = rs.getString(1);
                    if (lastId != null && lastId.length() >= 12) {
                        int lastNum = Integer.parseInt(lastId.substring(8));
                        return today + String.format("%04d", lastNum + 1);
                    }
                }
            }
        } catch (Exception e) {}
        return defaultId;
    }

    // ================= POPUP CLASSES =================
    
    class PilihBarangFrame extends JFrame {
        public PilihBarangFrame(JTextField targetField) {
            setTitle("Pilih Barang"); setSize(800, 500); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            JPanel p = new JPanel(new BorderLayout(10,10)); p.setBorder(new EmptyBorder(10,10,10,10)); p.setBackground(Color.WHITE);
            
            // Search
            JPanel top = new JPanel(new BorderLayout(5,5)); top.setOpaque(false);
            JTextField txtCari = new JTextField(); JButton btnCari = new JButton("Cari");
            top.add(new JLabel("Cari Barang:"), BorderLayout.WEST); top.add(txtCari, BorderLayout.CENTER); top.add(btnCari, BorderLayout.EAST);
            p.add(top, BorderLayout.NORTH);

            // Table
            DefaultTableModel m = new DefaultTableModel(new String[]{"ID", "Nama", "Harga", "Stok"}, 0){ @Override public boolean isCellEditable(int r,int c){return false;}};
            JTable t = new JTable(m); t.setRowHeight(25);
            p.add(new JScrollPane(t), BorderLayout.CENTER);
            p.add(new JLabel("Klik 2x untuk memilih", SwingConstants.CENTER), BorderLayout.SOUTH);

            // Load
            loadData(m, "");

            // Listeners
            btnCari.addActionListener(e -> loadData(m, txtCari.getText()));
            t.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if(e.getClickCount()==2 && t.getSelectedRow()!=-1) {
                        int r = t.getSelectedRow();
                        targetField.setText(m.getValueAt(r, 1).toString());
                        targetField.putClientProperty("selectedBarangId", m.getValueAt(r, 0));
                        dispose();
                    }
                }
            });

            add(p); setVisible(true);
        }

    private void loadData(DefaultTableModel m, String q) {
    m.setRowCount(0);
    String keyword = q == null ? "" : q.trim().toLowerCase();

    try {
        DetailBarangDAO detailDao = new DetailBarangDAO();
        BarangDAO barangDao = new BarangDAO();

        List<DetailBarang> detailList = detailDao.findAll();
        Set<Integer> barangSudahAdaDetail = new HashSet<>();

        for (DetailBarang d : detailList) {
            Integer idBarang = d.getIdBarang();
            String nama = d.getNamaBarang() == null ? "-" : d.getNamaBarang();
            String harga = d.getHargaJual() == null ? "-" : formatThousand(d.getHargaJual().intValue());
           String stok = d.getStok() <= 0 ? "-" : String.valueOf(d.getStok());
            String barcode = d.getBarcode() == null ? "" : d.getBarcode();

            boolean match = keyword.isEmpty()
                    || nama.toLowerCase().contains(keyword)
                    || barcode.toLowerCase().contains(keyword);

            if (match) {
                m.addRow(new Object[]{
                        idBarang,
                        nama,
                        harga,
                        stok
                });
            }

            if (idBarang != null) barangSudahAdaDetail.add(idBarang);
        }

        // 2️⃣ TAMPILKAN BARANG MASTER (YANG BELUM PERNAH DIBELI)
        for (Barang b : barangDao.findAll()) {
            Integer id = b.getId();

            // ❗ skip jika sudah punya detail
            if (id != null && barangSudahAdaDetail.contains(id)) continue;

            String nama = b.getNama() == null ? "-" : b.getNama();

            boolean match = keyword.isEmpty()
                    || nama.toLowerCase().contains(keyword);

            if (match) {
                m.addRow(new Object[]{
                        id,
                        nama,
                        "-",   // ❗ BELUM PERNAH DIBELI → tidak punya harga
                        "-"    // ❗ BELUM PERNAH DIBELI → tidak punya stok
                });
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    }

    class PilihSupplierFrame extends JFrame {
        public PilihSupplierFrame(JTextField targetField) {
            setTitle("Pilih Supplier"); setSize(600, 400); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            JPanel p = new JPanel(new BorderLayout(10,10)); p.setBorder(new EmptyBorder(10,10,10,10)); p.setBackground(Color.WHITE);
            
            JPanel top = new JPanel(new BorderLayout(5,5)); top.setOpaque(false);
            JTextField txtCari = new JTextField(); JButton btnCari = new JButton("Cari");
            top.add(new JLabel("Cari Supplier:"), BorderLayout.WEST); top.add(txtCari, BorderLayout.CENTER); top.add(btnCari, BorderLayout.EAST);
            p.add(top, BorderLayout.NORTH);

            DefaultTableModel m = new DefaultTableModel(new String[]{"ID", "Nama", "Telp"}, 0){ @Override public boolean isCellEditable(int r,int c){return false;}};
            JTable t = new JTable(m); t.setRowHeight(25);
            p.add(new JScrollPane(t), BorderLayout.CENTER);
            p.add(new JLabel("Klik 2x untuk memilih", SwingConstants.CENTER), BorderLayout.SOUTH);

            loadData(m, "");

            btnCari.addActionListener(e -> loadData(m, txtCari.getText()));
            t.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if(e.getClickCount()==2 && t.getSelectedRow()!=-1) {
                        int r = t.getSelectedRow();
                        targetField.setText(m.getValueAt(r, 1).toString());
                        targetField.putClientProperty("selectedSupplierId", m.getValueAt(r, 0));
                        dispose();
                    }
                }
            });
            add(p); setVisible(true);
        }

        private void loadData(DefaultTableModel m, String q) {
            m.setRowCount(0);
            String sql = "SELECT * FROM data_supplier WHERE nama_supplier LIKE ?";
            try(Connection c = DatabaseHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, "%"+q+"%");
                ResultSet rs = ps.executeQuery();
                while(rs.next()) m.addRow(new Object[]{rs.getInt("id_supplier"), rs.getString("nama_supplier"), rs.getString("notelp_supplier")});
            } catch(Exception e){}
        }
    }
}