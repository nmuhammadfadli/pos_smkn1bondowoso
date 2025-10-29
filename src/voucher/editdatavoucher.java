package voucher;

import supplier.*;
import page.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class editdatavoucher extends JPanel {
private JTextField txtGuru;
    public editdatavoucher() {
        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));
        setBorder(new EmptyBorder(30, 50, 30, 50));

        // ===== Bagian atas =====
        JLabel title = new JLabel("Edit Data Voucher", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 24));
        title.setForeground(new Color(40, 40, 40));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setIcon(new ImageIcon(getClass().getResource("/Icon/tambahbarang.png"))); // ganti sesuai path

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(imageLabel, BorderLayout.CENTER);

        // ===== Form input (tengah) =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(40, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Baris input
     
        addField(formPanel, gbc, 0, "Kode Voucher:");
        addField(formPanel, gbc, 1, "Nominal:");
        addField(formPanel, gbc, 2, "Tanggal:");
        addField(formPanel, gbc, 3, "Nama Guru:");
        

        // ===== Tombol =====
        RoundedButton btnKembali = new RoundedButton("Kembali", new Color(235, 235, 235), new Color(60, 60, 60));
        RoundedButton btnSimpan = new RoundedButton("Simpan", new Color(46, 204, 113), Color.WHITE);

        btnKembali.setPreferredSize(new Dimension(140, 45));
        btnSimpan.setPreferredSize(new Dimension(140, 45));

        btnSimpan.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "✅ Data Voucher berhasil diedit!", "Sukses", JOptionPane.INFORMATION_MESSAGE)
        );
        btnKembali.addActionListener(e -> {
    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
    if (frame instanceof uiresponsive.Mainmenu) {
        ((uiresponsive.Mainmenu) frame).showDataVoucher();
    }
});

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnKembali);
        bottomPanel.add(btnSimpan);

        // ===== Tambahkan ke panel utama =====
        add(topPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // === Helper: Field input ===
private void addField(JPanel panel, GridBagConstraints gbc, int gridx, String labelText) {
    int row = gridx / 3;
    int col = gridx % 3;

    gbc.gridx = col;
    gbc.gridy = row;

    JPanel fieldPanel = new JPanel(new BorderLayout(5, 5));
    fieldPanel.setOpaque(false);
    JLabel label = new JLabel(labelText);
    label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

    RoundedTextField field = new RoundedTextField(12);
    field.setFont(new Font("Segoe UI", Font.PLAIN, 14));

    // kalau label kategori, tambahkan event klik untuk buka popup
    if (labelText.equals("Nama Guru:")) {
        txtGuru = field;
        field.setEditable(false);
        field.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new PilihGuruFrame(txtGuru); // buka frame popup
            }
        });
    }

    fieldPanel.add(label, BorderLayout.NORTH);
    fieldPanel.add(field, BorderLayout.CENTER);
    panel.add(fieldPanel, gbc);
}

    // === Rounded TextField ===
    class RoundedTextField extends JTextField {
        private int radius = 15;

        public RoundedTextField(int size) {
            super(size);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    // === Rounded Button (smooth shadow & besar) ===
    class RoundedButton extends JButton {
        private final Color backgroundColor;
        private final Color textColor;
        private int radius = 25;

        public RoundedButton(String text, Color bg, Color fg) {
            super(text);
            this.backgroundColor = bg;
            this.textColor = fg;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Shadow halus (lebih smooth dan lembut)
            for (int i = 0; i < 6; i++) {
                g2.setColor(new Color(0, 0, 0, 10 - i)); // semakin redup di luar
                g2.fillRoundRect(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, radius, radius);
            }

            // Warna tombol
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, radius, radius);

            // Teks tombol
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(getText())) / 2;
            int textY = (getHeight() + fm.getAscent()) / 2 - 3;
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }
        class PilihGuruFrame extends JFrame {
    public PilihGuruFrame(JTextField targetField) {
        setTitle("Pilih Nama Guru");
        setSize(600, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(250, 250, 250));

        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.setOpaque(false);
        JTextField txtSearch = new JTextField();
        JButton btnSearch = new JButton("Cari");
        styleButton(btnSearch, new Color(255, 140, 0));
        searchPanel.add(new JLabel("Cari Kategori:"), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Tabel Kategori
        String[] kolom = {"Nama Guru", "Jabatan"};
        Object[][] data = {
            {"Pak Mursid", "Guru TKJ"},
        };
        DefaultTableModel model = new DefaultTableModel(data, kolom);
        JTable tabel = new JTable(model);
        tabel.setRowHeight(26);
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(tabel);
        panel.add(scroll, BorderLayout.CENTER);

        // Tombol bawah
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);
        JButton btnPilih = new JButton("Pilih");
        JButton btnCancel = new JButton("Cancel");
        styleButton(btnPilih, new Color(0, 180, 0));
        styleButton(btnCancel, new Color(220, 0, 0));
        btnPanel.add(btnPilih);
        btnPanel.add(btnCancel);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Aksi tombol
        btnCancel.addActionListener(e -> dispose());
        btnPilih.addActionListener(e -> {
            int row = tabel.getSelectedRow();
            if (row != -1) {
                String nama = tabel.getValueAt(row, 0).toString();
                targetField.setText(nama); // isi otomatis ke textfield utama
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Pilih dulu Gurunya!");
            }
        });

        add(panel);
        setVisible(true);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(120, 40));
    }
}
}
