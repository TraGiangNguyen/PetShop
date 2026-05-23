package is216.petshop.dao;

import is216.petshop.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public boolean checkLogin(String username, String password) {
        String sql = "SELECT * FROM TAI_KHOAN_NHAN_VIEN WHERE USERNAME=? AND PASSWORD=?";
        
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

    public String getUserRole(String username) {
        String sql = "SELECT nv.CHUCVU FROM TAI_KHOAN_NHAN_VIEN tk " +
                     "JOIN nhan_vien nv ON tk.MANHANVIEN = nv.MANHANVIEN " +
                     "WHERE tk.USERNAME = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return "";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("CHUCVU");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public is216.petshop.model.NhanVienModel getNhanVienByUsername(String username) {
        String sql = "SELECT nv.* FROM TAI_KHOAN_NHAN_VIEN tk " +
                     "JOIN nhan_vien nv ON tk.MANHANVIEN = nv.MANHANVIEN " +
                     "WHERE tk.USERNAME = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return null;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                is216.petshop.model.NhanVienModel nv = new is216.petshop.model.NhanVienModel();
                nv.setMaNhanVien(rs.getInt("MANHANVIEN"));
                nv.setHoTen(rs.getString("HOTEN"));
                nv.setChucVu(rs.getString("CHUCVU"));
                nv.setSdt(rs.getString("SDT"));
                nv.setEmail(rs.getString("EMAIL"));
                nv.setNgayVaoLam(rs.getDate("NGAYVAOLAM"));
                nv.setTrangThai(rs.getString("TRANGTHAI"));
                return nv;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
