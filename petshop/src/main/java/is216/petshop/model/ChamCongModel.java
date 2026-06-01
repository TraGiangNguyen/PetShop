package is216.petshop.model;

import java.sql.Timestamp;
import java.util.Date;

public class ChamCongModel {
    private int maChamCong;
    private int maNhanVien;
    private String hoTen; // Display helper
    private Date ngay;
    private Timestamp gioVao;
    private Timestamp gioRa;
    private double soGioLam;
    private double tangCa;
    private String trangThai;
    private String ghiChu;

    public ChamCongModel() {
    }

    public ChamCongModel(int maChamCong, int maNhanVien, String hoTen, Date ngay, Timestamp gioVao, Timestamp gioRa, double soGioLam, double tangCa, String trangThai, String ghiChu) {
        this.maChamCong = maChamCong;
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.ngay = ngay;
        this.gioVao = gioVao;
        this.gioRa = gioRa;
        this.soGioLam = soGioLam;
        this.tangCa = tangCa;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public int getMaChamCong() { return maChamCong; }
    public void setMaChamCong(int maChamCong) { this.maChamCong = maChamCong; }

    public int getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(int maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public Date getNgay() { return ngay; }
    public void setNgay(Date ngay) { this.ngay = ngay; }

    public Timestamp getGioVao() { return gioVao; }
    public void setGioVao(Timestamp gioVao) { this.gioVao = gioVao; }

    public Timestamp getGioRa() { return gioRa; }
    public void setGioRa(Timestamp gioRa) { this.gioRa = gioRa; }

    public double getSoGioLam() { return soGioLam; }
    public void setSoGioLam(double soGioLam) { this.soGioLam = soGioLam; }

    public double getTangCa() { return tangCa; }
    public void setTangCa(double tangCa) { this.tangCa = tangCa; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
