package is216.petshop.dao;

import is216.petshop.model.ChamCongModel;
import is216.petshop.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChamCongDAO {

    public ChamCongDAO() {
        ensureTablesExist();
    }

    public void ensureTablesExist() {
        String ddl = "CREATE TABLE IF NOT EXISTS CHAM_CONG (" +
                     "  MACHAMCONG  INT AUTO_INCREMENT PRIMARY KEY," +
                     "  MANHANVIEN  INT NOT NULL," +
                     "  NGAY        DATE NOT NULL," +
                     "  GIOVAO      DATETIME NOT NULL," +
                     "  GIORA       DATETIME," +
                     "  SOGIOLAM    DOUBLE DEFAULT 0," +
                     "  TANGCA      DOUBLE DEFAULT 0," +
                     "  TRANGTHAI   VARCHAR(255) NOT NULL," +
                     "  GHICHU      VARCHAR(255)" +
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(ddl);
            
            // Tự động nâng cấp/sửa cấu trúc bảng nếu các cột bị đặt NOT NULL sai quy định ở database cũ
            try {
                st.execute("ALTER TABLE CHAM_CONG MODIFY COLUMN GIORA DATETIME NULL");
            } catch (Exception ignored) {}
            try {
                st.execute("ALTER TABLE CHAM_CONG MODIFY COLUMN SOGIOLAM DOUBLE DEFAULT 0");
            } catch (Exception ignored) {}
            try {
                st.execute("ALTER TABLE CHAM_CONG MODIFY COLUMN TANGCA DOUBLE DEFAULT 0");
            } catch (Exception ignored) {}
            try {
                st.execute("ALTER TABLE CHAM_CONG MODIFY COLUMN GHICHU VARCHAR(255) NULL");
            } catch (Exception ignored) {}
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkIn(int maNhanVien) {
        // Check if already checked in today
        ChamCongModel today = getTodayRecord(maNhanVien);
        if (today != null) {
            return false;
        }

        String sql = "INSERT INTO CHAM_CONG (MANHANVIEN, NGAY, GIOVAO, TRANGTHAI, GHICHU) VALUES (?, CURDATE(), NOW(), 'Có mặt', '')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNhanVien);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkOut(int maNhanVien) {
        ChamCongModel today = getTodayRecord(maNhanVien);
        if (today == null || today.getGioVao() == null || today.getGioRa() != null) {
            return false; // Not checked in or already checked out
        }

        Timestamp gioVao = today.getGioVao();
        Timestamp gioRa = new Timestamp(System.currentTimeMillis());

        // Calculation of hours
        double msPerHour = 1000.0 * 60 * 60;
        double totalHours = (gioRa.getTime() - gioVao.getTime()) / msPerHour;
        if (totalHours < 0) totalHours = 0;

        // Lunch break subtraction: check-in is before 12:00 and check-out is after 13:00
        Calendar calVao = Calendar.getInstance();
        calVao.setTime(gioVao);
        Calendar calRa = Calendar.getInstance();
        calRa.setTime(gioRa);

        boolean crossedLunch = (calVao.get(Calendar.HOUR_OF_DAY) < 12) && (calRa.get(Calendar.HOUR_OF_DAY) >= 13);
        if (crossedLunch) {
            totalHours = Math.max(0, totalHours - 1);
        }

        // Capped standard working hours
        double sogiolam = Math.round(Math.max(0, Math.min(8.0, totalHours)) * 100.0) / 100.0;

        // Overtime: any hours exceeding standard 8 hours of work
        double rawOvertime = totalHours - 8.0;
        double tangca = Math.round(Math.max(0, rawOvertime) * 100.0) / 100.0;

        String sql = "UPDATE CHAM_CONG SET GIORA = ?, SOGIOLAM = ?, TANGCA = ? WHERE MACHAMCONG = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, gioRa);
            ps.setDouble(2, sogiolam);
            ps.setDouble(3, tangca);
            ps.setInt(4, today.getMaChamCong());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ChamCongModel getTodayRecord(int maNhanVien) {
        String sql = "SELECT * FROM CHAM_CONG WHERE MANHANVIEN = ? AND NGAY = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ChamCongModel> getRecordsByEmployee(int maNhanVien, String thangNam) {
        List<ChamCongModel> list = new ArrayList<>();
        String sql;
        boolean hasMonthFilter = (thangNam != null && !thangNam.trim().isEmpty() && thangNam.contains("/"));
        
        if (hasMonthFilter) {
            sql = "SELECT cc.*, nv.HOTEN FROM CHAM_CONG cc " +
                  "JOIN NHAN_VIEN nv ON cc.MANHANVIEN = nv.MANHANVIEN " +
                  "WHERE cc.MANHANVIEN = ? AND DATE_FORMAT(cc.NGAY, '%m/%Y') = ? " +
                  "ORDER BY cc.NGAY DESC";
        } else {
            sql = "SELECT cc.*, nv.HOTEN FROM CHAM_CONG cc " +
                  "JOIN NHAN_VIEN nv ON cc.MANHANVIEN = nv.MANHANVIEN " +
                  "WHERE cc.MANHANVIEN = ? " +
                  "ORDER BY cc.NGAY DESC LIMIT 100";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNhanVien);
            if (hasMonthFilter) {
                ps.setString(2, thangNam);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChamCongModel model = mapRow(rs);
                    model.setHoTen(rs.getString("HOTEN"));
                    list.add(model);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ChamCongModel> getAllRecords(String thangNam) {
        List<ChamCongModel> list = new ArrayList<>();
        String sql;
        boolean hasMonthFilter = (thangNam != null && !thangNam.trim().isEmpty() && thangNam.contains("/"));
        
        if (hasMonthFilter) {
            sql = "SELECT cc.*, nv.HOTEN FROM CHAM_CONG cc " +
                  "JOIN NHAN_VIEN nv ON cc.MANHANVIEN = nv.MANHANVIEN " +
                  "WHERE DATE_FORMAT(cc.NGAY, '%m/%Y') = ? " +
                  "ORDER BY cc.NGAY DESC";
        } else {
            sql = "SELECT cc.*, nv.HOTEN FROM CHAM_CONG cc " +
                  "JOIN NHAN_VIEN nv ON cc.MANHANVIEN = nv.MANHANVIEN " +
                  "ORDER BY cc.NGAY DESC LIMIT 100";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasMonthFilter) {
                ps.setString(1, thangNam);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChamCongModel model = mapRow(rs);
                    model.setHoTen(rs.getString("HOTEN"));
                    list.add(model);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private ChamCongModel mapRow(ResultSet rs) throws SQLException {
        ChamCongModel model = new ChamCongModel();
        model.setMaChamCong(rs.getInt("MACHAMCONG"));
        model.setMaNhanVien(rs.getInt("MANHANVIEN"));
        model.setNgay(rs.getDate("NGAY"));
        model.setGioVao(rs.getTimestamp("GIOVAO"));
        model.setGioRa(rs.getTimestamp("GIORA"));
        model.setSoGioLam(rs.getDouble("SOGIOLAM"));
        model.setTangCa(rs.getDouble("TANGCA"));
        model.setTrangThai(rs.getString("TRANGTHAI"));
        model.setGhiChu(rs.getString("GHICHU"));
        return model;
    }
}
