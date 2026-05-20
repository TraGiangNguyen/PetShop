package is216.petshop.Sales;

import is216.petshop.Product.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SalesController {
    private SalesPanel view;
    private ProductDAO productDAO; // Khai báo thêm DAO
    // Sau này bạn có thể khai báo thêm ProductDAO hoặc InvoiceDAO ở đây để kết nối Database

    public SalesController(SalesPanel view) {
        this.view = view;
        this.productDAO = new ProductDAO(); // Khởi tạo DAO 
        // Gắn sự kiện lắng nghe nút "Thêm vào giỏ" và "Thanh toán" từ View công khai
        this.view.addAddToCartListener(new AddToCartListener());
        this.view.addCheckoutListener(new CheckoutListener());
        
        // ===============================================
        // KÍCH HOẠT LẤY DỮ LIỆU TỪ DB LÊN GIAO DIỆN
        // ===============================================\
        
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
            
            // TODO: Nơi gọi InvoiceDAO để lưu hóa đơn vào cơ sở dữ liệu MySQL
            
            view.showMessage("Thanh toán đơn hàng thành công! 🐾", false);
            view.clearCart(); // Xóa sạch giỏ hàng sau khi thanh toán xong
        }
    }
}