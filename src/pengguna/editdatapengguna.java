package pengguna;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * editdatapengguna - versi fungsional tanpa mengubah desain.
 * Hak akses sekarang dropdown: 0 = Admin, 1 = Kasir
 */
public class editdatapengguna extends JPanel {

    // referensi field agar bisa diisi/dibaca
    private RoundedTextField fldId;
    private RoundedTextField fldUsername;
    private RoundedTextField fldPassword;
    private RoundedTextField fldAlamat;
    private RoundedTextField fldJabatan;
    private RoundedTextField fldNamaLengkap;
    private JComboBox<String> cmbHakAkses;               // <-- dropdown
    private RoundedTextField fldEmail;
    private RoundedTextField fldNotelp;

    private PenggunaDAO dao;

    public editdatapengguna() {
        try {
            dao = new PenggunaDAO();
        } catch (Exception ex) {
            dao = null;
            JOptionPane.showMessageDialog(this, "Peringatan: gagal inisialisasi PenggunaDAO:\n" + ex.getMessage(), "DB Warning", JOptionPane.WARNING_MESSAGE);
        }

        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));
        setBorder(new EmptyBorder(30, 50, 30, 50));

        // ===== Bagian atas =====
        JLabel title = new JLabel("Edit Data Pengguna", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 24));
        title.setForeground(new Color(40, 40, 40));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setIcon(new ImageIcon(getClass().getResource("/Images/pengguna.png")));

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
        fldId = addField(formPanel, gbc, 0, "ID Pengguna:");
        fldId.setEditable(false);
        fldUsername = addField(formPanel, gbc, 1, "Username:");
        fldPassword = addField(formPanel, gbc, 2, "Password:");
        fldAlamat = addField(formPanel, gbc, 3, "Alamat:");
        fldJabatan = addField(formPanel, gbc, 4, "Jabatan:");
        fldNamaLengkap = addField(formPanel, gbc, 5, "Nama Lengkap:");
        // Hak Akses: gantikan text field dengan combo box (tetap di posisi grid yang sama)
        addHakAksesField(formPanel, gbc, 6, "Hak Akses:");
        fldEmail = addField(formPanel, gbc, 7, "Email:");
        fldNotelp = addField(formPanel, gbc, 8, "No. Telp:");

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

        // muat data saat panel benar-benar ditampilkan
        this.addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> loadFromContext());
            }
        });
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(() -> loadFromContext());
            }
        });
    }

    private RoundedTextField addField(JPanel panel, GridBagConstraints gbc, int gridx, String labelText) {
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

        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(field, BorderLayout.CENTER);

        panel.add(fieldPanel, gbc);
        return field;
    }

    private void addHakAksesField(JPanel panel, GridBagConstraints gbc, int gridx, String labelText) {
        int row = gridx / 3;
        int col = gridx % 3;

        gbc.gridx = col;
        gbc.gridy = row;

        JPanel fieldPanel = new JPanel(new BorderLayout(5, 5));
        fieldPanel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Combo: tampilkan teks ringkas, nilai di-map saat load/save
        cmbHakAkses = new JComboBox<>(new String[] {"Admin (0)", "Kasir (1)"});
        cmbHakAkses.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbHakAkses.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        cmbHakAkses.setOpaque(false);

        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(cmbHakAkses, BorderLayout.CENTER);

        panel.add(fieldPanel, gbc);
    }

    private void loadFromContext() {
        String editingId = PenggunaContext.editingId;
        // clear fields first
        fldId.setText("");
        fldUsername.setText("");
        fldPassword.setText("");
        fldAlamat.setText("");
        fldJabatan.setText("");
        fldNamaLengkap.setText("");
        if (cmbHakAkses != null) cmbHakAkses.setSelectedIndex(0);
        fldEmail.setText("");
        fldNotelp.setText("");

        if (editingId == null) return;
        if (dao == null) return;

        try {
            Pengguna p = dao.findById(editingId);
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Data pengguna tidak ditemukan.", "Info", JOptionPane.INFORMATION_MESSAGE);
                PenggunaContext.editingId = null;
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataPengguna();
                return;
            }
            fldId.setText(p.getIdPengguna());
            fldUsername.setText(p.getUsername());
            fldPassword.setText(p.getPassword());
            fldAlamat.setText(p.getAlamat());
            fldJabatan.setText(p.getJabatan());
            fldNamaLengkap.setText(p.getNamaLengkap());
            // map integer hak akses -> combo index (0->Admin, 1->Kasir). default ke Admin jika null/unknown.
            Integer ha = p.getHakAkses();
            if (ha == null || ha == 0) cmbHakAkses.setSelectedIndex(0);
            else cmbHakAkses.setSelectedIndex(1);
            fldEmail.setText(p.getEmail());
            fldNotelp.setText(p.getNotelpPengguna());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data pengguna:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        if (dao == null) {
            JOptionPane.showMessageDialog(this, "Database tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idStr = fldId.getText().trim();
        String username = fldUsername.getText().trim();
        String password = fldPassword.getText();
        String alamat = fldAlamat.getText().trim();
        String jabatan = fldJabatan.getText().trim();
        String namaLengkap = fldNamaLengkap.getText().trim();
        String email = fldEmail.getText().trim();
        String notelp = fldNotelp.getText().trim();

        if (username.isEmpty() || namaLengkap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Nama Lengkap harus diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // map combo -> integer hak akses
        Integer hakAkses = 0;
        if (cmbHakAkses != null) {
            int idx = cmbHakAkses.getSelectedIndex();
            // idx 0 -> Admin (0), idx 1 -> Kasir (1)
            hakAkses = (idx == 1) ? 1 : 0;
        }

        Pengguna p = new Pengguna();
        p.setIdPengguna(idStr);
        p.setUsername(username);
        p.setPassword(password);
        p.setAlamat(alamat);
        p.setJabatan(jabatan);
        p.setNamaLengkap(namaLengkap);
        p.setHakAkses(hakAkses);
        p.setEmail(email);
        p.setNotelpPengguna(notelp);

        try {
            // update
            dao.update(p);
            JOptionPane.showMessageDialog(this, "✅ Data pengguna berhasil diedit!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            PenggunaContext.editingId = null;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataPengguna();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan perubahan:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
