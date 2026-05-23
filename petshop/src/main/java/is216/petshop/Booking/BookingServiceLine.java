package is216.petshop.Booking;

/**
 * One line from CHI_TIET_LICH_HEN joined with DICH_VU.
 */
public class BookingServiceLine {
    private int    maDichVu;
    private String tenDichVu;
    private double gia;
    private String ghiChu;   // from CHI_TIET_LICH_HEN.GHICHU

    public BookingServiceLine() {}

    public BookingServiceLine(int maDichVu, String tenDichVu, double gia, String ghiChu) {
        this.maDichVu  = maDichVu;
        this.tenDichVu = tenDichVu;
        this.gia       = gia;
        this.ghiChu    = ghiChu;
    }

    public int    getMaDichVu()           { return maDichVu; }
    public void   setMaDichVu(int v)      { this.maDichVu = v; }

    public String getTenDichVu()          { return tenDichVu; }
    public void   setTenDichVu(String v)  { this.tenDichVu = v; }

    public double getGia()                { return gia; }
    public void   setGia(double v)        { this.gia = v; }

    public String getGhiChu()            { return ghiChu; }
    public void   setGhiChu(String v)    { this.ghiChu = v; }
}
