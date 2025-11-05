package uiresponsive; // sesuaikan dengan package kamu

import Helper.DatabaseHelper;
import com.formdev.flatlaf.FlatLightLaf;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import pengguna.Pengguna;


public class Main {
    public static void main(String[] args) {
        try {
            DatabaseHelper.initDatabase();
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
             e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,"Gagal inisialisasi DB: "+e.getMessage());
            System.exit(1);
        }
                SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            if (login.isSucceeded()) {
                Pengguna user = login.getLoggedUser();
                try {
                    // buka MainFrame dengan informasi user
                    // Mainmenu main = new Mainmenu(user);
                    new Mainmenu().setVisible(true);
                } catch (SQLException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.exit(0);
            }
        });
        // ganti "Mainmenu" dengan nama JFrame Form kamu
       // new Mainmenu().setVisible(true);
    }
}
//
//package testsqlite;
//
//import Pengguna.Pengguna;
//import javax.swing.SwingUtilities;
//
//public class testsqlite {
//    public static void main(String[] args) {
//        try {
//            DatabaseHelper.initDatabase();
//            
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            javax.swing.JOptionPane.showMessageDialog(null,"Gagal inisialisasi DB: "+ex.getMessage());
//            System.exit(1);
//        }
//
//        SwingUtilities.invokeLater(() -> {
//            LoginDialog login = new LoginDialog(null);
//            login.setVisible(true);
//
//            if (login.isSucceeded()) {
//                Pengguna user = login.getLoggedUser();
//                // buka MainFrame dengan informasi user
//                MainFrame main = new MainFrame(user);
//                main.setVisible(true);
//            } else {
//                System.exit(0);
//            }
//        });
//        
//    }
//}
//
