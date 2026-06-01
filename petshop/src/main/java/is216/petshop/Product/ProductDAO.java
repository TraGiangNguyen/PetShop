package is216.petshop.Product;

import is216.petshop.Product.*;
import is216.petshop.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    /**
     * Lấy toàn bộ danh sách sản phẩm để hiển thị lên bảng
     */
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM SAN_PHAM ORDER BY MASANPHAM DESC";
        
        try (Connection conn = DBConnection.getConnection(); 
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("MASANPHAM")); 
                p.setName(rs.getString("TENSANPHAM"));
                p.setUnit(rs.getString("DONVITINH") != null ? rs.getString("DONVITINH") : "Cái");
                p.setPrice(rs.getLong("GIANIEMYET"));
                p.setTax(rs.getDouble("THUE"));
                p.setBarcode(rs.getString("MAVACH") != null ? rs.getString("MAVACH") : "");
                p.setCategory(rs.getString("THUONGHIEU") != null ? rs.getString("THUONGHIEU") : "—");
                p.setOrigin(rs.getString("XUATXU") != null ? rs.getString("XUATXU") : "");
                p.setSuitableFor(rs.getString("PHUHOP") != null ? rs.getString("PHUHOP") : "");
                p.setActiveBuy(rs.getInt("COTHEMUA"));
                p.setActiveSell(rs.getInt("COTHEBAN"));
                p.setActivePos(rs.getInt("POS"));
                
                int qty = rs.getInt("SL");
                p.setStock(qty < 0 ? 0 : qty);
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm kiếm sản phẩm theo tên, thương hiệu, mã vạch hoặc mã sản phẩm
     */
    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM SAN_PHAM WHERE TENSANPHAM LIKE ? OR THUONGHIEU LIKE ? OR MAVACH LIKE ? OR MASANPHAM LIKE ? ORDER BY MASANPHAM DESC";
        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("MASANPHAM")); 
                    p.setName(rs.getString("TENSANPHAM"));
                    p.setUnit(rs.getString("DONVITINH") != null ? rs.getString("DONVITINH") : "Cái");
                    p.setPrice(rs.getLong("GIANIEMYET"));
                    p.setTax(rs.getDouble("THUE"));
                    p.setBarcode(rs.getString("MAVACH") != null ? rs.getString("MAVACH") : "");
                    p.setCategory(rs.getString("THUONGHIEU") != null ? rs.getString("THUONGHIEU") : "—");
                    p.setOrigin(rs.getString("XUATXU") != null ? rs.getString("XUATXU") : "");
                    p.setSuitableFor(rs.getString("PHUHOP") != null ? rs.getString("PHUHOP") : "");
                    p.setActiveBuy(rs.getInt("COTHEMUA"));
                    p.setActiveSell(rs.getInt("COTHEBAN"));
                    p.setActivePos(rs.getInt("POS"));
                    
                    int qty = rs.getInt("SL");
                    p.setStock(qty < 0 ? 0 : qty);
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm mới sản phẩm vào database
     */
    public boolean insertProduct(Product p) {
        String sql = "INSERT INTO SAN_PHAM (TENSANPHAM, DONVITINH, GIANIEMYET, THUE, MAVACH, COTHEMUA, COTHEBAN, POS, THUONGHIEU, XUATXU, PHUHOP, SL) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getUnit().isEmpty() ? "Cái" : p.getUnit());
            ps.setLong(3, p.getPrice());
            ps.setDouble(4, p.getTax());
            ps.setString(5, p.getBarcode());
            ps.setInt(6, p.getActiveBuy());
            ps.setInt(7, p.getActiveSell());
            ps.setInt(8, p.getActivePos());
            ps.setString(9, p.getCategory().isEmpty() ? "PetShop" : p.getCategory());
            ps.setString(10, p.getOrigin());
            ps.setString(11, p.getSuitableFor());
            ps.setInt(12, p.getStock());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        p.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin sản phẩm
     */
    public boolean updateProduct(Product p) {
        String sql = "UPDATE SAN_PHAM SET TENSANPHAM = ?, DONVITINH = ?, GIANIEMYET = ?, THUE = ?, MAVACH = ?, " +
                     "COTHEMUA = ?, COTHEBAN = ?, POS = ?, THUONGHIEU = ?, XUATXU = ?, PHUHOP = ?, SL = ? WHERE MASANPHAM = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getUnit().isEmpty() ? "Cái" : p.getUnit());
            ps.setLong(3, p.getPrice());
            ps.setDouble(4, p.getTax());
            ps.setString(5, p.getBarcode());
            ps.setInt(6, p.getActiveBuy());
            ps.setInt(7, p.getActiveSell());
            ps.setInt(8, p.getActivePos());
            ps.setString(9, p.getCategory().isEmpty() ? "PetShop" : p.getCategory());
            ps.setString(10, p.getOrigin());
            ps.setString(11, p.getSuitableFor());
            ps.setInt(12, p.getStock());
            ps.setInt(13, p.getId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa sản phẩm hoặc vô hiệu hóa nếu có ràng buộc khóa ngoại
     */
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM SAN_PHAM WHERE MASANPHAM = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Không thể xoá trực tiếp sản phẩm do ràng buộc khoá ngoại. Chuyển sang vô hiệu hoá.");
            String sqlDeactivate = "UPDATE SAN_PHAM SET COTHEBAN = 0, COTHEMUA = 0, POS = 0 WHERE MASANPHAM = ?";
            try (Connection conn2 = DBConnection.getConnection();
                 PreparedStatement psDeactivate = conn2.prepareStatement(sqlDeactivate)) {
                psDeactivate.setInt(1, id);
                return psDeactivate.executeUpdate() > 0;
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Cập nhật lại số lượng tồn kho sau khi khách hàng mua/thanh toán thành công
     */
    public boolean updateStockQuantity(String productId, int purchasedQty) {
        String sql = "UPDATE SAN_PHAM SET SL = SL - ? WHERE MASANPHAM = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, purchasedQty);
            pstmt.setInt(2, Integer.parseInt(productId));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}