package laporan;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.Locale;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import Helper.DatabaseHelper;
import java.time.ZoneId;
import transaksi_pembelian.PembelianDAO;
import transaksi_pembelian.Pembelian;
import transaksi_pembelian.DetailPembelian;

public class laporanpembelian extends JPanel {

    // label value references agar dapat di-update
    private JLabel lblTotalPengeluaranValue;
    private JLabel lblJumlahPembelianValue;
    private JLabel lblBarangDibeliValue;
    private JLabel lblRataRataValue;

    // [PERUBAHAN 1] Pindahkan model dan tabel ke sini agar bisa diakses
    // oleh fungsi helper baru (loadTodayData)
    private DefaultTableModel model;
    private JTable tabel;
    private com.toedter.calendar.JDateChooser txtDari;
    private com.toedter.calendar.JDateChooser txtSampai;

    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public laporanpembelian() {
        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));

        // ===== HEADER =====
        JLabel title = new JLabel("Laporan Pembelian", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(20, 40, 10, 0));
        add(title, BorderLayout.NORTH);

        // ===== MAIN CONTENT =====
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBorder(new EmptyBorder(20, 40, 40, 40));
        mainPanel.setOpaque(false);

        // ==================== PANEL KIRI ====================
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setOpaque(false);

        // Filter tanggal
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setOpaque(false);
        JLabel lblDari = new JLabel("Dari:");
        JLabel lblSampai = new JLabel("Sampai:");
        
        // === FILTER PANEL ===
        // [PERUBAHAN 1] Inisialisasi field class, bukan variabel lokal
        txtDari = new com.toedter.calendar.JDateChooser();
        txtDari.setDateFormatString("yyyy-MM-dd");
        txtDari.setPreferredSize(new Dimension(120, 28));

        txtSampai = new com.toedter.calendar.JDateChooser();
        txtSampai.setDateFormatString("yyyy-MM-dd");
        txtSampai.setPreferredSize(new Dimension(120, 28));

        // (Penetapan tanggal dipindahkan ke loadTodayData())

        JButton btnFilter = createModernButton("Filter", new Color(33,150,243), 100, 36);
        JButton btnRefresh = createModernButton("Refresh", new Color(76,175,80), 100, 36);


        filterPanel.add(lblDari);
        filterPanel.add(txtDari);
        filterPanel.add(lblSampai);
        filterPanel.add(txtSampai);
        filterPanel.add(btnFilter);
        filterPanel.add(btnRefresh);


        // ===== TABEL PEMBELIAN =====
        String[] kolom = {"Tanggal", "Kode Pembelian", "Nama Barang", "Supplier", "Jumlah", "Harga", "Total"};
        // [PERUBAHAN 1] Inisialisasi field class, bukan variabel lokal
        model = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabel = new JTable(model); // <-- Hapus 'JTable'
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabel.setRowHeight(28);
        tabel.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        tabel.getTableHeader().setBackground(new Color(60, 80, 120));
        tabel.getTableHeader().setForeground(Color.WHITE);
        tabel.setGridColor(new Color(230, 230, 230));
        tabel.setSelectionBackground(new Color(93, 173, 226));
        tabel.setSelectionForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(tabel);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210), 2, true));

        leftPanel.add(filterPanel, BorderLayout.NORTH);
        leftPanel.add(scroll, BorderLayout.CENTER);

        // ==================== PANEL KANAN ====================
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(340, 0));

        DecimalFormat df = new DecimalFormat("#,###");

        // create summary panels but keep references to value labels for updates
        JPanel pTotal = createSummaryPanelWithLabel("Total Pengeluaran", "Rp " + df.format(0), new Color(244, 67, 54));
        lblTotalPengeluaranValue = findValueLabelInSummary(pTotal);

        rightPanel.add(pTotal);
        rightPanel.add(Box.createVerticalStrut(15));

        JPanel pJumlah = createSummaryPanelWithLabel("Jumlah Pembelian", "0", new Color(76, 175, 80));
        lblJumlahPembelianValue = findValueLabelInSummary(pJumlah);

        rightPanel.add(pJumlah);
        rightPanel.add(Box.createVerticalStrut(15));

        JPanel pBarang = createSummaryPanelWithLabel("Barang Dibeli", "0", new Color(33, 150, 243));
        lblBarangDibeliValue = findValueLabelInSummary(pBarang);

        rightPanel.add(pBarang);
        rightPanel.add(Box.createVerticalStrut(15));

        JPanel pRata = createSummaryPanelWithLabel("Rata-rata Pembelian", "Rp " + df.format(0), new Color(255, 167, 38));
        lblRataRataValue = findValueLabelInSummary(pRata);

        rightPanel.add(pRata);
        rightPanel.add(Box.createVerticalStrut(15));

        // ===== Gabungkan kiri dan kanan =====
        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);
        
        
        // [PERUBAHAN 3] Panggil fungsi baru untuk Refresh
        btnRefresh.addActionListener(e -> loadTodayData());

        // [PERUBAHAN 3] Panggil fungsi baru untuk load awal
        loadTodayData();
        
        // Listener Filter tetap seperti semula
        btnFilter.addActionListener(e -> {
            LocalDate dari = null, sampai = null;
            if (txtDari.getDate() != null)
                dari = txtDari.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (txtSampai.getDate() != null)
                sampai = txtSampai.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (dari != null && sampai != null && sampai.isBefore(dari)) {
                JOptionPane.showMessageDialog(this, "'Sampai' tidak boleh sebelum 'Dari'.",
                        "Rentang tanggal", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 'model' sekarang adalah field class, jadi bisa diakses
            loadAndPopulate(model, dari, sampai); 
        });


    }
    
    
    // =========================================================================
    // [PERUBAHAN 2] INI FUNGSI BARU UNTUK LOAD DATA HARI INI
    // =========================================================================
    /**
     * Fungsi helper baru untuk menyetel filter ke hari ini dan memuat data.
     */
    public void loadTodayData() {
        LocalDate today = LocalDate.now();
        
        // JDateChooser memerlukan java.util.Date
        java.util.Date utilDate = java.util.Date.from(
            today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        );
        
        txtDari.setDate(utilDate);
        txtSampai.setDate(utilDate);
        
        // Panggil fungsi load data utama dengan parameter hari ini
        // 'model' sudah jadi field class, jadi bisa diakses
        loadAndPopulate(model, today, today);
    }
    // =========================================================================
    

    /**
     * Muat data pembelian & detail dari DB, isi tabel model dan update ringkasan.
     * Jika dari/sampai kosong -> ambil semua.
     */
    private void loadAndPopulate(DefaultTableModel model, LocalDate dari, LocalDate sampai) {
        model.setRowCount(0);

        long totalPengeluaran = 0L;
        int jumlahPembelian = 0;
        long jumlahBarangDibeli = 0L;

        NumberFormat nf = NumberFormat.getInstance(new Locale("in", "ID"));
        DecimalFormat df = new DecimalFormat("#,###");

        try {
            PembelianDAO pdao = new PembelianDAO();
            List<Pembelian> purchases = pdao.findAllPembelian();

            List<Pembelian> filtered = new ArrayList<>();
            for (Pembelian p : purchases) {
                if (p == null || p.getTglPembelian() == null) continue;

                LocalDate tgl = parseDateSafe(p.getTglPembelian());
                if (tgl == null) continue;

                if (dari != null && tgl.isBefore(dari)) continue;
                if (sampai != null && tgl.isAfter(sampai)) continue;

                filtered.add(p);
            }

            // iterate filtered purchases and expand details into table rows
            for (Pembelian p : filtered) {
                List<DetailPembelian> dets = pdao.findDetailsByPembelian(p.getIdPembelian());
                if (dets == null || dets.isEmpty()) {
                    // tampilkan satu baris kosong detail (jika tidak ada detail)
                    model.addRow(new Object[]{
                            p.getTglPembelian(),
                            p.getIdPembelian(),
                            "-", // nama barang
                            "-", // supplier
                            0,
                            0,
                            p.getTotalHarga() == null ? 0 : p.getTotalHarga()
                    });
                    totalPengeluaran += p.getTotalHarga() == null ? 0 : p.getTotalHarga();
                    jumlahPembelian++;
                    continue;
                }

                // jika ada detail, tampilkan masing-masing detail per baris
                for (DetailPembelian d : dets) {
                    String namaBarang = resolveNamaBarang(d.getIdBarang());
                    String namaSupplier = resolveNamaSupplier(d.getIdSupplier());

                    int qty = d.getStok() == null ? 0 : d.getStok();
                    int harga = d.getHargaBeli() == null ? 0 : d.getHargaBeli();
                    int subtotal = d.getSubtotal() == null
                            ? (int) Math.min((long) qty * (long) harga, Integer.MAX_VALUE)
                            : d.getSubtotal();

                    model.addRow(new Object[]{
                            p.getTglPembelian(),
                            p.getIdPembelian(),
                            namaBarang,
                            namaSupplier,
                            qty,
                            harga,
                            subtotal
                    });

                    totalPengeluaran += subtotal;
                    jumlahBarangDibeli += qty;
                }

                jumlahPembelian++;
            }

            // update summary labels
            lblTotalPengeluaranValue.setText("Rp " + df.format(totalPengeluaran));
            lblJumlahPembelianValue.setText(String.valueOf(jumlahPembelian));
            lblBarangDibeliValue.setText(String.valueOf(jumlahBarangDibeli));

            if (jumlahPembelian > 0)
                lblRataRataValue.setText("Rp " + df.format(totalPengeluaran / jumlahPembelian));
            else
                lblRataRataValue.setText("Rp " + df.format(0));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal memuat data pembelian:\n" + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ... (Sisa kode method helper lainnya seperti parseDateSafe, resolveNamaBarang, dll.
    // tidak perlu diubah dan tetap di sini) ...
    
    private final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private LocalDate parseDateSafe(String s) {
        try {
            if (s == null || s.isEmpty()) return null;
            if (s.length() >= 10) s = s.substring(0, 10);
            return LocalDate.parse(s, ISO);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Resolve nama barang dari id (fallback ke "-" jika tidak ada).
     * Mencoba pakai query langsung ke tabel barang sehingga tidak tergantung DAO.
     */
    private String resolveNamaBarang(Integer idBarang) {
        if (idBarang == null) return "-";
        String sql = "SELECT nama FROM barang WHERE id = ? LIMIT 1";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBarang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException ignored) {}
        return "-";
    }

    /**
     * Resolve nama supplier dari id (fallback ke "-").
     */
    private String resolveNamaSupplier(Integer idSupplier) {
        if (idSupplier == null) return "-";
        String sql = "SELECT nama_supplier FROM data_supplier WHERE id_supplier = ? LIMIT 1";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSupplier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException ignored) {}
        return "-";
    }

    // =================== HELPER METHODS ===================
    private JTextField createDateField() {
        JTextField field = new JTextField(10);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190), 1, true),
                new EmptyBorder(5, 10, 5, 10)));
        return field;
    }

    // Tombol modern (rounded + hover + shadow)
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
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color c = hovered ? baseColor.brighter() : baseColor;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Shadow tipis
                g2.setColor(new Color(0, 0, 0, 40));
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
        for (Component cc : rp.getComponents()) {
            if (cc instanceof JLabel) {
                JLabel lbl = (JLabel) cc;
                // the first label is title, the second is value; choose the second (font semibold bigger)
                // We'll attempt to find the label with larger font size / semibold
            }
        }
        // fallback: search deeper for JLabel with font size >= 20
        for (Component cc : rp.getComponents()) {
            if (cc instanceof JLabel) {
                JLabel jl = (JLabel) cc;
                if (jl.getFont().getSize() >= 20) return jl;
            }
        }
        // last resort: return first JLabel found
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

    // =================== TESTING FRAME ===================
    public static void main(String[] args) {
        JFrame frame = new JFrame("Laporan Pembelian");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1300, 750);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(new laporanpembelian());
        frame.setVisible(true);
    }
    
}   