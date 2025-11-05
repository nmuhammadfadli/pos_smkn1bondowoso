package uiresponsive;

import Helper.DatabaseHelper;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pengguna.Pengguna;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

/**
 * UIResponsive - entry point yang sekarang melakukan init DB dan menampilkan login
 */
public class UIResponsive {

    // simpan user login global (bisa diakses dari mana saja)
    public static Pengguna currentUser;

    public static void main(String[] args) {
        try {
            DatabaseHelper.initDatabase();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Gagal inisialisasi DB: " + ex.getMessage(),
                    "Error Inisialisasi DB",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            if (login.isSucceeded()) {
                currentUser = login.getLoggedUser();
                try {
                    new Mainmenu().setVisible(true);
                } catch (SQLException ex) {
                    Logger.getLogger(UIResponsive.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.exit(0);
            }
        });
        
    }
    
}
