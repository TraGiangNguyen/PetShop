/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.dao;

import is216.petshop.model.NhanVien;
import is216.petshop.util.DBConnection; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class NhanVienDAO {
    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT nv.MANHANVIEN, nv.HOTEN, vt.TENVAITRO, nv.SDT, nv.EMAIL, hsl.MUCLUONG " +
                     "FROM NHANVIEN nv " +
                     "LEFT JOIN PHAN_QUYEN_NHAN_VIEN pq ON nv.MANHANVIEN = pq.MANHANVIEN " +
                     "LEFT JOIN VAI_TRO vt ON pq.MAVAITRO = vt.MAVAITRO " +
                     "LEFT JOIN HO_SO_LUONG hsl ON nv.MANHANVIEN = hsl.MANHANVIEN " +
                     "WHERE nv.TRANGTHAI = 'Đang làm việc'"; 

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNhanVien(rs.getInt("MANHANVIEN"));
                nv.setHoTen(rs.getString("HOTEN"));
                nv.setChucVu(rs.getString("TENVAITRO") != null ? rs.getString("TENVAITRO") : "Chưa gắn vai trò");
                nv.setSoDienThoai(rs.getString("SDT"));
                nv.setEmail(rs.getString("EMAIL"));
                nv.setLuong(rs.getLong("MUCLUONG"));
                list.add(nv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}