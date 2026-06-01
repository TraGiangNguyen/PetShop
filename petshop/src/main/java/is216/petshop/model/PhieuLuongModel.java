package is216.petshop.model;

public class PhieuLuongModel {
    private int maPhieu;
    private int maNhanVien;
    private String hoTen; // Display helper
    private String thangNam;
    private long luong;
    private long tongBaoHiemNv;
    private long tongThueTncn;
    private long thucLinh;
    private String trangThai;

    public PhieuLuongModel() {
    }

    public PhieuLuongModel(int maPhieu, int maNhanVien, String hoTen, String thangNam, long luong, long tongBaoHiemNv, long tongThueTncn, long thucLinh, String trangThai) {
        this.maPhieu = maPhieu;
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.thangNam = thangNam;
        this.luong = luong;
        this.tongBaoHiemNv = tongBaoHiemNv;
        this.tongThueTncn = tongThueTncn;
        this.thucLinh = thucLinh;
        this.trangThai = trangThai;
    }

    public int getMaPhieu() { return maPhieu; }
    public void setMaPhieu(int maPhieu) { this.maPhieu = maPhieu; }

    public int getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(int maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getThangNam() { return thangNam; }
    public void setThangNam(String thangNam) { this.thangNam = thangNam; }

    public long getLuong() { return luong; }
    public void setLuong(long luong) { this.luong = luong; }

    public long getTongBaoHiemNv() { return tongBaoHiemNv; }
    public void setTongBaoHiemNv(long tongBaoHiemNv) { this.tongBaoHiemNv = tongBaoHiemNv; }

    public long getTongThueTncn() { return tongThueTncn; }
    public void setTongThueTncn(long tongThueTncn) { this.tongThueTncn = tongThueTncn; }

    public long getThucLinh() { return thucLinh; }
    public void setThucLinh(long thucLinh) { this.thucLinh = thucLinh; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
