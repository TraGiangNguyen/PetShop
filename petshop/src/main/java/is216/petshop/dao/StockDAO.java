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
            "  NHACUNGCAP VARCHAR(255) DEFAULT 'Nhập lẻ tự do'," +
            "  MANV     INT" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS CHI_TIET_HOA_DON_NHAP_HANG (" +
            "  MANDNH    INT NOT NULL," +
            "  MASANPHAM INT NOT NULL," +
            "  SL        INT DEFAULT 0," +
            "  GIANHAP   DECIMAL(18,2) DEFAULT 0," +
            "  PRIMARY KEY (MANDNH, MASANPHAM)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS DON_HANG_MUA (" +
            "  MADONMUA  INT AUTO_INCREMENT PRIMARY KEY," +
            "  NGAYLAP   DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "  NHACUNGCAP VARCHAR(255) DEFAULT 'Công ty TNHH Pet Vina'," +
            "  TONGTIEN  DECIMAL(18,2) DEFAULT 0," +
            "  TRANGTHAI VARCHAR(50) DEFAULT 'Chờ nhập kho'" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS CHI_TIET_DON_HANG_MUA (" +
            "  MADONMUA  INT NOT NULL," +
            "  MASANPHAM INT NOT NULL," +
            "  SL        INT DEFAULT 0," +
            "  GIANHAP   DECIMAL(18,2) DEFAULT 0," +
            "  PRIMARY KEY (MADONMUA, MASANPHAM)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS PHIEU_KIEM_KE (" +
            "  MAPHIEUKK  INT AUTO_INCREMENT PRIMARY KEY," +
            "  NGAYLAP    DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "  MANV       INT," +
            "  GHICHU     VARCHAR(500)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS CHI_TIET_PHIEU_KIEM_KE (" +
            "  MAPHIEUKK  INT NOT NULL," +
            "  MASANPHAM  INT NOT NULL," +
            "  SLTHUCTE   INT DEFAULT 0," +
            "  SLLECH     INT DEFAULT 0," +
            "  LYDO       VARCHAR(255)," +
            "  PRIMARY KEY (MAPHIEUKK, MASANPHAM)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        };
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            if (conn != null) {
                // Check if HOA_DON_NHAP_HANG and CHI_TIET_HOA_DON_NHAP_HANG have the new schema (MANDNH)
                boolean hasNewSchema = false;
                try {
                    try (ResultSet rs1 = st.executeQuery("SELECT MANDNH FROM HOA_DON_NHAP_HANG LIMIT 1");
                         ResultSet rs2 = st.executeQuery("SELECT MANDNH FROM CHI_TIET_HOA_DON_NHAP_HANG LIMIT 1")) {
                        hasNewSchema = true;
                    }
                } catch (Exception ex) {
                    try {
                        try (ResultSet rs1 = st.executeQuery("SELECT mandnh FROM hoa_don_nhap_hang LIMIT 1");
                             ResultSet rs2 = st.executeQuery("SELECT mandnh FROM chi_tiet_hoa_don_nhap_hang LIMIT 1")) {
                            hasNewSchema = true;
                        }
                    } catch (Exception e) {}
                }

                if (!hasNewSchema) {
                    // Query and drop any foreign key constraints referencing HOA_DON_NHAP_HANG dynamically
                    try {
                        String query = "SELECT TABLE_NAME, CONSTRAINT_NAME " +
                                       "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                                       "WHERE REFERENCED_TABLE_NAME LIKE '%hoa_don_nhap%' " +
                                       "AND TABLE_SCHEMA = DATABASE()";
                        List<String[]> fks = new ArrayList<>();
                        try (PreparedStatement ps = conn.prepareStatement(query);
                             ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                fks.add(new String[]{rs.getString("TABLE_NAME"), rs.getString("CONSTRAINT_NAME")});
                            }
                        }
                        
                        if (!fks.isEmpty()) {
                            try { st.execute("SET FOREIGN_KEY_CHECKS = 0"); } catch (Exception e) {}
                            for (String[] fk : fks) {
                                try {
                                    String dropFk = "ALTER TABLE " + fk[0] + " DROP FOREIGN KEY " + fk[1];
                                    st.execute(dropFk);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                            try { st.execute("SET FOREIGN_KEY_CHECKS = 1"); } catch (Exception e) {}
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        try { st.execute("SET FOREIGN_KEY_CHECKS = 0"); } catch (Exception e) {}
                        try { st.execute("DROP TABLE IF EXISTS CHI_TIET_HOA_DON_NHAP"); } catch (Exception e) {}
                        try { st.execute("DROP TABLE IF EXISTS chi_tiet_hoa_don_nhap"); } catch (Exception e) {}
                        try { st.execute("DROP TABLE IF EXISTS CHI_TIET_HOA_DON_NHAP_HANG"); } catch (Exception e) {}
                        try { st.execute("DROP TABLE IF EXISTS chi_tiet_hoa_don_nhap_hang"); } catch (Exception e) {}
                        try { st.execute("DROP TABLE IF EXISTS HOA_DON_NHAP_HANG"); } catch (Exception e) {}
                        try { st.execute("DROP TABLE IF EXISTS hoa_don_nhap_hang"); } catch (Exception e) {}
                        try { st.execute("SET FOREIGN_KEY_CHECKS = 1"); } catch (Exception e) {}
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Keep foreign key checks disabled during DDL table creations to prevent errno 150
                try { st.execute("SET FOREIGN_KEY_CHECKS = 0"); } catch (Exception e) {}
                for (String sql : ddl) {
                    try {
                        st.execute(sql);
                    } catch (Exception e) {
                        System.err.println("❌ Failed to execute DDL: " + sql);
                        e.printStackTrace();
                        try {
                            java.nio.file.Files.writeString(
                                java.nio.file.Paths.get("d:\\Java\\draft\\PetShop\\petshop\\db_error.txt"),
                                "Failed SQL: " + sql + "\nError: " + e.getMessage() + "\n\n",
                                java.nio.file.StandardOpenOption.CREATE,
                                java.nio.file.StandardOpenOption.APPEND
                            );
                        } catch (Exception ex) {}
                    }
                }
                try { st.execute("SET FOREIGN_KEY_CHECKS = 1"); } catch (Exception e) {}
                
                // Add NHACUNGCAP column to HOA_DON_NHAP_HANG if missing
                try {
                    st.execute("ALTER TABLE HOA_DON_NHAP_HANG ADD COLUMN NHACUNGCAP VARCHAR(255) DEFAULT 'Nhập lẻ tự do'");
                } catch (Exception alterEx) {
                    try {
                        st.execute("ALTER TABLE hoa_don_nhap_hang ADD COLUMN nhacungcap VARCHAR(255) DEFAULT 'Nhập lẻ tự do'");
                    } catch (Exception e) {}
                }
                
                // Add MANV column to HOA_DON_NHAP_HANG if missing
                try {
                    st.execute("ALTER TABLE HOA_DON_NHAP_HANG ADD COLUMN MANV INT");
                } catch (Exception alterEx) {
                    try {
                        st.execute("ALTER TABLE hoa_don_nhap_hang ADD COLUMN manv INT");
                    } catch (Exception e) {}
                }

                // Add foreign key constraint to CHI_TIET_HOA_DON_NHAP_HANG pointing to HOA_DON_NHAP_HANG
                try {
                    st.execute("ALTER TABLE CHI_TIET_HOA_DON_NHAP_HANG ADD CONSTRAINT fk_ct_nhap_hang FOREIGN KEY (MANDNH) REFERENCES HOA_DON_NHAP_HANG(MANDNH) ON DELETE CASCADE");
                } catch (Exception e) {
                    try {
                        st.execute("ALTER TABLE chi_tiet_hoa_don_nhap_hang ADD CONSTRAINT fk_ct_nhap_hang FOREIGN KEY (mandnh) REFERENCES hoa_don_nhap_hang(mandnh) ON DELETE CASCADE");
                    } catch (Exception ex) {}
                }

                // Fix negative stock levels in database
                try {
                    st.execute("UPDATE SAN_PHAM SET SL = 0 WHERE SL < 0");
                } catch (Exception ex) {
                    try {
                        st.execute("UPDATE san_pham SET SL = 0 WHERE SL < 0");
                    } catch (Exception e) {}
                }

                // Seed a pending purchase order if empty
                boolean hasPending = false;
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM DON_HANG_MUA")) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        hasPending = true;
                    }
                } catch (Exception ex) {}

                if (!hasPending) {
                    // Try to fetch first two products
                    List<Integer> prodIds = new ArrayList<>();
                    try (ResultSet rs = st.executeQuery("SELECT MASANPHAM FROM SAN_PHAM LIMIT 2")) {
                        while (rs.next()) {
                            prodIds.add(rs.getInt("MASANPHAM"));
                        }
                    } catch (Exception ex) {}

                    if (!prodIds.isEmpty()) {
                        int newPoId = 1020;
                        try {
                            st.execute("INSERT INTO DON_HANG_MUA (MADONMUA, NHACUNGCAP, TONGTIEN, TRANGTHAI) " +
                                       "VALUES (" + newPoId + ", 'Công ty TNHH Pet Vina', 105000, 'Chờ nhập kho')");
                            st.execute("INSERT INTO CHI_TIET_DON_HANG_MUA (MADONMUA, MASANPHAM, SL, GIANHAP) " +
                                       "VALUES (" + newPoId + ", " + prodIds.get(0) + ", 3, 35000)");
                        } catch (Exception ex) {
                            try {
                                try (PreparedStatement ps = conn.prepareStatement(
                                        "INSERT INTO DON_HANG_MUA (NHACUNGCAP, TONGTIEN, TRANGTHAI) VALUES (?, ?, ?)",
                                        Statement.RETURN_GENERATED_KEYS)) {
                                    ps.setString(1, "Công ty TNHH Pet Vina");
                                    ps.setDouble(2, 105000);
                                    ps.setString(3, "Chờ nhập kho");
                                    ps.executeUpdate();
                                    try (ResultSet keys = ps.getGeneratedKeys()) {
                                        if (keys.next()) {
                                            int generatedId = keys.getInt(1);
                                            try (PreparedStatement psDet = conn.prepareStatement(
                                                    "INSERT INTO CHI_TIET_DON_HANG_MUA (MADONMUA, MASANPHAM, SL, GIANHAP) VALUES (?, ?, ?, ?)")) {
                                                psDet.setInt(1, generatedId);
                                                psDet.setInt(2, prodIds.get(0));
                                                psDet.setInt(3, 3);
                                                psDet.setDouble(4, 35000);
                                                psDet.executeUpdate();
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
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
                    psDetail.setInt(1, newId);
                    psDetail.setInt(2, item.productId);
                    psDetail.setInt(3, item.quantity);
                    psDetail.setDouble(4, item.costPrice);
                    psDetail.addBatch();

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

    // ─────────────────────────────────────────────────────────────────────────
    // NEW PURCHASE ORDER & AUDIT SUPPORT METHODS
    // ─────────────────────────────────────────────────────────────────────────

    public List<PurchaseOrder> getPendingPurchaseOrders() {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT MADONMUA, NGAYLAP, NHACUNGCAP, TONGTIEN, TRANGTHAI FROM DON_HANG_MUA WHERE TRANGTHAI = 'Chờ nhập kho' ORDER BY MADONMUA DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new PurchaseOrder(
                        rs.getInt("MADONMUA"),
                        rs.getTimestamp("NGAYLAP"),
                        rs.getString("NHACUNGCAP"),
                        rs.getDouble("TONGTIEN"),
                        rs.getString("TRANGTHAI")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PurchaseOrderDetail> getPurchaseOrderDetails(int orderId) {
        List<PurchaseOrderDetail> list = new ArrayList<>();
        String sql = "SELECT c.MASANPHAM, p.TENSANPHAM, c.SL, c.GIANHAP " +
                     "FROM CHI_TIET_DON_HANG_MUA c " +
                     "JOIN SAN_PHAM p ON p.MASANPHAM = c.MASANPHAM " +
                     "WHERE c.MADONMUA = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int qty = rs.getInt("SL");
                    double price = rs.getDouble("GIANHAP");
                    list.add(new PurchaseOrderDetail(
                            rs.getInt("MASANPHAM"),
                            rs.getString("TENSANPHAM"),
                            qty,
                            price,
                            qty * price
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public String getPurchaseOrderSupplier(int orderId) {
        String sql = "SELECT NHACUNGCAP FROM DON_HANG_MUA WHERE MADONMUA = ?";
        try (Connection conn = is216.petshop.util.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("NHACUNGCAP");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Nhà cung cấp";
    }

    public List<RestockOrder> getImportHistory() {
        List<RestockOrder> list = new ArrayList<>();
        String sql = "SELECT hd.MANDNH, hd.NGAYNHAP, hd.TONGTIEN, hd.NHACUNGCAP, nv.HOTEN AS TENNV " +
                     "FROM HOA_DON_NHAP_HANG hd " +
                     "LEFT JOIN NHAN_VIEN nv ON nv.MANHANVIEN = hd.MANV " +
                     "ORDER BY hd.MANDNH DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new RestockOrder(
                        rs.getInt("MANDNH"),
                        rs.getTimestamp("NGAYNHAP"),
                        rs.getDouble("TONGTIEN"),
                        rs.getString("NHACUNGCAP"),
                        rs.getString("TENNV") != null ? rs.getString("TENNV") : "Hệ thống"
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RestockOrderDetail> getImportDetails(int importId) {
        List<RestockOrderDetail> list = new ArrayList<>();
        String sql = "SELECT c.MASANPHAM, p.TENSANPHAM, c.SL, c.GIANHAP " +
                     "FROM CHI_TIET_HOA_DON_NHAP_HANG c " +
                     "JOIN SAN_PHAM p ON p.MASANPHAM = c.MASANPHAM " +
                     "WHERE c.MANDNH = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, importId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int qty = rs.getInt("SL");
                    double price = rs.getDouble("GIANHAP");
                    list.add(new RestockOrderDetail(
                            rs.getInt("MASANPHAM"),
                            rs.getString("TENSANPHAM"),
                            qty,
                            price,
                            qty * price
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean executeStockImport(int purchaseOrderId, int employeeId, List<PurchaseOrderDetail> details, double total) throws Exception {
        if (details.isEmpty()) return false;

        String supplier = "Nhập lẻ tự do";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT NHACUNGCAP FROM DON_HANG_MUA WHERE MADONMUA = ?")) {
            ps.setInt(1, purchaseOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    supplier = rs.getString("NHACUNGCAP");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int newId = -1;
            String sqlHeader = "INSERT INTO HOA_DON_NHAP_HANG (TONGTIEN, NHACUNGCAP, MANV) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDouble(1, total);
                ps.setString(2, supplier);
                ps.setInt(3, employeeId);
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

            String sqlDetail = "INSERT INTO CHI_TIET_HOA_DON_NHAP_HANG (MANDNH, MASANPHAM, SL, GIANHAP) VALUES (?, ?, ?, ?)";
            String sqlUpdateStock = "UPDATE SAN_PHAM SET SL = SL + ? WHERE MASANPHAM = ?";
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                 PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateStock)) {
                for (PurchaseOrderDetail item : details) {
                    psDetail.setInt(1, newId);
                    psDetail.setInt(2, item.productId);
                    psDetail.setInt(3, item.quantity);
                    psDetail.setDouble(4, item.costPrice);
                    psDetail.addBatch();

                    psUpdate.setInt(1, item.quantity);
                    psUpdate.setInt(2, item.productId);
                    psUpdate.addBatch();
                }
                psDetail.executeBatch();
                psUpdate.executeBatch();
            }

            String sqlUpdatePo = "UPDATE DON_HANG_MUA SET TRANGTHAI = 'Đã nhập kho', TONGTIEN = ? WHERE MADONMUA = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdatePo)) {
                ps.setDouble(1, total);
                ps.setInt(2, purchaseOrderId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ex) {}
            }
        }
    }

    public boolean executeStockImport(int purchaseOrderId, int employeeId) {
        List<PurchaseOrderDetail> details = getPurchaseOrderDetails(purchaseOrderId);
        double total = 0;
        for (PurchaseOrderDetail item : details) {
            total += item.subtotal;
        }
        try {
            return executeStockImport(purchaseOrderId, employeeId, details, total);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<AuditOrder> getAuditHistory() {
        List<AuditOrder> list = new ArrayList<>();
        String sql = "SELECT p.MAPHIEUKK, p.NGAYLAP, p.GHICHU, nv.HOTEN AS TENNV " +
                     "FROM PHIEU_KIEM_KE p " +
                     "LEFT JOIN NHAN_VIEN nv ON nv.MANHANVIEN = p.MANV " +
                     "ORDER BY p.MAPHIEUKK DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new AuditOrder(
                        rs.getInt("MAPHIEUKK"),
                        rs.getTimestamp("NGAYLAP"),
                        rs.getString("TENNV") != null ? rs.getString("TENNV") : "Hệ thống",
                        rs.getString("GHICHU")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<AuditOrderDetail> getAuditDetails(int auditId) {
        List<AuditOrderDetail> list = new ArrayList<>();
        String sql = "SELECT c.MASANPHAM, p.TENSANPHAM, c.SLTHUCTE, c.SLLECH, c.LYDO " +
                     "FROM CHI_TIET_PHIEU_KIEM_KE c " +
                     "JOIN SAN_PHAM p ON p.MASANPHAM = c.MASANPHAM " +
                     "WHERE c.MAPHIEUKK = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auditId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AuditOrderDetail(
                            rs.getInt("MASANPHAM"),
                            rs.getString("TENSANPHAM"),
                            rs.getInt("SLTHUCTE"),
                            rs.getInt("SLLECH"),
                            rs.getString("LYDO")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createAuditOrder(int employeeId, String note, List<AuditOrderDetail> items) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int newId = -1;
            String sqlHeader = "INSERT INTO PHIEU_KIEM_KE (MANV, GHICHU) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, employeeId);
                ps.setString(2, note);
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

            String sqlDetail = "INSERT INTO CHI_TIET_PHIEU_KIEM_KE (MAPHIEUKK, MASANPHAM, SLTHUCTE, SLLECH, LYDO) VALUES (?, ?, ?, ?, ?)";
            String sqlUpdateStock = "UPDATE SAN_PHAM SET SL = ? WHERE MASANPHAM = ?";
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                 PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateStock)) {
                for (AuditOrderDetail item : items) {
                    psDetail.setInt(1, newId);
                    psDetail.setInt(2, item.productId);
                    psDetail.setInt(3, item.actualQty);
                    psDetail.setInt(4, item.diffQty);
                    psDetail.setString(5, item.reason);
                    psDetail.addBatch();

                    psUpdate.setInt(1, item.actualQty);
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
                try { conn.rollback(); } catch (Exception ex) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ex) {}
            }
        }
    }

    public boolean createPurchaseOrder(String supplier, double totalAmount, List<RestockItem> items) {
        String sqlHeader = "INSERT INTO DON_HANG_MUA (NHACUNGCAP, TONGTIEN, TRANGTHAI) VALUES (?, ?, 'Chờ nhập kho')";
        String sqlDetail = "INSERT INTO CHI_TIET_DON_HANG_MUA (MADONMUA, MASANPHAM, SL, GIANHAP) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);
            
            int newId = -1;
            try (PreparedStatement ps = conn.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, supplier.isEmpty() ? "Công ty TNHH Pet Vina" : supplier);
                ps.setDouble(2, totalAmount);
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
            
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                for (RestockItem item : items) {
                    psDetail.setInt(1, newId);
                    psDetail.setInt(2, item.productId);
                    psDetail.setInt(3, item.quantity);
                    psDetail.setDouble(4, item.costPrice);
                    psDetail.addBatch();
                }
                psDetail.executeBatch();
            }
            
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ex) {}
            }
        }
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT MADONMUA, NGAYLAP, NHACUNGCAP, TONGTIEN, TRANGTHAI FROM DON_HANG_MUA ORDER BY MADONMUA DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new PurchaseOrder(
                        rs.getInt("MADONMUA"),
                        rs.getTimestamp("NGAYLAP"),
                        rs.getString("NHACUNGCAP"),
                        rs.getDouble("TONGTIEN"),
                        rs.getString("TRANGTHAI")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Supplier> getSuppliers() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT k.MAKH, k.TENKH, k.SODIENTHOAI, k.DIACHI, k.EMAIL, n.MASOTHUE, n.DIEUKHOANTHANHTOAN, n.GHICHU " +
                     "FROM khach_hang k " +
                     "JOIN nha_cung_cap n ON k.MAKH = n.MADOITAC " +
                     "ORDER BY k.MAKH DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Supplier(
                        rs.getInt("MAKH"),
                        rs.getString("TENKH"),
                        rs.getString("SODIENTHOAI"),
                        rs.getString("DIACHI"),
                        rs.getString("EMAIL"),
                        rs.getString("MASOTHUE"),
                        rs.getString("DIEUKHOANTHANHTOAN"),
                        rs.getString("GHICHU")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertSupplier(Supplier s) {
        String sqlGetId = "SELECT COALESCE(MAX(MAKH), 0) + 1 FROM khach_hang";
        String sqlKh = "INSERT INTO khach_hang (MAKH, TENKH, SODIENTHOAI, DIACHI, EMAIL, DIEMTICHLUY, NGAYBATDAU) VALUES (?, ?, ?, ?, ?, 0, CURRENT_DATE)";
        String sqlNcc = "INSERT INTO nha_cung_cap (MADOITAC, MASOTHUE, DIEUKHOANTHANHTOAN, GHICHU) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);
            
            int newId = -1;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlGetId)) {
                if (rs.next()) {
                    newId = rs.getInt(1);
                }
            }
            
            if (newId == -1) {
                conn.rollback();
                return false;
            }
            
            try (PreparedStatement ps = conn.prepareStatement(sqlKh)) {
                ps.setInt(1, newId);
                ps.setString(2, s.name);
                ps.setString(3, s.phone);
                ps.setString(4, s.address.isEmpty() ? "—" : s.address);
                ps.setString(5, s.email.isEmpty() ? "—" : s.email);
                ps.executeUpdate();
            }
            
            try (PreparedStatement ps = conn.prepareStatement(sqlNcc)) {
                ps.setInt(1, newId);
                ps.setString(2, s.taxCode.isEmpty() ? "—" : s.taxCode);
                ps.setString(3, s.paymentTerms.isEmpty() ? "—" : s.paymentTerms);
                ps.setString(4, s.note.isEmpty() ? "—" : s.note);
                ps.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ex) {}
            }
        }
    }

    public boolean updateSupplier(Supplier s) {
        String sqlKh = "UPDATE khach_hang SET TENKH = ?, SODIENTHOAI = ?, DIACHI = ?, EMAIL = ? WHERE MAKH = ?";
        String sqlNcc = "UPDATE nha_cung_cap SET MASOTHUE = ?, DIEUKHOANTHANHTOAN = ?, GHICHU = ? WHERE MADOITAC = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);
            
            try (PreparedStatement ps = conn.prepareStatement(sqlKh)) {
                ps.setString(1, s.name);
                ps.setString(2, s.phone);
                ps.setString(3, s.address);
                ps.setString(4, s.email);
                ps.setInt(5, s.id);
                ps.executeUpdate();
            }
            
            try (PreparedStatement ps = conn.prepareStatement(sqlNcc)) {
                ps.setString(1, s.taxCode);
                ps.setString(2, s.paymentTerms);
                ps.setString(3, s.note);
                ps.setInt(4, s.id);
                ps.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ex) {}
            }
        }
    }

    public boolean deleteSupplier(int id) {
        String sqlNcc = "DELETE FROM nha_cung_cap WHERE MADOITAC = ?";
        String sqlKh = "DELETE FROM khach_hang WHERE MAKH = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);
            
            try (PreparedStatement ps = conn.prepareStatement(sqlNcc)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            
            try (PreparedStatement ps = conn.prepareStatement(sqlKh)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ex) {}
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DATA MODEL NESTED CLASSES
    // ─────────────────────────────────────────────────────────────────────────

    public static class Supplier {
        public int id;
        public String name;
        public String phone;
        public String address;
        public String email;
        public String taxCode;
        public String paymentTerms;
        public String note;

        public Supplier(int id, String name, String phone, String address, String email, String taxCode, String paymentTerms, String note) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.address = address;
            this.email = email;
            this.taxCode = taxCode;
            this.paymentTerms = paymentTerms;
            this.note = note;
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

    public static class PurchaseOrder {
        public int orderId;
        public Timestamp date;
        public String supplier;
        public double total;
        public String status;

        public PurchaseOrder(int orderId, Timestamp date, String supplier, double total, String status) {
            this.orderId = orderId;
            this.date = date;
            this.supplier = supplier;
            this.total = total;
            this.status = status;
        }
    }

    public static class PurchaseOrderDetail {
        public int productId;
        public String productName;
        public int quantity;
        public double costPrice;
        public double subtotal;

        public PurchaseOrderDetail(int productId, String productName, int quantity, double costPrice, double subtotal) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.costPrice = costPrice;
            this.subtotal = subtotal;
        }
    }

    public static class RestockOrder {
        public int id;
        public Timestamp date;
        public double total;
        public String supplier;
        public String employeeName;

        public RestockOrder(int id, Timestamp date, double total, String supplier, String employeeName) {
            this.id = id;
            this.date = date;
            this.total = total;
            this.supplier = supplier;
            this.employeeName = employeeName;
        }
    }

    public static class RestockOrderDetail {
        public int productId;
        public String productName;
        public int quantity;
        public double costPrice;
        public double subtotal;

        public RestockOrderDetail(int productId, String productName, int quantity, double costPrice, double subtotal) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.costPrice = costPrice;
            this.subtotal = subtotal;
        }
    }

    public static class AuditOrder {
        public int id;
        public Timestamp date;
        public String employeeName;
        public String note;

        public AuditOrder(int id, Timestamp date, String employeeName, String note) {
            this.id = id;
            this.date = date;
            this.employeeName = employeeName;
            this.note = note;
        }
    }

    public static class AuditOrderDetail {
        public int productId;
        public String productName;
        public int actualQty;
        public int diffQty;
        public String reason;

        public AuditOrderDetail(int productId, String productName, int actualQty, int diffQty, String reason) {
            this.productId = productId;
            this.productName = productName;
            this.actualQty = actualQty;
            this.diffQty = diffQty;
            this.reason = reason;
        }
    }
}
