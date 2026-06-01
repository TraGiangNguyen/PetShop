package is216.petshop.model;

import java.util.Date;

public class DonNghiPhepModel {
    private int maDon;
    private int maNhanVien;
    private String hoTen; // Display helper
    private String loaiNghi;
    private Date tuNgay;
    private Date denNgay;
    private int soNgay;
    private String lyDo;
    private String trangThai;
    private Integer nguoiDuyet;
    private String tenNguoiDuyet; // Display helper

    public DonNghiPhepModel() {
    }

    public DonNghiPhepModel(int maDon, int maNhanVien, String hoTen, String loaiNghi, Date tuNgay, Date denNgay, int soNgay, String lyDo, String trangThai, Integer nguoiDuyet, String tenNguoiDuyet) {
        this.maDon = maDon;
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.loaiNghi = loaiNghi;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.soNgay = soNgay;
        this.lyDo = lyDo;
        this.trangThai = trangThai;
        this.nguoiDuyet = nguoiDuyet;
        this.tenNguoiDuyet = tenNguoiDuyet;
    }

    public int getMaDon() { return maDon; }
    public void setMaDon(int maDon) { this.maDon = maDon; }

    public int getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(int maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getLoaiNghi() { return loaiNghi; }
    public void setLoaiNghi(String loaiNghi) { this.loaiNghi = loaiNghi; }

    public Date getTuNgay() { return tuNgay; }
    public void setTuNgay(Date tuNgay) { this.tuNgay = tuNgay; }

    public Date getDenNgay() { return denNgay; }
    public void setDenNgay(Date denNgay) { this.denNgay = denNgay; }

    public int getSoNgay() { return soNgay; }
    public void setSoNgay(int soNgay) { this.soNgay = soNgay; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Integer getNguoiDuyet() { return nguoiDuyet; }
    public void setNguoiDuyet(Integer nguoiDuyet) { this.nguoiDuyet = nguoiDuyet; }

    public String getTenNguoiDuyet() { return tenNguoiDuyet; }
    public void setTenNguoiDuyet(String tenNguoiDuyet) { this.tenNguoiDuyet = tenNguoiDuyet; }
}
