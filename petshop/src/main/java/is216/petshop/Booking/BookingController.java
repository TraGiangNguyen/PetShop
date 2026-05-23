package is216.petshop.Booking;

import is216.petshop.Customer.Customer;
import is216.petshop.Customer.CustomerDAO;
import is216.petshop.Customer.CustomerDialog;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller – wires BookingPanel (View) ↔ BookingDAO (Model).
 */
public class BookingController {

    private final BookingPanel view;
    private final BookingDAO   dao;

    public BookingController(BookingPanel view) {
        this.view = view;
        this.dao  = new BookingDAO();

        dao.ensureTablesExist();

        // Populate service checkboxes
        view.populateServices(dao.getAllServices());

        // Wire listeners
        view.addFindCustomerListener(e -> handleFindCustomer());
        view.addBookListener(e -> handleBook());
        view.addFilterListener(e -> handleFilter(e.getActionCommand()));

        refreshBookingList("");
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void handleFindCustomer() {
        String phone = view.getPhoneInput();
        if (phone.isEmpty()) {
            view.showMessage("Vui lòng nhập số điện thoại!", false);
            return;
        }

        CustomerDAO cDao = new CustomerDAO();
        List<Customer> list = cDao.search(phone);

        if (list.isEmpty()) {
            int reply = JOptionPane.showConfirmDialog(view,
                    "Không tìm thấy khách hàng! Bạn có muốn thêm mới?",
                    "Thêm khách hàng", JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
                CustomerDialog dlg = new CustomerDialog(owner, null, cDao, null);
                dlg.setVisible(true);
                list = cDao.search(phone);
            }
        }

        if (!list.isEmpty()) {
            Customer c = list.get(0);
            view.setSelectedCustomer(c);
            // Load this customer's pets
            Map<Integer, String> pets = dao.getPetsByCustomer(c.getId());
            view.populatePets(pets);
        } else {
            view.setSelectedCustomer(null);
            view.clearPets();
        }
    }

    private void handleBook() {
        // 1. Services must be selected first
        List<Integer> selectedServices = view.getSelectedServiceIds();
        if (selectedServices.isEmpty()) {
            view.showMessage("Vui lòng chọn ít nhất một dịch vụ!", true);
            return;
        }

        // 2. If order has at least 1 service → customer info is mandatory
        Customer c = view.getSelectedCustomer();
        if (c == null) {
            view.showMessage(
                "Lịch hẹn có dịch vụ bắt buộc phải có thông tin khách hàng.\n" +
                "Vui lòng nhập số điện thoại và nhấn \"Tìm\" để liên kết khách hàng!",
                true);
            return;
        }

        // 3. Parse appointment datetime
        Timestamp ts;
        try {
            String raw = view.getDateInput() + " " + view.getTimeInput();
            Date parsed = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(raw);
            ts = new Timestamp(parsed.getTime());
        } catch (Exception ex) {
            view.showMessage("Định dạng ngày/giờ không hợp lệ! Dùng yyyy-MM-dd và HH:mm.", true);
            return;
        }

        Booking b = new Booking();
        b.setMaKh(c.getId());
        b.setMaThuCung(view.getSelectedPetId());
        b.setThoiGianHen(ts);

        boolean ok = dao.insert(b, c.getId(), selectedServices, view.getNoteInput());
        if (ok) {
            view.showMessage("Đã đặt lịch hẹn thành công! 🐾", false);
            view.clearForm();
            refreshBookingList("");
        } else {
            view.showMessage("Đặt lịch thất bại. Kiểm tra lại thông tin!", true);
        }
    }

    private void handleFilter(String text) {
        refreshBookingList(text == null ? "" : text.trim());
    }

    private void handleComplete(int maLichHen) {
        String currentStatus = "Đợi check-in";
        try (java.sql.Connection conn = is216.petshop.util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement("SELECT TRANGTHAI FROM LICH_HEN WHERE MALICHHEN = ?")) {
            ps.setInt(1, maLichHen);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    currentStatus = rs.getString("TRANGTHAI");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String nextStatus = "Hoàn thành";
        if ("Đợi check-in".equalsIgnoreCase(currentStatus)) {
            nextStatus = "Đang thực hiện";
            dao.updateStatus(maLichHen, nextStatus);
            view.showMessage("Check-in thành công! Lịch hẹn đang được thực hiện. 🐾", false);
        } else if ("Đang thực hiện".equalsIgnoreCase(currentStatus)) {
            nextStatus = "Chờ thanh toán";
            dao.updateStatus(maLichHen, nextStatus);
            view.showMessage("Check-out thành công! Lịch hẹn đã chuyển sang chờ thanh toán. 💵", false);
        } else {
            dao.updateStatus(maLichHen, nextStatus);
        }
        refreshBookingList("");
    }

    private void handleCancel(int maLichHen) {
        dao.updateStatus(maLichHen, "Đã hủy");
        refreshBookingList("");
    }

    private void refreshBookingList(String filter) {
        List<Booking> bookings = dao.search(filter);
        view.displayBookings(
                bookings,
                e -> handleComplete((Integer) e.getSource()),
                e -> handleCancel((Integer) e.getSource())
        );
    }
}
