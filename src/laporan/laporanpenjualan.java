package laporan;

import transaksi_penjualan.TransactionDAO;
import transaksi_penjualan.TransactionItem;
import transaksi_penjualan.TransactionRecord;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * laporanpenjualan - versi ringkas dengan card summary di kanan
 * (card style disesuaikan seperti laporanpembelian)
 */
public class laporanpenjualan extends JPanel {
    private final TransactionDAO txDao = new TransactionDAO();
    private final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DecimalFormat moneyFmt = new DecimalFormat("#,###");

    // UI
    private JTextField txtDari;
    private JTextField txtSampai;
    private DefaultTableModel modelTrans;
    private JTable tabelTrans;

    // summary value labels (references to value labels inside cards)
    private JLabel lblTotalPendapatanValue;
    private JLabel lblJumlahTransaksiValue;
    private JLabel lblBarangTerjualValue;
    private JLabel lblRataRataValue;

    public laporanpenjualan() {
        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));

        // HEADER
        JLabel title = new JLabel("Laporan Penjualan", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(18, 24, 8, 0));
        add(title, BorderLayout.NORTH);

        // MAIN
        JPanel main = new JPanel(new BorderLayout(16, 0));
        main.setBorder(new EmptyBorder(12, 24, 18, 24));
        main.setOpaque(false);

        // LEFT: tabel transaksi (single table)
        JPanel left = new JPanel(new BorderLayout(8,8));
        left.setOpaque(false);

        // filter bar
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filter.setOpaque(false);
        filter.add(new JLabel("Dari:"));
        txtDari = createDateField();
        filter.add(txtDari);
        filter.add(new JLabel("Sampai:"));
        txtSampai = createDateField();
        filter.add(txtSampai);
        JButton bFilter = createModernButton("Filter", new Color(0,180,0), 100, 36);
        JButton bRefresh = createModernButton("Refresh", new Color(33,150,243), 100, 36);
        filter.add(bFilter);
        filter.add(bRefresh);

        left.add(filter, BorderLayout.NORTH);

        // table columns
        String[] cols = {"ID", "Kode Transaksi", "Tanggal", "Kasir", "Guru", "Total Harga", "Total Bayar", "Kembalian", "Metode"};
        modelTrans = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        tabelTrans = new JTable(modelTrans);
        tabelTrans.setRowHeight(26);
        tabelTrans.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        tabelTrans.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelTrans.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelTrans.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        // hide ID column visually
        tabelTrans.getColumnModel().getColumn(0).setMinWidth(0);
        tabelTrans.getColumnModel().getColumn(0).setMaxWidth(0);
        tabelTrans.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane sp = new JScrollPane(tabelTrans);
        sp.setPreferredSize(new Dimension(900, 420));
        sp.setBorder(BorderFactory.createLineBorder(new Color(210,210,210), 2, true));
        left.add(sp, BorderLayout.CENTER);

        // RIGHT: ringkasan cards (desain seperti laporanpembelian)
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(340, 0));

        // create card panels and extract value labels to update later
        JPanel pTotal = createSummaryPanelWithLabel("Total Pendapatan", "Rp " + moneyFmt.format(0), new Color(255, 167, 38));
        lblTotalPendapatanValue = findValueLabelInSummary(pTotal);

        JPanel pJumlah = createSummaryPanelWithLabel("Jumlah Transaksi", "0", new Color(76, 175, 80));
        lblJumlahTransaksiValue = findValueLabelInSummary(pJumlah);

        JPanel pBarang = createSummaryPanelWithLabel("Barang Terjual", "0", new Color(244, 67, 54));
        lblBarangTerjualValue = findValueLabelInSummary(pBarang);

        JPanel pRata = createSummaryPanelWithLabel("Rata-rata Transaksi", "Rp " + moneyFmt.format(0), new Color(255, 167, 38));
        lblRataRataValue = findValueLabelInSummary(pRata);

        right.add(pTotal);
        right.add(Box.createVerticalStrut(15));
        right.add(pJumlah);
        right.add(Box.createVerticalStrut(15));
        right.add(pBarang);
        right.add(Box.createVerticalStrut(15));
        right.add(pRata);
        right.add(Box.createVerticalStrut(15));

        main.add(left, BorderLayout.CENTER);
        main.add(right, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);

        // listeners: filter & refresh
        bFilter.addActionListener(e -> {
            LocalDate d1 = parseDate(txtDari.getText().trim());
            LocalDate d2 = parseDate(txtSampai.getText().trim());
            if (txtDari.getText().trim().length() > 0 && d1 == null) {
                JOptionPane.showMessageDialog(this, "Format 'Dari' salah. Gunakan yyyy-MM-dd", "Format tanggal", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (txtSampai.getText().trim().length() > 0 && d2 == null) {
                JOptionPane.showMessageDialog(this, "Format 'Sampai' salah. Gunakan yyyy-MM-dd", "Format tanggal", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (d1 != null && d2 != null && d2.isBefore(d1)) {
                JOptionPane.showMessageDialog(this, "'Sampai' tidak boleh sebelum 'Dari'.", "Rentang tanggal", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loadTransactions(d1, d2);
        });
        bRefresh.addActionListener(e -> {
            txtDari.setText("");
            txtSampai.setText("");
            loadTransactions(null, null);
        });

        // double-click opens detail dialog
        tabelTrans.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int r = tabelTrans.rowAtPoint(e.getPoint());
                if (r < 0) return;
                int modelRow = tabelTrans.convertRowIndexToModel(r);
                if (e.getClickCount() == 2) {
                    Object idObj = modelTrans.getValueAt(modelRow, 0);
                    if (idObj != null) {
                        long idTrans = Long.parseLong(idObj.toString());
                        showTransactionItemsDialog(idTrans);
                    }
                }
            }
        });

        // initial load
        loadTransactions(null, null);
    }

    // Load transactions, optionally filter by date range (inclusive)
    private void loadTransactions(LocalDate dari, LocalDate sampai) {
        try {
            List<TransactionRecord> list = txDao.findAllTransactions();
            modelTrans.setRowCount(0);
            List<TransactionRecord> filtered = new ArrayList<>();
            for (TransactionRecord tr : list) {
                LocalDate tgl = parseDateSafe(tr.getTglTransaksi());
                boolean include = true;
                if (dari != null && (tgl == null || tgl.isBefore(dari))) include = false;
                if (sampai != null && (tgl == null || tgl.isAfter(sampai))) include = false;
                if (include) filtered.add(tr);
            }

            for (TransactionRecord tr : filtered) {
                String th = tr.getTotalHarga() == null ? "0" : tr.getTotalHarga().toPlainString();
                String tb = tr.getTotalBayar() == null ? "0" : tr.getTotalBayar().toPlainString();
                String kb = tr.getKembalian() == null ? "0" : tr.getKembalian().toPlainString();
                String kasir = tr.getNamaKasir() == null ? "-" : tr.getNamaKasir();
                String guru = tr.getNamaGuru() == null ? "-" : tr.getNamaGuru();
                modelTrans.addRow(new Object[]{
                        tr.getIdTransaksi(),
                        tr.getKodeTransaksi(),
                        tr.getTglTransaksi(),
                        kasir,
                        guru,
                        th, tb, kb,
                        tr.getPaymentMethod()
                });
            }

            // update summary
            updateSummary(filtered);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal load transaksi: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // summary calculation (uses findItemsByTransaction to count sold quantities)
    private void updateSummary(List<TransactionRecord> transactions) {
        try {
            BigDecimal totalPendapatan = BigDecimal.ZERO;
            int jumlahTrans = transactions.size();
            long totalBarangTerjual = 0;

            for (TransactionRecord tr : transactions) {
                if (tr.getTotalHarga() != null) totalPendapatan = totalPendapatan.add(tr.getTotalHarga());
                try {
                    List<TransactionItem> items = txDao.findItemsByTransaction(tr.getIdTransaksi());
                    for (TransactionItem it : items) totalBarangTerjual += it.getJumlahBarang();
                } catch (Exception ignore) { }
            }

            lblTotalPendapatanValue.setText("Rp " + moneyFmt.format(totalPendapatan));
            lblJumlahTransaksiValue.setText(String.valueOf(jumlahTrans));
            lblBarangTerjualValue.setText(String.valueOf(totalBarangTerjual));
            if (jumlahTrans == 0) lblRataRataValue.setText("Rp 0");
            else lblRataRataValue.setText("Rp " + moneyFmt.format(totalPendapatan.divide(BigDecimal.valueOf(jumlahTrans)).longValue()));
        } catch (Exception ex) {
            lblTotalPendapatanValue.setText("Rp 0");
            lblJumlahTransaksiValue.setText("0");
            lblBarangTerjualValue.setText("0");
            lblRataRataValue.setText("Rp 0");
        }
    }

    // Show modal dialog listing items for given transaction id
    private void showTransactionItemsDialog(long idTransaksi) {
        try {
            List<TransactionItem> items = txDao.findItemsByTransaction(idTransaksi);
            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this) instanceof Frame ? (Frame) SwingUtilities.getWindowAncestor(this) : null,
                    "Detail Barang - Transaksi " + idTransaksi, Dialog.ModalityType.APPLICATION_MODAL);
            dlg.setLayout(new BorderLayout(8,8));

            String[] cols = {"ID Detail","ID Barang","Nama Barang","Qty","Harga Unit","Subtotal"};
            DefaultTableModel m = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r,int c){ return false; }
            };
            JTable t = new JTable(m);
            t.setRowHeight(26);
            t.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            for (TransactionItem it : items) {
                String harga = it.getHargaUnit() == null ? "0" : it.getHargaUnit().toPlainString();
                String sub = it.getSubtotal() == null ? "0" : it.getSubtotal().toPlainString();
                m.addRow(new Object[]{
                        it.getIdDetailPenjualan(),
                        it.getIdDetailBarang(),
                        it.getNamaBarang(),
                        it.getJumlahBarang(),
                        harga,
                        sub
                });
            }
            // hide id detail col visually
            t.getColumnModel().getColumn(0).setMinWidth(0);
            t.getColumnModel().getColumn(0).setMaxWidth(0);
            t.getColumnModel().getColumn(0).setWidth(0);

            JScrollPane sp = new JScrollPane(t);
            sp.setPreferredSize(new Dimension(760, 320));
            sp.setBorder(BorderFactory.createLineBorder(new Color(200,200,200),1,true));
            dlg.add(sp, BorderLayout.CENTER);

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton bClose = new JButton("Tutup");
            bClose.addActionListener(e -> dlg.dispose());
            btns.add(bClose);
            dlg.add(btns, BorderLayout.SOUTH);

            dlg.pack();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal ambil detail: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =================== UI helpers (cards like laporanpembelian) ===================
    private JTextField createDateField() {
        JTextField f = new JTextField(10);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190,190,190),1,true),
                new EmptyBorder(4,8,4,8)));
        return f;
    }

    private JButton createModernButton(String text, Color baseColor, int w, int h) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
                setPreferredSize(new Dimension(w, h));
                setContentAreaFilled(false);
                setBorderPainted(false);
                setOpaque(false);

                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent evt) { hovered = true; repaint(); }
                    @Override public void mouseExited(java.awt.event.MouseEvent evt) { hovered = false; repaint(); }
                });
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = hovered ? baseColor.brighter() : baseColor;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(0,0,0,40));
                g2.fillRoundRect(2, 2, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        return btn;
    }

    private JPanel createSummaryPanelWithLabel(String title, String value, Color color) {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setOpaque(false);
        outerPanel.setMaximumSize(new Dimension(340, 100));

        // Panel rounded dengan shadow
        JPanel panel = new RoundedPanel(20, color);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        lblValue.setForeground(Color.WHITE);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(lblValue, BorderLayout.CENTER);

        outerPanel.add(panel, BorderLayout.CENTER);
        return outerPanel;
    }

    // helper untuk mengambil JLabel value dari panel summary (karena desain return panel)
    // Asumsi: struktur panel -> outerPanel -> RoundedPanel -> center component is JLabel value
    private JLabel findValueLabelInSummary(JPanel summaryPanel) {
        if (summaryPanel.getComponentCount() == 0) return new JLabel("");
        Component c = summaryPanel.getComponent(0); // RoundedPanel
        if (!(c instanceof JPanel)) return new JLabel("");
        JPanel rp = (JPanel) c;
        // cari JLabel dengan font size >= 20 (value label)
        for (Component cc : rp.getComponents()) {
            if (cc instanceof JLabel) {
                JLabel jl = (JLabel) cc;
                if (jl.getFont().getSize() >= 20) return jl;
            }
        }
        // fallback: return first JLabel found
        for (Component cc : rp.getComponents()) {
            if (cc instanceof JLabel) return (JLabel) cc;
        }
        return new JLabel("");
    }

    // Panel Rounded dengan efek shadow lembut
    class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
            setPreferredSize(new Dimension(320, 90));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Shadow
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, cornerRadius, cornerRadius);

            // Background utama
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, cornerRadius, cornerRadius);

            g2.dispose();
        }
    }

    // parse yyyy-MM-dd or take first 10 chars if datetime
    private LocalDate parseDate(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try {
            return LocalDate.parse(s, ISO);
        } catch (DateTimeParseException ex) {
            if (s.length() >= 10) {
                try { return LocalDate.parse(s.substring(0,10), ISO); } catch (Exception e) { return null; }
            }
            return null;
        }
    }

    private LocalDate parseDateSafe(String s) { return parseDate(s); }

    // quick testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Laporan Penjualan - Ringkas");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1200, 700);
            f.setLocationRelativeTo(null);
            f.setContentPane(new laporanpenjualan());
            f.setVisible(true);
        });
    }
}
