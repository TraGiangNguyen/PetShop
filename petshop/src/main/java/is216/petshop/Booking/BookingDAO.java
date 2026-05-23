package is216.petshop.Booking;

import is216.petshop.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO – reads/writes LICH_HEN and CHI_TIET_LICH_HEN (MySQL).
 *
 * Schema (MySQL adaptation of the Oracle reference):
 *
 *   LICH_HEN       (MALICHHEN PK AI, MAKH FK, MATHUCUNG FK, THOIGIANHEN, TRANGTHAI, MANV)
 *   CHI_TIET_LICH_HEN (MALICHHEN, MAKH, MADICHVU, GHICHU)  PK(MALICHHEN, MADICHVU)
 *   DICH_VU        (MADICHVU PK AI, TENDICHVU, GIA, COTHEBAN)
 *   DOI_TAC        (MADOITAC, TENDOITAC, SODIENTHOAI, …)
 *   KHACH_HANG     (MADOITAC PK FK→DOI_TAC, DIEMTICHLUY, …)
 *   HO_SO_THU_CUNG (MATHUCUNG PK AI, MADOITAC FK→KHACH_HANG, TENTHUCUNG, LOAITHUCUNG)
 */
public class BookingDAO {

    // ─────────────────────────────────────────────────────────────────────────
    // DDL – ensure tables exist (MySQL syntax)
    // ─────────────────────────────────────────────────────────────────────────
    public void ensureTablesExist() {
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS HO_SO_THU_CUNG (" +
            "  MATHUCUNG  INT AUTO_INCREMENT PRIMARY KEY," +
            "  MAKH       INT NOT NULL," +
            "  TENTHUCUNG VARCHAR(255) NOT NULL," +
            "  LOAITHUCUNG VARCHAR(100)," +
            "  GIOITINH    VARCHAR(10)," +
            "  NGAYSINH    DATE," +
            "  TRANGTHAI   VARCHAR(50)," +
            "  FOREIGN KEY (MAKH) REFERENCES KHACH_HANG(MAKH)" +
            ")",
            "CREATE TABLE IF NOT EXISTS LICH_HEN (" +
            "  MALICHHEN  INT AUTO_INCREMENT PRIMARY KEY," +
            "  MAKH       INT NOT NULL," +
            "  MATHUCUNG  INT," +
            "  THOIGIANHEN DATETIME," +
            "  TRANGTHAI  VARCHAR(100) DEFAULT 'Đợi check-in'," +
            "  MANV       INT" +
            ")",
            "CREATE TABLE IF NOT EXISTS CHI_TIET_LICH_HEN (" +
            "  MALICHHEN INT NOT NULL," +
            "  MAKH      INT NOT NULL," +
            "  MADICHVU  INT NOT NULL," +
            "  GHICHU    VARCHAR(500)," +
            "  PRIMARY KEY(MALICHHEN, MADICHVU)" +
            ")",
            "CREATE TABLE IF NOT EXISTS DICH_VU (" +
            "  MADICHVU  INT AUTO_INCREMENT PRIMARY KEY," +
            "  TENDICHVU VARCHAR(255) NOT NULL," +
            "  GIA       DECIMAL(18,2) DEFAULT 0," +
            "  COTHEBAN  TINYINT(1) DEFAULT 1" +
            ")"
        };
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            for (String sql : ddl) st.execute(sql);

            // 1. Check if COTHEBAN column exists in DICH_VU table
            boolean hasCoTheBan = false;
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "DICH_VU", "COTHEBAN")) {
                if (rs.next()) {
                    hasCoTheBan = true;
                }
            } catch (Exception colEx) {
                // Fallback check
            }

            if (!hasCoTheBan) {
                // Also check lowercase table/column name just in case MySQL is case-sensitive
                try (ResultSet rs = conn.getMetaData().getColumns(null, null, "dich_vu", "cotheban")) {
                    if (rs.next()) {
                        hasCoTheBan = true;
                    }
                } catch (Exception colEx) {}
            }

            if (!hasCoTheBan) {
                System.out.println("Adding COTHEBAN column to DICH_VU table...");
                try {
                    st.execute("ALTER TABLE DICH_VU ADD COLUMN COTHEBAN TINYINT(1) DEFAULT 1");
                } catch (Exception alterEx) {
                    System.out.println("Failed to alter DICH_VU with uppercase, trying lowercase: " + alterEx.getMessage());
                    try {
                        st.execute("ALTER TABLE dich_vu ADD COLUMN cotheban TINYINT(1) DEFAULT 1");
                    } catch (Exception alterEx2) {
                        alterEx2.printStackTrace();
                    }
                }
            }

            // 2. Seed services that match SAN_PHAM if they are missing
            String[][] servicesToSeed = {
                {"Combo Tắm & Cắt tỉa lông mèo dưới 5kg", "250000.00"},
                {"Dịch vụ Khách sạn thú cưng - Phòng Deluxe", "200000.00"},
                {"Dịch vụ Đưa đón thú cưng nội thành dưới 5km", "80000.00"},
                {"Spa & Grooming (Tắm gội)", "150000.00"},
                {"Massage & Relaxing", "100000.00"},
                {"Pet Hotel (Khách sạn chó mèo)", "200000.00"},
                {"Vaccination (Tiêm phòng)", "250000.00"},
                {"Khám sức khỏe tổng quát", "300000.00"}
            };

            for (String[] service : servicesToSeed) {
                String name = service[0];
                double price = Double.parseDouble(service[1]);
                
                boolean exists = false;
                String checkSql = "SELECT COUNT(*) FROM DICH_VU WHERE TENDICHVU = ?";
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setString(1, name);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            exists = true;
                        }
                    }
                } catch (Exception checkEx) {
                    // Try lowercase
                    String checkSqlLw = "SELECT COUNT(*) FROM dich_vu WHERE tendichvu = ?";
                    try (PreparedStatement ps = conn.prepareStatement(checkSqlLw)) {
                        ps.setString(1, name);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                exists = true;
                            }
                        }
                    } catch (Exception e) {}
                }

                if (!exists) {
                    System.out.println("Seeding service into DICH_VU: " + name);
                    String insertSql = "INSERT INTO DICH_VU (TENDICHVU, GIA, COTHEBAN) VALUES (?, ?, 1)";
                    try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                        ps.setString(1, name);
                        ps.setDouble(2, price);
                        ps.executeUpdate();
                    } catch (Exception insEx) {
                        String insertSqlLw = "INSERT INTO dich_vu (tendichvu, gia, cotheban) VALUES (?, ?, 1)";
                        try (PreparedStatement ps = conn.prepareStatement(insertSqlLw)) {
                            ps.setString(1, name);
                            ps.setDouble(2, price);
                            ps.executeUpdate();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ – list of available services from DICH_VU
    // ─────────────────────────────────────────────────────────────────────────
    public List<BookingServiceLine> getAllServices() {
        List<BookingServiceLine> list = new ArrayList<>();
        String sql = "SELECT MADICHVU, TENDICHVU, GIA FROM DICH_VU WHERE COTHEBAN = 1 ORDER BY TENDICHVU";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new BookingServiceLine(
                        rs.getInt("MADICHVU"),
                        rs.getString("TENDICHVU"),
                        rs.getDouble("GIA"),
                        ""
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ – pets belonging to a customer
    // ─────────────────────────────────────────────────────────────────────────
    /** Returns a map of MATHUCUNG → TENTHUCUNG for the given customer. */
    public Map<Integer, String> getPetsByCustomer(int maKh) {
        Map<Integer, String> map = new LinkedHashMap<>();
        // HO_SO_THU_CUNG uses MAKH as the FK to KHACH_HANG (MySQL schema)
        String sql = "SELECT MATHUCUNG, TENTHUCUNG, LOAITHUCUNG " +
                     "FROM HO_SO_THU_CUNG WHERE MAKH = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maKh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String label = rs.getString("TENTHUCUNG")
                            + " (" + nvl(rs.getString("LOAITHUCUNG")) + ")";
                    map.put(rs.getInt("MATHUCUNG"), label);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ – pending bookings (status = "Đợi check-in")
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns only pending appointments (TRANGTHAI = 'Đợi check-in') that match
     * the optional text filter. Sorted by THOIGIANHEN ascending (soonest first).
     */
    public List<Booking> search(String filter) {
        List<Booking> list = new ArrayList<>();
        // Join KHACH_HANG directly (MySQL schema uses flat table, no DOI_TAC split)
        String sql =
            "SELECT lh.MALICHHEN, lh.MAKH, lh.MATHUCUNG, lh.THOIGIANHEN, " +
            "       lh.TRANGTHAI, lh.MANV, " +
            "       kh.TENKH AS TENKH, kh.SODIENTHOAI, " +
            "       tc.TENTHUCUNG, tc.LOAITHUCUNG " +
            "FROM   LICH_HEN lh " +
            "  JOIN KHACH_HANG kh ON kh.MAKH = lh.MAKH " +
            "  LEFT JOIN HO_SO_THU_CUNG tc ON tc.MATHUCUNG = lh.MATHUCUNG " +
            "WHERE  lh.TRANGTHAI IN ('Đợi check-in', 'Đang thực hiện') " +
            "ORDER BY lh.THOIGIANHEN ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            String lower = (filter == null) ? "" : filter.toLowerCase().trim();
            while (rs.next()) {
                Booking b = mapRow(rs);
                // Load child services
                b.setServices(loadServices(conn, b.getMaLichHen(), b.getMaKh()));
                if (lower.isEmpty() || matches(b, lower)) {
                    list.add(b);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Booking> getBookingsByStatus(String status) {
        List<Booking> list = new ArrayList<>();
        String sql =
            "SELECT lh.MALICHHEN, lh.MAKH, lh.MATHUCUNG, lh.THOIGIANHEN, " +
            "       lh.TRANGTHAI, lh.MANV, " +
            "       kh.TENKH AS TENKH, kh.SODIENTHOAI, " +
            "       tc.TENTHUCUNG, tc.LOAITHUCUNG " +
            "FROM   LICH_HEN lh " +
            "  JOIN KHACH_HANG kh ON kh.MAKH = lh.MAKH " +
            "  LEFT JOIN HO_SO_THU_CUNG tc ON tc.MATHUCUNG = lh.MATHUCUNG " +
            "WHERE  lh.TRANGTHAI = ? " +
            "ORDER BY lh.THOIGIANHEN ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = mapRow(rs);
                    b.setServices(loadServices(conn, b.getMaLichHen(), b.getMaKh()));
                    list.add(b);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertPet(int maKh, String tenThuCung, String loaiThuCung) {
        String sql = "INSERT INTO HO_SO_THU_CUNG (MAKH, TENTHUCUNG, LOAITHUCUNG) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maKh);
            ps.setString(2, tenThuCung);
            ps.setString(3, loaiThuCung);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Inserts one row in LICH_HEN and zero-or-more rows in CHI_TIET_LICH_HEN.
     *
     * @param b         the booking header
     * @param maKh      customer id (KHACH_HANG.MADOITAC)
     * @param services  list of selected service ids
     * @param ghiChu    shared note applied to every service line
     * @return {@code true} on success
     */
    public boolean insert(Booking b, int maKh, List<Integer> services, String ghiChu) {
        String sqlLh =
            "INSERT INTO LICH_HEN (MAKH, MATHUCUNG, THOIGIANHEN, TRANGTHAI, MANV) " +
            "VALUES (?, ?, ?, 'Đợi check-in', ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            int newId;
            try (PreparedStatement ps = conn.prepareStatement(sqlLh, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, maKh);
                if (b.getMaThuCung() != null) ps.setInt(2, b.getMaThuCung());
                else                          ps.setNull(2, Types.INTEGER);
                ps.setTimestamp(3, b.getThoiGianHen());
                if (b.getMaNv() != null) ps.setInt(4, b.getMaNv());
                else                     ps.setNull(4, Types.INTEGER);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) { conn.rollback(); return false; }
                    newId = keys.getInt(1);
                }
            }

            // Insert service lines
            if (services != null && !services.isEmpty()) {
                String sqlDet =
                    "INSERT INTO CHI_TIET_LICH_HEN (MALICHHEN, MAKH, MADICHVU, GHICHU) " +
                    "VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlDet)) {
                    for (int madv : services) {
                        ps.setInt(1, newId);
                        ps.setInt(2, maKh);
                        ps.setInt(3, madv);
                        ps.setString(4, ghiChu);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────
    public boolean updateStatus(int maLichHen, String trangThai) {
        String sql = "UPDATE LICH_HEN SET TRANGTHAI = ? WHERE MALICHHEN = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setInt(2, maLichHen);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setMaLichHen(rs.getInt("MALICHHEN"));
        b.setMaKh(rs.getInt("MAKH"));
        Object tc = rs.getObject("MATHUCUNG");
        b.setMaThuCung(tc != null ? rs.getInt("MATHUCUNG") : null);
        b.setThoiGianHen(rs.getTimestamp("THOIGIANHEN"));
        b.setTrangThai(rs.getString("TRANGTHAI"));
        Object nv = rs.getObject("MANV");
        b.setMaNv(nv != null ? rs.getInt("MANV") : null);

        // KHACH_HANG uses TENKH (MySQL flat table, no DOI_TAC split)
        b.setTenKhachHang(rs.getString("TENKH"));
        b.setSoDienThoai(rs.getString("SODIENTHOAI"));
        b.setTenThuCung(rs.getString("TENTHUCUNG"));
        b.setLoaiThuCung(rs.getString("LOAITHUCUNG"));
        return b;
    }

    private List<BookingServiceLine> loadServices(Connection conn, int maLichHen, int maKh)
            throws SQLException {
        List<BookingServiceLine> list = new ArrayList<>();
        String sql =
            "SELECT ct.MADICHVU, dv.TENDICHVU, dv.GIA, ct.GHICHU " +
            "FROM CHI_TIET_LICH_HEN ct " +
            "  JOIN DICH_VU dv ON dv.MADICHVU = ct.MADICHVU " +
            "WHERE ct.MALICHHEN = ? AND ct.MAKH = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maLichHen);
            ps.setInt(2, maKh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BookingServiceLine(
                            rs.getInt("MADICHVU"),
                            rs.getString("TENDICHVU"),
                            rs.getDouble("GIA"),
                            rs.getString("GHICHU")
                    ));
                }
            }
        }
        return list;
    }

    private boolean matches(Booking b, String lower) {
        return (b.getTenKhachHang() != null && b.getTenKhachHang().toLowerCase().contains(lower))
            || (b.getSoDienThoai() != null  && b.getSoDienThoai().toLowerCase().contains(lower))
            || (b.getTenThuCung()  != null  && b.getTenThuCung().toLowerCase().contains(lower))
            || b.getServicesSummary().toLowerCase().contains(lower);
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
