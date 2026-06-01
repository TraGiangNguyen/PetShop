package is216.petshop.util;

import is216.petshop.dao.ChamCongDAO;
import is216.petshop.dao.DonNghiPhepDAO;
import is216.petshop.dao.PayrollDAO;

import java.sql.Connection;
import java.sql.Statement;

public class TestInsert {
    public static void main(String[] args) {
        System.out.println("🌱 Starting database seeding...");
        
        // 1. Ensure all new tables exist first
        try {
            new ChamCongDAO();
            new DonNghiPhepDAO();
            new PayrollDAO();
            System.out.println("✅ Ensured Chấm công, Nghỉ phép, and Bảng lương tables exist.");
        } catch (Exception e) {
            System.err.println("❌ Failed to ensure tables exist:");
            e.printStackTrace();
            return;
        }

        // 2. Perform mock data seeding
        String[] sqls = {
            // Delete existing records to avoid primary key/unique clashes on rerun
            "DELETE FROM `QUAN_LY_PHEP` WHERE `NAM` = 2026",
            "DELETE FROM `DON_XIN_NGHI_PHEP` WHERE `MANHANVIEN` IN (2, 3, 4)",
            "DELETE FROM `CHAM_CONG` WHERE `MANHANVIEN` IN (1, 2, 3) AND DATE_FORMAT(`NGAY`, '%m/%Y') = '05/2026'",

            // 1. Insert leave balances
            "INSERT INTO `QUAN_LY_PHEP` (`MANHANVIEN`, `NAM`, `TONGPHEP`, `DADUNG`, `CONLAI`) VALUES " +
            "(1, 2026, 12, 0, 12), " +
            "(2, 2026, 12, 2, 10), " +
            "(3, 2026, 12, 0, 12), " +
            "(4, 2026, 12, 1, 11), " +
            "(5, 2026, 12, 0, 12)",

            // 2. Insert leave requests
            "INSERT INTO `DON_XIN_NGHI_PHEP` (`MANHANVIEN`, `LOAINGHI`, `TUNGAY`, `DENNGAY`, `SONGAY`, `LYDO`, `TRANGTHAI`, `NGUOIDUYET`) VALUES " +
            "(2, 'Nghỉ phép năm', '2026-05-15', '2026-05-16', 2, 'Có việc gia đình ở quê', 'Đã duyệt', 1)",
            
            "INSERT INTO `DON_XIN_NGHI_PHEP` (`MANHANVIEN`, `LOAINGHI`, `TUNGAY`, `DENNGAY`, `SONGAY`, `LYDO`, `TRANGTHAI`, `NGUOIDUYET`) VALUES " +
            "(3, 'Nghỉ ốm', '2026-05-29', '2026-05-29', 1, 'Bị sốt xuất huyết đi khám', 'Chờ duyệt', NULL)",
            
            "INSERT INTO `DON_XIN_NGHI_PHEP` (`MANHANVIEN`, `LOAINGHI`, `TUNGAY`, `DENNGAY`, `SONGAY`, `LYDO`, `TRANGTHAI`, `NGUOIDUYET`) VALUES " +
            "(4, 'Nghỉ phép năm', '2026-05-10', '2026-05-10', 1, 'Đi du lịch cá nhân', 'Từ chối', 1)",

            // 3. Insert Chấm công records
            // === Nhân viên 1 ===
            "INSERT INTO `CHAM_CONG` (`MANHANVIEN`, `NGAY`, `GIOVAO`, `GIORA`, `SOGIOLAM`, `TANGCA`, `TRANGTHAI`, `GHICHU`) VALUES " +
            "(1, '2026-05-20', '2026-05-20 08:00:00', '2026-05-20 18:30:00', 8.0, 1.5, 'Có mặt', 'Hoàn thành tốt ca làm')",
            "INSERT INTO `CHAM_CONG` (`MANHANVIEN`, `NGAY`, `GIOVAO`, `GIORA`, `SOGIOLAM`, `TANGCA`, `TRANGTHAI`, `GHICHU`) VALUES " +
            "(1, '2026-05-21', '2026-05-21 08:00:00', '2026-05-21 17:00:00', 8.0, 0.0, 'Có mặt', 'Đúng giờ')",
            "INSERT INTO `CHAM_CONG` (`MANHANVIEN`, `NGAY`, `GIOVAO`, `GIORA`, `SOGIOLAM`, `TANGCA`, `TRANGTHAI`, `GHICHU`) VALUES " +
            "(1, '2026-05-22', '2026-05-22 08:00:00', '2026-05-22 19:00:00', 8.0, 2.0, 'Có mặt', 'Tăng ca đóng kho')",

            // === Nhân viên 2 ===
            "INSERT INTO `CHAM_CONG` (`MANHANVIEN`, `NGAY`, `GIOVAO`, `GIORA`, `SOGIOLAM`, `TANGCA`, `TRANGTHAI`, `GHICHU`) VALUES " +
            "(2, '2026-05-20', '2026-05-20 08:30:00', '2026-05-20 17:00:00', 7.5, 0.0, 'Có mặt', 'Đi muộn 30p')",
            "INSERT INTO `CHAM_CONG` (`MANHANVIEN`, `NGAY`, `GIOVAO`, `GIORA`, `SOGIOLAM`, `TANGCA`, `TRANGTHAI`, `GHICHU`) VALUES " +
            "(2, '2026-05-21', '2026-05-21 08:00:00', '2026-05-21 18:00:00', 8.0, 1.0, 'Có mặt', 'Hỗ trợ khách trễ')",
            "INSERT INTO `CHAM_CONG` (`MANHANVIEN`, `NGAY`, `GIOVAO`, `GIORA`, `SOGIOLAM`, `TANGCA`, `TRANGTHAI`, `GHICHU`) VALUES " +
            "(2, '2026-05-22', '2026-05-22 08:00:00', '2026-05-22 20:00:00', 8.0, 3.0, 'Có mặt', 'Tăng ca kiểm kê cuối tuần')",

            // === Nhân viên 3 ===
            "INSERT INTO `CHAM_CONG` (`MANHANVIEN`, `NGAY`, `GIOVAO`, `GIORA`, `SOGIOLAM`, `TANGCA`, `TRANGTHAI`, `GHICHU`) VALUES " +
            "(3, '2026-05-20', '2026-05-20 08:00:00', '2026-05-20 17:00:00', 8.0, 0.0, 'Có mặt', 'Đúng giờ')",
            "INSERT INTO `CHAM_CONG` (`MANHANVIEN`, `NGAY`, `GIOVAO`, `GIORA`, `SOGIOLAM`, `TANGCA`, `TRANGTHAI`, `GHICHU`) VALUES " +
            "(3, '2026-05-21', '2026-05-21 08:00:00', '2026-05-21 17:00:00', 8.0, 0.0, 'Có mặt', 'Đúng giờ')",
            "INSERT INTO `CHAM_CONG` (`MANHANVIEN`, `NGAY`, `GIOVAO`, `GIORA`, `SOGIOLAM`, `TANGCA`, `TRANGTHAI`, `GHICHU`) VALUES " +
            "(3, '2026-05-22', '2026-05-22 08:00:00', '2026-05-22 18:30:00', 8.0, 1.5, 'Có mặt', 'Hỗ trợ đóng hàng')"
        };

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            if (conn == null) {
                System.err.println("❌ Database connection is null!");
                return;
            }
            
            for (String sql : sqls) {
                st.executeUpdate(sql);
            }
            System.out.println("🎉 Database seeding completed successfully! All mock records inserted.");
        } catch (Exception e) {
            System.err.println("❌ Database seeding failed:");
            e.printStackTrace();
        }
    }
}
