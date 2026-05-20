package is216.petshop.dao;

import is216.petshop.model.Customer;
import is216.petshop.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;

public class CustomerDAO {
    
    public ArrayList<Customer> getAll() {
        ArrayList<Customer> list = new ArrayList<>();
        String sql = "{CALL SP_LAY_DANH_SACH_KHACH_HANG()}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql);
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
        String sql = "{CALL SP_TIM_KIEM_KHACH_HANG(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
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
        String sql = "{CALL SP_THEM_KHACH_HANG(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, c.getName());
            cs.setString(2, c.getPhone());
            cs.setString(3, c.getAddress());
            cs.setString(4, c.getEmail());
            cs.setString(5, c.getPartnerType());
            cs.setInt(6, c.getLoyaltyPoints());
            cs.setDate(7, new java.sql.Date(c.getJoinDate().getTime()));
            return cs.executeUpdate() > 0;
        }
    }

    public boolean update(Customer c) throws SQLException {
        String sql = "{CALL SP_SUA_KHACH_HANG(?, ?, ?, ?, ?, ?, ?, ?)}";
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
        String sql = "{CALL SP_XOA_KHACH_HANG(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, id);
            return cs.executeUpdate() > 0;
        }
    }

    public boolean updateLoyaltyPoints(int id, int change) throws SQLException {
        String sql = "{CALL SP_CAP_NHAT_DIEM_KHACH_HANG(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, id);
            cs.setInt(2, change);
            return cs.executeUpdate() > 0;
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("MADOITAC"));
        c.setName(rs.getString("TENDOITAC"));
        c.setPhone(rs.getString("SODIENTHOAI"));
        c.setAddress(rs.getString("DIACHI"));
        c.setEmail(rs.getString("EMAIL"));
        c.setPartnerType(rs.getString("LOAIDOITAC"));
        c.setLoyaltyPoints(rs.getInt("DIEMTICHLUY"));
        c.setJoinDate(rs.getDate("NGAYTHAMGIA"));
        c.setType(rs.getString("LOAIKHACHHANG"));
        return c;
    }
}

