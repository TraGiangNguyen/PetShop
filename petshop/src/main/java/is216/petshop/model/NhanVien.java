/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.model;

/**
 *
 * @author Trần Minh Quyền
 */
public class NhanVien {
    private int maNhanVien;
    private String hoTen;
    private String chucVu; // Lấy từ bảng VAI_TRO
    private String soDienThoai;
    private String email;
    private long luong; // Lấy từ bảng HO_SO_LUONG

    public NhanVien() {}

    public NhanVien(int maNhanVien, String hoTen, String chucVu, String soDienThoai, String email, long luong) {
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.chucVu = chucVu;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.luong = luong;
    }

    public int getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(int maNhanVien) { this.maNhanVien = maNhanVien; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public long getLuong() { return luong; }
    public void setLuong(long luong) { this.luong = luong; }
}
