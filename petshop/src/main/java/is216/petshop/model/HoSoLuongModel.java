package is216.petshop.model;

import java.util.Date;

public class HoSoLuongModel {
    private int maNhanVien;
    private String hoTen; // Display helper
    private long mucLuong;
    private long giamTruBanThan;
    private int songuoiPhuThuoc;
    private long tienGiamNpt;
    private String email;
    private Date ngayCapNhat;

    public HoSoLuongModel() {
    }

    public HoSoLuongModel(int maNhanVien, String hoTen, String email, long mucLuong, long giamTruBanThan, int songuoiPhuThuoc, long tienGiamNpt, Date ngayCapNhat) {
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.email = email;
        this.mucLuong = mucLuong;
        this.giamTruBanThan = giamTruBanThan;
        this.songuoiPhuThuoc = songuoiPhuThuoc;
        this.tienGiamNpt = tienGiamNpt;
        this.ngayCapNhat = ngayCapNhat;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(int maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public long getMucLuong() { return mucLuong; }
    public void setMucLuong(long mucLuong) { this.mucLuong = mucLuong; }

    public long getGiamTruBanThan() { return giamTruBanThan; }
    public void setGiamTruBanThan(long giamTruBanThan) { this.giamTruBanThan = giamTruBanThan; }

    public int getSonguoiPhuThuoc() { return songuoiPhuThuoc; }
    public void setSonguoiPhuThuoc(int songuoiPhuThuoc) { this.songuoiPhuThuoc = songuoiPhuThuoc; }

    public long getTienGiamNpt() { return tienGiamNpt; }
    public void setTienGiamNpt(long tienGiamNpt) { this.tienGiamNpt = tienGiamNpt; }

    public Date getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Date ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}
