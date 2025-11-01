package voucher;

import guru.Guru;
import guru.GuruDAO;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class editdatavoucher extends JPanel {
    private RoundedTextField txtKode, txtNominal, txtTanggal, txtGuru;
    private VoucherDAO dao;

    public editdatavoucher() {
        try { dao = new VoucherDAO(); } catch (Exception ex) { dao = null; }

        setLayout(new BorderLayout());
        setBackground(new Color(236,236,236));
        setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("Edit Data Voucher", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 24));
        title.setForeground(new Color(40, 40, 40));
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setIcon(new ImageIcon(getClass().getResource("/Icon/tambahbarang.png")));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(imageLabel, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false); formPanel.setBorder(new EmptyBorder(40,0,0,0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,15,10,15); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

        txtKode = new RoundedTextField(12);
        txtNominal = new RoundedTextField(12);
        txtTanggal = new RoundedTextField(12);
        txtGuru = new RoundedTextField(12);
        txtGuru.setEditable(false);
        txtGuru.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtGuru.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                new PilihGuruFrame(txtGuru);
            }
        });

        addField(formPanel, gbc, 0, "Kode Voucher:", txtKode);
        addField(formPanel, gbc, 1, "Nominal:", txtNominal);
        addField(formPanel, gbc, 2, "Bulan:", txtTanggal);
        addField(formPanel, gbc, 3, "Nama Guru:", txtGuru);

        RoundedButton btnKembali = new RoundedButton("Kembali", new Color(235,235,235), new Color(60,60,60));
        RoundedButton btnSimpan = new RoundedButton("Simpan", new Color(46,204,113), Color.WHITE);
        btnKembali.setPreferredSize(new Dimension(140,45));
        btnSimpan.setPreferredSize(new Dimension(140,45));

        btnSimpan.addActionListener(e -> onSave());
        btnKembali.addActionListener(e -> {
            VoucherContext.editingId = null;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataVoucher();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnKembali); bottomPanel.add(btnSimpan);

        add(topPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) !=0 && isShowing()) {
                SwingUtilities.invokeLater(() -> loadIfEdit());
            }
        });
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int gridx, String labelText, JComponent comp) {
        int row = gridx / 3; int col = gridx % 3;
        gbc.gridx = col; gbc.gridy = row;
        JPanel fieldPanel = new JPanel(new BorderLayout(5,5)); fieldPanel.setOpaque(false);
        JLabel label = new JLabel(labelText); label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fieldPanel.add(label, BorderLayout.NORTH); fieldPanel.add(comp, BorderLayout.CENTER);
        panel.add(fieldPanel, gbc);
    }

    private void loadIfEdit() {
        if (VoucherContext.editingId == null) {
            txtKode.setText(""); txtNominal.setText(""); txtTanggal.setText(""); txtGuru.setText("");
            txtGuru.putClientProperty("id_guru", null);
            return;
        }
        if (dao == null) return;
        try {
            Voucher v = dao.findById(VoucherContext.editingId);
            if (v == null) { JOptionPane.showMessageDialog(this, "Voucher tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE); VoucherContext.editingId = null; return; }
            txtKode.setText(v.getKode());
            txtNominal.setText(v.getCurrentBalance()==null?"":v.getCurrentBalance().toPlainString());
            txtTanggal.setText(v.getBulan()==null?"":v.getBulan());
            txtGuru.setText(v.getNamaGuru()==null?"":v.getNamaGuru());
            txtGuru.putClientProperty("id_guru", v.getIdGuru()==0?null:v.getIdGuru());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat voucher:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        if (dao == null) { JOptionPane.showMessageDialog(this, "Database tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        if (VoucherContext.editingId == null) { JOptionPane.showMessageDialog(this, "Tidak ada voucher yang dipilih untuk diedit.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        String kode = txtKode.getText().trim();
        String nominalStr = txtNominal.getText().trim().replaceAll("[^0-9-]", "");
        String tanggal = txtTanggal.getText().trim();
        Integer idGuru = (Integer) txtGuru.getClientProperty("id_guru");
        if (kode.isEmpty()) { JOptionPane.showMessageDialog(this, "Kode voucher harus diisi.", "Validasi", JOptionPane.WARNING_MESSAGE); return; }
        BigDecimal bal = BigDecimal.ZERO;
        try { if (!nominalStr.isEmpty()) bal = new BigDecimal(nominalStr); } catch (Exception ex) { bal = BigDecimal.ZERO; }

        Voucher v = new Voucher();
        v.setIdVoucher(VoucherContext.editingId);
        v.setKode(kode);
        v.setBulan(tanggal.isEmpty()?null:tanggal);
        v.setIdGuru(idGuru==null?0:idGuru);
        v.setCurrentBalance(bal);

        try {
            dao.update(v);
            JOptionPane.showMessageDialog(this, "Voucher berhasil diupdate.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            VoucherContext.editingId = null;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof uiresponsive.Mainmenu) ((uiresponsive.Mainmenu) frame).showDataVoucher();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan perubahan:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // RoundedTextField & RoundedButton same visuals as tambahdatavoucher
    class RoundedTextField extends JTextField {
        private int radius = 15;
        public RoundedTextField(int size) { super(size); setOpaque(false); setBorder(BorderFactory.createEmptyBorder(6,12,6,12)); }
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius); g2.setColor(new Color(200,200,200)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius); super.paintComponent(g); g2.dispose(); }
    }
    class RoundedButton extends JButton { private final Color backgroundColor; private final Color textColor; private int radius = 25; public RoundedButton(String text, Color bg, Color fg) { super(text); backgroundColor = bg; textColor = fg; setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false); setFont(new Font("Segoe UI", Font.BOLD, 15)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); } @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); for (int i = 0; i < 6; i++) { g2.setColor(new Color(0,0,0,10 - i)); g2.fillRoundRect(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, radius, radius); } g2.setColor(backgroundColor); g2.fillRoundRect(0,0,getWidth()-2,getHeight()-2,radius,radius); g2.setColor(textColor); FontMetrics fm = g2.getFontMetrics(); int textX = (getWidth() - fm.stringWidth(getText()))/2; int textY = (getHeight() + fm.getAscent())/2 -3; g2.drawString(getText(), textX, textY); g2.dispose(); } }

    // PilihGuruFrame same as tambahdatavoucher (uses GuruDAO)
    class PilihGuruFrame extends JFrame {
        public PilihGuruFrame(final JTextField targetField) {
            setTitle("Pilih Guru");
            setSize(600,450); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JPanel panel = new JPanel(new BorderLayout(10,10)); panel.setBorder(new EmptyBorder(15,15,15,15)); panel.setBackground(new Color(250,250,250));
            JPanel searchPanel = new JPanel(new BorderLayout(8,8)); searchPanel.setOpaque(false);
            JTextField txtSearch = new JTextField(); JButton btnSearch = new JButton("Cari");
            styleButton(btnSearch, new Color(255,140,0));
            searchPanel.add(new JLabel("Cari Guru:"), BorderLayout.WEST); searchPanel.add(txtSearch, BorderLayout.CENTER); searchPanel.add(btnSearch, BorderLayout.EAST);
            panel.add(searchPanel, BorderLayout.NORTH);
            String[] cols = {"ID Guru","Nama Guru","Jabatan"};
            javax.swing.table.DefaultTableModel m = new javax.swing.table.DefaultTableModel(cols,0) { @Override public boolean isCellEditable(int r,int c){return false;} };
            JTable tabel = new JTable(m); tabel.setRowHeight(26); tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JScrollPane scroll = new JScrollPane(tabel); panel.add(scroll, BorderLayout.CENTER);
            try { GuruDAO gdao = new GuruDAO(); java.util.List<Guru> list = gdao.findAll(); if (list!=null) for (Guru g: list) m.addRow(new Object[]{g.getIdGuru(), g.getNamaGuru(), g.getJabatan()}); } catch (Exception ex) {}
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10)); btnPanel.setOpaque(false);
            JButton btnPilih = new JButton("Pilih"); JButton btnCancel = new JButton("Batal"); styleButton(btnPilih, new Color(0,180,0)); styleButton(btnCancel, new Color(220,0,0));
            btnPanel.add(btnPilih); btnPanel.add(btnCancel); panel.add(btnPanel, BorderLayout.SOUTH);
            btnCancel.addActionListener(e -> dispose());
            btnPilih.addActionListener(e -> {
                int row = tabel.getSelectedRow();
                if (row != -1) {
                    Integer id = null;
                    try { id = Integer.parseInt(String.valueOf(m.getValueAt(row,0))); } catch (Exception ex) {}
                    String nama = String.valueOf(m.getValueAt(row,1));
                    targetField.setText(nama);
                    if (id != null) targetField.putClientProperty("id_guru", id);
                    dispose();
                } else JOptionPane.showMessageDialog(this, "Pilih dulu gurunya!");
            });
            add(panel); setVisible(true);
        }
        private void styleButton(JButton btn, Color color) { btn.setBackground(color); btn.setForeground(Color.WHITE); btn.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14)); btn.setFocusPainted(false); btn.setPreferredSize(new Dimension(120,40)); }
    }
}
