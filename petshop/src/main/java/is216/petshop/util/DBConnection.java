package is216.petshop.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {
        try {
            // Cập nhật kết nối tới database PETSHOP theo file SQL của bạn
            String url = "jdbc:mysql://localhost:3306/PETSHOP";
            String user = "root"; // Mặc định thường là root
            String password = ""; // Bạn hãy điền mật khẩu MySQL của bạn vào đây (nếu có)
            
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Kết nối Database PETSHOP thành công!");
            return conn;
        } catch (Exception e) {
            System.err.println("❌ Lỗi kết nối Database: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}