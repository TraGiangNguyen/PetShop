package is216.petshop.Customer;

import is216.petshop.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;

public class CustomerDAO {
    
    public ArrayList<Customer> getAll() {
        ArrayList<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM KHACH_HANG";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement cs = conn.prepareStatement(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<Customer> search(String query) {
        ArrayList<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM KHACH_HANG WHERE SODIENTHOAI = (?) ";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement cs = conn.prepareStatement(sql)) {
            cs.setString(1, query);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Customer c) throws SQLException {
        String sql = "{CALL sp_ThemKhachHang(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, c.getName());
            cs.setString(2, c.getPhone());
            cs.setString(3, c.getAddress());
            cs.setString(4, c.getEmail());
            cs.setInt(5, c.getLoyaltyPoints());
            cs.setDate(6, new java.sql.Date(c.getJoinDate().getTime()));
            return cs.executeUpdate() > 0;
        }
    }

    public boolean update(Customer c) throws SQLException {
        String sql = "{CALL sp_SuaKhachHang(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, c.getId());
            cs.setString(2, c.getName());
            cs.setString(3, c.getPhone());
            cs.setString(4, c.getAddress());
            cs.setString(5, c.getEmail());
            cs.setString(6, c.getPartnerType());
            cs.setInt(7, c.getLoyaltyPoints());
            cs.setDate(8, new java.sql.Date(c.getJoinDate().getTime()));
            return cs.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "{CALL sp_XoaKhachHang(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, id);
            return cs.executeUpdate() > 0;
        }
    }

    public boolean updateLoyaltyPoints(int id, int change) throws SQLException {
        String sql = "{CALL sp_CapNhatDiemTichLuyKhachHang(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, id);
            cs.setInt(2, change);
            return cs.executeUpdate() > 0;
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("MAKH"));
        c.setName(rs.getString("TENKH"));
        c.setPhone(rs.getString("SODIENTHOAI"));
        c.setAddress(rs.getString("DIACHI"));
        c.setEmail(rs.getString("EMAIL"));
        c.setLoyaltyPoints(rs.getInt("DIEMTICHLUY"));
        c.setJoinDate(rs.getDate("NGAYBATDAU"));
        return c;
    }
}

