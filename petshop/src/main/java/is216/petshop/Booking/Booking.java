package is216.petshop.Booking;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model – maps to LICH_HEN (joined with DOI_TAC, HO_SO_THU_CUNG, DICH_VU).
 *
 * LICH_HEN: MALICHHEN, MAKH, MATHUCUNG, THOIGIANHEN, TRANGTHAI, MANV
 * CHI_TIET_LICH_HEN: MALICHHEN, MAKH, MADICHVU, GHICHU
 */
public class Booking {

    // ── From LICH_HEN ─────────────────────────────────────────────────────────
    private int       maLichHen;
    private int       maKh;           // FK → KHACH_HANG(MADOITAC)
    private Integer   maThuCung;      // FK → HO_SO_THU_CUNG(MATHUCUNG), nullable
    private Timestamp thoiGianHen;
    private String    trangThai;      // "Đợi check-in" | "Đang thực hiện" | "Hoàn thành" | "Đã hủy"
    private Integer   maNv;           // assigned staff, nullable

    // ── Joined from DOI_TAC (via KHACH_HANG) ──────────────────────────────────
    private String    tenKhachHang;
    private String    soDienThoai;

    // ── Joined from HO_SO_THU_CUNG ────────────────────────────────────────────
    private String    tenThuCung;
    private String    loaiThuCung;

    // ── Joined from NHANVIEN ──────────────────────────────────────────────────
    private String    tenNhanVien;

    // ── From CHI_TIET_LICH_HEN (may be multiple rows) ────────────────────────
    private List<BookingServiceLine> services = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────
    public Booking() {}

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public int       getMaLichHen()      { return maLichHen; }
    public void      setMaLichHen(int v) { this.maLichHen = v; }

    public int       getMaKh()           { return maKh; }
    public void      setMaKh(int v)      { this.maKh = v; }

    public Integer   getMaThuCung()      { return maThuCung; }
    public void      setMaThuCung(Integer v) { this.maThuCung = v; }

    public Timestamp getThoiGianHen()           { return thoiGianHen; }
    public void      setThoiGianHen(Timestamp v) { this.thoiGianHen = v; }

    public String    getTrangThai()      { return trangThai; }
    public void      setTrangThai(String v) { this.trangThai = v; }

    public Integer   getMaNv()           { return maNv; }
    public void      setMaNv(Integer v)  { this.maNv = v; }

    public String    getTenKhachHang()   { return tenKhachHang; }
    public void      setTenKhachHang(String v) { this.tenKhachHang = v; }

    public String    getSoDienThoai()    { return soDienThoai; }
    public void      setSoDienThoai(String v) { this.soDienThoai = v; }

    public String    getTenThuCung()     { return tenThuCung; }
    public void      setTenThuCung(String v) { this.tenThuCung = v; }

    public String    getLoaiThuCung()    { return loaiThuCung; }
    public void      setLoaiThuCung(String v) { this.loaiThuCung = v; }

    public String    getTenNhanVien()    { return tenNhanVien; }
    public void      setTenNhanVien(String v) { this.tenNhanVien = v; }

    public List<BookingServiceLine> getServices()         { return services; }
    public void setServices(List<BookingServiceLine> svc) { this.services = svc; }

    /** Convenience: first service name, or empty string if none. */
    public String getFirstServiceName() {
        return services.isEmpty() ? "" : services.get(0).getTenDichVu();
    }

    /** Convenience: comma-joined service names. */
    public String getServicesSummary() {
        if (services.isEmpty()) return "—";
        StringBuilder sb = new StringBuilder();
        for (BookingServiceLine s : services) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(s.getTenDichVu());
        }
        return sb.toString();
    }

    /** Calculate total amount of the booking services */
    public double getTongTien() {
        double total = 0;
        for (BookingServiceLine s : services) {
            total += s.getGia();
        }
        return total;
    }
}
