package is216.petshop.view;

import is216.petshop.view.ProductForm;
import is216.petshop.view.NhanVienPanel;
import is216.petshop.view.Customerpanel;
import is216.petshop.view.PlaceholderPanel;
import is216.petshop.Login.LoginForm;
import is216.petshop.Login.LoginController;
import is216.petshop.dao.UserDAO;
import is216.petshop.Sales.SalesPanel;
import is216.petshop.Sales.SalesController;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JPanel pnlSidebar;
    private JPanel pnlContent; // Vùng chứa nội dung chính

    private JButton btnSales;
    private JButton btnCustomer;
    private JButton btnProduct;
    private JButton btnInvoice;
    private JButton btnEmployee;
    private JButton btnLogout;
    
    public MainFrame() {
        initComponents(); 
        setLocationRelativeTo(null); 
    
        // Hiển thị thẻ mặc định là Sản phẩm
        showPanel(new ProductForm()); 
    }
    
    private void initComponents() {
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

        // Khởi tạo các nút Menu bằng hàm hỗ trợ
        btnSales = createMenuButton("🛒 Bán hàng");
        btnCustomer = createMenuButton("👤 Khách hàng");
        btnProduct = createMenuButton("📦 Sản phẩm");
        btnInvoice = createMenuButton("📄 Hóa đơn");
        btnEmployee = createMenuButton("👥 Nhân viên");

        // Yêu cầu các nút lắng nghe sự kiện click chuột
        btnProduct.addActionListener(e -> btnProductActionPerformed());
        btnSales.addActionListener(e -> btnSalesActionPerformed());
        btnCustomer.addActionListener(e -> btnCustomerActionPerformed());
        btnInvoice.addActionListener(e -> btnInvoiceActionPerformed());
        btnEmployee.addActionListener(e -> btnEmployeeActionPerformed());
        
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
        btnLogout.addActionListener(e -> {
            LoginForm loginView = new LoginForm();
            new LoginController(loginView, new UserDAO());
            loginView.setVisible(true);
            dispose(); // Đóng cửa sổ hiện tại
        });
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
        
        // Hiệu ứng Hover đơn giản
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

    public void showPanel(JPanel panel) {
        pnlContent.removeAll();
        pnlContent.add(panel, BorderLayout.CENTER);
        pnlContent.revalidate();
        pnlContent.repaint();
    }
    
    private void btnEmployeeActionPerformed() {                                          
        showPanel(new NhanVienPanel());
    }
    
    private void btnSalesActionPerformed() {
        SalesPanel salesPanel = new SalesPanel();
        new SalesController(salesPanel);
        showPanel(salesPanel);
    }
    
    private void btnProductActionPerformed() {                                          
        showPanel(new ProductForm());
    }

    private void btnCustomerActionPerformed() {                                          
        showPanel(new Customerpanel());
    }
    
    private void btnInvoiceActionPerformed() {
        SalesPanel salesPanel = new SalesPanel();
        new SalesController(salesPanel);
        showPanel(salesPanel);
    }
}
