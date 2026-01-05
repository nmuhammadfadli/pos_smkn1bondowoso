package transaksi_penjualan;

import barang.DetailBarangDAO;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Helper.DatabaseHelper;

/**
 * TransactionDAO: proses penjualan dan query riwayat transaksi
 * Kompatibel NetBeans 8.2 (tanpa text block)
 */
public class TransactionDAO {
    private DetailBarangDAO detailDao = new DetailBarangDAO();

    /**
     * Proses penjualan secara atomik
     */
 // ubah signature: return long (idTransaksi)
public long processSale(List<SaleItem> items, Integer voucherId, BigDecimal cashPaid,
                        String kodeTransCandidate, String idPengguna, String paymentMethodParam) throws Exception {

    if (items == null || items.isEmpty()) {
        throw new IllegalArgumentException("Tidak ada item transaksi.");
    }
    if (cashPaid == null) cashPaid = BigDecimal.ZERO;

    // validasi awal
    for (SaleItem it : items) {
        if (it == null) throw new IllegalArgumentException("Item transaksi null.");
        if (it.getQty() <= 0) throw new IllegalArgumentException("Qty harus > 0 untuk idDetailBarang=" + it.getIdDetailBarang());
        if (it.getPrice() == null) throw new IllegalArgumentException("Harga null untuk idDetailBarang=" + it.getIdDetailBarang());
    }

    try (Connection conn = DatabaseHelper.getConnection()) {
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
        } catch (Throwable ignore) {}

        conn.setAutoCommit(false);
        try {
            // Hitung total harga
            BigDecimal totalHarga = BigDecimal.ZERO;
            for (SaleItem it : items) {
                totalHarga = totalHarga.add(it.getPrice().multiply(BigDecimal.valueOf(it.getQty())));
            }

            // VOUCHER (sama seperti sebelumnya)
            BigDecimal usedFromVoucher = BigDecimal.ZERO;
            BigDecimal voucherBalance = BigDecimal.ZERO;
            if (voucherId != null) {
                String sel = "SELECT current_balance FROM kode_voucher WHERE id_voucher = ?";
                try (PreparedStatement ps = conn.prepareStatement(sel)) {
                    ps.setInt(1, voucherId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String s = rs.getString("current_balance");
                            voucherBalance = (s == null || s.trim().isEmpty()) ? BigDecimal.ZERO : new BigDecimal(s);
                        } else {
                            throw new SQLException("Voucher dengan id " + voucherId + " tidak ditemukan.");
                        }
                    }
                }

                usedFromVoucher = voucherBalance.min(totalHarga);
                if (usedFromVoucher.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newBal = voucherBalance.subtract(usedFromVoucher);
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE kode_voucher SET current_balance = ? WHERE id_voucher = ?")) {
                        ps.setString(1, newBal.toPlainString());
                        ps.setInt(2, voucherId);
                        ps.executeUpdate();
                    }
                }
            }

            BigDecimal sisa = totalHarga.subtract(usedFromVoucher);
            BigDecimal totalBayar = usedFromVoucher.add(cashPaid);
            BigDecimal kembalian = BigDecimal.ZERO;
            if (cashPaid.compareTo(sisa) >= 0) {
                kembalian = cashPaid.subtract(sisa);
                sisa = BigDecimal.ZERO;
            } else {
                sisa = sisa.subtract(cashPaid);
            }

            String computedPaymentMethod = "CASH";
            if (usedFromVoucher.compareTo(BigDecimal.ZERO) > 0 && cashPaid.compareTo(BigDecimal.ZERO) == 0)
                computedPaymentMethod = "VOUCHER";
            else if (usedFromVoucher.compareTo(BigDecimal.ZERO) > 0 && cashPaid.compareTo(BigDecimal.ZERO) > 0)
                computedPaymentMethod = "MIX";
            else if (usedFromVoucher.compareTo(BigDecimal.ZERO) == 0 && cashPaid.compareTo(BigDecimal.ZERO) == 0)
                computedPaymentMethod = "CREDIT";

            String finalPaymentMethod = (paymentMethodParam != null && !paymentMethodParam.trim().isEmpty())
                    ? paymentMethodParam.trim().toUpperCase()
                    : computedPaymentMethod;

            // Jika kode kosong, generate KODE DI SINI (dengan koneksi yang sama) supaya deterministic
            String kodeToUse = generateKodeTransaksiIfEmpty(conn, kodeTransCandidate);

            // set tgl_transaksi explicit (yyyy-MM-dd)
            String tglTransaksi = java.time.LocalDate.now().toString();

            // INSERT header (sertakan tgl_transaksi)
            long idTrans;
            String insTrans = "INSERT INTO transaksi_penjualan " +
                    "(kode_transaksi, tgl_transaksi, total_harga, total_bayar, kembalian, payment_method, id_voucher, id_pengguna) " +
                    "VALUES (?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(insTrans, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, kodeToUse);
                ps.setString(2, tglTransaksi); // important: set tanggal
                ps.setString(3, totalHarga.toPlainString());
                ps.setString(4, totalBayar.toPlainString());
                ps.setString(5, kembalian.toPlainString());
                ps.setString(6, finalPaymentMethod);
                if (voucherId != null) ps.setInt(7, voucherId);
                else ps.setNull(7, Types.INTEGER);
                if (idPengguna != null) ps.setString(8, idPengguna);
                else ps.setNull(8, Types.VARCHAR);
                ps.executeUpdate();

                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) idTrans = gk.getLong(1);
                    else throw new SQLException("Gagal mendapatkan id transaksi.");
                }
            }

            // INSERT detail + update stok (pakai conn yg sama)
            String insDetail = "INSERT INTO detail_penjualan " +
                    "(id_transaksi, id_detail_barang, jumlah_barang, harga_unit, subtotal) VALUES (?,?,?,?,?)";
            try (PreparedStatement psIns = conn.prepareStatement(insDetail)) {
                for (SaleItem it : items) {
                    detailDao.decreaseStock(conn, it.getIdDetailBarang(), it.getQty());

                    BigDecimal subtotal = it.getPrice().multiply(BigDecimal.valueOf(it.getQty()));
                    psIns.setLong(1, idTrans);
                    psIns.setInt(2, it.getIdDetailBarang());
                    psIns.setInt(3, it.getQty());
                    psIns.setString(4, it.getPrice().toPlainString());
                    psIns.setString(5, subtotal.toPlainString());
                    psIns.executeUpdate();
                }
            }

            // voucher_usage
            if (usedFromVoucher.compareTo(BigDecimal.ZERO) > 0 && voucherId != null) {
                String insUsage = "INSERT INTO voucher_usage (id_voucher, id_transaksi, used_amount) VALUES (?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(insUsage)) {
                    ps.setInt(1, voucherId);
                    ps.setLong(2, idTrans);
                    ps.setString(3, usedFromVoucher.toPlainString());
                    ps.executeUpdate();
                }
            }

            // receivable (piutang)
            if (sisa.compareTo(BigDecimal.ZERO) > 0 && !"DONASI".equals(finalPaymentMethod)) {
                String insReceivable = "INSERT INTO receivable " +
                        "(id_transaksi, amount_total, amount_paid, amount_outstanding, status) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(insReceivable)) {
                    ps.setLong(1, idTrans);
                    ps.setString(2, totalHarga.toPlainString());
                    ps.setString(3, totalBayar.toPlainString());
                    ps.setString(4, sisa.toPlainString());
                    ps.setString(5, "OPEN");
                    ps.executeUpdate();
                }
            }

            conn.commit();
            // Kembalikan idTrans (pemanggil dapat memakai id/kode untuk cetak/refresh)
            return idTrans;
        } catch (Exception ex) {
            try { conn.rollback(); } catch (Throwable t) {}
            throw ex;
        } finally {
            try { conn.setAutoCommit(true); } catch (Throwable ignore) {}
        }
    }
}
private String generateKodeTransaksiIfEmpty(Connection conn, String candidate) throws SQLException {
    if (candidate != null && !candidate.trim().isEmpty()) return candidate.trim();

    String prefix = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
    String sql = "SELECT kode_transaksi FROM transaksi_penjualan WHERE kode_transaksi LIKE ? ORDER BY id_transaksi DESC LIMIT 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, prefix + "%");
        try (ResultSet rs = ps.executeQuery()) {
            int next = 1;
            if (rs.next()) {
                String last = rs.getString(1);
                if (last != null && last.length() > prefix.length()) {
                    try {
                        next = Integer.parseInt(last.substring(prefix.length())) + 1;
                    } catch (NumberFormatException ignored) {}
                }
            }
            return prefix + String.format("%04d", next);
        }
    }
}


  // Overload untuk kompatibilitas: pemanggil lama tetap bisa memakai signature lama.
public void processSale(List<SaleItem> items, Integer voucherId, BigDecimal cashPaid,
                        String kodeTrans, String idPengguna) throws Exception {
    processSale(items, voucherId, cashPaid, kodeTrans, idPengguna, null);
}


    /**
     * Ambil semua transaksi (header)
     */
    public List<TransactionRecord> findAllTransactions() throws SQLException {
        List<TransactionRecord> list = new ArrayList<>();
        String sql = "SELECT " +
                "t.id_transaksi, t.kode_transaksi, t.tgl_transaksi, " +
                "t.total_harga, t.total_bayar, t.kembalian, t.payment_method, " +
                "t.id_voucher, t.id_pengguna, " +
                "p.nama_lengkap AS nama_kasir, g.nama_guru AS nama_guru " +
                "FROM transaksi_penjualan t " +
                "LEFT JOIN data_pengguna p ON t.id_pengguna = p.id_pengguna " +
                "LEFT JOIN kode_voucher v ON t.id_voucher = v.id_voucher " +
                "LEFT JOIN data_guru g ON v.id_guru = g.id_guru " +
                "ORDER BY t.tgl_transaksi DESC, t.id_transaksi DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TransactionRecord tr = new TransactionRecord(
                        rs.getLong("id_transaksi"),
                        rs.getString("kode_transaksi"),
                        rs.getString("tgl_transaksi"),
                        new BigDecimal(rs.getString("total_harga") == null ? "0" : rs.getString("total_harga")),
                        new BigDecimal(rs.getString("total_bayar") == null ? "0" : rs.getString("total_bayar")),
                        new BigDecimal(rs.getString("kembalian") == null ? "0" : rs.getString("kembalian")),
                        rs.getString("payment_method"),
                        rs.wasNull() ? null : rs.getInt("id_voucher"),
                        rs.getString("id_pengguna"),
                        rs.getString("nama_kasir"),
                        rs.getString("nama_guru")
                );
                list.add(tr);
            }
        }
        return list;
    }

    /**
     * Ambil detail item berdasarkan id_transaksi
     */
  // transaksi_penjualan/TransactionDAO.java
public List<TransactionItem> findItemsByTransaction(long idTransaksi) throws SQLException {
    List<TransactionItem> result = new ArrayList<>();
    String sql =
        "SELECT dp.id_detail_penjualan, dp.id_detail_barang, dp.jumlah_barang, dp.harga_unit, dp.subtotal, " +
        "       db.id_detail_pembelian, b.nama AS nama_barang, p.harga_beli " +
        "FROM detail_penjualan dp " +
        "LEFT JOIN detail_barang db ON dp.id_detail_barang = db.id_detail_barang " +
        "LEFT JOIN barang b ON db.id_barang = b.id " +
        "LEFT JOIN detail_pembelian p ON db.id_detail_pembelian = p.id_detail_pembelian " +
        "WHERE dp.id_transaksi = ?";

    try (Connection conn = DatabaseHelper.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, idTransaksi);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TransactionItem it = new TransactionItem();
                it.setIdDetailPenjualan(rs.getInt("id_detail_penjualan"));
                it.setIdDetailBarang(rs.getInt("id_detail_barang"));
                it.setNamaBarang(rs.getString("nama_barang"));
                it.setJumlahBarang(rs.getInt("jumlah_barang"));

                // parsing BigDecimal aman
                it.setHargaUnit(parseBigDecimalSafe(rs.getString("harga_unit")));
                it.setSubtotal(parseBigDecimalSafe(rs.getString("subtotal")));
                it.setHargaBeli(parseBigDecimalSafe(rs.getString("harga_beli"))); // bisa null -> ZERO

                result.add(it);
            }
        }
    }
    return result;
}

private static BigDecimal parseBigDecimalSafe(String s) {
    if (s == null) return BigDecimal.ZERO;
    s = s.trim();
    if (s.isEmpty()) return BigDecimal.ZERO;
    try { return new BigDecimal(s); } catch (Exception ex) { return BigDecimal.ZERO; }
}
}