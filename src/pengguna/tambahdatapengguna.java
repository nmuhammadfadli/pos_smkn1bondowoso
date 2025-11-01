package pengguna;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * tambahdatapengguna - UI desain dipertahankan; sekarang berfungsi insert & update.
 */
public class tambahdatapengguna extends JPanel {

    // field references (agar bisa read/write)
    private RoundedTextField txtId;
    private RoundedTextField txtUsername;
    private RoundedTextField txtPassword;
    private RoundedTextField txtAlamat;
    private RoundedTextField txtJabatan;
    private RoundedTextField txtNamaLengkap;
    private RoundedTextField txtHakAkses;
    private RoundedTextField txtEmail;
    private RoundedTextField txtNotelp;

    private PenggunaDAO penggunaDao;

    public tambahdatapengguna() {
        try {
            penggunaDao = new PenggunaDAO();
        } catch (Exception ex) {
            penggunaDao = null;
            JOptionPane.showMessageDialog(this, "Peringatan: gagal inisialisasi PenggunaDAO:\n" + ex.getMessage(), "DB Warning", JOptionPane.WARNING_MESSAGE);
        }

        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));
        setBorder(new EmptyBorder(30, 50, 30, 50));

        // ===== Bagian atas =====
        JLabel title = new JLabel("Tambah Data Pengguna", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 24));
        title.setForeground(new Color(40, 40, 40));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setIcon(new ImageIcon(getClass().getResource("/Images/pengguna.png"))); // tetap

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(imageLabel, BorderLayout.CENTER);

        // ===== Form input (tengah) =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Baris input — simpan referensi field
        txtId = addField(formPanel, gbc, 0, "ID Pengguna:");
        txtId.setEditable(false); // id auto
        txtUsername = addField(formPanel, gbc, 1, "Username:");
        txtPassword = addField(formPanel, gbc, 2, "Password:");
        txtAlamat = addField(formPanel, gbc, 3, "Alamat:");
        txtJabatan = addField(formPanel, gbc, 4, "Jabatan:");
        txtNamaLengkap = addField(formPanel, gbc, 5, "Nama Lengkap:");
        txtHakAkses = addField(formPanel, gbc, 6, "Hak Akses:");
        txtEmail = addField(formPanel, gbc, 7, "Email:");
        txtNotelp = addField(formPanel, gbc, 8, "No. Telp:");

        // ===== Tombol =====
        RoundedButton btnKembali = new RoundedButton("Kembali", new Color(235, 235, 235), new Color(60, 60, 60));
        RoundedButton btnSimpan = new RoundedButton("Simpan", new Color(46, 204, 113), Color.WHITE);

        btnKembali.setPreferredSize(new Dimension(140, 45));
        btnSimpan.setPreferredSize(new Dimension(140, 45));

        btnSimpan.addActionListener(e -> onSave());
        btnKembali.addActionListener(e -> {
            PenggunaContext.editingId = null;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) {
                ((uiresponsive.Mainmenu) frame).showDataPengguna();
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

        // load when shown (edit mode)
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(() -> loadIfEdit());
            }
        });
    }

    private RoundedTextField addField(JPanel panel, GridBagConstraints gbc, int gridx, String labelText) {
        int row = gridx / 3;
        int col = gridx % 3;

        gbc.gridx = col;
        gbc.gridy = row;

        JPanel fieldPanel = new JPanel(new BorderLayout(5,5));
        fieldPanel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        RoundedTextField field = new RoundedTextField(12);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(field, BorderLayout.CENTER);
        panel.add(fieldPanel, gbc);

        return field;
    }

    private void loadIfEdit() {
        String editingId = PenggunaContext.editingId;
        // clear first
        txtId.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtAlamat.setText("");
        txtJabatan.setText("");
        txtNamaLengkap.setText("");
        txtHakAkses.setText("");
        txtEmail.setText("");
        txtNotelp.setText("");

        if (editingId == null || penggunaDao == null) return;

        try {
            Pengguna p = penggunaDao.findById(editingId);
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Data pengguna tidak ditemukan.", "Info", JOptionPane.INFORMATION_MESSAGE);
                PenggunaContext.editingId = null;
                return;
            }
            txtId.setText(p.getIdPengguna());
            txtUsername.setText(p.getUsername());
            txtPassword.setText(p.getPassword());
            txtAlamat.setText(p.getAlamat());
            txtJabatan.setText(p.getJabatan());
            txtNamaLengkap.setText(p.getNamaLengkap());
            txtHakAkses.setText(p.getHakAkses() == null ? "" : String.valueOf(p.getHakAkses()));
            txtEmail.setText(p.getEmail());
            txtNotelp.setText(p.getNotelpPengguna());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat pengguna:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        if (penggunaDao == null) {
            JOptionPane.showMessageDialog(this, "Database tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // collect values
        String id = txtId.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String alamat = txtAlamat.getText().trim();
        String jabatan = txtJabatan.getText().trim();
        String namaLengkap = txtNamaLengkap.getText().trim();
        String hakAksesStr = txtHakAkses.getText().trim();
        String email = txtEmail.getText().trim();
        String notelp = txtNotelp.getText().trim();

        if (username.isEmpty() || namaLengkap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Nama Lengkap harus diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer hakAkses = null;
        if (!hakAksesStr.isEmpty()) {
            try {
                hakAkses = Integer.parseInt(hakAksesStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Hak Akses harus angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Pengguna p = new Pengguna();
        p.setUsername(username);
        p.setPassword(password);
        p.setAlamat(alamat);
        p.setJabatan(jabatan);
        p.setNamaLengkap(namaLengkap);
        p.setHakAkses(hakAkses);
        p.setEmail(email);
        p.setNotelpPengguna(notelp);

        try {
            if (PenggunaContext.editingId == null || PenggunaContext.editingId.trim().isEmpty()) {
                // insert
                String newId = penggunaDao.insert(p);
                JOptionPane.showMessageDialog(this, "Pengguna berhasil ditambahkan. ID: " + newId, "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // update (set id)
                p.setIdPengguna(PenggunaContext.editingId);
                penggunaDao.update(p);
                JOptionPane.showMessageDialog(this, "Pengguna berhasil diperbarui.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                PenggunaContext.editingId = null;
            }

            // kembali ke daftar
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataPengguna();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan pengguna:\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // === Rounded TextField & Button (preserve style) ===
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
            g2.dispose();
            super.paintComponent(g);
        }
    }

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
            for (int i = 0; i < 6; i++) {
                g2.setColor(new Color(0, 0, 0, 10 - i));
                g2.fillRoundRect(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, radius, radius);
            }
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, radius, radius);
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(getText())) / 2;
            int textY = (getHeight() + fm.getAscent()) / 2 - 3;
            g2.drawString(getText(), textX, textY);
            g2.dispose();
        }
    }
}
