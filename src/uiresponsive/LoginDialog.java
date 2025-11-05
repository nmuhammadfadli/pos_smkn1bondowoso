package uiresponsive;

import pengguna.Pengguna;
import pengguna.PenggunaDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class LoginDialog extends JDialog {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblErrorMessage;
    private boolean succeeded = false;
    private Pengguna loggedUser = null;

    // --- [UBAH] Warna Desain ---
    private Color colorBackground = new Color(235, 28, 44);     // Latar form (Merah dari user)
    private Color colorPrimary = Color.WHITE;                   // Latar logo (Putih)
    private Color colorText = Color.WHITE;                      // Teks di form (putih di atas merah)
    private Color colorError = new Color(255, 180, 0);          // Kuning/Oranye untuk error
    private Color colorButton = new Color(235, 28, 44);         // Merah (untuk teks tombol Login)
    
    private Font fontTitle = new Font("SansSerif", Font.BOLD, 28);
    private Font fontLabel = new Font("SansSerif", Font.PLAIN, 14);
    private Font fontInput = new Font("SansSerif", Font.PLAIN, 16);
    private Font fontButton = new Font("SansSerif", Font.BOLD, 16);

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        initUI(parent);
    }

    private void initUI(Frame parent) {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        
        JPanel formPanel = createFormPanel();
        JPanel logoPanel = createLogoPanel();

        mainPanel.add(formPanel);
        mainPanel.add(logoPanel);

        getContentPane().add(mainPanel);
        setTitle("Login");
        
        setPreferredSize(new Dimension(800, 550)); 
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(colorBackground);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 40, 10, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel lblTitle = new JLabel("Member Login");
        lblTitle.setFont(fontTitle);
        lblTitle.setForeground(colorText);
        lblTitle.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.insets = new Insets(20, 40, 20, 40);
        panel.add(lblTitle, gbc);

        gbc.insets = new Insets(10, 40, 0, 40);
        
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(fontLabel);
        lblUsername.setForeground(colorText);
        panel.add(lblUsername, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 40, 10, 40);

        JPanel usernamePanel = new JPanel(new BorderLayout());
        usernamePanel.setOpaque(false);
        usernamePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, colorText));
        
        txtUsername = new JTextField(20);
        styleTextField(txtUsername);
        usernamePanel.add(txtUsername, BorderLayout.CENTER);
        
        panel.add(usernamePanel, gbc); 

        gbc.gridy++;
        gbc.insets = new Insets(10, 40, 0, 40);
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(fontLabel);
        lblPassword.setForeground(colorText);
        panel.add(lblPassword, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 40, 10, 40);

        txtPassword = new JPasswordField(20);
        styleTextField(txtPassword); 
        
        JToggleButton btnShowHide = new JToggleButton();
        
        String iconShowFile = "eye_open.png";
        String iconHideFile = "eye_closed.png";
        int iconSize = 20; 

        btnShowHide.setIcon(loadIcon(iconHideFile, iconSize));
        btnShowHide.setSelectedIcon(loadIcon(iconShowFile, iconSize));
        styleShowHideButton(btnShowHide);
        
        char defaultEchoChar = txtPassword.getEchoChar();
        btnShowHide.addActionListener(e -> {
            if (btnShowHide.isSelected()) txtPassword.setEchoChar((char) 0);
            else txtPassword.setEchoChar(defaultEchoChar);
        });

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setOpaque(false);
        passwordPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, colorText));
        txtPassword.setBorder(null);

        passwordPanel.add(txtPassword, BorderLayout.CENTER);
        passwordPanel.add(btnShowHide, BorderLayout.EAST);
        panel.add(passwordPanel, gbc);

        gbc.gridy++;
        lblErrorMessage = new JLabel("<html>&nbsp;</html>");
        lblErrorMessage.setFont(fontLabel);
        lblErrorMessage.setForeground(colorError);
        lblErrorMessage.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblErrorMessage, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 40, 20, 40);
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnLogin = new JButton("LOGIN");
        styleButtonPrimary(btnLogin);

        JButton btnCancel = new JButton("Batal");
        styleButtonSecondary(btnCancel);

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnLogin);
        panel.add(buttonPanel, gbc);

        btnLogin.addActionListener(e -> doLogin());
        btnCancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnLogin);

        return panel;
    }

private JPanel createLogoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(colorPrimary);

        GridBagConstraints gbcLogo = new GridBagConstraints();
        gbcLogo.gridx = 0;
        gbcLogo.gridy = 0; 
        gbcLogo.insets = new Insets(10, 10, 10, 10);

        String logoFileName = "logo1.png";
        JLabel lblLogo = new JLabel();
        lblLogo.setIcon(loadLogo(logoFileName));
        
        panel.add(lblLogo, gbcLogo);

        gbcLogo.gridy = 1;
        JLabel lblTeksLogo = new JLabel("SMKN 1 BONDOWOSO");
        
        lblTeksLogo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTeksLogo.setForeground(Color.BLACK);
        
        panel.add(lblTeksLogo, gbcLogo);

        return panel;
    }

    private ImageIcon loadIcon(String fileName, int size) {
        try {
            java.net.URL imgURL = getClass().getResource("/icon/" + fileName);
            if (imgURL == null) {
                System.err.println("Icon tidak ditemukan: /icon/" + fileName);
                return createPlaceholderIcon(size);
            }
            
            ImageIcon originalIcon = new ImageIcon(imgURL);
            Image img = originalIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
            
        } catch (Exception e) {
            e.printStackTrace();
            return createPlaceholderIcon(size);
        }
    }
    
    private ImageIcon loadLogo(String fileName) {
        try {
            java.net.URL imgURL = getClass().getResource("/icon/" + fileName);
            if (imgURL == null) {
                System.err.println("Logo tidak ditemukan: /icon/" + fileName);
                return createPlaceholderIcon(300);
            }
            return new ImageIcon(imgURL);
        } catch (Exception e) {
            e.printStackTrace();
            return createPlaceholderIcon(300);
        }
    }
    
    private ImageIcon createPlaceholderIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, size, size);
        g2d.setColor(Color.WHITE);
        g2d.drawString("LOGO GAGAL DIMUAT", 50, size / 2);
        g2d.dispose();
        return new ImageIcon(img);
    }

    private void styleTextField(JTextField field) {
        field.setFont(fontInput);
        field.setForeground(colorText);
        field.setCaretColor(colorText);
        field.setBorder(null);
        field.setOpaque(false);
    }

    private void styleShowHideButton(JToggleButton btn) {
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI());
    }

    private void styleButtonPrimary(JButton btn) {
        btn.setFont(fontButton);
        btn.setForeground(colorButton); 
        btn.setBackground(Color.WHITE);  
        btn.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color hoverBg = new Color(240, 240, 240);
        Color normalBg = Color.WHITE;

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hoverBg);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(normalBg);
            }
        });
    }

    private void styleButtonSecondary(JButton btn) {
        btn.setFont(fontButton);
        btn.setForeground(colorText); // Teks putih
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createLineBorder(colorText, 1)); // Border putih
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color hoverColor = Color.LIGHT_GRAY;
        Color normalColor = colorText;

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setForeground(hoverColor);
                btn.setBorder(BorderFactory.createLineBorder(hoverColor, 1));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setForeground(normalColor);
                btn.setBorder(BorderFactory.createLineBorder(normalColor, 1));
            }
        });
    }

    private void doLogin() {
        lblErrorMessage.setText("<html>&nbsp;</html>");
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty()) {
            showError("Username tidak boleh kosong");
            return;
        }
        if (password.isEmpty()) {
            showError("Password tidak boleh kosong");
            return;
        }

        try {
            PenggunaDAO dao = new PenggunaDAO();
            Pengguna p = dao.findByUsername(username);
            if (p == null) {
                showError("Username tidak ditemukan");
                return;
            }

            String stored = p.getPassword() == null ? "" : p.getPassword();
            if (!stored.equals(password)) {
                showError("Password salah");
                return;
            }

            succeeded = true;
            loggedUser = p;
            dispose();
        } catch (Exception ex) {
            showError("Gagal terhubung ke database.");
            ex.printStackTrace();
        }
    }
    
    private void showError(String message) {
        lblErrorMessage.setText(message);
    }

    public boolean isSucceeded() { return succeeded; }
    public Pengguna getLoggedUser() { return loggedUser; }
}