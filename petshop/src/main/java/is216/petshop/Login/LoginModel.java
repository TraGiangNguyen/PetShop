/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.Login;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 *
 * @author Admin
 */
public class LoginModel {
    private final String DB_URL = "jdbc:mysql://localhost:3306/petstore";
    private final String USER = "root";
    private final String PASS = "123456";
    
    public boolean checkLogin(String username, String password) {
        // Sử dụng try-with-resources để tự động đóng kết nối
        String query = "SELECT * FROM `TAI_KHOAN_NHAN_VIEN` WHERE USERNAME = ? AND PASSWORD = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            // Truyền tham số vào câu query để tránh SQL Injection
            pstmt.setString(1, username);
            pstmt.setString(2, password); // Trong thực tế nên mã hóa mật khẩu (VD: BCrypt)
            
            try (ResultSet rs = pstmt.executeQuery()) {
                // Nếu ResultSet có dữ liệu (rs.next() == true), nghĩa là tài khoản tồn tại
                return rs.next();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
