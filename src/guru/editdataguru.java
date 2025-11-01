package guru;

import supplier.*;
import page.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class editdataguru extends JPanel {

    private JTextField fldId;
    private JTextField fldNama;
    private JTextField fldTelp;
    private JTextField fldJabatan;

    private GuruDAO dao;

    public editdataguru() {
        try { dao = new GuruDAO(); } catch (Exception ex) { dao = null; }

        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));
        setBorder(new EmptyBorder(30, 50, 30, 50));

        // ===== Bagian atas =====
        JLabel title = new JLabel("Edit Data Guru", SwingConstants.CENTER);
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

        fldId = new RoundedTextField(12);
        fldId.setEditable(false);
        fldNama = new RoundedTextField(12);
        fldTelp = new RoundedTextField(12);
        fldJabatan = new RoundedTextField(12);

        addField(formPanel, gbc, 0, "ID Guru:", fldId);
        addField(formPanel, gbc, 1, "Nama Guru:", fldNama);
        addField(formPanel, gbc, 2, "No. Telp:", fldTelp);
        addField(formPanel, gbc, 3, "Jabatan:", fldJabatan);

        RoundedButton btnKembali = new RoundedButton("Kembali", new Color(235, 235, 235), new Color(60, 60, 60));
        RoundedButton btnSimpan = new RoundedButton("Simpan", new Color(46, 204, 113), Color.WHITE);
        btnKembali.setPreferredSize(new Dimension(140, 45));
        btnSimpan.setPreferredSize(new Dimension(140, 45));

        btnSimpan.addActionListener(e -> onSave());
        btnKembali.addActionListener(e -> {
            GuruContext.editingId = null;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataGuru();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnKembali);
        bottomPanel.add(btnSimpan);

        add(topPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(() -> loadIfEdit());
            }
        });
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int gridx, String labelText, JComponent comp) {
        int row = gridx / 3;
        int col = gridx % 3;
        gbc.gridx = col; gbc.gridy = row;
        JPanel fieldPanel = new JPanel(new BorderLayout(5,5));
        fieldPanel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(comp, BorderLayout.CENTER);
        panel.add(fieldPanel, gbc);
    }

    private void loadIfEdit() {
        if (GuruContext.editingId == null) {
            fldId.setText(""); fldNama.setText(""); fldTelp.setText(""); fldJabatan.setText("");
            return;
        }
        if (dao == null) return;
        try {
            Guru g = dao.findById(GuruContext.editingId);
            if (g == null) { JOptionPane.showMessageDialog(this, "Data guru tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE); GuruContext.editingId = null; return; }
            fldId.setText(String.valueOf(g.getIdGuru()));
            fldNama.setText(g.getNamaGuru());
            fldTelp.setText(g.getNotelpGuru());
            fldJabatan.setText(g.getJabatan());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data guru:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        if (dao == null) { JOptionPane.showMessageDialog(this, "Database tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        String nama = fldNama.getText().trim();
        String telp = fldTelp.getText().trim();
        String jab = fldJabatan.getText().trim();
        if (nama.isEmpty()) { JOptionPane.showMessageDialog(this, "Nama guru harus diisi.", "Validasi", JOptionPane.WARNING_MESSAGE); return; }
        try {
            if (GuruContext.editingId == null) {
                JOptionPane.showMessageDialog(this, "Tidak ada guru yang dipilih untuk diedit.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Guru g = new Guru();
            g.setIdGuru(GuruContext.editingId);
            g.setNamaGuru(nama);
            g.setNotelpGuru(telp.isEmpty()?null:telp);
            g.setJabatan(jab.isEmpty()?null:jab);
            dao.update(g);
            JOptionPane.showMessageDialog(this, "Guru berhasil diupdate.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            GuruContext.editingId = null;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataGuru();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan perubahan:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // === RoundedTextField and RoundedButton are identical to tambahdataguru to preserve design ===
    class RoundedTextField extends JTextField {
        private int radius = 15;
        public RoundedTextField(int size) { super(size); setOpaque(false); setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12)); }
        @Override protected void paintComponent(Graphics g) {
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
    class RoundedButton extends JButton {
        private final Color backgroundColor; private final Color textColor; private int radius = 25;
        public RoundedButton(String text, Color bg, Color fg) { super(text); this.backgroundColor = bg; this.textColor = fg; setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false); setFont(new Font("Segoe UI", Font.BOLD, 15)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < 6; i++) { g2.setColor(new Color(0, 0, 0, 10 - i)); g2.fillRoundRect(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, radius, radius); }
            g2.setColor(backgroundColor); g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, radius, radius);
            g2.setColor(textColor); FontMetrics fm = g2.getFontMetrics(); int textX = (getWidth() - fm.stringWidth(getText())) / 2; int textY = (getHeight() + fm.getAscent()) / 2 - 3; g2.drawString(getText(), textX, textY); g2.dispose();
        }
    }
}
