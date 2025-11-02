package transaksi_penjualan;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class SaleItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idDetailBarang;
    private int qty;
    private BigDecimal price;

    // no-arg constructor (for code that wants to set fields later)
    public SaleItem() {
        this.idDetailBarang = 0;
        this.qty = 0;
        this.price = BigDecimal.ZERO;
    }

    // convenience constructor
    public SaleItem(int idDetailBarang, int qty, BigDecimal price) {
        this.idDetailBarang = idDetailBarang;
        this.qty = qty;
        this.price = price == null ? BigDecimal.ZERO : price;
    }

    // getters
    public int getIdDetailBarang() { return idDetailBarang; }
    public int getQty() { return qty; }
    public BigDecimal getPrice() { return price; }

    // setters (used by callers that build object then set fields)
    public void setIdDetailBarang(int idDetailBarang) { this.idDetailBarang = idDetailBarang; }
    public void setQty(int qty) { this.qty = qty; }
    public void setPrice(BigDecimal price) { this.price = price == null ? BigDecimal.ZERO : price; }

    @Override
    public String toString() {
        return "SaleItem{" +
                "idDetailBarang=" + idDetailBarang +
                ", qty=" + qty +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SaleItem)) return false;
        SaleItem saleItem = (SaleItem) o;
        return idDetailBarang == saleItem.idDetailBarang &&
                qty == saleItem.qty &&
                Objects.equals(price, saleItem.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDetailBarang, qty, price);
    }
}
