package is216.petshop.dao;

import is216.petshop.Product.Product;
import is216.petshop.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    public void ensureTablesExist() {
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS HOA_DON_NHAP_HANG (" +
            "  MANDNH   INT AUTO_INCREMENT PRIMARY KEY," +
            "  NGAYNHAP DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "  TONGTIEN DECIMAL(18,2) DEFAULT 0," +
            "  MANV     INT" +
            ")",
            "CREATE TABLE IF NOT EXISTS CHI_TIET_HOA_DON_NHAP_HANG (" +
            "  MANDNH    INT NOT NULL," +
            "  MASANPHAM INT NOT NULL," +
            "  SL        INT DEFAULT 0," +
            "  GIANHAP   DECIMAL(18,2) DEFAULT 0," +
            "  PRIMARY KEY (MANDNH, MASANPHAM)" +
            ")"
        };
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            if (conn != null) {
                for (String sql : ddl) {
                    st.execute(sql);
                }
                
                // 3. Fix negative stock levels in database
                try {
                    st.execute("UPDATE SAN_PHAM SET SL = 0 WHERE SL < 0");
                } catch (Exception ex) {
                    try {
                        st.execute("UPDATE san_pham SET SL = 0 WHERE SL < 0");
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT MASANPHAM, TENSANPHAM, GIANIEMYET, SL, DONVITINH, THUONGHIEU FROM SAN_PHAM WHERE COTHEMUA = 1 ORDER BY MASANPHAM DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("MASANPHAM"));
                p.setName(rs.getString("TENSANPHAM"));
                p.setPrice(rs.getLong("GIANIEMYET"));
                int qty = rs.getInt("SL");
                p.setStock(qty < 0 ? 0 : qty);
                
                // We're dynamically reusing product fields. Brand is set to Category, Unit is set as imageUrl for display mapping
                p.setCategory(rs.getString("THUONGHIEU") != null ? rs.getString("THUONGHIEU") : "—");
                String unit = rs.getString("DONVITINH") != null ? rs.getString("DONVITINH") : "Cái";
                p.setCategory(rs.getString("THUONGHIEU") != null ? rs.getString("THUONGHIEU") : "—");
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertProduct(String name, long price, String brand, String unit) {
        String sql = "INSERT INTO SAN_PHAM (TENSANPHAM, GIANIEMYET, SL, DONVITINH, THUONGHIEU, COTHEMUA, COTHEBAN, POS) " +
                     "VALUES (?, ?, 0, ?, ?, 1, 1, 1)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setLong(2, price);
            ps.setString(3, unit.isEmpty() ? "Cái" : unit);
            ps.setString(4, brand.isEmpty() ? "PetShop" : brand);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createRestockOrder(int employeeId, long totalAmount, List<RestockItem> items) {
        String sqlHeader = "INSERT INTO HOA_DON_NHAP_HANG (TONGTIEN, MANV) VALUES (?, ?)";
        String sqlDetail = "INSERT INTO CHI_TIET_HOA_DON_NHAP_HANG (MANDNH, MASANPHAM, SL, GIANHAP) VALUES (?, ?, ?, ?)";
        String sqlUpdateStock = "UPDATE SAN_PHAM SET SL = SL + ? WHERE MASANPHAM = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);

            int newId = -1;
            try (PreparedStatement ps = conn.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDouble(1, totalAmount);
                ps.setInt(2, employeeId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        newId = keys.getInt(1);
                    }
                }
            }

            if (newId == -1) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                 PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateStock)) {
                for (RestockItem item : items) {
                    // 1. Insert detail line
                    psDetail.setInt(1, newId);
                    psDetail.setInt(2, item.productId);
                    psDetail.setInt(3, item.quantity);
                    psDetail.setDouble(4, item.costPrice);
                    psDetail.addBatch();

                    // 2. Update stock level
                    psUpdate.setInt(1, item.quantity);
                    psUpdate.setInt(2, item.productId);
                    psUpdate.addBatch();
                }
                psDetail.executeBatch();
                psUpdate.executeBatch();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception rollbackEx) { rollbackEx.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception closeEx) { closeEx.printStackTrace(); }
            }
        }
    }

    public static class RestockItem {
        public int productId;
        public String productName;
        public int quantity;
        public double costPrice;

        public RestockItem(int productId, String productName, int quantity, double costPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.costPrice = costPrice;
        }
    }
}
