package is216.petshop.Product;

import is216.petshop.Product.*;
import is216.petshop.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    

    /**
     * Lấy toàn bộ danh sách sản phẩm để hiển thị lên bảng
     */public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM SAN_PHAM";
        
        // GỌI TRỰC TIẾP TỪ CLASS DBCONNECTION DÙNG CHUNG Ở ĐÂY
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
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Cập nhật lại số lượng tồn kho sau khi khách hàng mua/thanh toán thành công
     */
public boolean updateStockQuantity(String productId, int purchasedQty) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
        
        // TIẾP TỤC TÁI SỬ DỤNG DBCONNECTION
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, purchasedQty);
            pstmt.setString(2, productId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}