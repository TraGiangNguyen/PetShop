package is216.petshop.dao;

import is216.petshop.util.DBConnection;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class InvoiceDAO {
    
    public boolean createOrder(Integer maKH, long totalAmount, DefaultTableModel cartModel, String trangThai, int diemDung) {
        String sqlDetail = "INSERT INTO chi_tiet_don_hang (MASANPHAM, MADONHANG, SOLUONG, DONGIA, THANHTIEN) VALUES (?, ?, ?, ?, ?)";
        String sqlUpdateStock = "UPDATE san_pham SET SL = SL - ? WHERE MASANPHAM = ?";

        Connection conn = null;
        CallableStatement psOrder = null;
        PreparedStatement psDetail = null;
        PreparedStatement psUpdateStock = null;
        
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;
            
            // Turn off auto-commit for transaction
            conn.setAutoCommit(false);
            
            // 1. Get a default employee ID
            int maNhanVien = 1;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT MANHANVIEN FROM nhan_vien LIMIT 1")) {
                if (rs.next()) {
                    maNhanVien = rs.getInt("MANHANVIEN");
                }
            } catch (Exception ex) {
                // Ignore and use default 1
            }
            
            // 2. Insert Order using stored procedure sp_themDonHang
            String sqlProc = "{call sp_themDonHang(?, ?, ?, ?, ?, ?, ?)}";
            psOrder = conn.prepareCall(sqlProc);
            
            if (maKH != null) {
                psOrder.setString(1, String.valueOf(maKH));
            } else {
                psOrder.setNull(1, java.sql.Types.VARCHAR);
            }
            psOrder.setString(2, String.valueOf(maNhanVien));
            psOrder.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            psOrder.setDouble(4, (double) totalAmount);
            psOrder.setInt(5, diemDung); // p_DIEMMUONDUNG
            psOrder.setString(6, trangThai);
            psOrder.setString(7, trangThai.equals("Chờ thanh toán") ? "Chờ thanh toán tại quầy" : "Thanh toán tại quầy");
            
            psOrder.execute();
            
            // 3. Get generated Order ID
            int orderId = 0;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT LAST_INSERT_ID()")) {
                if (rs.next()) {
                    orderId = rs.getInt(1);
                }
            }
            
            if (orderId == 0) {
                conn.rollback();
                return false;
            }
            
            // 4. Insert Order Details & Update Stock
            psDetail = conn.prepareStatement(sqlDetail);
            psUpdateStock = conn.prepareStatement(sqlUpdateStock);
            
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int productId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
                int quantity = Integer.parseInt(cartModel.getValueAt(i, 2).toString());
                long itemTotal = Long.parseLong(cartModel.getValueAt(i, 3).toString());
                long unitPrice = itemTotal / quantity;
                
                // Details
                psDetail.setInt(1, productId);
                psDetail.setInt(2, orderId);
                psDetail.setInt(3, quantity);
                psDetail.setLong(4, unitPrice);
                psDetail.setLong(5, itemTotal);
                psDetail.addBatch();
                
                // Update stock in san_pham table
                psUpdateStock.setInt(1, quantity);
                psUpdateStock.setInt(2, productId);
                psUpdateStock.addBatch();
            }
            
            psDetail.executeBatch();
            psUpdateStock.executeBatch();
            
            // Commit Transaction
            conn.commit();
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            try {
                if (psOrder != null) psOrder.close();
                if (psDetail != null) psDetail.close();
                if (psUpdateStock != null) psUpdateStock.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static class PendingOrder {
        public int id;
        public Timestamp date;
        public long totalAmount;
        public Integer customerId;
        public String customerName;
        public String customerPhone;
        public String employeeName;
        public String note;
    }

    public static class OrderItem {
        public int productId;
        public String productName;
        public int quantity;
        public long price;
    }

    public java.util.List<PendingOrder> getPendingOrders() {
        java.util.List<PendingOrder> list = new java.util.ArrayList<>();
        String sql = "SELECT d.MADONHANG, d.NGAYTAO, d.TONGTIENTAMTINH, d.MAKH, d.GHICHU, " +
                     "       k.TENKH AS TENKH, k.SODIENTHOAI AS SDTKH, " +
                     "       n.HOTEN AS TENNV " +
                     "FROM don_hang d " +
                     "LEFT JOIN khach_hang k ON d.MAKH = k.MAKH " +
                     "LEFT JOIN nhan_vien n ON d.MANHANVIEN = n.MANHANVIEN " +
                     "WHERE d.TRANGTHAI = 'Chờ thanh toán' " +
                     "ORDER BY d.NGAYTAO DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PendingOrder o = new PendingOrder();
                o.id = rs.getInt("MADONHANG");
                o.date = rs.getTimestamp("NGAYTAO");
                o.totalAmount = rs.getLong("TONGTIENTAMTINH");
                o.customerId = rs.getObject("MAKH") != null ? rs.getInt("MAKH") : null;
                o.customerName = rs.getString("TENKH") != null ? rs.getString("TENKH") : "Khách vãng lai";
                o.customerPhone = rs.getString("SDTKH") != null ? rs.getString("SDTKH") : "";
                o.employeeName = rs.getString("TENNV") != null ? rs.getString("TENNV") : "Hệ thống";
                o.note = rs.getString("GHICHU");
                list.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public java.util.List<OrderItem> getOrderDetails(int orderId) {
        java.util.List<OrderItem> list = new java.util.ArrayList<>();
        String sql = "SELECT c.MASANPHAM, c.SOLUONG, c.DONGIA, s.TENSANPHAM " +
                     "FROM chi_tiet_don_hang c " +
                     "JOIN san_pham s ON c.MASANPHAM = s.MASANPHAM " +
                     "WHERE c.MADONHANG = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.productId = rs.getInt("MASANPHAM");
                    item.productName = rs.getString("TENSANPHAM");
                    item.quantity = rs.getInt("SOLUONG");
                    item.price = rs.getLong("DONGIA");
                    list.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteOrder(int orderId) {
        String sqlDetails = "DELETE FROM chi_tiet_don_hang WHERE MADONHANG = ?";
        String sqlOrder = "DELETE FROM don_hang WHERE MADONHANG = ?";
        Connection conn = null;
        PreparedStatement psDetails = null;
        PreparedStatement psOrder = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);

            psDetails = conn.prepareStatement(sqlDetails);
            psDetails.setInt(1, orderId);
            psDetails.executeUpdate();

            psOrder = conn.prepareStatement(sqlOrder);
            psOrder.setInt(1, orderId);
            int affected = psOrder.executeUpdate();

            conn.commit();
            return affected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            try {
                if (psDetails != null) psDetails.close();
                if (psOrder != null) psOrder.close();
                if (conn != null) conn.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}
