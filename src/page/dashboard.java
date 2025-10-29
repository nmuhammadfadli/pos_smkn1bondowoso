
package page;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


public class dashboard extends JPanel {

    public dashboard() {
        setLayout(new GridBagLayout());
        setBackground(new Color(236, 236, 236));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        // =======================
        // ROW 1 - 4 CARD KECIL
        // =======================
        gbc.gridy = 0;
        gbc.weighty = 0.25;

        gbc.gridx = 0;
        add(createTopCard("Total Penjualan", "Rp 120.000.000"), gbc);

        gbc.gridx = 1;
        add(createTopCard("Barang Terjual", "1.245"), gbc);

        gbc.gridx = 2;
        add(createTopCard("Pelanggan Baru", "58"), gbc);

        gbc.gridx = 3;
        add(createTopCard("Stok Menipis", "12 Item"), gbc);

        // =======================
        // ROW 2 - 3 CARD PANJANG
        // =======================
        gbc.gridy = 1;
        gbc.weighty = 0.35;

        gbc.gridx = 0;
        add(createWideCard("Aktivitas Hari Ini", "📦 37 transaksi selesai • 3 menunggu konfirmasi"), gbc);

        gbc.gridx = 1;
        add(createWideCard("Statistik Pengunjung", "👥 215 pengunjung hari ini • 12 pelanggan baru"), gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 2;
        add(createWideCard("Analisis Mingguan", "📊 Grafik performa penjualan minggu ini"), gbc);

        // =======================
        // ROW 3 - 2 CARD BAWAH
        // =======================
        gbc.gridy = 2;
        gbc.weighty = 0.4;

        gbc.gridx = 0;
        gbc.gridwidth = 3;
        add(createLargeCard("Grafik Penjualan Bulanan", "📈 Area untuk chart utama atau data visual"), gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        add(createSmallCard("Notifikasi", "🔔 Tidak ada notifikasi baru"), gbc);
    }

    // ====== CARD CREATION FUNCTIONS ======

    private RoundedPanel createTopCard(String title, String value) {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 90));
        card.setBorder(new EmptyBorder(10, 20, 10, 20));
        card.setShadowVisible(true);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(new Color(100, 100, 100));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
        lblValue.setForeground(new Color(60, 60, 60));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        addHoverEffect(card, lblTitle, lblValue);
        return card;
    }

    private RoundedPanel createWideCard(String title, String desc) {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 25, 20, 25));
        card.setPreferredSize(new Dimension(250, 100));
        card.setShadowVisible(true);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        lblTitle.setForeground(new Color(60, 60, 60));

        JLabel lblDesc = new JLabel("<html><center>" + desc + "</center></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(new Color(120, 120, 120));
        lblDesc.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblDesc, BorderLayout.CENTER);

        addHoverEffect(card, lblTitle, lblDesc);
        return card;
    }

    private RoundedPanel createLargeCard(String title, String desc) {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30));
        card.setShadowVisible(true);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
        lblTitle.setForeground(new Color(60, 60, 60));

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc.setForeground(new Color(120, 120, 120));
        lblDesc.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblDesc, BorderLayout.CENTER);

        addHoverEffect(card, lblTitle, lblDesc);
        return card;
    }

    private RoundedPanel createSmallCard(String title, String desc) {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30));
        card.setShadowVisible(true);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        lblTitle.setForeground(new Color(60, 60, 60));

        JLabel lblDesc = new JLabel("<html><center>" + desc + "</center></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(new Color(120, 120, 120));
        lblDesc.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblDesc, BorderLayout.CENTER);

        addHoverEffect(card, lblTitle, lblDesc);
        return card;
    }

    // ====== HOVER EFFECT ======

    private void addHoverEffect(RoundedPanel card, JLabel... labels) {
        Color normalTextColor = new Color(60, 60, 60);
        Color hoverTextColor = Color.WHITE;

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setGradientVisible(true);
                for (JLabel lbl : labels) {
                    lbl.setForeground(hoverTextColor);
                }
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setGradientVisible(false);
                for (JLabel lbl : labels) {
                    lbl.setForeground(normalTextColor);
                }
                card.repaint();
            }
        });
    }

    // ====== CUSTOM PANEL CLASS ======

    static class RoundedPanel extends JPanel {
        private int cornerRadius;
        private Color backgroundColor;
        private boolean shadowVisible = false;
        private boolean gradientVisible = false;

        public RoundedPanel(int radius, Color bgColor) {
            super();
            cornerRadius = radius;
            backgroundColor = bgColor;
            setOpaque(false);
        }

        public void setShadowVisible(boolean visible) {
            shadowVisible = visible;
        }

        public void setGradientVisible(boolean visible) {
            gradientVisible = visible;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadowSize = shadowVisible ? 6 : 0;

            // shadow
            if (shadowVisible) {
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fillRoundRect(shadowSize, shadowSize,
                        getWidth() - shadowSize * 2, getHeight() - shadowSize * 2,
                        cornerRadius, cornerRadius);
            }

            // background / gradient
            if (gradientVisible) {
                GradientPaint gradient = new GradientPaint(0, 0, new Color(0xFF5E4D),
                        getWidth(), getHeight(), new Color(0xFFA62B));
                g2.setPaint(gradient);
            } else {
                g2.setColor(backgroundColor);
            }

            g2.fillRoundRect(0, 0, getWidth() - shadowSize, getHeight() - shadowSize,
                    cornerRadius, cornerRadius);

            g2.dispose();
        }
    }
}
