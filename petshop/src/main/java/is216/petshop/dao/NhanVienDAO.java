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
        String sql = "SELECT * FROM NHAN_VIEN";
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
                nv.setNgayVaoLam(rs.getDate("NGAYVAOLAM"));
                nv.setTrangThai(rs.getString("TRANGTHAI"));
                list.add(nv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addNhanVien(NhanVienModel nv, String username, String password) {
        // 1. SỬA: Thay đổi câu SQL thành 8 dấu chấm hỏi tương ứng với 8 tham số IN
        String sql = "{CALL sp_ThemNhanVien(?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection con = DBConnection.getConnection();
                java.sql.CallableStatement cs = con.prepareCall(sql)) {
            
            // 2. SỬA: Map chính xác các thuộc tính theo đúng thứ tự khai báo trong Stored Procedure
            cs.setString(1, nv.getHoTen());
            cs.setString(2, nv.getSdt());
            cs.setString(3, nv.getEmail());
            
            // Kiểm tra tránh lỗi NullPointerException nếu ngày vào làm bị trống
            if (nv.getNgayVaoLam() != null) {
                cs.setDate(4, new java.sql.Date(nv.getNgayVaoLam().getTime()));
            } else {
                cs.setNull(4, java.sql.Types.DATE);
            }
            
            cs.setString(5, nv.getChucVu());
            cs.setString(6, nv.getTrangThai());
            cs.setString(7, username);
            cs.setString(8, password);
            
            // Thực thi câu lệnh
            cs.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm nhân viên: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateNhanVien(NhanVienModel nv) { // Tôi đã bỏ tham số int maVaiTro vì bạn không dùng tới nó
        // 1. Sửa lại: Cần đúng 7 dấu chấm hỏi tương ứng với 7 tham số IN
        String sql = "{CALL sp_CapNhatNhanVien(?, ?, ?, ?, ?, ?, ?)}"; 
        
        try (Connection con = DBConnection.getConnection();
                java.sql.CallableStatement cs = con.prepareCall(sql)) {
            
            // 2. Sửa lại: Thứ tự truyền vào phải KHỚP 100% với Stored Procedure
            cs.setInt(1, nv.getMaNhanVien());       // IN p_MaNhanVien
            cs.setString(2, nv.getHoTen());         // IN p_HoTen
            cs.setString(3, nv.getChucVu());        // IN p_ChucVu (Đưa lên vị trí số 3)
            cs.setString(4, nv.getSdt());           // IN p_Sdt
            cs.setString(5, nv.getEmail());         // IN p_Email
            
            // Xử lý an toàn cho ngày tháng để tránh lỗi NullPointerException
            if (nv.getNgayVaoLam() != null) {
                cs.setDate(6, new java.sql.Date(nv.getNgayVaoLam().getTime())); // IN p_NgayVaoLam
            } else {
                cs.setNull(6, java.sql.Types.DATE);
            }
            
            // 3. Sửa lại: Bổ sung tham số thứ 7 bị thiếu
            cs.setString(7, nv.getTrangThai());     // IN p_TrangThai
            
            cs.executeUpdate();
            return true;
            
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật nhân viên: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteNhanVien(int maNhanVien) {
        String sql = "UPDATE NHAN_VIEN SET TRANGTHAI = 0 WHERE MANHANVIEN = (?)";
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