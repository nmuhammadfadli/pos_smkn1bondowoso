package uiresponsive;

import Helper.DatabaseHelper;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import pengguna.Pengguna;

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

    /**
     * Utility untuk mengetes menjalankan/preview Jasper report menggunakan koneksi JDBC (SQLite).
     * Panggil UIResponsive.testPrintReportFromJdbc("kode_transaksi_anda") dari action/button.
     *
     * Pastikan:
     * - file JRXML atau .jasper berada di resources/report/nota_minimarket.jrxml (atau .jasper)
     * - sqlite-jdbc & jasperreports ada di classpath runtime
     */
    public static void testPrintReportFromJdbc(String kodeTransaksi) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                throw new RuntimeException("Connection returned null! Periksa DatabaseHelper.getConnection()");
            }

            // coba cari .jrxml dulu, kalau tidak ada coba .jasper
            InputStream jrxmlStream = UIResponsive.class.getResourceAsStream("/report/reportPenjualan.jrxml");
            if (jrxmlStream != null) {
                // compile jrxml ke JasperReport (berguna saat testing)
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
                Map<String, Object> params = buildParamMap(kodeTransaksi);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                JasperViewer.viewReport(jasperPrint, false);
                return;
            }

            // coba file .jasper (sudah terkompilasi)
            InputStream jasperStream = UIResponsive.class.getResourceAsStream("/report/reportPenjualan.jasper");
            if (jasperStream != null) {
                Map<String, Object> params = buildParamMap(kodeTransaksi);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperStream, params, conn);
                JasperViewer.viewReport(jasperPrint, false);
                return;
            }

            // kalau tidak ditemukan
            throw new RuntimeException("File report tidak ditemukan di resources/report/ (reportPenjualan.jrxml atau .jasper)");
        } catch (JRException jre) {
            jre.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error Jasper: " + jre.getMessage() + "\nLihat console untuk stacktrace.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saat generate report: " + ex.getMessage() + "\nLihat console untuk stacktrace.");
        } finally {
            try {
                if (conn != null && !conn.isClosed()) conn.close();
            } catch (Exception e) { /* ignore */ }
        }
    }

    private static Map<String, Object> buildParamMap(String kode) {
        Map<String, Object> p = new HashMap<>();
        p.put("kode_transaksi", kode);
        return p;
    }
}
