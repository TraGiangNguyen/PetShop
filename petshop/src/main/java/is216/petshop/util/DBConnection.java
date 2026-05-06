package is216.petshop.util;

import java.sql.Connection;
import java.sql.DriverManager;

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