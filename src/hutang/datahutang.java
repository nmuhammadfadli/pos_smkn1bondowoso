package hutang;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

public class datahutang extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    private ReceivableDAO dao;
    // map baris -> id_receivable
    private final List<Integer> rowIds = new ArrayList<>();

    public datahutang() {
        try { dao = new ReceivableDAO(); } catch (Exception ex) { dao = null; }
        initUI();

        // reload saat panel visible
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

        // =============================
        // PANEL ATAS (SEARCH & BUTTON)
        // =============================
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        // Search field
        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Cari daftar hutang...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.setBorder(new RoundedBorder(15));

        // Tombol-tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnLunasi = createButton("Lunasi", new Color(46, 204, 113));
        JButton btnRefresh = createButton("Refresh", new Color(155, 89, 182));

        buttonPanel.add(btnLunasi);
        buttonPanel.add(btnRefresh);

        topPanel.add(searchField, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // =============================
        // TABEL DATA HUTANG (SEMUA KOLOM)
        // =============================
        String[] columns = {
            "ID Hutang",
            "Kode Transaksi",
            "Voucher ID",
            "Voucher Code",
            "Owner",
            "Jumlah Total",
            "Jumlah Bayar",
            "Sisa Hutang",
            "Tanggal",
            "Status"
        };
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

        // Dummy row (sementara sebelum load)
        model.addRow(new Object[]{"H1001", "TRX91", "", "VR0023", "Mamba", "110.000", "10.000", "100.000", "18 Okt 2025", "OPEN"});

        // =============================
        // TAMBAHKAN SEMUA KE PANEL
        // =============================
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // =============================
        // EVENT: POPUP LUNASI HUTANG
        // =============================
        btnLunasi.addActionListener(e -> onLunasi());
        btnRefresh.addActionListener(e -> loadData());

        // search realtime
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                filterTable(searchField.getText().trim());
            }
        });

        // double-click untuk melihat detail singkat
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < rowIds.size()) {
                        Integer id = rowIds.get(r);
                        try {
                            Receivable rec = (dao==null)?null:dao.findById(id);
                            if (rec != null) {
                                String msg = "ID Hutang: " + rec.getIdReceivable()
                                    + "\nTransaksi: TRX" + rec.getIdTransaksi()
                                    + "\nVoucher ID: " + (rec.getVoucherId()==null?"-":rec.getVoucherId())
                                    + "\nVoucher Code: " + (rec.getVoucherCode()==null?"-":rec.getVoucherCode())
                                    + "\nOwner: " + (rec.getOwnerName()==null?"-":rec.getOwnerName())
                                    + "\nTotal: " + fmt(rec.getAmountTotal())
                                    + "\nTerbayar: " + fmt(rec.getAmountPaid())
                                    + "\nSisa: " + fmt(rec.getAmountOutstanding())
                                    + "\nStatus: " + rec.getStatus()
                                    + "\nTanggal: " + rec.getCreatedAt();
                                JOptionPane.showMessageDialog(datahutang.this, msg, "Detail Hutang", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(datahutang.this, "Gagal mengambil detail:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
    }

    private void loadData() {
        model.setRowCount(0);
        rowIds.clear();
        if (dao == null) return;
        try {
            java.util.List<Receivable> list = dao.findAll();
            if (list == null) return;
            for (Receivable r : list) {
                String kodeTrans = (r.getIdTransaksi() <= 0) ? (r.getVoucherCode()==null?"-":r.getVoucherCode()) : ("TRX" + r.getIdTransaksi());
                Object voucherIdObj = (r.getVoucherId() == null) ? "" : r.getVoucherId();
                model.addRow(new Object[]{
                        r.getIdReceivable(),
                        kodeTrans,
                        voucherIdObj,
                        (r.getVoucherCode()==null) ? "" : r.getVoucherCode(),
                        (r.getOwnerName()==null) ? "" : r.getOwnerName(),
                        fmt(r.getAmountTotal()),
                        fmt(r.getAmountPaid()),
                        fmt(r.getAmountOutstanding()),
                        r.getCreatedAt(),
                        (r.getStatus()==null) ? "" : r.getStatus()
                });
                rowIds.add(r.getIdReceivable());
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data hutang:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(String q) {
        model.setRowCount(0);
        rowIds.clear();
        if (dao == null) return;
        try {
            java.util.List<Receivable> list = dao.findAll();
            if (list == null) return;
            if (q.isEmpty()) {
                for (Receivable r : list) {
                    String kodeTrans = (r.getIdTransaksi() <= 0) ? (r.getVoucherCode()==null?"-":r.getVoucherCode()) : ("TRX" + r.getIdTransaksi());
                    Object voucherIdObj = (r.getVoucherId() == null) ? "" : r.getVoucherId();
                    model.addRow(new Object[]{
                            r.getIdReceivable(),
                            kodeTrans,
                            voucherIdObj,
                            (r.getVoucherCode()==null) ? "" : r.getVoucherCode(),
                            (r.getOwnerName()==null) ? "" : r.getOwnerName(),
                            fmt(r.getAmountTotal()),
                            fmt(r.getAmountPaid()),
                            fmt(r.getAmountOutstanding()),
                            r.getCreatedAt(),
                            (r.getStatus()==null) ? "" : r.getStatus()
                    });
                    rowIds.add(r.getIdReceivable());
                }
                return;
            }
            String lower = q.toLowerCase();
            for (Receivable r : list) {
                String kodeTrans = (r.getIdTransaksi() <= 0) ? (r.getVoucherCode()==null?"-":r.getVoucherCode()) : ("TRX" + r.getIdTransaksi());
                boolean match = false;
                if (String.valueOf(r.getIdReceivable()).toLowerCase().contains(lower)) match = true;
                if (!match && kodeTrans != null && kodeTrans.toLowerCase().contains(lower)) match = true;
                if (!match && r.getVoucherCode() != null && r.getVoucherCode().toLowerCase().contains(lower)) match = true;
                if (!match && r.getOwnerName() != null && r.getOwnerName().toLowerCase().contains(lower)) match = true;
                if (!match && r.getStatus() != null && r.getStatus().toLowerCase().contains(lower)) match = true;
                if (!match && r.getCreatedAt() != null && r.getCreatedAt().toLowerCase().contains(lower)) match = true;
                if (!match && r.getAmountOutstanding() != null && fmt(r.getAmountOutstanding()).toLowerCase().contains(lower)) match = true;

                if (match) {
                    Object voucherIdObj = (r.getVoucherId() == null) ? "" : r.getVoucherId();
                    model.addRow(new Object[]{
                            r.getIdReceivable(),
                            kodeTrans,
                            voucherIdObj,
                            (r.getVoucherCode()==null) ? "" : r.getVoucherCode(),
                            (r.getOwnerName()==null) ? "" : r.getOwnerName(),
                            fmt(r.getAmountTotal()),
                            fmt(r.getAmountPaid()),
                            fmt(r.getAmountOutstanding()),
                            r.getCreatedAt(),
                            (r.getStatus()==null) ? "" : r.getStatus()
                    });
                    rowIds.add(r.getIdReceivable());
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencari data hutang:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onLunasi() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris hutang yang ingin dilunasi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (sel >= rowIds.size()) {
            JOptionPane.showMessageDialog(this, "ID hutang tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final int idReceivable = rowIds.get(sel);
        Receivable r = null;
        try { if (dao != null) r = dao.findById(idReceivable); } catch (SQLException ignored) {}

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Pelunasan Hutang", true);
        dialog.setSize(450, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(250, 250, 250));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel lblKodeHutang = new JLabel("Kode Hutang:");
        lblKodeHutang.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKodeHutang.setForeground(new Color(40, 40, 40));
        panel.add(lblKodeHutang, gbc);

        gbc.gridy++;
        JTextField txtKodeHutang = new JTextField();
        txtKodeHutang.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtKodeHutang.setPreferredSize(new Dimension(350, 36));
        txtKodeHutang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        String preKode = (r != null && r.getVoucherCode()!=null && !r.getVoucherCode().isEmpty()) ? r.getVoucherCode() : ("H" + idReceivable);
        txtKodeHutang.setText(preKode);
        txtKodeHutang.setEditable(false);
        panel.add(txtKodeHutang, gbc);

        gbc.gridy++;
        JLabel lblDibayar = new JLabel("Jumlah Dibayar:");
        lblDibayar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDibayar.setForeground(new Color(40, 40, 40));
        panel.add(lblDibayar, gbc);

        gbc.gridy++;
        JTextField txtDibayar = new JTextField();
        txtDibayar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDibayar.setPreferredSize(new Dimension(350, 36));
        txtDibayar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        if (r != null && r.getAmountOutstanding() != null) {
            txtDibayar.setText(r.getAmountOutstanding().toPlainString());
        }
        panel.add(txtDibayar, gbc);

        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelButton.setOpaque(false);
        JButton btnSimpan = createButton("Simpan", new Color(0, 200, 0));
        JButton btnBatal = createButton("Batal", new Color(220, 0, 0));
        panelButton.add(btnBatal);
        panelButton.add(btnSimpan);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(panelButton, BorderLayout.SOUTH);

        btnBatal.addActionListener(ev -> dialog.dispose());

        btnSimpan.addActionListener(ev -> {
            String dibayar = txtDibayar.getText().trim();
            if (dibayar.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Jumlah pembayaran harus diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String clean = dibayar.replaceAll("[^0-9\\-]", "");
            BigDecimal payment;
            try { payment = new BigDecimal(clean); } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Jumlah pembayaran tidak valid.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                if (dao == null) { JOptionPane.showMessageDialog(dialog, "Database tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                dao.applyPayment(idReceivable, payment);
                JOptionPane.showMessageDialog(dialog,
                        "Pembayaran sebesar " + fmt(payment) + " berhasil diterapkan pada hutang " + txtKodeHutang.getText(),
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Gagal melunasi hutang:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ia) {
                JOptionPane.showMessageDialog(dialog, ia.getMessage(), "Validasi", JOptionPane.WARNING_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private String fmt(BigDecimal val) {
        if (val == null) return "0";
        try {
            NumberFormat nf = NumberFormat.getInstance(new Locale("in","ID"));
            return nf.format(val);
        } catch (Exception ex) {
            return val.toPlainString();
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

                // Soft shadow
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
            insets.left = insets.right = insets.top = insets.bottom = radius / 2; return insets;
        }
    }
}
