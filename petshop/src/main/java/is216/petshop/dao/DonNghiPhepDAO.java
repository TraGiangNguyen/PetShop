package is216.petshop.dao;

import is216.petshop.model.DonNghiPhepModel;
import is216.petshop.model.QuanLyPhepModel;
import is216.petshop.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DonNghiPhepDAO {

    public DonNghiPhepDAO() {
        ensureTablesExist();
    }

    public void ensureTablesExist() {
        String phepDdl = "CREATE TABLE IF NOT EXISTS QUAN_LY_PHEP (" +
                         "  IDPHEP       INT AUTO_INCREMENT PRIMARY KEY," +
                         "  MANHANVIEN   INT NOT NULL," +
                         "  NAM          INT NOT NULL," +
                         "  TONGPHEP     INT NOT NULL DEFAULT 12," +
                         "  DADUNG       INT NOT NULL DEFAULT 0," +
                         "  CONLAI       INT NOT NULL DEFAULT 12" +
                         ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        String donDdl = "CREATE TABLE IF NOT EXISTS DON_XIN_NGHI_PHEP (" +
                        "  MADON        INT AUTO_INCREMENT PRIMARY KEY," +
                        "  MANHANVIEN   INT NOT NULL," +
                        "  LOAINGHI     VARCHAR(255) NOT NULL," +
                        "  TUNGAY       DATE NOT NULL," +
                        "  DENNGAY      DATE NOT NULL," +
                        "  SONGAY       INT NOT NULL," +
                        "  LYDO         VARCHAR(255) NOT NULL," +
                        "  TRANGTHAI    VARCHAR(255) DEFAULT 'Chờ duyệt' NOT NULL," +
                        "  NGUOIDUYET   INT" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(phepDdl);
            st.execute(donDdl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public QuanLyPhepModel getLeaveBalance(int maNhanVien, int nam) {
        String sql = "SELECT * FROM QUAN_LY_PHEP WHERE MANHANVIEN = ? AND NAM = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNhanVien);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    QuanLyPhepModel balance = new QuanLyPhepModel();
                    balance.setIdPhep(rs.getInt("IDPHEP"));
                    balance.setMaNhanVien(rs.getInt("MANHANVIEN"));
                    balance.setNam(rs.getInt("NAM"));
                    balance.setTongPhep(rs.getInt("TONGPHEP"));
                    balance.setDaDung(rs.getInt("DADUNG"));
                    balance.setConLai(rs.getInt("CONLAI"));
                    return balance;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If not exists, insert a default balance of 12 days for this year
        try {
            String insertSql = "INSERT INTO QUAN_LY_PHEP (MANHANVIEN, NAM, TONGPHEP, DADUNG, CONLAI) VALUES (?, ?, 12, 0, 12)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, maNhanVien);
                ps.setInt(2, nam);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return new QuanLyPhepModel(keys.getInt(1), maNhanVien, nam, 12, 0, 12);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new QuanLyPhepModel(0, maNhanVien, nam, 12, 0, 12);
    }

    public boolean submitRequest(DonNghiPhepModel model) {
        String sql = "INSERT INTO DON_XIN_NGHI_PHEP (MANHANVIEN, LOAINGHI, TUNGAY, DENNGAY, SONGAY, LYDO, TRANGTHAI) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'Chờ duyệt')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, model.getMaNhanVien());
            ps.setString(2, model.getLoaiNghi());
            ps.setDate(3, new java.sql.Date(model.getTuNgay().getTime()));
            ps.setDate(4, new java.sql.Date(model.getDenNgay().getTime()));
            ps.setInt(5, model.getSoNgay());
            ps.setString(6, model.getLyDo());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<DonNghiPhepModel> getRequestsByEmployee(int maNhanVien) {
        List<DonNghiPhepModel> list = new ArrayList<>();
        String sql = "SELECT dn.*, nv.HOTEN as HOTEN, approver.HOTEN as TEN_NGUOIDUYET " +
                     "FROM DON_XIN_NGHI_PHEP dn " +
                     "JOIN NHAN_VIEN nv ON dn.MANHANVIEN = nv.MANHANVIEN " +
                     "LEFT JOIN NHAN_VIEN approver ON dn.NGUOIDUYET = approver.MANHANVIEN " +
                     "WHERE dn.MANHANVIEN = ? ORDER BY dn.TUNGAY DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DonNghiPhepModel don = mapRow(rs);
                    don.setHoTen(rs.getString("HOTEN"));
                    don.setTenNguoiDuyet(rs.getString("TEN_NGUOIDUYET"));
                    list.add(don);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<DonNghiPhepModel> getAllRequests() {
        List<DonNghiPhepModel> list = new ArrayList<>();
        String sql = "SELECT dn.*, nv.HOTEN as HOTEN, approver.HOTEN as TEN_NGUOIDUYET " +
                     "FROM DON_XIN_NGHI_PHEP dn " +
                     "JOIN NHAN_VIEN nv ON dn.MANHANVIEN = nv.MANHANVIEN " +
                     "LEFT JOIN NHAN_VIEN approver ON dn.NGUOIDUYET = approver.MANHANVIEN " +
                     "ORDER BY dn.TRANGTHAI = 'Chờ duyệt' DESC, dn.TUNGAY DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                DonNghiPhepModel don = mapRow(rs);
                don.setHoTen(rs.getString("HOTEN"));
                don.setTenNguoiDuyet(rs.getString("TEN_NGUOIDUYET"));
                list.add(don);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateRequestStatus(int maDon, String trangThai, int maNguoiDuyet) {
        Connection conn = null;
        PreparedStatement psUpdate = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);

            // 1. Update the request status
            String updateSql = "UPDATE DON_XIN_NGHI_PHEP SET TRANGTHAI = ?, NGUOIDUYET = ? WHERE MADON = ?";
            psUpdate = conn.prepareStatement(updateSql);
            psUpdate.setString(1, trangThai);
            psUpdate.setInt(2, maNguoiDuyet);
            psUpdate.setInt(3, maDon);
            int rows = psUpdate.executeUpdate();
            if (rows == 0) {
                conn.rollback();
                return false;
            }

            // 2. If approved, deduct leave days from QUAN_LY_PHEP
            if ("Đã duyệt".equals(trangThai)) {
                // Get request info
                String reqSql = "SELECT MANHANVIEN, SONGAY, TUNGAY FROM DON_XIN_NGHI_PHEP WHERE MADON = ?";
                int empId = 0, days = 0;
                Date startDate = null;
                try (PreparedStatement psReq = conn.prepareStatement(reqSql)) {
                    psReq.setInt(1, maDon);
                    try (ResultSet rs = psReq.executeQuery()) {
                        if (rs.next()) {
                            empId = rs.getInt("MANHANVIEN");
                            days = rs.getInt("SONGAY");
                            startDate = rs.getDate("TUNGAY");
                        }
                    }
                }

                if (empId > 0 && days > 0 && startDate != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(startDate);
                    int year = cal.get(Calendar.YEAR);

                    // Ensure balance row exists first
                    getLeaveBalance(empId, year); 

                    // Update balance
                    String balanceSql = "UPDATE QUAN_LY_PHEP SET DADUNG = DADUNG + ?, CONLAI = CONLAI - ? WHERE MANHANVIEN = ? AND NAM = ?";
                    try (PreparedStatement psBal = conn.prepareStatement(balanceSql)) {
                        psBal.setInt(1, days);
                        psBal.setInt(2, days);
                        psBal.setInt(3, empId);
                        psBal.setInt(4, year);
                        psBal.executeUpdate();
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
            if (psUpdate != null) {
                try { psUpdate.close(); } catch (Exception ex) {}
            }
            if (conn != null) {
                try { conn.close(); } catch (Exception ex) {}
            }
        }
    }

    private DonNghiPhepModel mapRow(ResultSet rs) throws SQLException {
        DonNghiPhepModel model = new DonNghiPhepModel();
        model.setMaDon(rs.getInt("MADON"));
        model.setMaNhanVien(rs.getInt("MANHANVIEN"));
        model.setLoaiNghi(rs.getString("LOAINGHI"));
        model.setTuNgay(rs.getDate("TUNGAY"));
        model.setDenNgay(rs.getDate("DENNGAY"));
        model.setSoNgay(rs.getInt("SONGAY"));
        model.setLyDo(rs.getString("LYDO"));
        model.setTrangThai(rs.getString("TRANGTHAI"));
        model.setNguoiDuyet((Integer) rs.getObject("NGUOIDUYET"));
        return model;
    }
}
