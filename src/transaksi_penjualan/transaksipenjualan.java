package transaksi_penjualan;

import barang.DetailBarang;
import barang.DetailBarangDAO;
import Helper.DatabaseHelper;
import voucher.Voucher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pengguna.Pengguna;
import uiresponsive.UIResponsive;

// Jasper imports
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JRException;

public class transaksipenjualan extends JPanel {

    // ==== DAO / helper ====
    private final DetailBarangDAO detailDao = new DetailBarangDAO();
    private final TransactionDAO txDao = new TransactionDAO();
    private Pengguna user;

    // ==== Komponen UI ====
    private JTextField txtKode, txtTanggal, txtBarcode, txtNama, txtJumlah;

    // Dashboard Besar (JLabel Angka)
    private JLabel lblBigTotal, lblBigBayar, lblBigKembali;

    // Tabel
    private DefaultTableModel model;
    private JTable tabel;

    // Footer Inputs
    private JTextField txtVoucher;
    private JTextField txtFooterTotal, txtFooterBayar, txtFooterKembali;
    private JComboBox<String> cmbMetodeBayar;

    // Tombol & Checkbox
    private JButton btnCetak, btnSimpan, btnHapus;
    private JCheckBox chkLangsungCetak; // [BARU] Checkbox

    // Logic Variables
    private JTextField txtHarga = new JTextField();
    private DetailBarang selectedDetail = null;
    private Voucher selectedVoucher = null;

    // ==== PALETTE WARNA ====
    private final Color BG_MAIN = new Color(242, 245, 255);
    private final Color BOX_RED = new Color(255, 215, 215);
    private final Color BOX_YELLOW = new Color(255, 253, 208);
    private final Color BOX_GREEN = new Color(210, 255, 210);
    private final Color BTN_RED = new Color(220, 53, 69);
    private final Color BTN_GREEN = new Color(40, 167, 69);
    private final Color BTN_PURPLE = new Color(111, 66, 193);

    public transaksipenjualan() {
        this.user = UIResponsive.currentUser;
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

        String namaKasir = (user != null) ? user.getNamaLengkap() : "Kasir";
        String idKasir = (user != null) ? user.getIdPengguna() : "0";
        JLabel lblTitle = new JLabel("Kasir: " + namaKasir);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitle.setForeground(new Color(50, 50, 70));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        topLeftContainer.add(lblTitle);
        topLeftContainer.add(Box.createVerticalStrut(20));

        // Row 1: Kode & Tanggal
        JPanel row1 = new JPanel(new GridLayout(1, 2, 20, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(1200, 70));

        txtKode = createModernField(row1, "Kode Transaksi");
        txtTanggal = createModernField(row1, "Tanggal");
        txtTanggal.setText(LocalDate.now().toString());
        txtKode.setEditable(false);
        txtTanggal.setEditable(false);

        topLeftContainer.add(row1);
        topLeftContainer.add(Box.createVerticalStrut(15));

        // Row 2: Barcode, Nama, Jumlah
        JPanel row2 = new JPanel(new GridBagLayout());
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(1200, 70));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 20);

        gbc.weightx = 0.25;
        gbc.gridx = 0;
        txtBarcode = createModernFieldGrid(row2, "Barcode", gbc);

        gbc.weightx = 0.55;
        gbc.gridx = 1;
        txtNama = createModernFieldGrid(row2, "Nama Barang", gbc);
        txtNama.setEditable(false);
        txtNama.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc.weightx = 0.2;
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        txtJumlah = createModernFieldGrid(row2, "Jumlah", gbc);

        topLeftContainer.add(row2);

        // --- Right Side (Dashboard Kotak) ---
        JPanel topRightDashboard = new JPanel(new GridLayout(3, 1, 0, 10));
        topRightDashboard.setOpaque(false);
        topRightDashboard.setPreferredSize(new Dimension(320, 180));

        lblBigTotal = new JLabel("0", SwingConstants.RIGHT);
        lblBigBayar = new JLabel("0", SwingConstants.RIGHT);
        lblBigKembali = new JLabel("0", SwingConstants.RIGHT);

        topRightDashboard.add(createDashboardBox("Total", lblBigTotal, BOX_RED));
        topRightDashboard.add(createDashboardBox("Bayar", lblBigBayar, BOX_YELLOW));
        topRightDashboard.add(createDashboardBox("Kembalian", lblBigKembali, BOX_GREEN));

        topPanel.add(topLeftContainer, BorderLayout.CENTER);
        topPanel.add(topRightDashboard, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // ================= CENTER SECTION (TABLE) =================
        String[] columns = {"ID", "Kode Barang", "Nama Barang", "Harga", "Jumlah", "Sub Total"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tabel = new JTable(model);
        styleModernTable();

        JScrollPane scrollPane = new JScrollPane(tabel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(scrollPane);
        tablePanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        tablePanel.setOpaque(false);

        add(tablePanel, BorderLayout.CENTER);

        // ================= BOTTOM SECTION =================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // -- Bottom Left (Inputs) --
        JPanel bottomLeft = new JPanel(new GridLayout(3, 1, 5, 10));
        bottomLeft.setOpaque(false);
        bottomLeft.setPreferredSize(new Dimension(380, 150));

        // ROW 1: TOTAL & VOUCHER
        JPanel totalVoucherContainer = new JPanel(new GridLayout(1, 2, 10, 0));
        totalVoucherContainer.setOpaque(false);

        txtFooterTotal = createFooterField();
        txtFooterTotal.setEditable(false);

        txtVoucher = createFooterField();
        txtVoucher.setEditable(false);
        txtVoucher.setCursor(new Cursor(Cursor.HAND_CURSOR));
        txtVoucher.setText("- Voucher -");
        txtVoucher.setHorizontalAlignment(SwingConstants.CENTER);
        txtVoucher.setForeground(Color.GRAY);
        txtVoucher.setBackground(Color.WHITE);

        totalVoucherContainer.add(txtFooterTotal);
        totalVoucherContainer.add(txtVoucher);
        bottomLeft.add(createFooterRow("Total", totalVoucherContainer));

        // ROW 2: BAYAR & METODE
        JPanel bayarContainer = new JPanel(new GridLayout(1, 2, 10, 0));
        bayarContainer.setOpaque(false);

        txtFooterBayar = createFooterField();
        txtFooterBayar.setBackground(Color.WHITE);

        txtFooterBayar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onBayar();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) return;
                try {
                    String raw = txtFooterBayar.getText().replace(".", "").replace(",", "");
                    if (raw.isEmpty()) return;
                    long val = Long.parseLong(raw);
                    txtFooterBayar.setText(formatCurrency(new BigDecimal(val)));
                } catch (NumberFormatException ex) {
                }
            }
        });

        String[] metode = {"Cash", "Kredit", "Donasi"};
        cmbMetodeBayar = new JComboBox<>(metode);
        cmbMetodeBayar.setFont(new Font("SansSerif", Font.PLAIN, 14));

        bayarContainer.add(txtFooterBayar);
        bayarContainer.add(cmbMetodeBayar);
        bottomLeft.add(createFooterRow("Bayar", bayarContainer));

        // ROW 3: KEMBALIAN
        txtFooterKembali = createFooterField();
        txtFooterKembali.setEditable(false);
        bottomLeft.add(createFooterRow("Kembalian", txtFooterKembali));

        // -- Bottom Right (Buttons + Checkbox) --
        // Gunakan BoxLayout Y_AXIS agar Checkbox berada di bawah Tombol
        JPanel bottomRight = new JPanel();
        bottomRight.setLayout(new BoxLayout(bottomRight, BoxLayout.Y_AXIS));
        bottomRight.setOpaque(false);

        // Panel Tombol (FlowLayout Right)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.RIGHT_ALIGNMENT); // Rata kanan dalam Box

        btnCetak = createModernButton("Cetak", BTN_PURPLE);
        btnSimpan = createModernButton("Simpan", BTN_GREEN);
        btnHapus = createModernButton("Hapus", BTN_RED);

        btnPanel.add(btnCetak);
        btnPanel.add(btnSimpan);
        btnPanel.add(btnHapus);

        // Panel Checkbox (FlowLayout Right agar rata kanan di bawah tombol "Cetak")
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        checkPanel.setOpaque(false);
        checkPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        chkLangsungCetak = new JCheckBox("Langsung Cetak Nota?");
        chkLangsungCetak.setFont(new Font("SansSerif", Font.PLAIN, 13));
        chkLangsungCetak.setOpaque(false);
        chkLangsungCetak.setCursor(new Cursor(Cursor.HAND_CURSOR));

        checkPanel.add(chkLangsungCetak);

        // Gabungkan
        bottomRight.add(btnPanel);
        bottomRight.add(checkPanel);

        bottomPanel.add(bottomLeft, BorderLayout.WEST);
        bottomPanel.add(bottomRight, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            TableColumnModel cm = tabel.getColumnModel();
            if (cm.getColumnCount() > 0) {
                cm.getColumn(0).setMinWidth(0);
                cm.getColumn(0).setMaxWidth(0);
                cm.getColumn(0).setPreferredWidth(0);
            }
        });
    }

    // ================= CUSTOM COMPONENT CREATION =================

    private JTextField createModernField(JPanel parent, String labelText) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel l = new JLabel(labelText);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(new Color(80, 80, 80));
        JTextField f = new RoundedTextField(15);
        f.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        f.setFont(new Font("SansSerif", Font.PLAIN, 15));
        f.setPreferredSize(new Dimension(100, 45));
        p.add(l, BorderLayout.NORTH);
        p.add(f, BorderLayout.CENTER);
        parent.add(p);
        return f;
    }

    private JTextField createModernFieldGrid(JPanel parent, String labelText, GridBagConstraints gbc) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel l = new JLabel(labelText);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(new Color(80, 80, 80));
        JTextField f = new RoundedTextField(15);
        f.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        f.setFont(new Font("SansSerif", Font.PLAIN, 15));
        f.setPreferredSize(new Dimension(100, 45));
        p.add(l, BorderLayout.NORTH);
        p.add(f, BorderLayout.CENTER);
        parent.add(p, gbc);
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
                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(5, 15, 5, 15));
        JLabel tLabel = new JLabel(title);
        tLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        tLabel.setForeground(new Color(60, 60, 60));
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
        f.setBorder(BorderFactory.createCompoundBorder(f.getBorder(), BorderFactory.createEmptyBorder(0, 10, 0, 10)));
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
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
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
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(new Color(180, 180, 180));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    // ================= LOGIC METHODS =================
    private void initLogicListeners() {
        txtNama.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPilihBarangFrame();
            }
        });
        txtVoucher.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPilihVoucherFrame();
            }
        });
        KeyAdapter enterToAdd = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onTambah();
            }
        };
        txtJumlah.addKeyListener(enterToAdd);
        txtBarcode.addKeyListener(enterToAdd);
        txtFooterBayar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                recalcKembalian();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                recalcKembalian();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                recalcKembalian();
            }
        });
        btnHapus.addActionListener(e -> onHapus());
        btnSimpan.addActionListener(e -> onBayar());
        btnCetak.addActionListener(e -> onCetak());
        tabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = tabel.getSelectedRow();
                    if (r >= 0) editCartRowQty(tabel.convertRowIndexToModel(r));
                }
            }
        });
    }

    private void onTambah() {
        try {
            if (selectedDetail == null) {
                String code = txtBarcode.getText().trim();
                if (!code.isEmpty()) {
                    List<DetailBarang> list = detailDao.findAll();
                    for (DetailBarang d : list) {
                        if (d.getBarcode() != null && d.getBarcode().equals(code)) {
                            selectedDetail = d;
                            txtNama.setText(d.getNamaBarang());
                            txtHarga.setText(d.getHargaJual().toPlainString());
                            break;
                        }
                    }
                }
            }
            if (selectedDetail == null) {
                JOptionPane.showMessageDialog(this, "Barang tidak ditemukan / belum dipilih.");
                return;
            }
            int qty;
            try {
                qty = Integer.parseInt(txtJumlah.getText().trim());
            } catch (NumberFormatException e) {
                qty = 1;
            }
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Qty harus > 0");
                return;
            }
            if (qty > selectedDetail.getStok()) {
                JOptionPane.showMessageDialog(this, "Stok kurang (Sisa: " + selectedDetail.getStok() + ")");
                return;
            }
            BigDecimal harga = selectedDetail.getHargaJual();
            int existingRow = -1;
            int currentQty = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                int id = Integer.parseInt(model.getValueAt(i, 0).toString());
                if (id == selectedDetail.getId()) {
                    existingRow = i;
                    currentQty = Integer.parseInt(model.getValueAt(i, 4).toString());
                    break;
                }
            }
            if (existingRow != -1) {
                int newQty = currentQty + qty;
                if (newQty > selectedDetail.getStok()) {
                    JOptionPane.showMessageDialog(this, "Total Qty melebihi stok.");
                    return;
                }
                model.setValueAt(newQty, existingRow, 4);
                model.setValueAt(formatCurrency(harga.multiply(new BigDecimal(newQty))), existingRow, 5);
            } else {
                model.addRow(new Object[]{selectedDetail.getId(), selectedDetail.getIdBarang(), selectedDetail.getNamaBarang(), formatCurrency(harga), qty, formatCurrency(harga.multiply(new BigDecimal(qty)))});
            }
            updateTotal();
            selectedDetail = null;
            txtBarcode.setText("");
            txtNama.setText("");
            txtJumlah.setText("");
            txtHarga.setText("");
            txtBarcode.requestFocus();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < model.getRowCount(); i++)
            total = total.add(parseCurrency(model.getValueAt(i, 5).toString()));
        if (selectedVoucher != null && selectedVoucher.getCurrentBalance() != null) {
            /* Logic Diskon */
        }
        String fmt = formatCurrency(total);
        txtFooterTotal.setText(fmt);
        lblBigTotal.setText(fmt);
        recalcKembalian();
    }

    private void recalcKembalian() {
        try {
            BigDecimal total = parseCurrency(txtFooterTotal.getText());
            String bayarStr = txtFooterBayar.getText().trim().replace(".", "").replace(",", "");
            BigDecimal bayar = bayarStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(bayarStr);
            lblBigBayar.setText(formatCurrency(bayar));
            BigDecimal kembali = bayar.subtract(total);
            String kmbStr = (kembali.signum() < 0) ? "0" : formatCurrency(kembali);
            txtFooterKembali.setText(kmbStr);
            lblBigKembali.setText(kmbStr);
        } catch (Exception e) {
        }
    }

   // ====== Ganti method onBayar() dengan ini ======
private void onBayar() {
    if (model.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "Tidak ada item untuk dibayar.");
        return;
    }

    try {
        // Kumpulkan item dari tabel
        List<SaleItem> items = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            int id = Integer.parseInt(model.getValueAt(i, 0).toString());
            int qty = Integer.parseInt(model.getValueAt(i, 4).toString());
            BigDecimal price = parseCurrency(model.getValueAt(i, 3).toString());
            items.add(new SaleItem(id, qty, price));
        }

        BigDecimal total = parseCurrency(txtFooterTotal.getText());
        BigDecimal bayar = parseCurrency(txtFooterBayar.getText());

        // Metode pembayaran -> normalisasi
        String metodeUi = (cmbMetodeBayar.getSelectedItem() != null) ? cmbMetodeBayar.getSelectedItem().toString() : "Cash";
        String paymentMethodForDb;
        switch (metodeUi.toLowerCase()) {
            case "cash": paymentMethodForDb = "CASH"; break;
            case "kredit": paymentMethodForDb = "CREDIT"; break;
            case "donasi": paymentMethodForDb = "DONASI"; break;
            default: paymentMethodForDb = metodeUi.toUpperCase().replaceAll("\\s+","_"); break;
        }

        // Validasi bayar untuk CASH
        if ("CASH".equals(paymentMethodForDb) && bayar.compareTo(total) < 0) {
            JOptionPane.showMessageDialog(this, "Uang kurang!");
            return;
        }

        Integer voucherId = (selectedVoucher != null) ? selectedVoucher.getIdVoucher() : null;
        String idPengguna = (this.user != null) ? this.user.getIdPengguna() : null;

        // CATAT: kode yang akan disimpan, jika user sudah mengisi txtKode gunakan itu
        String kodeCandidate = txtKode.getText();
        if (kodeCandidate == null) kodeCandidate = "";

        // Simpan transaksi melalui DAO.
        // Pastikan TransactionDAO.processSale menangani penyimpanan (insert transaksi + detail + update stok).
        // Kita mengirim kodeCandidate (boleh kosong). Jika DAO membuat kode sendiri, kita akan ambil dari DB setelahnya.
        txDao.processSale(items, voucherId, bayar, kodeCandidate, idPengguna, paymentMethodForDb);

        // Ambil kode yang sebenarnya tersimpan:
        // 1) jika kita memang mengirim kodeCandidate dan itu tidak kosong -> anggap itu kode tersimpan
        // 2) jika kosong -> query DB untuk kode terakhir (fallback)
        String kodeTersimpan = (kodeCandidate != null && !kodeCandidate.trim().isEmpty())
                ? kodeCandidate
                : fetchLastSavedKode();

        if (kodeTersimpan == null || kodeTersimpan.trim().isEmpty()) {
            // masih kosong -> beri warning tapi transaksi sudah tersimpan (tidak bisa rollback di sini)
            JOptionPane.showMessageDialog(this, "Transaksi tersimpan, tetapi kode transaksi tidak ditemukan untuk dicetak.");
        } else {
            JOptionPane.showMessageDialog(this, "Transaksi Berhasil! (Kode: " + kodeTersimpan + ")");
            // Cetak otomatis (selalu panggil cetak setelah simpan, sesuai permintaan)
            try {
                printTransaction(kodeTersimpan);
            } catch (Exception printEx) {
                // Jangan rollback transaksi; tampilkan pesan saja
                JOptionPane.showMessageDialog(this, "Transaksi tersimpan, tapi gagal mencetak: " + printEx.getMessage());
                printEx.printStackTrace();
            }
        }

        // Setelah semua (cetak atau gagal cetak), bersihkan UI -> siapkan transaksi baru
        resetFields();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi: " + e.getMessage());
        e.printStackTrace();
    }
}


    private void onHapus() {
        int r = tabel.getSelectedRow();
        if (r >= 0) {
            model.removeRow(r);
            updateTotal();
        } else {
            if (JOptionPane.showConfirmDialog(this, "Reset Transaksi?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                resetFields();
        }
    }

   // ====== Ganti method onCetak() dengan versi yang menerima optional kode (atau gunakan printTransaction) ======
private void onCetak() {
    // Jika tombol Cetak dipencet manual, gunakan kode di txtKode (jika ada) atau ambil yang terakhir
    try {
        String kode = txtKode.getText();
        if (kode == null || kode.trim().isEmpty()) {
            kode = fetchLastSavedKode();
        }
        if (kode == null || kode.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada transaksi untuk dicetak.");
            return;
        }
        // Panggil helper cetak
        printTransaction(kode);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal mencetak: " + e.getMessage());
        e.printStackTrace();
    }
}
 
private String fetchLastSavedKode() {
    String kode = null;
    try (Connection conn = DatabaseHelper.getConnection();
         PreparedStatement ps = conn.prepareStatement("SELECT kode_transaksi FROM transaksi_penjualan ORDER BY id_transaksi DESC LIMIT 1");
         ResultSet rs = ps.executeQuery()) {
        if (rs.next()) kode = rs.getString(1);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return kode;
}

/**
 * Cetak transaksi berdasarkan kode yang diberikan.
 * Memakai testPrintReportFromJdbc (yang sudah ada di kelas) sehingga
 * akan mencari .jrxml/.jasper di resources/report/.
 */
private void printTransaction(String kodeTransaksi) throws Exception {
    if (kodeTransaksi == null || kodeTransaksi.trim().isEmpty()) {
        throw new IllegalArgumentException("kodeTransaksi kosong.");
    }
    // pakai helper yang sudah dibuat (testPrintReportFromJdbc)
    testPrintReportFromJdbc(kodeTransaksi);
}

    /**
     * Helper: coba cari JRXML dulu, kalau ada compile dan isi pakai JDBC.
     * Jika tidak ada, coba .jasper terkompilasi. Mencari beberapa nama (nota_minimarket / reportPenjualan).
     */
    private void testPrintReportFromJdbc(String kodeTransaksi) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                throw new RuntimeException("Connection returned null! Periksa DatabaseHelper.getConnection()");
            }

            // coba beberapa path yang umum
            String[] jrxmlPaths = {"/report/reportPenjualan.jrxml", "/report/reportPenjualan.jrxml", "/report/reportPenjualan.jrxml"};
            String[] jasperPaths = {"/report/reportPenjualan.jasper", "/report/reportPenjualan.jasper", "/report/reportPenjualan.jasper"};

            InputStream jrxmlStream = null;
            for (String p : jrxmlPaths) {
                jrxmlStream = getClass().getResourceAsStream(p);
                if (jrxmlStream != null) break;
            }

            Map<String, Object> params = new HashMap<>();
            // gunakan nama parameter sesuai JRXML yang kamu kirim sebelumnya: "kode_transaksi"
            params.put("kode_transaksi", kodeTransaksi);

            if (jrxmlStream != null) {
                try (InputStream is = jrxmlStream) {
                    JasperReport jasperReport = JasperCompileManager.compileReport(is);
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                    JasperViewer.viewReport(jasperPrint, false);
                    if (chkLangsungCetak.isSelected()) {
                        JasperPrintManager.printReport(jasperPrint, true);
                    }
                    return;
                }
            }

            InputStream jasperStream = null;
            for (String p : jasperPaths) {
                jasperStream = getClass().getResourceAsStream(p);
                if (jasperStream != null) break;
            }

            if (jasperStream != null) {
                try (InputStream is = jasperStream) {
                    JasperPrint jasperPrint = JasperFillManager.fillReport(is, params, conn);
                    JasperViewer.viewReport(jasperPrint, false);
                    if (chkLangsungCetak.isSelected()) {
                        JasperPrintManager.printReport(jasperPrint, true);
                    }
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "File report (.jrxml / .jasper) tidak ditemukan di resources/report/. Pastikan nama file dan lokasi benar.");
        } catch (JRException jre) {
            jre.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error Jasper: " + jre.getMessage() + "\nLihat console untuk detail.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal generate report: " + ex.getMessage() + "\nLihat console untuk stacktrace.");
        } finally {
            try {
                if (conn != null && !conn.isClosed()) conn.close();
            } catch (Exception e) {
            }
        }
    }

    private void resetFields() {
        selectedDetail = null;
        selectedVoucher = null;
        txtBarcode.setText("");
        txtNama.setText("");
        txtJumlah.setText("");
        txtVoucher.setText("- Voucher -");
        txtVoucher.setForeground(Color.GRAY);
        model.setRowCount(0);
        updateTotal();
        txtFooterBayar.setText("");
        lblBigBayar.setText("0");
        lblBigTotal.setText("0");
        lblBigKembali.setText("0");
        txtKode.setText(generateTransactionCode());
        txtTanggal.setText(LocalDate.now().toString());
    }

    public void requestFokusKeBarcode() {
        SwingUtilities.invokeLater(() -> {
            if (txtBarcode != null) txtBarcode.requestFocusInWindow();
        });
    }

    private String generateTransactionCode() {
        String prefix = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        int next = 1;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT kode_transaksi FROM transaksi_penjualan WHERE kode_transaksi LIKE ? ORDER BY kode_transaksi DESC LIMIT 1")) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String last = rs.getString(1);
                    if (last != null && last.length() > prefix.length())
                        try {
                            next = Integer.parseInt(last.substring(prefix.length())) + 1;
                        } catch (NumberFormatException ignored) {
                        }
                }
            }
        } catch (Exception ex) {
        }
        return prefix + String.format("%04d", next);
    }

    private void openPilihBarangFrame() {
        try {
            List<DetailBarang> list = detailDao.findAll();
            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Pilih Barang", Dialog.ModalityType.APPLICATION_MODAL);
            dlg.setSize(600, 400);
            dlg.setLocationRelativeTo(this);
            String[] col = {"ID", "Barcode", "Nama", "Stok", "Harga"};
            DefaultTableModel m = new DefaultTableModel(col, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            for (DetailBarang d : list) m.addRow(new Object[]{d.getId(), d.getBarcode(), d.getNamaBarang(), d.getStok(), d.getHargaJual()});
            JTable t = new JTable(m);
            t.setRowHeight(25);
            t.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int r = t.getSelectedRow();
                        if (r >= 0) {
                            int id = (int) t.getValueAt(r, 0);
                            for (DetailBarang db : list) if (db.getId() == id) selectedDetail = db;
                            txtBarcode.setText(selectedDetail.getBarcode());
                            txtNama.setText(selectedDetail.getNamaBarang());
                            txtHarga.setText(selectedDetail.getHargaJual().toPlainString());
                            txtJumlah.requestFocus();
                            dlg.dispose();
                        }
                    }
                }
            });
            JPanel p = new JPanel(new BorderLayout());
            p.add(new JScrollPane(t), BorderLayout.CENTER);
            JLabel info = new JLabel("Klik 2x pada barang untuk memilih");
            info.setHorizontalAlignment(SwingConstants.CENTER);
            info.setBorder(new EmptyBorder(5, 0, 5, 0));
            p.add(info, BorderLayout.SOUTH);
            dlg.add(p);
            dlg.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openPilihVoucherFrame() {
        try {
            List<Voucher> list = DatabaseHelper.getAllVouchers();
            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Pilih Voucher", Dialog.ModalityType.APPLICATION_MODAL);
            dlg.setSize(500, 350);
            dlg.setLocationRelativeTo(this);
            String[] col = {"Kode", "Nominal"};
            DefaultTableModel m = new DefaultTableModel(col, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            if (list != null)
                for (Voucher v : list) m.addRow(new Object[]{v.getKode(), formatCurrency(v.getCurrentBalance())});
            JTable t = new JTable(m);
            t.setRowHeight(25);
            t.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && t.getSelectedRow() != -1) {
                        int r = t.getSelectedRow();
                        String kode = t.getValueAt(r, 0).toString();
                        for (Voucher v : list) if (v.getKode().equals(kode)) selectedVoucher = v;
                        txtVoucher.setText(selectedVoucher.getKode());
                        txtVoucher.setForeground(Color.BLACK);
                        updateTotal();
                        dlg.dispose();
                    }
                }
            });
            JPanel p = new JPanel(new BorderLayout());
            p.add(new JScrollPane(t), BorderLayout.CENTER);
            JLabel info = new JLabel("Klik 2x pilih, Klik kanan hapus");
            info.setHorizontalAlignment(SwingConstants.CENTER);
            t.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        selectedVoucher = null;
                        txtVoucher.setText("- Voucher -");
                        txtVoucher.setForeground(Color.GRAY);
                        updateTotal();
                        dlg.dispose();
                    }
                }
            });
            p.add(info, BorderLayout.SOUTH);
            dlg.add(p);
            dlg.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void editCartRowQty(int modelRow) {
        String s = JOptionPane.showInputDialog(this, "Ubah Jumlah:");
        if (s != null && !s.isEmpty()) {
            try {
                int q = Integer.parseInt(s);
                model.setValueAt(q, modelRow, 4);
                BigDecimal h = parseCurrency(model.getValueAt(modelRow, 3).toString());
                model.setValueAt(formatCurrency(h.multiply(new BigDecimal(q))), modelRow, 5);
                updateTotal();
            } catch (Exception e) {
            }
        }
    }

    private String formatCurrency(BigDecimal val) {
        if (val == null) return "0";
        DecimalFormat formatter = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setCurrencySymbol("");
        symbols.setMonetaryDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        formatter.setDecimalFormatSymbols(symbols);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(val).trim();
    }

    private BigDecimal parseCurrency(String val) {
        try {
            if (val == null || val.isEmpty()) return BigDecimal.ZERO;
            return new BigDecimal(val.replace(".", "").replace(",", "."));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}