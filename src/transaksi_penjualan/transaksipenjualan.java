package transaksi_penjualan;

import barang.DetailBarang;
import barang.DetailBarangDAO;
import Helper.DatabaseHelper;
import voucher.Voucher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
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
import javax.swing.table.TableRowSorter;

/**
 * transaksipenjualan - Panel bergaya, lengkap fungsi transaksi (sama seperti TransactionDialog)
 *
 * Gunakan di NetBeans 8.2; cukup masukkan file ini ke package transaksi_penjualan.
 */
public class transaksipenjualan extends JPanel {

    // ==== DAO / helper ====
    private final DetailBarangDAO detailDao = new DetailBarangDAO();
    private final TransactionDAO txDao = new TransactionDAO();

    // ==== kiri (input) ====
    private JTextField txtKode;
    private JTextField txtNama;
    private JButton btnPilihBarang;
    private JTextField txtHarga;
    private JTextField txtVoucher;
    private JButton btnPilihVoucher;
    private JTextField txtJumlah;
    private JButton btnKeranjang;
    private JTextField txtBarcode;
    private JTextField txtSubtotal;

    // ==== kanan (keranjang) ====
    private DefaultTableModel model;
    private JTable tabel;

    // ==== pembayaran area ====
    private JComboBox<String> cbVoucher;
    private JLabel lblTotal;
    private JTextField txtJumlahBayar;
    private JLabel lblKembali;
    private JButton btnSelesai;
    private JButton btnReset;
    private JButton btnCancel;
    private JButton btnBayar; // optional (alias)

    // ==== internal state ====
    private DetailBarang selectedDetail = null; // diisi saat picker memilih
    private Voucher[] vouchers = new Voucher[0];

    public transaksipenjualan() {
        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));
        initHeader();
        initContent();
        loadVouchers();
        txtKode.setText(generateTransactionCode());
        lblTotal.setText("0");
        txtJumlahBayar.setText("0");
        lblKembali.setText("0");
    }

    private void initHeader() {
        JLabel tanggal = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), SwingConstants.RIGHT);
        tanggal.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 30, 0, 30));
        header.add(tanggal, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void initContent() {
        JPanel content = new JPanel(new GridLayout(1, 2, 40, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 50, 20, 50));

        // LEFT panel (null layout to reproduce visual)
        JPanel leftPanel = new JPanel(null);
        leftPanel.setOpaque(false);

        int x = 0, y = 0, fieldW = 450, fieldH = 38, gapY = 80;

        JLabel lblKode = makeLabel("Kode Transaksi:", x, y);
        txtKode = makeField(x, y + 22, fieldW, fieldH);
        txtKode.setEditable(false);
        leftPanel.add(lblKode);
        leftPanel.add(txtKode);

        y += gapY;
        JLabel lblNama = makeLabel("Nama Barang:", x, y);
        txtNama = makeField(x, y + 22, fieldW - 90, fieldH);
        btnPilihBarang = createButton("Pilih", new Color(255, 140, 0), 90, 46);
        btnPilihBarang.setBounds(x + fieldW - 80, y + 22, 90, 46);
        leftPanel.add(lblNama);
        leftPanel.add(txtNama);
        leftPanel.add(btnPilihBarang);

        // barcode (hidden field visually optional)
        txtBarcode = createRoundedField();
        txtBarcode.setBounds(x, y + 22 + 48, 200, 28); // not shown in design but kept
        txtBarcode.setVisible(false);
        leftPanel.add(txtBarcode);

        y += gapY;
        JLabel lblHarga = makeLabel("Harga:", x, y);
        txtHarga = makeField(x, y + 22, fieldW, fieldH);
        leftPanel.add(lblHarga);
        leftPanel.add(txtHarga);

        y += gapY;
        JLabel lblVoucher = makeLabel("Voucher (jika ada):", x, y);
        txtVoucher = makeField(x, y + 22, fieldW - 90, fieldH);
        btnPilihVoucher = createButton("Pilih", new Color(255, 140, 0), 90, 46);
        btnPilihVoucher.setBounds(x + fieldW - 80, y + 22, 90, 46);
        leftPanel.add(lblVoucher);
        leftPanel.add(txtVoucher);
        leftPanel.add(btnPilihVoucher);

        y += gapY;
        JLabel lblJumlah = makeLabel("Jumlah:", x, y);
        txtJumlah = makeField(x, y + 22, 120, fieldH);
        leftPanel.add(lblJumlah);
        leftPanel.add(txtJumlah);

        y += gapY;
        JLabel lblSub = makeLabel("Subtotal:", x, y);
        txtSubtotal = makeField(x, y + 22, 200, fieldH);
        txtSubtotal.setEditable(false);
        leftPanel.add(lblSub);
        leftPanel.add(txtSubtotal);

        // Tombol Masukkan Keranjang
        btnKeranjang = createButton("Masukkan Keranjang", new Color(0,180,0), fieldW + 10, 55);
        btnKeranjang.setBounds(x, y + gapY + 15, fieldW + 10, 55);
        leftPanel.add(btnKeranjang);

        // RIGHT panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        // Tabel Keranjang (columns same as original)
        String[] kolom = {"ID Detail","Kode Barang","Nama Barang","Harga","Jumlah","Subtotal"};
        model = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        tabel = new JTable(model);
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabel.setRowHeight(28);
        tabel.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        scroll.setPreferredSize(new Dimension(520, 260));
        scroll.getViewport().setBackground(new Color(230,230,230));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210,210,210), 2, true));
        rightPanel.add(scroll);

        // Buttons Cancel & Reset
        JPanel tombolPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        tombolPanel.setOpaque(false);
        btnCancel = createButton("Cancel", new Color(218,165,32), 120, 45);
        btnReset = createButton("Reset", new Color(255,0,0), 120, 45);
        tombolPanel.add(btnCancel);
        tombolPanel.add(btnReset);
        tombolPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tombolPanel.setBorder(new EmptyBorder(15,0,15,0));
        rightPanel.add(tombolPanel);

        // Pembayaran panel (grid)
        JPanel bayarPanel = new JPanel(new GridLayout(4,2,15,15));
        bayarPanel.setOpaque(false);
        bayarPanel.setBorder(new EmptyBorder(0,0,15,0));

        JLabel lblMetode = new JLabel("Metode Bayar:");
        lblMetode.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JLabel lblTotalLabel = new JLabel("Total Harga:");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JLabel lblJumlahBayarLabel = new JLabel("Jumlah Bayar:");
        lblJumlahBayarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JLabel lblKembaliLabel = new JLabel("Kembali:");
        lblKembaliLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        String[] metodeBayarOptions = {"Tunai", "Transfer Bank", "QRIS", "Debit"};
        JComboBox<String> cmbMetodeBayar = new JComboBox<>(metodeBayarOptions);
        cmbMetodeBayar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbMetodeBayar.setPreferredSize(new Dimension(200,35));

        cbVoucher = new JComboBox<>(); // (unused visually in this grid; we keep txtVoucher on left)
        lblTotal = new JLabel("0");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTotal.setOpaque(true);
        lblTotal.setBackground(Color.LIGHT_GRAY);
        lblTotal.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));

        txtJumlahBayar = createRoundedField();
        txtJumlahBayar.setText("0");
        lblKembali = new JLabel("0");
        lblKembali.setOpaque(true);
        lblKembali.setBackground(Color.LIGHT_GRAY);
        lblKembali.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));

        bayarPanel.add(lblMetode);
        bayarPanel.add(cmbMetodeBayar);
        bayarPanel.add(lblTotalLabel);
        bayarPanel.add(lblTotal);
        bayarPanel.add(lblJumlahBayarLabel);
        bayarPanel.add(txtJumlahBayar);
        bayarPanel.add(lblKembaliLabel);
        bayarPanel.add(lblKembali);

        // wrapper card-like
        JPanel bayarWrapper = new JPanel();
        bayarWrapper.setLayout(new BoxLayout(bayarWrapper, BoxLayout.Y_AXIS));
        bayarWrapper.setBackground(new Color(255,140,0));
        bayarWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(6,6,6,6),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1,1,3,3, new Color(230,230,230)),
                        new EmptyBorder(12,12,12,12)
                )
        ));
        bayarWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        bayarWrapper.add(bayarPanel);
        bayarWrapper.setMaximumSize(new Dimension(520,220));
        rightPanel.add(bayarWrapper);

        // Tombol selesai
        btnSelesai = createButton("Selesaikan Transaksi", new Color(0,200,0), 520, 55);
        btnSelesai.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel selesaiPanel = new JPanel();
        selesaiPanel.setOpaque(false);
        selesaiPanel.add(btnSelesai);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(selesaiPanel);

        // aksi tombol
        btnKeranjang.addActionListener(e -> onTambah());
        btnCancel.addActionListener(e -> model.setRowCount(0));
        btnReset.addActionListener(e -> resetFields());
        btnSelesai.addActionListener(e -> onBayar());

        // double-click edit qty
        tabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = tabel.getSelectedRow();
                    if (r >= 0) {
                        int modelRow = tabel.convertRowIndexToModel(r);
                        editCartRowQty(modelRow);
                    }
                }
            }
        });

        // listeners recalc subtotal pada input
        DocumentListener recalc = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalcSubtotal(); }
            @Override public void removeUpdate(DocumentEvent e) { recalcSubtotal(); }
            @Override public void changedUpdate(DocumentEvent e) { recalcSubtotal(); }
        };
        txtJumlah.getDocument().addDocumentListener(recalc);
        txtHarga.getDocument().addDocumentListener(recalc);

        txtJumlahBayar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalcKembalian(); }
            @Override public void removeUpdate(DocumentEvent e) { recalcKembalian(); }
            @Override public void changedUpdate(DocumentEvent e) { recalcKembalian(); }
        });

        // picker actions
        btnPilihBarang.addActionListener(e -> openPilihBarangFrame(txtNama));
        btnPilihVoucher.addActionListener(e -> openPilihVoucherFrame(txtVoucher));

        content.add(leftPanel);
        content.add(rightPanel);
        add(content, BorderLayout.CENTER);

        // hide ID column visually
        SwingUtilities.invokeLater(() -> {
            TableColumnModel cm = tabel.getColumnModel();
            if (cm.getColumnCount() > 0) {
                cm.getColumn(0).setMinWidth(0);
                cm.getColumn(0).setMaxWidth(0);
                cm.getColumn(0).setPreferredWidth(0);
            }
        });
    }

    // ========== Core behavior (sama dengan TransactionDialog) ==========

    private void onTambah() {
        try {
            if (selectedDetail == null) {
                JOptionPane.showMessageDialog(this, "Pilih barang dulu (tombol Pilih) atau isi Nama Barang.");
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
            for (int i = 0; i < model.getRowCount(); i++) {
                int rId = Integer.parseInt(model.getValueAt(i, 0).toString());
                if (rId == idDetail) {
                    existingQty = Integer.parseInt(model.getValueAt(i, 4).toString()); // jumlah column index 4
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
                model.setValueAt(newQty, rowFound, 4);
                model.setValueAt(newSub.toPlainString(), rowFound, 5);
            } else {
                String kodeBarang = String.valueOf(selectedDetail.getIdBarang());
                String nama = selectedDetail.getNamaBarang() == null ? "-" : selectedDetail.getNamaBarang();
                model.addRow(new Object[]{
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
            txtNama.setText("");
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
            int idDetail = Integer.parseInt(model.getValueAt(row, 0).toString());
            int stok = 0;
            DetailBarang d = detailDao.findById(idDetail);
            if (d != null) stok = d.getStok();

            String cur = model.getValueAt(row, 4).toString();
            String s = JOptionPane.showInputDialog(this, "Ubah jumlah (stok tersedia: " + stok + "):", cur);
            if (s == null) return;
            int newQty = Integer.parseInt(s.trim());
            if (newQty <= 0) { JOptionPane.showMessageDialog(this, "Qty harus > 0"); return; }
            if (newQty > stok) { JOptionPane.showMessageDialog(this, "Qty melebihi stok"); return; }
            BigDecimal harga = new BigDecimal(model.getValueAt(row, 3).toString());
            BigDecimal newSub = harga.multiply(BigDecimal.valueOf(newQty));
            model.setValueAt(newQty, row, 4);
            model.setValueAt(newSub.toPlainString(), row, 5);
            updateTotal();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Input tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal ubah qty: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < model.getRowCount(); i++) {
            String sub = model.getValueAt(i, 5).toString();
            total = total.add(new BigDecimal(sub));
        }
        lblTotal.setText(total.toPlainString());
        recalcKembalian();
    }

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
            BigDecimal total = new BigDecimal(lblTotal.getText());
            BigDecimal bayar = new BigDecimal(txtJumlahBayar.getText().trim().replace(",",""));
            BigDecimal kembalian = bayar.subtract(total);
            if (kembalian.signum() < 0) lblKembali.setText("0");
            else lblKembali.setText(kembalian.toPlainString());
        } catch (Exception ex) {
            lblKembali.setText("0");
        }
    }

    private void onBayar() {
        if (model.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "Keranjang kosong."); return; }

        List<SaleItem> items = new ArrayList<>();
        try {
            for (int i = 0; i < model.getRowCount(); i++) {
                int idDetail = Integer.parseInt(model.getValueAt(i, 0).toString());
                int qty = Integer.parseInt(model.getValueAt(i, 4).toString());
                BigDecimal price = new BigDecimal(model.getValueAt(i, 3).toString());
                items.add(new SaleItem(idDetail, qty, price));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Data keranjang rusak: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Integer voucherId = null;
        String kodeVoucher = txtVoucher.getText().trim();
        if (!kodeVoucher.isEmpty()) {
            for (Voucher v : vouchers) {
                if (v != null && kodeVoucher.equals(v.getKode())) { voucherId = v.getIdVoucher(); break; }
            }
        }

        BigDecimal cashPaid;
        try {
            cashPaid = new BigDecimal(txtJumlahBayar.getText().trim().replace(",",""));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Format uang bayar tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String kodeTrans = txtKode.getText();

        try {
            txDao.processSale(items, voucherId, cashPaid, kodeTrans, null); // ubah null ke user id bila perlu
            JOptionPane.showMessageDialog(this, "Transaksi berhasil. Kode: " + kodeTrans, "Sukses", JOptionPane.INFORMATION_MESSAGE);

            model.setRowCount(0);
            updateTotal();
            txtJumlahBayar.setText("0");
            lblKembali.setText("0");
            txtKode.setText(generateTransactionCode());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memproses transaksi: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ========== Picker Frames ==========

    private void openPilihBarangFrame(JTextField targetField) {
        try {
            List<DetailBarang> list = detailDao.findAll();
            if (list == null || list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada item di stock.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JDialog dlg = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Pilih Barang",
                Dialog.ModalityType.APPLICATION_MODAL
            );
            dlg.setLayout(new BorderLayout(8,8));

            JPanel top = new JPanel(new BorderLayout(6,6));
            top.setBorder(new EmptyBorder(6,6,6,6));
            JTextField txtSearch = new JTextField();
            top.add(new JLabel("Cari (nama/barcode):"), BorderLayout.WEST);
            top.add(txtSearch, BorderLayout.CENTER);
            dlg.add(top, BorderLayout.NORTH);

            DefaultTableModel pickModel = new DefaultTableModel(new Object[]{"ID Detail","Barcode","Nama Barang","Stok","Harga"},0){
                @Override public boolean isCellEditable(int r,int c){ return false; }
            };
            for (DetailBarang d : list) {
                String nama = d.getNamaBarang() == null ? "-" : d.getNamaBarang();
                String harga = d.getHargaJual() == null ? "0" : d.getHargaJual().toPlainString();
                pickModel.addRow(new Object[]{ d.getId(), d.getBarcode() == null ? "-" : d.getBarcode(), nama, d.getStok(), harga});
            }

            JTable tPick = new JTable(pickModel);
            tPick.setFillsViewportHeight(true);
            TableRowSorter<javax.swing.table.TableModel> sorter = new TableRowSorter<>(pickModel);
            tPick.setRowSorter(sorter);

            JScrollPane sp = new JScrollPane(tPick);
            sp.setPreferredSize(new Dimension(720,360));
            dlg.add(sp, BorderLayout.CENTER);

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton ok = new JButton("OK");
            JButton cancel = new JButton("Cancel");
            btns.add(ok); btns.add(cancel);
            dlg.add(btns, BorderLayout.SOUTH);

            txtSearch.getDocument().addDocumentListener(new DocumentListener() {
                void filter() {
                    String q = txtSearch.getText().trim();
                    if (q.isEmpty()) sorter.setRowFilter(null);
                    else sorter.setRowFilter(RowFilter.regexFilter("(?i)"+ Pattern.quote(q)));
                }
                @Override public void insertUpdate(DocumentEvent e) { filter(); }
                @Override public void removeUpdate(DocumentEvent e) { filter(); }
                @Override public void changedUpdate(DocumentEvent e) { filter(); }
            });

            ok.addActionListener(ae -> {
                int sel = tPick.getSelectedRow();
                if (sel < 0) { JOptionPane.showMessageDialog(dlg,"Pilih baris dulu."); return; }
                int modelRow = tPick.convertRowIndexToModel(sel);
                int idDetail = (Integer) pickModel.getValueAt(modelRow, 0);
                selectedDetail = null;
                for (DetailBarang d : list) { if (d.getId() == idDetail) { selectedDetail = d; break; } }
                if (selectedDetail != null) {
                    txtBarcode.setText(selectedDetail.getBarcode()==null?"":selectedDetail.getBarcode());
                    txtNama.setText(selectedDetail.getNamaBarang()==null?"":selectedDetail.getNamaBarang());
                    txtHarga.setText(selectedDetail.getHargaJual()==null?"0":selectedDetail.getHargaJual().toPlainString());
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

    private void openPilihVoucherFrame(JTextField targetField) {
        try {
            List<Voucher> list = DatabaseHelper.getAllVouchers();
            if (list == null || list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada voucher.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JDialog dlg = new JDialog(
            SwingUtilities.getWindowAncestor(this),
            "Pilih Voucher",
            Dialog.ModalityType.APPLICATION_MODAL
        );
            dlg.setSize(650,450);
            dlg.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new BorderLayout(10,10));
            panel.setBorder(new EmptyBorder(15,15,15,15));
            panel.setBackground(new Color(250,250,250));

            JTextField txtSearch = new JTextField();
            JButton btnSearch = new JButton("Cari");
            JPanel searchPanel = new JPanel(new BorderLayout(8,8));
            searchPanel.setOpaque(false);
            searchPanel.add(new JLabel("Cari Voucher:"), BorderLayout.WEST);
            searchPanel.add(txtSearch, BorderLayout.CENTER);
            searchPanel.add(btnSearch, BorderLayout.EAST);
            panel.add(searchPanel, BorderLayout.NORTH);

            String[] kolom = {"Kode Voucher","Nominal", "Guru"};
            DefaultTableModel m = new DefaultTableModel(kolom,0);
            for (Voucher v : list) {
                m.addRow(new Object[]{ v.getKode(), v.getCurrentBalance()==null?"0":v.getCurrentBalance().toPlainString()});
            }
            JTable t = new JTable(m);
            t.setRowHeight(26);
            t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            panel.add(new JScrollPane(t), BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
            btnPanel.setOpaque(false);
            JButton btnPilih = new JButton("Pilih");
            JButton btnCancel = new JButton("Cancel");
            styleButton(btnPilih, new Color(0,180,0));
            styleButton(btnCancel, new Color(220,0,0));
            btnPanel.add(btnPilih);
            btnPanel.add(btnCancel);
            panel.add(btnPanel, BorderLayout.SOUTH);

            btnCancel.addActionListener(e -> dlg.dispose());
            btnPilih.addActionListener(e -> {
                int row = t.getSelectedRow();
                if (row != -1) {
                    String kode = t.getValueAt(row,0).toString();
                    targetField.setText(kode);
                    dlg.dispose();
                } else JOptionPane.showMessageDialog(dlg,"Pilih dulu vouchernya!");
            });

            dlg.add(panel);
            dlg.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal buka voucher: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadVouchers() {
        try {
            List<Voucher> list = DatabaseHelper.getAllVouchers();
            if (list == null) list = new ArrayList<>();
            vouchers = new Voucher[list.size()];
            cbVoucher = new JComboBox<>();
            cbVoucher.addItem("- Tidak pakai voucher -");
            int i = 0;
            for (Voucher v : list) {
                vouchers[i] = v;
                String lbl = v.getKode() + " (saldo: " + (v.getCurrentBalance() == null ? "0" : v.getCurrentBalance().toPlainString()) + ")";
                cbVoucher.addItem(lbl);
                i++;
            }
        } catch (Exception ex) {
            cbVoucher = new JComboBox<>(new String[]{"- Tidak pakai voucher -"});
            vouchers = new Voucher[0];
        }
    }

    // ========== UI helpers ==========
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
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,15,15);
                g2.setColor(new Color(190,190,190));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,15,15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    private static JButton createButton(String text, Color bgColor, int width, int height) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,40));
                g2.fillRoundRect(3,3,getWidth()-3,getHeight()-3,20,20);
                g2.setColor(bgColor);
                g2.fillRoundRect(0,0,getWidth()-6,getHeight()-6,20,20);
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

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(120,40));
    }

    // trans code generator (sama)
    private String generateTransactionCode() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = today;
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

    private void resetFields() {
        txtKode.setText("");
        txtNama.setText("");
        txtHarga.setText("");
        txtVoucher.setText("");
        txtJumlah.setText("");
        txtSubtotal.setText("");
    }

    // ====== Main testing ======
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Transaksi Penjualan - Desain & Fungsi");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1300, 750);
            f.setLocationRelativeTo(null);
            f.setContentPane(new transaksipenjualan());
            f.setVisible(true);
        });
    }
}
