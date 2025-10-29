package hutang;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

public class datahutang extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    public datahutang() {
        initUI();
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
        // TABEL DATA HUTANG
        // =============================
        String[] columns = {"ID Hutang", "Kode Transaksi", "Jumlah Total", "Jumlah Bayar", "Sisa Hutang", "Tanggal"};
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

        // Dummy data
        model.addRow(new Object[]{"H1001", "TRX91", "110.000", "10.000", "100.000", "18 Okt 2025"});

        // =============================
        // TAMBAHKAN SEMUA KE PANEL
        // =============================
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // =============================
        // EVENT: POPUP LUNASI HUTANG
        // =============================
        btnLunasi.addActionListener(e -> showPelunasanPopup());
    }

private void showPelunasanPopup() {
    // ===== POPUP INPUT HUTANG =====
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
    gbc.gridwidth = 2; // biar label & field sejajar rata kiri

    // ===== Label & field Kode Hutang =====
    JLabel lblKodeHutang = new JLabel("Kode Hutang:");
    lblKodeHutang.setFont(new Font("Segoe UI", Font.BOLD, 14));
    lblKodeHutang.setForeground(new Color(40, 40, 40));
    panel.add(lblKodeHutang, gbc);

    gbc.gridy++;
    JTextField txtKodeHutang = new JTextField();
    txtKodeHutang.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    txtKodeHutang.setPreferredSize(new Dimension(350, 36)); // diperpanjang
    txtKodeHutang.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
            new EmptyBorder(5, 10, 5, 10)
    ));
    panel.add(txtKodeHutang, gbc);

    // ===== Label & field Jumlah Dibayar =====
    gbc.gridy++;
    JLabel lblDibayar = new JLabel("Jumlah Dibayar:");
    lblDibayar.setFont(new Font("Segoe UI", Font.BOLD, 14));
    lblDibayar.setForeground(new Color(40, 40, 40));
    panel.add(lblDibayar, gbc);

    gbc.gridy++;
    JTextField txtDibayar = new JTextField();
    txtDibayar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    txtDibayar.setPreferredSize(new Dimension(350, 36)); // diperpanjang juga
    txtDibayar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
            new EmptyBorder(5, 10, 5, 10)
    ));
    panel.add(txtDibayar, gbc);

    // ===== Tombol Simpan dan Batal =====
    JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    panelButton.setOpaque(false);
    JButton btnSimpan = createButton("Simpan", new Color(0, 200, 0));
    JButton btnBatal = createButton("Batal", new Color(220, 0, 0));
    panelButton.add(btnBatal);
    panelButton.add(btnSimpan);

    dialog.add(panel, BorderLayout.CENTER);
    dialog.add(panelButton, BorderLayout.SOUTH);

    // ===== Aksi tombol =====
    btnBatal.addActionListener(ev -> dialog.dispose());

    btnSimpan.addActionListener(ev -> {
        String kode = txtKodeHutang.getText().trim();
        String dibayar = txtDibayar.getText().trim();

        if (kode.isEmpty() || dibayar.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Lengkapi semua field terlebih dahulu!");
            return;
        }

        JOptionPane.showMessageDialog(dialog,
                "Hutang dengan kode " + kode + " berhasil dilunasi sejumlah Rp" + dibayar,
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
        dialog.dispose();
    });

    dialog.setVisible(true);
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

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius / 2;
            return insets;
        }
    }
}
