package is216.petshop.Sales;

import is216.petshop.Product.*;
import is216.petshop.dao.InvoiceDAO;
import is216.petshop.Customer.CustomerDAO;
import is216.petshop.Customer.Customer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SalesController {
    private SalesPanel view;
    private ProductDAO productDAO; // Khai báo thêm DAO

    public SalesController(SalesPanel view) {
        this.view = view;
        this.productDAO = new ProductDAO(); // Khởi tạo DAO 
        
        // Gắn sự kiện lắng nghe nút "Thêm vào giỏ", "Thanh toán", và "Chờ" từ View công khai
        this.view.addAddToCartListener(new AddToCartListener());
        this.view.addCheckoutListener(new CheckoutListener());
        this.view.addPendingListener(new PendingListener());
        this.view.addViewPendingListener(e -> showPendingOrdersList());
        this.view.addFinishedServicesListener(e -> showFinishedServicesList());
        
        // Gắn sự kiện tìm kiếm và chọn khách hàng
        this.view.addFindCustomerListener(e -> {
            String phone = view.getPhoneInput();
            if (phone.isEmpty()) {
                view.showMessage("Vui lòng nhập số điện thoại để tìm!", true);
                return;
            }
            CustomerDAO customerDAO = new CustomerDAO();
            List<Customer> list = customerDAO.search(phone);
            view.showCustomerSelectionDialog(list);
        });
        
        // ===============================================
        // KÍCH HOẠT LẤY DỮ LIỆU TỪ DB LÊN GIAO DIỆN
        // ===============================================
        loadProductData();
    }

    private void loadProductData() {
        // 1. Nhờ DAO chui vào Database lấy toàn bộ sản phẩm
        List<Product> list = productDAO.getAllProducts();
        
        // 2. Nếu lấy thành công, đẩy sang cho View vẽ ra màn hình
        if (list != null) {
            view.displayProducts(list);
        }
    }

    /**
     * Lớp nội bộ xử lý sự kiện khi nhấn nút "Thêm vào giỏ hàng"
     */
    class AddToCartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 1. Lấy thông tin sản phẩm đang được chọn từ giao diện
            Object[] selectedProduct = view.getSelectedProductInfo();
            
            if (selectedProduct == null) {
                view.showMessage("Vui lòng chọn một sản phẩm trước!", true);
                return;
            }

            // Giả định mảng Object chứa: [Mã SP, Tên SP, Giá tiền]
            int productId =  (int)selectedProduct[0];
            String productName = (String) selectedProduct[1];
            long price = (long) selectedProduct[2];
            
            // Lấy số lượng người dùng muốn mua từ ô nhập liệu hoặc mặc định là 1
            int quantity = view.getInputQuantity(); 

            // 2. Yêu cầu View cập nhật dữ liệu vào bảng Giỏ hàng
            view.addProductToCartTable(productId, productName, price, quantity);
            
            // 3. Tính toán và cập nhật lại tổng tiền hiển thị trên đơn hàng
            view.updateTotalPrice();
        }
    }

    /**
     * Lớp nội bộ xử lý sự kiện khi nhấn nút "Thanh toán"
     */
    class CheckoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Kiểm tra xem giỏ hàng có trống không trước khi thanh toán
            if (view.getCartRowCount() == 0) {
                view.showMessage("Giỏ hàng đang trống, không thể thanh toán!", true);
                return;
            }

            // Bắt buộc phải có thông tin khách hàng trước khi thanh toán
            Customer customer = view.getSelectedCustomer();
            if (customer == null) {
                view.showMessage(
                    "Vui lòng chọn khách hàng trước khi thanh toán!\n" +
                    "Nhập số điện thoại và nhấn \"Tìm\" để liên kết khách hàng.",
                    true);
                return;
            }

            // Validate cash received
            long cashReceived = view.getCashReceived();
            long neededTotal = view.getCurrentTotal();

            if (cashReceived <= 0) {
                view.showMessage("Vui lòng nhập số tiền khách đưa trước khi thanh toán!", true);
                return;
            }

            if (cashReceived < neededTotal) {
                view.showMessage("Số tiền khách đưa không đủ để thanh toán!\n(Còn thiếu: " + 
                        String.format("%,d", neededTotal - cashReceived) + " VNĐ)", true);
                return;
            }
            
            // Lấy tổng tiền
            long totalAmount = 0;
            for (int i = 0; i < view.getCartRowCount(); i++) {
                totalAmount += Long.parseLong(view.getCartModel().getValueAt(i, 3).toString());
            }
            
            Integer customerId = customer.getId();


            // Thực hiện lưu hóa đơn qua InvoiceDAO với trạng thái "Hoàn thành"
            InvoiceDAO invoiceDAO = new InvoiceDAO();
            boolean success = invoiceDAO.createOrder(customerId, totalAmount, view.getCartModel(), "Hoàn thành", view.getPointsToUse());
            
            if (success) {
                // If it was linked to a service booking, mark that booking as Hoàn thành (completed/paid)
                Integer bookingId = view.getCurrentBookingId();
                if (bookingId != null) {
                    is216.petshop.Booking.BookingDAO bDao = new is216.petshop.Booking.BookingDAO();
                    bDao.updateStatus(bookingId, "Hoàn thành");
                }

                view.showMessage("Thanh toán đơn hàng thành công! 🐾\nHóa đơn đã được lưu với trạng thái Hoàn thành.", false);
                view.clearCart(); // Xóa sạch giỏ hàng sau khi thanh toán xong
                loadProductData(); // Tải lại sản phẩm để cập nhật số lượng tồn kho mới
            } else {
                view.showMessage("Có lỗi xảy ra khi thanh toán đơn hàng!", true);
            }
        }
    }

    /**
     * Lớp nội bộ xử lý sự kiện khi nhấn nút "Chờ"
     */
    class PendingListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Kiểm tra xem giỏ hàng có trống không
            if (view.getCartRowCount() == 0) {
                view.showMessage("Giỏ hàng đang trống, không thể lưu đơn chờ!", true);
                return;
            }
            
            // Lấy thông tin khách hàng được chọn
            Customer customer = view.getSelectedCustomer();
            Integer customerId = (customer != null) ? customer.getId() : null;
            
            // Lấy tổng tiền
            long totalAmount = 0;
            for (int i = 0; i < view.getCartRowCount(); i++) {
                totalAmount += Long.parseLong(view.getCartModel().getValueAt(i, 3).toString());
            }
            
            // Thực hiện lưu hóa đơn qua InvoiceDAO với trạng thái "Chờ thanh toán"
            InvoiceDAO invoiceDAO = new InvoiceDAO();
            boolean success = invoiceDAO.createOrder(customerId, totalAmount, view.getCartModel(), "Chờ thanh toán", view.getPointsToUse());
            
            if (success) {
                view.showMessage("Lưu đơn chờ thanh toán thành công! 🐾", false);
                view.clearCart(); // Xóa sạch giỏ hàng sau khi lưu xong
                loadProductData(); // Tải lại sản phẩm
            } else {
                view.showMessage("Có lỗi xảy ra khi lưu đơn chờ thanh toán!", true);
            }
        }
    }

    private void showPendingOrdersList() {
        InvoiceDAO dao = new InvoiceDAO();
        java.util.List<InvoiceDAO.PendingOrder> orders = dao.getPendingOrders();
        if (orders.isEmpty()) {
            view.showMessage("Không có đơn hàng nào đang chờ thanh toán!", false);
            return;
        }
        view.showPendingOrdersDialog(orders, this::restorePendingOrderToCart);
    }

    private void restorePendingOrderToCart(InvoiceDAO.PendingOrder order) {
        InvoiceDAO dao = new InvoiceDAO();
        java.util.List<InvoiceDAO.OrderItem> items = dao.getOrderDetails(order.id);
        
        // Clear active cart
        view.clearCart();

        // Populate customer
        if (order.customerId != null) {
            CustomerDAO customerDAO = new CustomerDAO();
            List<Customer> list = customerDAO.search(order.customerPhone);
            if (!list.isEmpty()) {
                view.setSelectedCustomer(list.get(0));
            }
        }

        // Add items to cart
        for (InvoiceDAO.OrderItem item : items) {
            view.addProductToCartTable(item.productId, item.productName, item.price, item.quantity);
        }

        // Update totals
        view.updateTotalPrice();

        // Delete from database so it can be completed/edited without duplication
        boolean deleted = dao.deleteOrder(order.id);
        if (deleted) {
            view.showMessage("Nạp lại đơn hàng HD-" + order.id + " chờ thanh toán thành công! 🐾\nĐơn hàng đã được đưa vào giỏ để xử lý.", false);
        } else {
            view.showMessage("Nạp lại đơn hàng thành công, nhưng có lỗi khi xóa trạng thái chờ!", true);
        }
    }

    private void showFinishedServicesList() {
        is216.petshop.Booking.BookingDAO bDao = new is216.petshop.Booking.BookingDAO();
        java.util.List<is216.petshop.Booking.Booking> finished = bDao.getBookingsByStatus("Chờ thanh toán");
        if (finished.isEmpty()) {
            view.showMessage("Không có dịch vụ hoàn thành nào đang chờ thanh toán!", false);
            return;
        }
        view.showFinishedServicesDialog(finished, booking -> {
            view.loadBookingIntoCart(booking);
        });
    }
}