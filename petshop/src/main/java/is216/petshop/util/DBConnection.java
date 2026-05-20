<<<<<<< HEAD
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author Admin
 */
public class DBConnection {
    public static Connection getConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/petstore";
            String user = "petstore";
            String password = "123456";

            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connected successfully!");
            return conn;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
=======
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
>>>>>>> main
