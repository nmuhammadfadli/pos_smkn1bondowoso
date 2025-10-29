package laporan;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.text.DecimalFormat;

public class laporanpembelian extends JPanel {

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
        JTextField txtDari = createDateField();
        JTextField txtSampai = createDateField();
        JButton btnFilter = createModernButton("Filter", new Color(33, 150, 243), 120, 40);

        filterPanel.add(lblDari);
        filterPanel.add(txtDari);
        filterPanel.add(lblSampai);
        filterPanel.add(txtSampai);
        filterPanel.add(btnFilter);

        // ===== TABEL PEMBELIAN =====
        String[] kolom = {"Tanggal", "Kode Pembelian", "Nama Barang", "Supplier", "Jumlah", "Harga", "Total"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);
        JTable tabel = new JTable(model);
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabel.setRowHeight(28);
        tabel.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));

        // Contoh data
        model.addRow(new Object[]{"2025-10-01", "PB001", "Gula Pasir", "PT Maju Jaya", 10, 25000, 250000});
        model.addRow(new Object[]{"2025-10-02", "PB002", "Minyak Goreng", "CV Indofood", 8, 28000, 224000});
        model.addRow(new Object[]{"2025-10-03", "PB003", "Tepung Terigu", "PT Sinar Abadi", 12, 23000, 276000});

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

        rightPanel.add(createSummaryPanel("Total Pengeluaran", "Rp " + df.format(750000), new Color(244, 67, 54))); // Merah
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(createSummaryPanel("Jumlah Pembelian", "3", new Color(76, 175, 80))); // Hijau
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(createSummaryPanel("Barang Dibeli", "30", new Color(33, 150, 243))); // Biru
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(createSummaryPanel("Rata-rata Pembelian", "Rp " + df.format(250000), new Color(255, 167, 38))); // Oren

        // ===== Gabungkan kiri dan kanan =====
        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);
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

    private JPanel createSummaryPanel(String title, String value, Color color) {
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
