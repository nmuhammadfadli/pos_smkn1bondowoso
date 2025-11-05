package page;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D; // [BARU] Untuk style titik chart
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat; // [BARU] Untuk format tanggal di chart
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// [BARU] Import JFreeChart
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
// [AKHIR BARU]

// Import DAO dan model dari package lain
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

    // Referensi untuk kartu produk terlaris
    private JPanel topProductContentPanel;
    private JLabel topProductPlaceholder;

    // [BARU] Referensi untuk panel konten kartu grafik
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

        // =======================
        // ROW 0 - KARTU SELAMAT DATANG
        // =======================
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        add(createWelcomeCard(), gbc);

        // =======================
        // ROW 1 - 4 KARTU STATISTIK
        // =======================
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.25;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(10, 10, 10, 10); // (top, left, bottom, right)

        // Kartu 1: Total Penjualan
        gbc.gridx = 0;
        RoundedPanel cardPenjualan = createTopCard(
                "Penjualan Hari Ini", "Memuat...",
                "/Icon/totalpenjualan.png", new Color(0, 150, 136));
        lblTotalPenjualanValue = findValueLabelInCard(cardPenjualan);
        add(cardPenjualan, gbc);

        // Kartu 2: Jumlah Transaksi
        gbc.gridx = 1;
        RoundedPanel cardTransaksi = createTopCard(
                "Transaksi Hari Ini", "Memuat...",
                "/Icon/totaltransaksi.png", new Color(33, 150, 243));
        lblJumlahTransaksiValue = findValueLabelInCard(cardTransaksi);
        add(cardTransaksi, gbc);

        // Kartu 3: Barang Terjual
        gbc.gridx = 2;
        RoundedPanel cardBarang = createTopCard(
                "Barang Terjual", "Memuat...",
                "/Icon/totalbarang.png", new Color(255, 152, 0));
        lblBarangTerjualValue = findValueLabelInCard(cardBarang);
        add(cardBarang, gbc);

        // Kartu 4: Total Pembelian
        gbc.gridx = 3;
        RoundedPanel cardPembelian = createTopCard(
                "Pembelian Hari Ini", "Memuat...",
                "/Icon/totalpembelian.png", new Color(244, 67, 54));
        lblTotalPembelianValue = findValueLabelInCard(cardPembelian);
        add(cardPembelian, gbc);

        // =======================
        // [DIUBAH] ROW 2 - KARTU GRAFIK & PRODUK TERLARIS
        // =======================
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // Baris ini akan mengisi sisa ruang vertikal

        // Kartu Grafik (lebar 3)
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        add(createMainChartCard(), gbc); // [DIUBAH] Panel ini sekarang berisi placeholder

        // Kartu Produk Terlaris (lebar 1)
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        add(createTopProductCard(), gbc); // Mengganti createActivityCard()

        // Mulai ambil data di background
        loadDashboardData();
    }

    /**
     * Mengambil data di background thread agar UI tidak freeze.
     */
    public void loadDashboardData() {
        // Teks awal
        lblTotalPenjualanValue.setText("Rp 0");
        lblJumlahTransaksiValue.setText("0 Transaksi");
        lblBarangTerjualValue.setText("0 Item");
        lblTotalPembelianValue.setText("Rp 0");

        // SwingWorker untuk proses background
        SwingWorker<DashboardData, Void> worker = new SwingWorker<DashboardData, Void>() {

            @Override
            protected DashboardData doInBackground() throws Exception {
                LocalDate today = LocalDate.now();
                // [BARU] Tentukan rentang 30 hari
                LocalDate thirtyDaysAgo = today.minusDays(29); // 29 hari lalu + hari ini = 30 hari
                DashboardData data = new DashboardData();
                
                // Map untuk menghitung produk
                Map<String, Long> productTally = new HashMap<>();
                // [BARU] Map untuk data chart
                Map<LocalDate, BigDecimal> salesPerDay = new HashMap<>();

                // 1. Hitung Data Penjualan (dari laporanpenjualan)
                try {
                    List<TransactionRecord> list = txDao.findAllTransactions();
                    for (TransactionRecord tr : list) {
                        LocalDate tgl = parseDateSafe(tr.getTglTransaksi());
                        if (tgl == null || tr.getTotalHarga() == null) continue;
                        
                        // Cek untuk 4 kartu statistik (HARI INI)
                        if (tgl.equals(today)) {
                            data.totalPenjualan = data.totalPenjualan.add(tr.getTotalHarga());
                            data.jumlahTransaksi++;

                            // Hitung barang terjual & tally produk
                            List<TransactionItem> items = txDao.findItemsByTransaction(tr.getIdTransaksi());
                            for (TransactionItem it : items) {
                                long qty = it.getJumlahBarang();
                                data.barangTerjual += qty;
                                
                                String productName = it.getNamaBarang();
                                if(productName != null) {
                                    productTally.put(productName, productTally.getOrDefault(productName, 0L) + qty);
                                }
                            }
                        }
                        
                        // [BARU] Cek untuk data grafik (30 HARI TERAKHIR)
                        if (!tgl.isBefore(thirtyDaysAgo) && !tgl.isAfter(today)) {
                            BigDecimal currentSales = salesPerDay.getOrDefault(tgl, BigDecimal.ZERO);
                            salesPerDay.put(tgl, currentSales.add(tr.getTotalHarga()));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Gagal hitung penjualan: " + e.getMessage());
                }

                // [BARU] Konversi Map salesPerDay ke XYDataset (TimeSeries)
                TimeSeries salesSeries = new TimeSeries("Penjualan");
                for (int i = 29; i >= 0; i--) { // Loop 30 hari (dari 29 hari lalu s/d hari ini)
                    LocalDate date = today.minusDays(i);
                    BigDecimal total = salesPerDay.getOrDefault(date, BigDecimal.ZERO);
                    // Gunakan org.jfree.data.time.Day untuk sumbu tanggal
                    salesSeries.add(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), total);
                }
                data.salesChartDataset = new TimeSeriesCollection(salesSeries);


                // Konversi Map produk ke List dan urutkan
                for (Map.Entry<String, Long> entry : productTally.entrySet()) {
                    data.topProducts.add(new ProductSummary(entry.getKey(), entry.getValue()));
                }
                data.topProducts.sort((p1, p2) -> Long.compare(p2.quantity, p1.quantity));


                // 2. Hitung Data Pembelian (dari laporanpembelian)
                try {
                    List<Pembelian> purchases = pDao.findAllPembelian();
                    for (Pembelian p : purchases) {
                        LocalDate tgl = parseDateSafe(p.getTglPembelian());
                        if (tgl != null && tgl.equals(today)) {
                            if (p.getTotalHarga() != null) {
                                data.totalPembelian += p.getTotalHarga();
                            }
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
                    // Update UI di Event Dispatch Thread
                    DashboardData data = get();
                    lblTotalPenjualanValue.setText("Rp " + moneyFmt.format(data.totalPenjualan));
                    lblJumlahTransaksiValue.setText(data.jumlahTransaksi + " Transaksi");
                    lblBarangTerjualValue.setText(data.barangTerjual + " Item");
                    lblTotalPembelianValue.setText("Rp " + moneyFmt.format(data.totalPembelian));
                    
                    // Update kartu produk terlaris
                    populateTopProductCard(data.topProducts);
                    
                    // [BARU] Update kartu grafik
                    if (data.salesChartDataset != null) {
                        JFreeChart lineChart = createLineChart(data.salesChartDataset);
                        ChartPanel chartPanel = new ChartPanel(lineChart);
                        chartPanel.setMouseWheelEnabled(true); // Aktifkan zoom
                        chartPanel.setOpaque(false);
                        
                        chartCardContentPanel.removeAll(); // Hapus placeholder
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

    // Helper untuk menyimpan data dari background thread
    private class DashboardData {
        BigDecimal totalPenjualan = BigDecimal.ZERO;
        int jumlahTransaksi = 0;
        long barangTerjual = 0;
        long totalPembelian = 0;
        List<ProductSummary> topProducts = new ArrayList<>();
        // [BARU] Field untuk menyimpan dataset chart
        XYDataset salesChartDataset;
    }

    // ====== FUNGSI PEMBUAT KARTU ======

    /**
     * Membuat kartu statistik dengan ikon. (Padding sudah diperbaiki)
     */
    private RoundedPanel createTopCard(String title, String initialValue, String iconName, Color bgColor) {
        RoundedPanel card = new RoundedPanel(20, bgColor);
        card.setLayout(new BorderLayout(25, 0)); // Jarak horizontal ikon & teks
        card.setPreferredSize(new Dimension(220, 150));
        card.setBorder(new EmptyBorder(20, 25, 20, 25)); // Padding keliling
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
            public void mouseEntered(MouseEvent e) {
                card.setHovered(true);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setHovered(false);
            }
        });
        return card;
    }

    /**
     * Helper untuk mencari JLabel value di dalam kartu.
     */
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

    /**
     * Membuat kartu selamat datang. (Padding sudah diperbaiki)
     */
    private RoundedPanel createWelcomeCard() {
        RoundedPanel card = new RoundedPanel(20, new Color(255, 255, 255));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30)); // padding luar card
        card.setShadowVisible(true);

        String nama = (user != null && user.getNamaLengkap() != null)
                ? user.getNamaLengkap()
                : "Pengguna";

        JLabel lblWelcome = new JLabel("👋 Selamat datang kembali, " + nama + "!");
        lblWelcome.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
        lblWelcome.setForeground(new Color(50, 50, 50));

        JLabel lblSub = new JLabel("Ringkasan aktivitas hari ini di toko Anda.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(120, 120, 120));

        // Panel isi teks dengan padding tambahan di dalam card
        JPanel inner = new JPanel(new GridLayout(2, 1, 0, 8));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(10, 15, 10, 15)); // padding dalam isi teks
        inner.add(lblWelcome);
        inner.add(lblSub);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }


    /**
     * [DIUBAH] Membuat kartu untuk grafik (awalnya berisi placeholder).
     */
    private RoundedPanel createMainChartCard() {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout(0, 15));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        card.setShadowVisible(true);

        JLabel lblTitle = new JLabel("Grafik Penjualan 30 Hari Terakhir");
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        lblTitle.setForeground(new Color(50, 50, 50));
        card.add(lblTitle, BorderLayout.NORTH);

        // [DIUBAH] Panel ini (sekarang jadi variabel instance) akan
        // menampung placeholder, lalu diganti dengan ChartPanel.
        chartCardContentPanel = new JPanel(new BorderLayout());
        chartCardContentPanel.setOpaque(false);
        
        // Placeholder awal
        JLabel lblPlaceholder = new JLabel("📈 Memuat data grafik...");
        lblPlaceholder.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblPlaceholder.setForeground(new Color(150, 150, 150));
        lblPlaceholder.setHorizontalAlignment(SwingConstants.CENTER);
        chartCardContentPanel.add(lblPlaceholder, BorderLayout.CENTER);

        chartCardContentPanel.setBorder(BorderFactory.createDashedBorder(
                new Color(200, 200, 200), 1.2f, 5.0f, 2.0f, false));
        chartCardContentPanel.setBackground(new Color(250, 250, 250));
        chartCardContentPanel.setOpaque(true);

        card.add(chartCardContentPanel, BorderLayout.CENTER);
        return card;
    }
    
    /**
     * [BARU] Helper untuk membuat dan men-style objek JFreeChart.
     */
    private JFreeChart createLineChart(XYDataset dataset) {
        // Buat chart menggunakan TimeSeriesChart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            null,                      // Judul (sudah ada di kartu)
            "Tanggal",                 // Label Sumbu X (Domain)
            "Total Penjualan (Rp)",    // Label Sumbu Y (Range)
            dataset,                   // Data
            true,                      // Tampilkan Legenda
            true,                      // Tampilkan Tooltip
            false                      // Tanpa URL
        );

        // === STYLING ===
        Font segoeUI12 = new Font("Segoe UI", Font.PLAIN, 12);
        Font segoeUI10 = new Font("Segoe UI", Font.PLAIN, 10);
        Color gridColor = new Color(220, 220, 220);
        Color chartBlue = new Color(33, 150, 243); // Warna dari kartu transaksi

        // Background
        chart.setBackgroundPaint(Color.WHITE);
        chart.getLegend().setBackgroundPaint(Color.WHITE);
        chart.getLegend().setItemFont(segoeUI12);

        // Plot (Area gambar)
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(250, 250, 250));
        plot.setDomainGridlinePaint(gridColor);
        plot.setRangeGridlinePaint(gridColor);
        
        // Sumbu Y (Range)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(segoeUI12);
        rangeAxis.setTickLabelFont(segoeUI10);
        // Format Sumbu Y sebagai mata uang (opsional, tapi bagus)
        rangeAxis.setNumberFormatOverride(new DecimalFormat("Rp #,###"));

        // Sumbu X (Domain/Tanggal)
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("dd MMM")); // Format tgl: "25 Des"
        domainAxis.setLabelFont(segoeUI12);
        domainAxis.setTickLabelFont(segoeUI10);

        // Renderer (Garis dan Titik)
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, chartBlue); // Warna garis
        renderer.setSeriesStroke(0, new BasicStroke(2.5f)); // Ketebalan garis
        
        // Style titik
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-3.5, -3.5, 7, 7)); // Ukuran titik
        renderer.setSeriesFillPaint(0, chartBlue); // Warna isi titik
        renderer.setUseFillPaint(true);

        return chart;
    }


    /**
     * [BARU] Membuat kartu untuk "Produk Terlaris".
     */
    private RoundedPanel createTopProductCard() {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout(0, 15));
        card.setBorder(new EmptyBorder(25, 25, 25, 25)); // padding luar card
        card.setShadowVisible(true);

        // === Judul ===
        JLabel lblTitle = new JLabel("Produk Terlaris Hari Ini");
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        lblTitle.setForeground(new Color(50, 50, 50));
        card.add(lblTitle, BorderLayout.NORTH);

        // === Panel Konten ===
        topProductContentPanel = new JPanel();
        topProductContentPanel.setLayout(new BoxLayout(topProductContentPanel, BoxLayout.Y_AXIS));
        topProductContentPanel.setOpaque(true);
        topProductContentPanel.setBackground(Color.WHITE); // putih bersih
        topProductContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // padding isi

        // Placeholder awal
        topProductPlaceholder = new JLabel("<html><i>Memuat data produk...</i></html>");
        topProductPlaceholder.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topProductPlaceholder.setForeground(new Color(120, 120, 120));
        topProductContentPanel.add(topProductPlaceholder);

        // === ScrollPane ===
        JScrollPane scrollPane = new JScrollPane(topProductContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(Color.WHITE); // pastikan putih
        scrollPane.setOpaque(true);
        scrollPane.setBackground(Color.WHITE); // juga putih
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    /**
     * [BARU] Mengisi kartu Produk Terlaris dengan dacreateTopCard()ta dari SwingWorker.
     */
    private void populateTopProductCard(List<ProductSummary> products) {
        topProductContentPanel.removeAll(); // Hapus placeholder

        if (products.isEmpty()) {
            topProductPlaceholder.setText("<html><i>Belum ada produk terjual hari ini.</i></html>");
            topProductContentPanel.add(topProductPlaceholder);
        } else {
            // Tampilkan (misal) 7 produk teratas
            int count = 0;
            for (ProductSummary product : products) {
                if (count >= 7) break; // Batasi jumlah
                
                topProductContentPanel.add(createProductRowPanel(product));
                topProductContentPanel.add(Box.createVerticalStrut(15)); // Jarak antar item
                count++;
            }
        }
        
        topProductContentPanel.revalidate();
        topProductContentPanel.repaint();
    }
    
    /**
     * [BARU] Helper untuk membuat satu baris item di kartu "Produk Terlaris".
     */
    public JPanel createProductRowPanel(ProductSummary product) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // Batasi tinggi baris

        // Ikon (emoji simpel, tanpa dependensi file)
        JLabel lblIcon = new JLabel("🏷️"); // Emoji Tag
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        row.add(lblIcon, BorderLayout.WEST);

        // Nama Produk
        JLabel lblName = new JLabel(product.name);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblName.setForeground(new Color(60, 60, 60));
        row.add(lblName, BorderLayout.CENTER);

        // Jumlah Terjual
        JLabel lblQty = new JLabel(product.quantity + " Pcs  ");
        lblQty.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblQty.setForeground(new Color(33, 150, 243)); // Warna biru
        row.add(lblQty, BorderLayout.EAST);
        
        return row;
    }


    // ====== KELAS PANEL CUSTOM (DENGAN HOVER EFFECT) ======

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