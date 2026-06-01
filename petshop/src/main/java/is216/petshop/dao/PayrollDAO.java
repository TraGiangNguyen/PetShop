package is216.petshop.dao;

import is216.petshop.model.HoSoLuongModel;
import is216.petshop.model.PhieuLuongModel;
import is216.petshop.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PayrollDAO {

    public PayrollDAO() {
        ensureTablesExist();
    }

    public void ensureTablesExist() {
        String profileDdl = "CREATE TABLE IF NOT EXISTS HO_SO_LUONG (" +
                            "  MANHANVIEN       INT PRIMARY KEY," +
                            "  MUCLUONG         BIGINT NOT NULL," +
                            "  GIAMTRUBANTHAN   BIGINT NOT NULL DEFAULT 15500000," +
                            "  SONGUOIPHUTHUOC  INT NOT NULL DEFAULT 0," +
                            "  TIENGIAMNPT      BIGINT NOT NULL DEFAULT 0," +
                            "  NGAYCAPNHAP      DATE NOT NULL" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        String plDdl = "CREATE TABLE IF NOT EXISTS PHIEU_LUONG (" +
                       "  MAPHIEU          INT AUTO_INCREMENT PRIMARY KEY," +
                       "  MANHANVIEN       INT NOT NULL," +
                       "  THANGNAM         VARCHAR(255) NOT NULL," +
                       "  LUONG            BIGINT NOT NULL," +
                       "  TONGBAOHIEMNV    BIGINT NOT NULL," +
                       "  TONGTHUETNCN     BIGINT NOT NULL," +
                       "  THUCLINH         BIGINT NOT NULL," +
                       "  TRANGTHAI        VARCHAR(255) DEFAULT 'Chờ duyệt' NOT NULL" +
                       ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        String configDdl = "CREATE TABLE IF NOT EXISTS CAU_HINH_HE_SO (" +
                           "  MA_CAU_HINH VARCHAR(50) PRIMARY KEY," +
                           "  GIA_TRI     VARCHAR(255) NOT NULL" +
                           ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(profileDdl);
            st.execute(plDdl);
            st.execute(configDdl);

            // Check and insert defaults if empty
            String checkSql = "SELECT COUNT(*) FROM CAU_HINH_HE_SO";
            try (ResultSet rs = st.executeQuery(checkSql)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    insertDefaultConfigs(conn);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertDefaultConfigs(Connection conn) throws SQLException {
        String sql = "INSERT INTO CAU_HINH_HE_SO (MA_CAU_HINH, GIA_TRI) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String[][] defaults = {
                {"BHXH_RATE", "8.0"},
                {"BHYT_RATE", "1.6"},
                {"BHTN_RATE", "1.0"},
                {"TRAN_BAO_HIEM", "36000000"},
                {"PIT_LIMIT_1", "10000000"},
                {"PIT_RATE_1", "5.0"},
                {"PIT_LIMIT_2", "30000000"},
                {"PIT_RATE_2", "10.0"},
                {"PIT_LIMIT_3", "60000000"},
                {"PIT_RATE_3", "20.0"},
                {"PIT_LIMIT_4", "100000000"},
                {"PIT_RATE_4", "30.0"},
                {"PIT_RATE_5", "35.0"}
            };
            for (String[] def : defaults) {
                ps.setString(1, def[0]);
                ps.setString(2, def[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public double getConfigDouble(String key, double defaultValue) {
        String sql = "SELECT GIA_TRI FROM CAU_HINH_HE_SO WHERE MA_CAU_HINH = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Double.parseDouble(rs.getString("GIA_TRI"));
                }
            }
        } catch (Exception e) {
            // Fallback
        }
        return defaultValue;
    }

    public long getConfigLong(String key, long defaultValue) {
        String sql = "SELECT GIA_TRI FROM CAU_HINH_HE_SO WHERE MA_CAU_HINH = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Long.parseLong(rs.getString("GIA_TRI"));
                }
            }
        } catch (Exception e) {
            // Fallback
        }
        return defaultValue;
    }

    public boolean updateConfig(String key, String value) {
        String sql = "INSERT INTO CAU_HINH_HE_SO (MA_CAU_HINH, GIA_TRI) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE GIA_TRI = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, value);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<HoSoLuongModel> getSalaryProfiles() {
        List<HoSoLuongModel> list = new ArrayList<>();
        String sql = "SELECT nv.MANHANVIEN, nv.HOTEN, nv.EMAIL, hsl.MUCLUONG, hsl.GIAMTRUBANTHAN, " +
                     "       hsl.SONGUOIPHUTHUOC, hsl.TIENGIAMNPT, hsl.NGAYCAPNHAP " +
                     "FROM NHAN_VIEN nv " +
                     "LEFT JOIN HO_SO_LUONG hsl ON nv.MANHANVIEN = hsl.MANHANVIEN " +
                     "WHERE nv.TRANGTHAI = 'Đang làm việc'";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                HoSoLuongModel profile = new HoSoLuongModel();
                profile.setMaNhanVien(rs.getInt("MANHANVIEN"));
                profile.setHoTen(rs.getString("HOTEN"));
                profile.setEmail(rs.getString("EMAIL"));
                
                long mucLuong = rs.getLong("MUCLUONG");
                if (rs.wasNull()) {
                    // Default values
                    profile.setMucLuong(10000000);
                    profile.setGiamTruBanThan(15500000);
                    profile.setSonguoiPhuThuoc(0);
                    profile.setTienGiamNpt(4400000L);
                    profile.setNgayCapNhat(new java.util.Date());
                } else {
                    profile.setMucLuong(mucLuong);
                    profile.setGiamTruBanThan(rs.getLong("GIAMTRUBANTHAN"));
                    profile.setSonguoiPhuThuoc(rs.getInt("SONGUOIPHUTHUOC"));
                    profile.setTienGiamNpt(rs.getLong("TIENGIAMNPT"));
                    profile.setNgayCapNhat(rs.getDate("NGAYCAPNHAP"));
                }
                list.add(profile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean upsertSalaryProfile(HoSoLuongModel profile) {
        String checkSql = "SELECT COUNT(*) FROM HO_SO_LUONG WHERE MANHANVIEN = ?";
        boolean exists = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, profile.getMaNhanVien());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sql;
        if (exists) {
            sql = "UPDATE HO_SO_LUONG SET MUCLUONG = ?, GIAMTRUBANTHAN = ?, SONGUOIPHUTHUOC = ?, TIENGIAMNPT = ?, NGAYCAPNHAP = CURDATE() " +
                  "WHERE MANHANVIEN = ?";
        } else {
            sql = "INSERT INTO HO_SO_LUONG (MUCLUONG, GIAMTRUBANTHAN, SONGUOIPHUTHUOC, TIENGIAMNPT, NGAYCAPNHAP, MANHANVIEN) " +
                  "VALUES (?, ?, ?, ?, CURDATE(), ?)";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, profile.getMucLuong());
            ps.setLong(2, profile.getGiamTruBanThan());
            ps.setInt(3, profile.getSonguoiPhuThuoc());
            ps.setLong(4, profile.getTienGiamNpt());
            ps.setInt(5, profile.getMaNhanVien());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<PhieuLuongModel> getPayrollRecords(String thangNam) {
        List<PhieuLuongModel> list = new ArrayList<>();
        String sql = "SELECT pl.*, nv.HOTEN FROM PHIEU_LUONG pl " +
                     "JOIN NHAN_VIEN nv ON pl.MANHANVIEN = nv.MANHANVIEN " +
                     "WHERE pl.THANGNAM = ? AND nv.TRANGTHAI = 'Đang làm việc' " +
                     "ORDER BY pl.MANHANVIEN";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, thangNam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuLuongModel pl = new PhieuLuongModel();
                    pl.setMaPhieu(rs.getInt("MAPHIEU"));
                    pl.setMaNhanVien(rs.getInt("MANHANVIEN"));
                    pl.setHoTen(rs.getString("HOTEN"));
                    pl.setThangNam(rs.getString("THANGNAM"));
                    pl.setLuong(rs.getLong("LUONG"));
                    pl.setTongBaoHiemNv(rs.getLong("TONGBAOHIEMNV"));
                    pl.setTongThueTncn(rs.getLong("TONGTHUETNCN"));
                    pl.setThucLinh(rs.getLong("THUCLINH"));
                    pl.setTrangThai(rs.getString("TRANGTHAI"));
                    list.add(pl);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean calculatePayroll(String thangNam) {
        // Query to sum up work logs
        String logsSql = "SELECT MANHANVIEN, SUM(SOGIOLAM) as SUM_SOGIO, SUM(TANGCA) as SUM_TANGCA " +
                         "FROM CHAM_CONG WHERE DATE_FORMAT(NGAY, '%m/%Y') = ? " +
                         "GROUP BY MANHANVIEN";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);

            List<HoSoLuongModel> profiles = getSalaryProfiles();
            
            for (HoSoLuongModel profile : profiles) {
                int empId = profile.getMaNhanVien();
                long baseWage = profile.getMucLuong();
                
                double sumSoGio = 0;
                double sumTangCa = 0;
                
                try (PreparedStatement psLog = conn.prepareStatement(logsSql)) {
                    psLog.setString(1, thangNam);
                    try (ResultSet rs = psLog.executeQuery()) {
                        while (rs.next()) {
                            if (rs.getInt("MANHANVIEN") == empId) {
                                sumSoGio = rs.getDouble("SUM_SOGIO");
                                sumTangCa = rs.getDouble("SUM_TANGCA");
                                break;
                            }
                        }
                    }
                }

                // Hourly rate: base wage / (26 days * 8 hours)
                double hourly = baseWage > 0 ? (baseWage / (26.0 * 8.0)) : 0.0;
                
                // Gross = Base + (Overtime * Hourly * 1.5)
                long gross = baseWage + Math.round(sumTangCa * hourly * 1.5);
                
                // Insurance: Dynamic rates capped at DB trần base salary
                double bhxh = getConfigDouble("BHXH_RATE", 8.0);
                double bhyt = getConfigDouble("BHYT_RATE", 1.6);
                double bhtn = getConfigDouble("BHTN_RATE", 1.0);
                long tranBH = getConfigLong("TRAN_BAO_HIEM", 36000000L);
                
                double totalInsuranceRate = (bhxh + bhyt + bhtn) / 100.0;
                long luongDongBH = Math.min(baseWage, tranBH);
                long insurance = Math.round(luongDongBH * totalInsuranceRate);

                // Personal Income Tax: Taxable = Gross - Insurance - 15.5M - (4.4M * dependents)
                long giamTruBanThan = profile.getGiamTruBanThan();
                long songuoiPhuThuoc = profile.getSonguoiPhuThuoc();
                long tiengiamnpt = profile.getTienGiamNpt() > 0 ? profile.getTienGiamNpt() : 4400000L;
                
                long taxableIncome = gross - insurance - giamTruBanThan - (songuoiPhuThuoc * tiengiamnpt);
                if (taxableIncome < 0) taxableIncome = 0;

                long pit = calculatePIT(taxableIncome);
                long thucLinh = gross - insurance - pit;
                if (thucLinh < 0) thucLinh = 0;

                // Check if payroll record already exists for this employee and month
                String checkExistSql = "SELECT MAPHIEU FROM PHIEU_LUONG WHERE MANHANVIEN = ? AND THANGNAM = ?";
                int existingMaphieu = 0;
                try (PreparedStatement psCheck = conn.prepareStatement(checkExistSql)) {
                    psCheck.setInt(1, empId);
                    psCheck.setString(2, thangNam);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            existingMaphieu = rs.getInt("MAPHIEU");
                        }
                    }
                }

                if (existingMaphieu > 0) {
                    // Update
                    String updateSql = "UPDATE PHIEU_LUONG SET LUONG = ?, TONGBAOHIEMNV = ?, TONGTHUETNCN = ?, THUCLINH = ?, TRANGTHAI = 'Chờ duyệt' " +
                                       "WHERE MAPHIEU = ?";
                    try (PreparedStatement psUpd = conn.prepareStatement(updateSql)) {
                        psUpd.setLong(1, gross);
                        psUpd.setLong(2, insurance);
                        psUpd.setLong(3, pit);
                        psUpd.setLong(4, thucLinh);
                        psUpd.setInt(5, existingMaphieu);
                        psUpd.executeUpdate();
                    }
                } else {
                    // Insert
                    String insertSql = "INSERT INTO PHIEU_LUONG (MANHANVIEN, THANGNAM, LUONG, TONGBAOHIEMNV, TONGTHUETNCN, THUCLINH, TRANGTHAI) " +
                                       "VALUES (?, ?, ?, ?, ?, ?, 'Chờ duyệt')";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, empId);
                        psIns.setString(2, thangNam);
                        psIns.setLong(3, gross);
                        psIns.setLong(4, insurance);
                        psIns.setLong(5, pit);
                        psIns.setLong(6, thucLinh);
                        psIns.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ex) {}
            }
        }
    }

    private long calculatePIT(long amount) {
        if (amount <= 0) return 0;
        
        long limit1 = getConfigLong("PIT_LIMIT_1", 10000000L);
        double rate1 = getConfigDouble("PIT_RATE_1", 5.0) / 100.0;
        
        long limit2 = getConfigLong("PIT_LIMIT_2", 30000000L);
        double rate2 = getConfigDouble("PIT_RATE_2", 10.0) / 100.0;
        
        long limit3 = getConfigLong("PIT_LIMIT_3", 60000000L);
        double rate3 = getConfigDouble("PIT_RATE_3", 20.0) / 100.0;
        
        long limit4 = getConfigLong("PIT_LIMIT_4", 100000000L);
        double rate4 = getConfigDouble("PIT_RATE_4", 30.0) / 100.0;
        
        double rate5 = getConfigDouble("PIT_RATE_5", 35.0) / 100.0;

        long pit = 0;
        long remaining = amount;
        
        // Bracket 1
        long size1 = limit1;
        long taxable1 = Math.max(0, Math.min(size1, remaining));
        pit += Math.round(taxable1 * rate1);
        remaining -= taxable1;
        
        // Bracket 2
        if (remaining > 0) {
            long size2 = limit2 - limit1;
            long taxable2 = Math.max(0, Math.min(size2, remaining));
            pit += Math.round(taxable2 * rate2);
            remaining -= taxable2;
        }
        
        // Bracket 3
        if (remaining > 0) {
            long size3 = limit3 - limit2;
            long taxable3 = Math.max(0, Math.min(size3, remaining));
            pit += Math.round(taxable3 * rate3);
            remaining -= taxable3;
        }
        
        // Bracket 4
        if (remaining > 0) {
            long size4 = limit4 - limit3;
            long taxable4 = Math.max(0, Math.min(size4, remaining));
            pit += Math.round(taxable4 * rate4);
            remaining -= taxable4;
        }
        
        // Bracket 5
        if (remaining > 0) {
            pit += Math.round(remaining * rate5);
        }

        return pit;
    }

    public boolean updatePayrollStatus(int maPhieu, String trangThai) {
        String sql = "UPDATE PHIEU_LUONG SET TRANGTHAI = ? WHERE MAPHIEU = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setInt(2, maPhieu);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean approveAllPayroll(String thangNam) {
        String sql = "UPDATE PHIEU_LUONG SET TRANGTHAI = 'Đã duyệt' WHERE THANGNAM = ? AND TRANGTHAI = 'Chờ duyệt'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, thangNam);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
