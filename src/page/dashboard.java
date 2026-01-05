package page;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import laporan.laporanpenjualan;
import pengguna.Pengguna;
import transaksi_penjualan.TransactionDAO;
import transaksi_penjualan.TransactionItem;
import transaksi_penjualan.TransactionRecord;
import transaksi_pembelian.Pembelian;
import transaksi_pembelian.PembelianDAO;
import uiresponsive.UIResponsive;

public class dashboard extends JPanel {

    private Pengguna user;
    private TransactionDAO txDao;
    private PembelianDAO pDao;
    private DecimalFormat moneyFmt;

    // Referensi ke label di dalam kartu agar bisa di-update
    private JLabel lblTotalPenjualanValue;
    private JLabel lblJumlahTransaksiValue;
    private JLabel lblBarangTerjualValue;
    private JLabel lblTotalPembelianValue;
    private JLabel lblLabaValue;

    // Referensi untuk kartu produk terlaris
    private JPanel topProductContentPanel;
    private JLabel topProductPlaceholder;

    // Referensi untuk panel konten kartu grafik
    private JPanel chartCardContentPanel;

    // Formatter tanggal
    private final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    // Kelas internal untuk menampung produk terlaris
    private class ProductSummary {
        String name;
        long quantity;

        ProductSummary(String name, long quantity) {
            this.name = (name != null) ? name : "Produk Tidak Dikenal";
            this.quantity = quantity;
        }
    }

    public dashboard() throws SQLException {
        // Inisialisasi
        this.user = UIResponsive.currentUser;
        this.txDao = new TransactionDAO();
        this.pDao = new PembelianDAO();
        this.moneyFmt = new DecimalFormat("#,###");

        setLayout(new GridBagLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // ROW 0 - welcome (now spans 5 columns)
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 5;          // <-- changed to 5
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        add(createWelcomeCard(), gbc);

        // ROW 1 - 5 cards (Penjualan, Transaksi, Barang Terjual, Pembelian, Laba)
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        // make each card take equal horizontal space
        gbc.weightx = 0.2;

        // Card 1: Penjualan
        gbc.gridx = 0;
        RoundedPanel cardPenjualan = createTopCard(
                "Penjualan", "Memuat...",
                "/Icon/totalpenjualan.png", new Color(0, 150, 136));
        lblTotalPenjualanValue = findValueLabelInCard(cardPenjualan);
        add(cardPenjualan, gbc);

        // Card 2: Transaksi
        gbc.gridx = 1;
        RoundedPanel cardTransaksi = createTopCard(
                "Transaksi", "Memuat...",
                "/Icon/totaltransaksi.png", new Color(33, 150, 243));
        lblJumlahTransaksiValue = findValueLabelInCard(cardTransaksi);
        add(cardTransaksi, gbc);

        // Card 3: Barang Terjual
        gbc.gridx = 2;
        RoundedPanel cardBarang = createTopCard(
                "Barang Terjual", "Memuat...",
                "/Icon/totalbarang.png", new Color(255, 152, 0));
        lblBarangTerjualValue = findValueLabelInCard(cardBarang);
        add(cardBarang, gbc);

        // Card 4: Pembelian
        gbc.gridx = 3;
        RoundedPanel cardPembelian = createTopCard(
                "Pembelian", "Memuat...",
                "/Icon/totalpembelian.png", new Color(244, 67, 54));
        lblTotalPembelianValue = findValueLabelInCard(cardPembelian);
        add(cardPembelian, gbc);

        // Card 5: Laba (use same createTopCard so size matches others)
        gbc.gridx = 4;
        RoundedPanel labaCard = createTopCard(
                "Laba", "Rp 0",
                "/Icon/totalpenjualan.png", new Color(76, 175, 80));
        lblLabaValue = findValueLabelInCard(labaCard);
        add(labaCard, gbc);

        // ROW 2 - chart (4 columns) + top products (1 column)
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.gridwidth = 4; // chart occupies 4 columns now
        add(createMainChartCard(), gbc);

        gbc.gridx = 4;
        gbc.gridwidth = 1;
        add(createTopProductCard(), gbc);

        // Load data
        loadDashboardData();
    }

    /**
     * Mengambil data di background thread agar UI tidak freeze.
     */
    public void loadDashboardData() {
        // default teks
        lblTotalPenjualanValue.setText("Rp 0");
        lblJumlahTransaksiValue.setText("0 Transaksi");
        lblBarangTerjualValue.setText("0 Item");
        lblTotalPembelianValue.setText("Rp 0");
        lblLabaValue.setText("Rp 0");

        SwingWorker<DashboardData, Void> worker = new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                LocalDate today = LocalDate.now();
                LocalDate thirtyDaysAgo = today.minusDays(29);
                DashboardData data = new DashboardData();

                Map<String, Long> productTally = new HashMap<>();
                Map<LocalDate, BigDecimal> salesPerDay = new HashMap<>();

                // 1. Penjualan
                try {
                    List<TransactionRecord> list = txDao.findAllTransactions();
                    for (TransactionRecord tr : list) {
                        LocalDate tgl = parseDateSafe(tr.getTglTransaksi());
                        if (tgl == null || tr.getTotalHarga() == null) continue;

                        if (tgl.equals(today)) {
                            data.totalPenjualan = safeAdd(data.totalPenjualan, tr.getTotalHarga());
                            data.jumlahTransaksi++;

                            List<TransactionItem> items = txDao.findItemsByTransaction(tr.getIdTransaksi());
                            for (TransactionItem it : items) {
                                long qty = it.getJumlahBarang();
                                data.barangTerjual += qty;

                                String productName = it.getNamaBarang();
                                if (productName != null) {
                                    productTally.put(productName, productTally.getOrDefault(productName, 0L) + qty);
                                }

                                // Perhitungan laba: (harga_jual - harga_beli) * qty
                                BigDecimal hargaUnit = (it.getHargaUnit() == null) ? BigDecimal.ZERO : it.getHargaUnit();
                                BigDecimal hargaBeli;
                                try {
                                    hargaBeli = it.getHargaBeli();
                                    if (hargaBeli == null) hargaBeli = BigDecimal.ZERO;
                                } catch (Throwable ignore) {
                                    hargaBeli = BigDecimal.ZERO;
                                }

                                BigDecimal qtyBd = BigDecimal.valueOf(qty);
                                BigDecimal labaPerItem = hargaUnit.subtract(hargaBeli).multiply(qtyBd);
                                data.totalLaba = data.totalLaba.add(labaPerItem);
                            }
                        }

                        // sales per day (30 hari)
                        if (!tgl.isBefore(thirtyDaysAgo) && !tgl.isAfter(today)) {
                            BigDecimal currentSales = salesPerDay.getOrDefault(tgl, BigDecimal.ZERO);
                            currentSales = safeAdd(currentSales, tr.getTotalHarga());
                            salesPerDay.put(tgl, currentSales);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Gagal hitung penjualan: " + e.getMessage());
                }

                // Build time series 30 hari
                TimeSeries salesSeries = new TimeSeries("Penjualan");
                for (int i = 29; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    BigDecimal total = salesPerDay.getOrDefault(date, BigDecimal.ZERO);
                    salesSeries.add(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), total);
                }
                data.salesChartDataset = new TimeSeriesCollection(salesSeries);

                // Top products
                for (Map.Entry<String, Long> entry : productTally.entrySet()) {
                    data.topProducts.add(new ProductSummary(entry.getKey(), entry.getValue()));
                }
                data.topProducts.sort((p1, p2) -> Long.compare(p2.quantity, p1.quantity));

                // 2. Pembelian hari ini
                try {
                    List<Pembelian> purchases = pDao.findAllPembelian();
                    for (Pembelian p : purchases) {
                        LocalDate tgl = parseDateSafe(p.getTglPembelian());
                        if (tgl != null && tgl.equals(today)) {
                            // gunakan helper safeAdd agar tipe p.getTotalHarga() fleksibel
                            data.totalPembelian = safeAdd(data.totalPembelian, p.getTotalHarga());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Gagal hitung pembelian: " + e.getMessage());
                }

                return data;
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    lblTotalPenjualanValue.setText("Rp " + moneyFmt.format(data.totalPenjualan));
                    lblJumlahTransaksiValue.setText(data.jumlahTransaksi + " Transaksi");
                    lblBarangTerjualValue.setText(data.barangTerjual + " Item");
                    lblTotalPembelianValue.setText("Rp " + moneyFmt.format(data.totalPembelian));
                    lblLabaValue.setText("Rp " + moneyFmt.format(data.totalLaba));

                    populateTopProductCard(data.topProducts);

                    if (data.salesChartDataset != null) {
                        JFreeChart lineChart = createLineChart(data.salesChartDataset);
                        ChartPanel chartPanel = new ChartPanel(lineChart);
                        chartPanel.setMouseWheelEnabled(true);
                        chartPanel.setOpaque(false);

                        chartCardContentPanel.removeAll();
                        chartCardContentPanel.add(chartPanel, BorderLayout.CENTER);
                        chartCardContentPanel.revalidate();
                        chartCardContentPanel.repaint();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    lblTotalPenjualanValue.setText("Error");
                    lblJumlahTransaksiValue.setText("Error");
                    lblBarangTerjualValue.setText("Error");
                    lblTotalPembelianValue.setText("Error");
                    lblLabaValue.setText("Error");
                }
            }
        };

        worker.execute();
    }

    // Helper untuk parse tanggal
    private LocalDate parseDateSafe(String s) {
        try {
            if (s == null || s.isEmpty()) return null;
            if (s.length() >= 10) s = s.substring(0, 10);
            return LocalDate.parse(s, ISO);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    // Helper yang fleksibel untuk menambahkan nilai ke BigDecimal
    private static BigDecimal safeAdd(BigDecimal base, Object value) {
        if (base == null) base = BigDecimal.ZERO;
        if (value == null) return base;
        try {
            if (value instanceof BigDecimal) {
                return base.add((BigDecimal) value);
            } else if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
                return base.add(BigDecimal.valueOf(((Number) value).longValue()));
            } else if (value instanceof Double || value instanceof Float) {
                return base.add(BigDecimal.valueOf(((Number) value).doubleValue()));
            } else if (value instanceof Number) {
                // general Number fallback
                return base.add(BigDecimal.valueOf(((Number) value).doubleValue()));
            } else {
                // coba parse string
                return base.add(new BigDecimal(value.toString()));
            }
        } catch (Throwable t) {
            // jika gagal parsing, abaikan penambahan
            return base;
        }
    }

    // Helper class to hold data from background
    private class DashboardData {
        BigDecimal totalPenjualan = BigDecimal.ZERO;
        int jumlahTransaksi = 0;
        long barangTerjual = 0;
        BigDecimal totalPembelian = BigDecimal.ZERO;
        BigDecimal totalLaba = BigDecimal.ZERO;
        List<ProductSummary> topProducts = new ArrayList<>();
        XYDataset salesChartDataset;
    }

    // ======= UI helper / card creators (tidak terlalu diubah dari versimu) =======

    private RoundedPanel createTopCard(String title, String initialValue, String iconName, Color bgColor) {
        RoundedPanel card = new RoundedPanel(20, bgColor);
        card.setLayout(new BorderLayout(25, 0));
        // set same preferred size for all top cards
        card.setPreferredSize(new Dimension(220, 150));
        card.setBorder(new EmptyBorder(20, 25, 20, 25));
        card.setShadowVisible(true);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        lblValue.setForeground(Color.WHITE);
        lblValue.setName("valueLabel");

        textPanel.add(lblTitle);
        textPanel.add(lblValue);

        JLabel lblIcon = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconName));
            Image img = icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            lblIcon.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblIcon.setText("!");
            lblIcon.setFont(new Font("Arial", Font.BOLD, 30));
            lblIcon.setForeground(Color.WHITE);
        }
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setVerticalAlignment(SwingConstants.CENTER);

        card.add(lblIcon, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { card.setHovered(true); }
            @Override
            public void mouseExited(MouseEvent e) { card.setHovered(false); }
        });
        return card;
    }

    // reuse createTopCard for uniformity; keep createSmallCard for other uses if needed
    private RoundedPanel createSmallCard(String title, String initialValue, String iconName, Color bgColor) {
        RoundedPanel card = new RoundedPanel(16, bgColor);
        card.setLayout(new BorderLayout(10, 0));
        card.setPreferredSize(new Dimension(160, 150));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setShadowVisible(true);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        lblValue.setForeground(Color.WHITE);
        lblValue.setName("valueLabel");

        textPanel.add(lblTitle);
        textPanel.add(lblValue);

        JLabel lblIcon = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconName));
            Image img = icon.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
            lblIcon.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblIcon.setText("₿");
            lblIcon.setFont(new Font("Arial", Font.BOLD, 24));
            lblIcon.setForeground(Color.WHITE);
        }
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setVerticalAlignment(SwingConstants.CENTER);

        card.add(lblIcon, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { card.setHovered(true); }
            @Override
            public void mouseExited(MouseEvent e) { card.setHovered(false); }
        });
        return card;
    }

    private JLabel findValueLabelInCard(RoundedPanel card) {
        for (Component comp : card.getComponents()) {
            if (comp instanceof JPanel) {
                for (Component textComp : ((JPanel) comp).getComponents()) {
                    if (textComp instanceof JLabel && "valueLabel".equals(textComp.getName())) {
                        return (JLabel) textComp;
                    }
                }
            }
        }
        return new JLabel("Error");
    }

    private RoundedPanel createWelcomeCard() {
        RoundedPanel card = new RoundedPanel(20, new Color(255, 255, 255));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30));
        card.setShadowVisible(true);

        String nama = (user != null && user.getNamaLengkap() != null) ? user.getNamaLengkap() : "Pengguna";

        JLabel lblWelcome = new JLabel("👋 Selamat datang kembali, " + nama + "!");
        lblWelcome.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
        lblWelcome.setForeground(new Color(50, 50, 50));

        JLabel lblSub = new JLabel("Ringkasan aktivitas hari ini di toko Anda.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(120, 120, 120));

        JPanel inner = new JPanel(new GridLayout(2, 1, 0, 8));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(10, 15, 10, 15));
        inner.add(lblWelcome);
        inner.add(lblSub);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private RoundedPanel createMainChartCard() {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout(0, 15));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        card.setShadowVisible(true);

        JLabel lblTitle = new JLabel("Grafik Penjualan 30 Hari Terakhir");
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        lblTitle.setForeground(new Color(50, 50, 50));
        card.add(lblTitle, BorderLayout.NORTH);

        chartCardContentPanel = new JPanel(new BorderLayout());
        chartCardContentPanel.setOpaque(false);

        JLabel lblPlaceholder = new JLabel("📈 Memuat data grafik...");
        lblPlaceholder.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblPlaceholder.setForeground(new Color(150, 150, 150));
        lblPlaceholder.setHorizontalAlignment(SwingConstants.CENTER);
        chartCardContentPanel.add(lblPlaceholder, BorderLayout.CENTER);

        chartCardContentPanel.setBorder(BorderFactory.createDashedBorder(new Color(200, 200, 200), 1.2f, 5.0f, 2.0f, false));
        chartCardContentPanel.setBackground(new Color(250, 250, 250));
        chartCardContentPanel.setOpaque(true);

        card.add(chartCardContentPanel, BorderLayout.CENTER);
        return card;
    }

    private JFreeChart createLineChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null,
                "Tanggal",
                "Total Penjualan (Rp)",
                dataset,
                true,
                true,
                false
        );

        Font segoeUI12 = new Font("Segoe UI", Font.PLAIN, 12);
        Font segoeUI10 = new Font("Segoe UI", Font.PLAIN, 10);
        Color gridColor = new Color(220, 220, 220);
        Color chartBlue = new Color(33, 150, 243);

        chart.setBackgroundPaint(Color.WHITE);
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(Color.WHITE);
            chart.getLegend().setItemFont(segoeUI12);
        }

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(250, 250, 250));
        plot.setDomainGridlinePaint(gridColor);
        plot.setRangeGridlinePaint(gridColor);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(segoeUI12);
        rangeAxis.setTickLabelFont(segoeUI10);
        rangeAxis.setNumberFormatOverride(new DecimalFormat("Rp #,###"));

        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("dd MMM"));
        domainAxis.setLabelFont(segoeUI12);
        domainAxis.setTickLabelFont(segoeUI10);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, chartBlue);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-3.5, -3.5, 7, 7));
        renderer.setSeriesFillPaint(0, chartBlue);
        renderer.setUseFillPaint(true);

        return chart;
    }

    private RoundedPanel createTopProductCard() {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout(0, 15));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        card.setShadowVisible(true);

        JLabel lblTitle = new JLabel("Produk Terlaris Hari Ini");
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        lblTitle.setForeground(new Color(50, 50, 50));
        card.add(lblTitle, BorderLayout.NORTH);

        topProductContentPanel = new JPanel();
        topProductContentPanel.setLayout(new BoxLayout(topProductContentPanel, BoxLayout.Y_AXIS));
        topProductContentPanel.setOpaque(true);
        topProductContentPanel.setBackground(Color.WHITE);
        topProductContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        topProductPlaceholder = new JLabel("<html><i>Memuat data produk...</i></html>");
        topProductPlaceholder.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topProductPlaceholder.setForeground(new Color(120, 120, 120));
        topProductContentPanel.add(topProductPlaceholder);

        JScrollPane scrollPane = new JScrollPane(topProductContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setOpaque(true);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private void populateTopProductCard(List<ProductSummary> products) {
        topProductContentPanel.removeAll();

        if (products.isEmpty()) {
            topProductPlaceholder.setText("<html><i>Belum ada produk terjual hari ini.</i></html>");
            topProductContentPanel.add(topProductPlaceholder);
        } else {
            int count = 0;
            for (ProductSummary product : products) {
                if (count >= 7) break;
                topProductContentPanel.add(createProductRowPanel(product));
                topProductContentPanel.add(Box.createVerticalStrut(15));
                count++;
            }
        }

        topProductContentPanel.revalidate();
        topProductContentPanel.repaint();
    }

    public JPanel createProductRowPanel(ProductSummary product) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lblIcon = new JLabel("🏷️");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        row.add(lblIcon, BorderLayout.WEST);

        JLabel lblName = new JLabel(product.name);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblName.setForeground(new Color(60, 60, 60));
        row.add(lblName, BorderLayout.CENTER);

        JLabel lblQty = new JLabel(product.quantity + " Pcs  ");
        lblQty.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblQty.setForeground(new Color(33, 150, 243));
        row.add(lblQty, BorderLayout.EAST);

        return row;
    }

    static class RoundedPanel extends JPanel {
        private int cornerRadius;
        private Color backgroundColor;
        private boolean shadowVisible = false;
        private boolean hovered = false;

        public RoundedPanel(int radius, Color bgColor) {
            super();
            cornerRadius = radius;
            backgroundColor = bgColor;
            setOpaque(false);
        }

        public void setShadowVisible(boolean visible) {
            shadowVisible = visible;
            int shadowSize = visible ? 6 : 0;
            setBorder(new EmptyBorder(shadowSize, shadowSize, shadowSize, shadowSize));
        }

        public void setHovered(boolean isHovered) {
            this.hovered = isHovered;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadowSize = shadowVisible ? 6 : 0;
            int width = getWidth() - (shadowSize * 2);
            int height = getHeight() - (shadowSize * 2);
            int x = shadowSize;
            int y = shadowSize;

            if (shadowVisible) {
                Color shadowColor = new Color(0, 0, 0, 50);
                g2.setColor(shadowColor);
                g2.fillRoundRect(x, y + 2, width, height, cornerRadius, cornerRadius);
            }

            Color c = hovered ? backgroundColor.brighter() : backgroundColor;
            g2.setColor(c);
            g2.fillRoundRect(x, y, width, height, cornerRadius, cornerRadius);
            g2.dispose();
        }
    }
}
