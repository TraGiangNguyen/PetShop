package is216.petshop.util;

import is216.petshop.Product.ProductPanel;
import is216.petshop.Employee.EmployeePanel;
import is216.petshop.Customer.CustomerPanel;
import is216.petshop.Sales.SalesPanel;
import is216.petshop.Product.ProductController;
// THÊM IMPORT CHO SALES CONTROLLER Ở ĐÂY
import is216.petshop.Sales.SalesController; 
import is216.petshop.view.InvoicePanel;
import javax.swing.*;
import java.awt.*;

// Nếu bạn dùng kéo thả NetBeans, hãy chọn tạo JFrame Form
public class MainView extends JFrame {
    private JPanel pnlSidebar;
    private JPanel pnlContent; // Vùng chứa nội dung chính

    private JButton btnSales;
    private JButton btnCustomer;
    private JButton btnProduct;
    private JButton btnInvoice;
    private JButton btnEmployee;
    private JButton btnLogout;
    
    public MainView() {
        // 1. Gọi hàm dựng bộ khung (đã bao gồm setTitle, setSize, tạo pnlSidebar, pnlContent...)
        initComponents(); 
    
        // 2. Căn giữa màn hình (Nên gọi sau khi đã có Size từ initComponents)
        setLocationRelativeTo(null); 
    
        // 3. Hiển thị thẻ mặc định là Bán hàng
        // --- CODE MỚI THÊM VÀO ĐÂY ---
        SalesPanel salesPanel = new SalesPanel();
        new SalesController(salesPanel); // Kết nối View và Controller bán hàng lại với nhau
        showPanel(salesPanel); // Nhúng panel vào vùng pnlContent chính
    }
    
    // 2. Hàm vẽ giao diện chính
    private void initComponents() {
        // Cài đặt thông số cơ bản cho Cửa sổ (JFrame)
        setTitle("Pet Store - Quản lý cửa hàng");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ==========================================
        // KHU VỰC 1: SIDEBAR (Thanh Menu màu tím bên trái)
        // ==========================================
        pnlSidebar = new JPanel();
        pnlSidebar.setBackground(new Color(60, 45, 130)); // Màu tím giống trong thiết kế
        pnlSidebar.setPreferredSize(new Dimension(220, 0));
        pnlSidebar.setLayout(new BorderLayout());

        // --- Phần trên của Sidebar (Logo và các nút menu) ---
        JPanel pnlMenu = new JPanel();
        pnlMenu.setOpaque(false); // Làm trong suốt để lộ nền tím
        pnlMenu.setLayout(new BoxLayout(pnlMenu, BoxLayout.Y_AXIS));
        pnlMenu.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tiêu đề/Logo
        JLabel lblLogo = new JLabel("<html><b style='font-size:18px'>🐾 Pet Store</b><br>Quản lý cửa hàng</html>");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        pnlMenu.add(lblLogo);

        // Khởi tạo các nút Menu bằng hàm hỗ trợ (viết ở dưới)
        btnSales = createMenuButton("🛒 Bán hàng");
        btnCustomer = createMenuButton("👤 Khách hàng");
        btnProduct = createMenuButton("📦 Sản phẩm");
        btnInvoice = createMenuButton("📄 Hóa đơn");
        btnEmployee = createMenuButton("👥 Nhân viên");

        //Yêu cầu các nút lắng nghe sự kiện click chuột
        btnProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProductActionPerformed(evt);
            }
        });
        btnSales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalesActionPerformed(evt);
            }
        });
        btnCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomerActionPerformed(evt);
            }
        });
        
        btnInvoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInvoiceActionPerformed(evt);
            }
        });
        
        btnEmployee.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEmployeeActionPerformed(evt);
            }
        });
        
        // Thêm các nút vào panel, chèn khoảng trống 10px giữa các nút
        pnlMenu.add(btnSales);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlMenu.add(btnCustomer);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlMenu.add(btnProduct);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlMenu.add(btnInvoice);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlMenu.add(btnEmployee);

        pnlSidebar.add(pnlMenu, BorderLayout.NORTH);

        // --- Phần dưới của Sidebar (Thông tin Admin & Đăng xuất) ---
        JPanel pnlBottom = new JPanel();
        pnlBottom.setOpaque(false);
        pnlBottom.setLayout(new BoxLayout(pnlBottom, BoxLayout.Y_AXIS));
        pnlBottom.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblUser = new JLabel("<html><span style='font-size:10px'>Xin chào,</span><br><b style='font-size:14px'>admin</b></html>");
        lblUser.setForeground(Color.WHITE);
        pnlBottom.add(lblUser);
        pnlBottom.add(Box.createRigidArea(new Dimension(0, 15)));

        btnLogout = createMenuButton("🚪 Đăng xuất");
        pnlBottom.add(btnLogout);

        pnlSidebar.add(pnlBottom, BorderLayout.SOUTH);

        // ==========================================
        // KHU VỰC 2: CONTENT (Vùng chứa nội dung bên phải)
        // ==========================================
        pnlContent = new JPanel();
        pnlContent.setBackground(new Color(245, 245, 245)); // Nền xám nhạt hiện đại
        pnlContent.setLayout(new BorderLayout());

        // ==========================================
        // LẮP RÁP VÀO CỬA SỔ CHÍNH
        // ==========================================
        add(pnlSidebar, BorderLayout.WEST);
        add(pnlContent, BorderLayout.CENTER);
    }

    // 3. Hàm hỗ trợ để tạo thiết kế chung cho các nút bấm ở Sidebar
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(60, 45, 130)); // Trùng màu nền để tệp vào Sidebar
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); // Chiều rộng kéo hết cỡ
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hiệu ứng Hover đơn giản nếu đang không dùng thư viện xịn
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(80, 60, 160)); // Sáng lên khi di chuột vào
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(60, 45, 130)); // Trở về màu cũ
            }
        });
        
        return btn;
    }

    // Hàm thay đổi Panel mà chúng ta đã nói ở các bước trước
    public void showPanel(JPanel panel) {
        pnlContent.removeAll();
        pnlContent.add(panel, BorderLayout.CENTER);
        pnlContent.revalidate();
        pnlContent.repaint();
    }
    
    private void btnEmployeeActionPerformed(java.awt.event.ActionEvent evt) {                                          
        showPanel(new EmployeePanel());
    }
    
    private void btnSalesActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // --- CODE MỚI THÊM VÀO ĐÂY ---
        SalesPanel salesPanel = new SalesPanel();
        new SalesController(salesPanel); // Kết nối View và Controller bán hàng lại với nhau
        showPanel(salesPanel); // Nhúng panel vào vùng pnlContent chính
    }
    
    private void btnProductActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // 1. Tạo Giao diện
        ProductPanel productPanel = new ProductPanel();
        
        // 2. TẠO CONTROLLER VÀ TRUYỀN GIAO DIỆN VÀO (Rất nhiều bạn quên bước này!)
        // Nếu thiếu dòng này, nút Thêm sẽ bị liệt vì không có ai lắng nghe nó
        ProductController controller = new ProductController(productPanel);
        
        // 3. Hiển thị lên màn hình
        showPanel(productPanel);
    }

    private void btnCustomerActionPerformed(java.awt.event.ActionEvent evt) {                                          
        showPanel(new CustomerPanel());
    }
    
    
    private void btnInvoiceActionPerformed(java.awt.event.ActionEvent evt) {                                          
        showPanel(new InvoicePanel());
    }
}