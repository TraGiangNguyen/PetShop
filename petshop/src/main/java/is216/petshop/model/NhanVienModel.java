/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.model;

import java.util.Date;

public class NhanVienModel {
    private int maNhanVien;
    private String hoTen;
    private String chucVu;
    private String sdt;
    private String email;
    private long luong;
    private Date ngayVaoLam;
    private String trangThai;

    public NhanVienModel() {
    }

    public NhanVienModel(int maNhanVien, String hoTen, String chucVu, String sdt, String email, long luong, Date ngayVaoLam, String trangThai) {
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.chucVu = chucVu;
        this.sdt = sdt;
        this.email = email;
        this.luong = luong;
        this.ngayVaoLam = ngayVaoLam;
        this.trangThai = trangThai;
    }

    public int getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(int maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getLuong() { return luong; }
    public void setLuong(long luong) { this.luong = luong; }

    public Date getNgayVaoLam() { return ngayVaoLam; }
    public void setNgayVaoLam(Date ngayVaoLam) { this.ngayVaoLam = ngayVaoLam; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
