package transaksi_penjualan;

import barang.DetailBarang;
import barang.DetailBarangDAO;
import Helper.DatabaseHelper;
import voucher.Voucher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * TransactionDialog - behavior preserved (sama seperti kode asli)
 * UI diimplementasikan mengikuti style transaksipenjualan.java (rounded fields, kanan keranjang, kiri input)
 *
 * Pastikan kelas DetailBarang, DetailBarangDAO, TransactionDAO, Voucher, DatabaseHelper, SaleItem ada di project.
 */
public class TransactionDialog extends JDialog {
    private final String currentUserId;
    private final String currentUserName;

    // Top
    private JTextField txtKodeTransaksi;
    private JLabel lblKasir;
    private JLabel lblTanggal;

    // Left form (names preserved to keep logic same)
    private JTextField txtBarcode = createRoundedField();
    private JTextField txtNamaBarang = createRoundedField();
    private JButton bPilihBarang = createButton("Pilih", new Color(255, 140, 0), 90, 46);
    private JTextField txtHarga = createRoundedField();
    private JTextField txtJumlah = createRoundedField();
    private JTextField txtSubtotal = createRoundedField();
    private JButton bTambah = createButton("Tambah", new Color(0, 180, 0), 120, 32);

    // Cart (right)
    private DefaultTableModel cartModel;
    private JTable tblCart;

    // Controls
    private JButton bReset = createButton("Reset", new Color(255, 0, 0), 120, 45);
    private JButton bHapus = createButton("Hapus", new Color(218, 165, 32), 120, 45);
    private JLabel lblTotalValue = new JLabel("0");
    private JTextField txtUangBayar = createRoundedField();
    private JLabel lblKembalian = new JLabel("0");
    private JButton bBayar = createButton("Bayar", new Color(0, 200, 0), 120, 32);
    private JButton bCetak = createButton("Cetak", new Color(0, 120, 215), 120, 32);

    // Voucher
    private JComboBox<String> cbVoucher;
    private Voucher[] vouchers = new Voucher[0];

    // DAOs / helpers
    private DetailBarangDAO detailDao = new DetailBarangDAO();
    private TransactionDAO txDao = new TransactionDAO();

    // Selected detail from picker
    private DetailBarang selectedDetail = null;

    public TransactionDialog(Frame parent, String currentUserId, String currentUserName) {
        super(parent, true);
        this.currentUserId = currentUserId;
        this.currentUserName = currentUserName;

        setTitle("Transaksi");
        initUI();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setPreferredSize(new Dimension(980, 640));
        setBackground(new Color(236, 236, 236));

        // HEADER: tanggal (kanan)
        JLabel tanggal = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), SwingConstants.RIGHT);
        tanggal.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 20, 8, 20));
        header.add(tanggal, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // CENTER: content dua kolom
        JPanel content = new JPanel(new GridLayout(1, 2, 40, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(6, 18, 18, 18));

        // LEFT panel (input) - menggunakan null layout untuk meniru desain transaksipenjualan
        JPanel leftPanel = new JPanel(null);
        leftPanel.setOpaque(false);
        int x = 0, y = 0, fieldW = 440, fieldH = 38, gapY = 70;

        JLabel lblKode = makeLabel("Kode Transaksi:", x, y);
        txtKodeTransaksi = makeField(x, y + 22, fieldW, fieldH);
        txtKodeTransaksi.setEditable(false);
        txtKodeTransaksi.setText(generateTransactionCode());
        leftPanel.add(lblKode);
        leftPanel.add(txtKodeTransaksi);

        y += gapY;
        JLabel lblNama = makeLabel("Nama Barang:", x, y);
        txtNamaBarang.setBounds(x, y + 22, fieldW - 90, fieldH);
        bPilihBarang.setBounds(x + fieldW - 80, y + 22, 90, 46);
        leftPanel.add(lblNama);
        leftPanel.add(txtNamaBarang);
        leftPanel.add(bPilihBarang);

        y += gapY;
        JLabel lblHarga = makeLabel("Harga/unit:", x, y);
        txtHarga.setBounds(x, y + 22, fieldW, fieldH);
        leftPanel.add(lblHarga);
        leftPanel.add(txtHarga);

        y += gapY;
        JLabel lblJumlah = makeLabel("Jumlah:", x, y);
        txtJumlah.setBounds(x, y + 22, 120, fieldH);
        leftPanel.add(lblJumlah);
        leftPanel.add(txtJumlah);

        y += gapY;
        JLabel lblSubtotal = makeLabel("Subtotal:", x, y);
        txtSubtotal.setBounds(x, y + 22, 200, fieldH);
        txtSubtotal.setEditable(false);
        leftPanel.add(lblSubtotal);
        leftPanel.add(txtSubtotal);

        // tombol Tambah
        bTambah.setBounds(x, y + gapY + 12, fieldW + 10, 48);
        leftPanel.add(bTambah);

        // RIGHT panel (cart & payment)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        // Cart table (with same columns as kode asli)
        cartModel = new DefaultTableModel(new Object[]{"ID Detail","Kode Barang","Nama Barang","Harga","Jumlah","Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        tblCart = new JTable(cartModel);
        tblCart.setFillsViewportHeight(true);
        tblCart.setRowHeight(26);
        tblCart.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        JScrollPane spCart = new JScrollPane(tblCart);
        spCart.setPreferredSize(new Dimension(520, 320));
        spCart.setBorder(BorderFactory.createLineBorder(new Color(210,210,210), 2, true));
        rightPanel.add(spCart);

        // tombol bawah (Reset, Hapus)
        JPanel tombolPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        tombolPanel.setOpaque(false);
        tombolPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        tombolPanel.add(bReset);
        tombolPanel.add(bHapus);
        rightPanel.add(tombolPanel);

        // voucher + total + bayar grid
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 1;
        grid.add(new JLabel("Voucher:"), g);
        g.gridx = 1; g.gridy = 0;
        cbVoucher = new JComboBox<>();
        loadVouchers();
        cbVoucher.setPreferredSize(new Dimension(300, 36));
        grid.add(cbVoucher, g);

        g.gridx = 0; g.gridy = 1;
        grid.add(new JLabel("Harga Total:"), g);
        g.gridx = 1; g.gridy = 1;
        lblTotalValue.setOpaque(true);
        lblTotalValue.setBackground(Color.LIGHT_GRAY);
        lblTotalValue.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
        grid.add(lblTotalValue, g);

        g.gridx = 0; g.gridy = 2;
        grid.add(new JLabel("Uang Dibayar:"), g);
        g.gridx = 1; g.gridy = 2;
        txtUangBayar.setText("0");
        txtUangBayar.setPreferredSize(new Dimension(180, 36));
        JPanel bayarRow = new JPanel(new BorderLayout(6,0));
        bayarRow.setOpaque(false);
        bayarRow.add(txtUangBayar, BorderLayout.CENTER);
        bayarRow.add(bBayar, BorderLayout.EAST);
        grid.add(bayarRow, g);

        g.gridx = 0; g.gridy = 3;
        grid.add(new JLabel("Kembalian:"), g);
        g.gridx = 1; g.gridy = 3;
        lblKembalian.setOpaque(true);
        lblKembalian.setBackground(Color.LIGHT_GRAY);
        lblKembalian.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
        grid.add(lblKembalian, g);

        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        JPanel cetakRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cetakRow.setOpaque(false);
        bCetak.setPreferredSize(new Dimension(140, 36));
        cetakRow.add(bCetak);
        grid.add(cetakRow, g);

        rightPanel.add(grid);

        // tombol Selesai di bawah
        JButton btnSelesai = createButton("Selesaikan Transaksi", new Color(0, 200, 0), 520, 52);
        btnSelesai.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel selesaiPanel = new JPanel();
        selesaiPanel.setOpaque(false);
        selesaiPanel.add(btnSelesai);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(selesaiPanel);

        // tambahkan kiri & kanan
        content.add(leftPanel);
        content.add(rightPanel);
        add(content, BorderLayout.CENTER);

        // hide ID detail column visually
        SwingUtilities.invokeLater(() -> {
            TableColumnModel cm = tblCart.getColumnModel();
            if (cm.getColumnCount() > 0) {
                cm.getColumn(0).setMinWidth(0);
                cm.getColumn(0).setMaxWidth(0);
                cm.getColumn(0).setPreferredWidth(0);
            }
            // set other preferred widths
            try {
                cm.getColumn(1).setPreferredWidth(80);
                cm.getColumn(2).setPreferredWidth(240);
                cm.getColumn(3).setPreferredWidth(90);
                cm.getColumn(4).setPreferredWidth(60);
                cm.getColumn(5).setPreferredWidth(110);
            } catch (Throwable ignored) {}
        });

        // ACTIONS (sama seperti kode asli)
        bPilihBarang.addActionListener(e -> openPilihBarangDialog());
        bTambah.addActionListener(e -> onTambah());
        bReset.addActionListener(e -> onResetCart());
        bHapus.addActionListener(e -> onHapusSelected());
        bBayar.addActionListener(e -> onBayar());
        bCetak.addActionListener(e -> onCetak());
        btnSelesai.addActionListener(e -> onBayar()); // selesaikan transaksi sama dengan Bayar

        // recalc subtotal when price or qty change
        DocumentListener recalc = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalcSubtotal(); }
            @Override public void removeUpdate(DocumentEvent e) { recalcSubtotal(); }
            @Override public void changedUpdate(DocumentEvent e) { recalcSubtotal(); }
        };
        txtJumlah.getDocument().addDocumentListener(recalc);
        txtHarga.getDocument().addDocumentListener(recalc);

        // recalc kembalian when bayar changed
        txtUangBayar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalcKembalian(); }
            @Override public void removeUpdate(DocumentEvent e) { recalcKembalian(); }
            @Override public void changedUpdate(DocumentEvent e) { recalcKembalian(); }
        });

        // double-click cart edits qty quickly
        tblCart.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int r = tblCart.getSelectedRow();
                    if (r >= 0) {
                        int modelRow = tblCart.convertRowIndexToModel(r);
                        editCartRowQty(modelRow);
                    }
                }
            }
        });
    }

    // ---------------- Picker dialog (table-based) ----------------
    private void openPilihBarangDialog() {
        try {
            List<DetailBarang> list = detailDao.findAll();
            if (list == null || list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada item di stock.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Dialog
            JDialog dlg = new JDialog(this, "Pilih Barang", true);
            dlg.setLayout(new BorderLayout(8,8));

            // search field
            JPanel top = new JPanel(new BorderLayout(6,6));
            top.setBorder(new EmptyBorder(6,6,6,6));
            JTextField txtSearch = new JTextField();
            top.add(new JLabel("Cari (nama/barcode):"), BorderLayout.WEST);
            top.add(txtSearch, BorderLayout.CENTER);
            dlg.add(top, BorderLayout.NORTH);

            // table model
            DefaultTableModel pickModel = new DefaultTableModel(new Object[]{"ID Detail","Barcode","Nama Barang","Stok","Harga"}, 0) {
                @Override public boolean isCellEditable(int r,int c){ return false; }
            };
            for (DetailBarang d : list) {
                String nama = d.getNamaBarang() == null ? "-" : d.getNamaBarang();
                String harga = d.getHargaJual() == null ? "0" : d.getHargaJual().toPlainString();
                pickModel.addRow(new Object[]{ d.getId(), d.getBarcode() == null ? "-" : d.getBarcode(), nama, d.getStok(), harga});
            }

            JTable tPick = new JTable(pickModel);
            tPick.setFillsViewportHeight(true);
            TableRowSorter<TableModel> sorter = new TableRowSorter<>(pickModel);
            tPick.setRowSorter(sorter);

            JScrollPane sp = new JScrollPane(tPick);
            sp.setPreferredSize(new Dimension(720, 360));
            dlg.add(sp, BorderLayout.CENTER);

            // buttons
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton ok = new JButton("OK");
            JButton cancel = new JButton("Cancel");
            btns.add(ok); btns.add(cancel);
            dlg.add(btns, BorderLayout.SOUTH);

            // filter logic
            txtSearch.getDocument().addDocumentListener(new DocumentListener() {
                void filter() {
                    String q = txtSearch.getText().trim();
                    if (q.isEmpty()) {
                        sorter.setRowFilter(null);
                    } else {
                        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
                    }
                }
                @Override public void insertUpdate(DocumentEvent e) { filter(); }
                @Override public void removeUpdate(DocumentEvent e) { filter(); }
                @Override public void changedUpdate(DocumentEvent e) { filter(); }
            });

            // actions
            ok.addActionListener(ae -> {
                int sel = tPick.getSelectedRow();
                if (sel < 0) {
                    JOptionPane.showMessageDialog(dlg, "Pilih baris dulu.");
                    return;
                }
                int modelRow = tPick.convertRowIndexToModel(sel);
                int idDetail = (Integer) pickModel.getValueAt(modelRow, 0);
                // find selected detail object
                selectedDetail = null;
                for (DetailBarang d : list) {
                    if (d.getId() == idDetail) { selectedDetail = d; break; }
                }
                if (selectedDetail != null) {
                    txtBarcode.setText(selectedDetail.getBarcode() == null ? "" : selectedDetail.getBarcode());
                    txtNamaBarang.setText(selectedDetail.getNamaBarang() == null ? "" : selectedDetail.getNamaBarang());
                    txtHarga.setText(selectedDetail.getHargaJual() == null ? "0" : selectedDetail.getHargaJual().toPlainString());
                    txtJumlah.setText("1");
                    recalcSubtotal();
                }
                dlg.dispose();
            });

            cancel.addActionListener(ae -> dlg.dispose());

            dlg.pack();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal buka picker: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------- Cart operations ----------------
    private void onTambah() {
        try {
            if (selectedDetail == null) {
                JOptionPane.showMessageDialog(this, "Pilih barang dulu (tombol Pilih).");
                return;
            }

            int stokAvailable = selectedDetail.getStok();
            int qty;
            try {
                qty = Integer.parseInt(txtJumlah.getText().trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka bulat.", "Input error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (qty <= 0) { JOptionPane.showMessageDialog(this, "Qty harus > 0"); return; }

            // check existing qty present in cart for same id_detail
            int idDetail = selectedDetail.getId();
            int existingQty = 0;
            int rowFound = -1;
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int rId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
                if (rId == idDetail) {
                    existingQty = Integer.parseInt(cartModel.getValueAt(i, 4).toString()); // jumlah column index 4
                    rowFound = i;
                    break;
                }
            }
            if ((long) existingQty + qty > stokAvailable) {
                JOptionPane.showMessageDialog(this, "Total qty melewati stok tersedia (" + stokAvailable + ")", "Stok", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal harga = selectedDetail.getHargaJual() == null ? BigDecimal.ZERO : selectedDetail.getHargaJual();
            BigDecimal sub = harga.multiply(BigDecimal.valueOf(qty));

            if (rowFound >= 0) {
                // merge: update jumlah & subtotal
                int newQty = existingQty + qty;
                BigDecimal newSub = harga.multiply(BigDecimal.valueOf(newQty));
                cartModel.setValueAt(newQty, rowFound, 4);
                cartModel.setValueAt(newSub.toPlainString(), rowFound, 5);
            } else {
                // add new row: columns [idDetail, kodeBarang, namaBarang, harga, jumlah, subtotal]
                String kodeBarang = String.valueOf(selectedDetail.getIdBarang());
                String nama = selectedDetail.getNamaBarang() == null ? "-" : selectedDetail.getNamaBarang();
                cartModel.addRow(new Object[]{
                        selectedDetail.getId(),
                        kodeBarang,
                        nama,
                        harga.toPlainString(),
                        qty,
                        sub.toPlainString()
                });
            }

            updateTotal();

            // clear selection & inputs after adding
            selectedDetail = null;
            txtNamaBarang.setText("");
            txtHarga.setText("");
            txtJumlah.setText("");
            txtSubtotal.setText("");
            txtBarcode.requestFocusInWindow();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal tambah item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editCartRowQty(int row) {
        try {
            int idDetail = Integer.parseInt(cartModel.getValueAt(row, 0).toString());
            int stok = 0;
            // find stok from DB
            DetailBarang d = detailDao.findById(idDetail);
            if (d != null) stok = d.getStok();

            String cur = cartModel.getValueAt(row, 4).toString();
            String s = JOptionPane.showInputDialog(this, "Ubah jumlah (stok tersedia: " + stok + "):", cur);
            if (s == null) return;
            int newQty = Integer.parseInt(s.trim());
            if (newQty <= 0) { JOptionPane.showMessageDialog(this, "Qty harus > 0"); return; }
            if (newQty > stok) { JOptionPane.showMessageDialog(this, "Qty melebihi stok"); return; }
            BigDecimal harga = new BigDecimal(cartModel.getValueAt(row, 3).toString());
            BigDecimal newSub = harga.multiply(BigDecimal.valueOf(newQty));
            cartModel.setValueAt(newQty, row, 4);
            cartModel.setValueAt(newSub.toPlainString(), row, 5);
            updateTotal();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Input tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal ubah qty: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onHapusSelected() {
        int r = tblCart.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris keranjang untuk dihapus.");
            return;
        }
        int modelRow = tblCart.convertRowIndexToModel(r);
        cartModel.removeRow(modelRow);
        updateTotal();
    }

    private void onResetCart() {
        if (JOptionPane.showConfirmDialog(this, "Reset semua item di keranjang?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            cartModel.setRowCount(0);
            updateTotal();
        }
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String sub = cartModel.getValueAt(i, 5).toString();
            total = total.add(new BigDecimal(sub));
        }
        lblTotalValue.setText(total.toPlainString());
        recalcKembalian();
    }

    // ---------------- Payment / finalize ----------------
    private void recalcSubtotal() {
        try {
            String hargaStr = txtHarga.getText().trim();
            String qtyStr = txtJumlah.getText().trim();
            if (hargaStr.isEmpty() || qtyStr.isEmpty()) { txtSubtotal.setText("0"); return; }
            BigDecimal harga = new BigDecimal(hargaStr.replace(",",""));
            int qty = Integer.parseInt(qtyStr);
            BigDecimal sub = harga.multiply(BigDecimal.valueOf(qty));
            txtSubtotal.setText(sub.toPlainString());
        } catch (Exception ex) {
            txtSubtotal.setText("0");
        }
    }

    private void recalcKembalian() {
        try {
            BigDecimal total = new BigDecimal(lblTotalValue.getText());
            BigDecimal bayar = new BigDecimal(txtUangBayar.getText().trim().replace(",",""));
            BigDecimal kembalian = bayar.subtract(total);
            if (kembalian.signum() < 0) lblKembalian.setText("0");
            else lblKembalian.setText(kembalian.toPlainString());
        } catch (Exception ex) {
            lblKembalian.setText("0");
        }
    }

    private void onBayar() {
        if (cartModel.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "Keranjang kosong."); return; }

        // build items (SaleItem expects detail id, qty, price)
        List<SaleItem> items = new ArrayList<>();
        try {
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int idDetail = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
                int qty = Integer.parseInt(cartModel.getValueAt(i, 4).toString());
                BigDecimal price = new BigDecimal(cartModel.getValueAt(i, 3).toString());
                items.add(new SaleItem(idDetail, qty, price));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Data keranjang rusak: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // voucher
        Integer voucherId = null;
        int idx = cbVoucher.getSelectedIndex();
        if (idx > 0) {
            int arrIndex = idx - 1;
            if (arrIndex >= 0 && arrIndex < vouchers.length && vouchers[arrIndex] != null) voucherId = vouchers[arrIndex].getIdVoucher();
        }

        // cash
        BigDecimal cashPaid;
        try {
            cashPaid = new BigDecimal(txtUangBayar.getText().trim().replace(",",""));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Format uang bayar tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String kodeTrans = txtKodeTransaksi.getText();

        try {
            txDao.processSale(items, voucherId, cashPaid, kodeTrans, currentUserId);
            JOptionPane.showMessageDialog(this, "Transaksi berhasil. Kode: " + kodeTrans, "Sukses", JOptionPane.INFORMATION_MESSAGE);

            // reset UI
            cartModel.setRowCount(0);
            updateTotal();
            txtUangBayar.setText("0");
            lblKembalian.setText("0");
            txtKodeTransaksi.setText(generateTransactionCode());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memproses transaksi: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCetak() {
        JOptionPane.showMessageDialog(this, "Fungsi cetak belum diimplementasikan.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------------- Load vouchers ----------------
    private void loadVouchers() {
        try {
            List<Voucher> list = DatabaseHelper.getAllVouchers();
            cbVoucher.removeAllItems();
            vouchers = new Voucher[list.size()];
            cbVoucher.addItem("- Tidak pakai voucher -");
            int i = 0;
            for (Voucher v : list) {
                vouchers[i] = v;
                String lbl = v.getKode() + " (saldo: " + (v.getCurrentBalance() == null ? "0" : v.getCurrentBalance().toPlainString()) + ")";
                cbVoucher.addItem(lbl);
                i++;
            }
        } catch (Exception ex) {
            cbVoucher.removeAllItems();
            cbVoucher.addItem("- Tidak pakai voucher -");
        }
    }

    // ---------------- transaction code generator ----------------
    private String generateTransactionCode() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "TRX" + today;
        int next = 1;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT kode_transaksi FROM transaksi_penjualan WHERE kode_transaksi LIKE ? ORDER BY kode_transaksi DESC LIMIT 1")) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String last = rs.getString(1);
                    if (last != null && last.length() > prefix.length()) {
                        String seq = last.substring(prefix.length());
                        try { next = Integer.parseInt(seq) + 1; } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (Exception ex) {
            // fallback next = 1
        }
        return prefix + String.format("%04d", next);
    }

    // ================= UI helper methods (from transaksipenjualan style) =================
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

    private static JTextField createRoundedField() {
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
        field.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    private static JButton createButton(String text, Color bgColor, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // soft shadow
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
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(width, height));
        return button;
    }

    // =========== testing main (optional) ===========
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Transaksi - Desain Baru (Dialog test)");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1200, 760);
            f.setLocationRelativeTo(null);
            TransactionDialog dlg = new TransactionDialog(f, "user-1", "Admin");
            dlg.setVisible(true);
            System.exit(0);
        });
    }
}
