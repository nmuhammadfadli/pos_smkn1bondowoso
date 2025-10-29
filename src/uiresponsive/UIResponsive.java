package uiresponsive;

import Helper.DatabaseHelper;
import pengguna.Pengguna;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

/**
 * UIResponsive - entry point yang sekarang melakukan init DB dan menampilkan login
 */
public class UIResponsive {

    public static void main(String[] args) {
        // 1) Inisialisasi database (jika gagal, tampilkan pesan dan keluar)
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

        // 2) Jalankan UI di EDT
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            if (login.isSucceeded()) {
                Pengguna user = login.getLoggedUser();
                // buka MainFrame (sesuaikan konstruktor jika MainFrame memerlukan argumen lain)
              
                new Mainmenu().setVisible(true);
            } else {
                // jika login gagal/ditutup, keluar aplikasi
                System.exit(0);
            }
        });
    }

}
