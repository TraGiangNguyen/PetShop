package is216.petshop.model;

public class QuanLyPhepModel {
    private int idPhep;
    private int maNhanVien;
    private int nam;
    private int tongPhep;
    private int daDung;
    private int conLai;

    public QuanLyPhepModel() {
    }

    public QuanLyPhepModel(int idPhep, int maNhanVien, int nam, int tongPhep, int daDung, int conLai) {
        this.idPhep = idPhep;
        this.maNhanVien = maNhanVien;
        this.nam = nam;
        this.tongPhep = tongPhep;
        this.daDung = daDung;
        this.conLai = conLai;
    }

    public int getIdPhep() { return idPhep; }
    public void setIdPhep(int idPhep) { this.idPhep = idPhep; }

    public int getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(int maNhanVien) { this.maNhanVien = maNhanVien; }

    public int getNam() { return nam; }
    public void setNam(int nam) { this.nam = nam; }

    public int getTongPhep() { return tongPhep; }
    public void setTongPhep(int tongPhep) { this.tongPhep = tongPhep; }

    public int getDaDung() { return daDung; }
    public void setDaDung(int daDung) { this.daDung = daDung; }

    public int getConLai() { return conLai; }
    public void setConLai(int conLai) { this.conLai = conLai; }
}
