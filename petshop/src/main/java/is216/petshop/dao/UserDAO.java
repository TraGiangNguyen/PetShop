package is216.petshop.dao;

import is216.petshop.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public boolean checkLogin(String username, String password) {
        // Tên bảng và tên cột phải khớp tuyệt đối với file SQL của bạn
        String sql = "SELECT * FROM TAI_KHOAN_NHAN_VIEN WHERE USERNAME=? AND PASSWORDHASD=?";
        
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            return rs.next(); // Có dữ liệu trả về = đăng nhập đúng

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
