/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.dao;

import is216.petshop.model.NhanVienModel;
import is216.petshop.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {
    public List<NhanVienModel> getDanhSachNhanVien() {
        List<NhanVienModel> list = new ArrayList<>();
        String sql = "SELECT nv.MANHANVIEN, nv.HOTEN, IFNULL(vt.TENVAITRO, 'Chưa cấp quyền') AS CHUCVU, " +
                "nv.SDT, nv.EMAIL, IFNULL(hsl.MUCLUONG, 0) AS LUONG, nv.NGAYVAOLAM, nv.TRANGTHAI " +
                "FROM NHAN_VIEN nv " +
                "LEFT JOIN PHAN_QUYEN_NHAN_VIEN pq ON nv.MANHANVIEN = pq.MANHANVIEN " +
                "LEFT JOIN VAI_TRO vt ON pq.MAVAITRO = vt.MAVAITRO " +
                "LEFT JOIN HO_SO_LUONG hsl ON nv.MANHANVIEN = hsl.MANHANVIEN " +
                "WHERE nv.TRANGTHAI = 'Đang làm việc'";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                NhanVienModel nv = new NhanVienModel();
                nv.setMaNhanVien(rs.getInt("MANHANVIEN"));
                nv.setHoTen(rs.getString("HOTEN"));
                nv.setChucVu(rs.getString("CHUCVU"));
                nv.setSdt(rs.getString("SDT"));
                nv.setEmail(rs.getString("EMAIL"));
                nv.setLuong(rs.getLong("LUONG"));
                nv.setNgayVaoLam(rs.getDate("NGAYVAOLAM"));
                nv.setTrangThai(rs.getString("TRANGTHAI"));
                list.add(nv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addNhanVien(NhanVienModel nv, int maCuaHang, int maVaiTro, String username, String password) {
        String sql = "{CALL sp_ThemNhanVien(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBConnection.getConnection();
                java.sql.CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, maCuaHang);
            cs.setString(2, nv.getHoTen());
            cs.setString(3, nv.getSdt());
            cs.setString(4, nv.getEmail());
            cs.setDate(5, new java.sql.Date(nv.getNgayVaoLam().getTime()));
            cs.setInt(6, maVaiTro);
            cs.setLong(7, nv.getLuong());
            cs.setString(8, username);
            cs.setString(9, password); // Trong thực tế nên hash password trước
            cs.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateNhanVien(NhanVienModel nv, int maVaiTro) {
        String sql = "{CALL sp_CapNhatNhanVien(?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBConnection.getConnection();
                java.sql.CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, nv.getMaNhanVien());
            cs.setString(2, nv.getHoTen());
            cs.setString(3, nv.getSdt());
            cs.setString(4, nv.getEmail());
            cs.setInt(5, maVaiTro);
            cs.setLong(6, nv.getLuong());
            cs.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteNhanVien(int maNhanVien) {
        String sql = "UPDATE NHAN_VIEN SET TRANGTHAI = 'Đã nghỉ việc' WHERE MANHANVIEN = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maNhanVien);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String[]> getDanhSachCuaHang() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT MACUAHANG, TENCUAHANG FROM CUA_HANG";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[] { String.valueOf(rs.getInt("MACUAHANG")), rs.getString("TENCUAHANG") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}