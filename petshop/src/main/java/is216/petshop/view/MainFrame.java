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
import java.awt.image.BufferedImage;
import java.awt.geom.*;

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
        // Hiển thị thẻ mặc định là Bán hàng
        btnSalesActionPerformed();
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
        pnlSidebar.setPreferredSize(new Dimension(240, 0));
        pnlSidebar.setLayout(new BorderLayout());

        // --- Phần trên của Sidebar (Logo và các nút menu) ---
        JPanel pnlMenu = new JPanel();
        pnlMenu.setOpaque(false); // Làm trong suốt để lộ nền tím
        pnlMenu.setLayout(new BoxLayout(pnlMenu, BoxLayout.Y_AXIS));
        pnlMenu.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Tiêu đề/Logo
        JLabel lblLogo = new JLabel("<html><b style='font-family:\"Segoe UI Emoji\"; font-size:18px'>\uD83D\uDC3E Pet Store</b><br>Quản lý cửa hàng</html>");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        pnlMenu.add(lblLogo);

        // Khởi tạo các nút Menu với icon vẽ bằng Graphics2D
        btnSales = createMenuButton("B\u00e1n h\u00e0ng", "cart");
        btnCustomer = createMenuButton("Kh\u00e1ch h\u00e0ng", "person");
        btnProduct = createMenuButton("S\u1ea3n ph\u1ea9m", "box");
        btnInvoice = createMenuButton("H\u00f3a \u0111\u01a1n", "document");
        btnEmployee = createMenuButton("Nh\u00e2n vi\u00ean", "people");

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

        btnLogout = createMenuButton("\u0110\u0103ng xu\u1ea5t", "logout");
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

    private JButton createMenuButton(String text, String iconType) {
        JButton btn = new JButton(text);
        btn.setIcon(createMenuIcon(iconType));
        btn.setIconTextGap(10);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(60, 45, 130));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hiệu ứng Hover đơn giản
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(80, 60, 160));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(60, 45, 130));
            }
        });
        
        return btn;
    }

    // Vẽ icon 18x18 bằng Graphics2D cho từng loại nút menu
    private ImageIcon createMenuIcon(String type) {
        int s = 18; // Kích thước icon
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (type) {
            case "cart": // Giỏ hàng
                g.drawRect(4, 3, 10, 8);
                g.drawLine(4, 3, 2, 0);
                g.fillOval(5, 13, 4, 4);
                g.fillOval(11, 13, 4, 4);
                break;
            case "person": // Người
                g.fillOval(6, 1, 6, 6);
                g.drawArc(2, 9, 14, 10, 0, 180);
                break;
            case "box": // Hộp/Gói hàng
                g.drawRect(2, 4, 14, 12);
                g.drawLine(2, 4, 9, 0);
                g.drawLine(16, 4, 9, 0);
                g.drawLine(9, 4, 9, 16);
                break;
            case "document": // Tài liệu
                g.drawRect(3, 1, 12, 15);
                g.drawLine(6, 5, 12, 5);
                g.drawLine(6, 8, 12, 8);
                g.drawLine(6, 11, 10, 11);
                break;
            case "people": // Nhóm người
                g.fillOval(3, 2, 5, 5);
                g.drawArc(0, 8, 11, 8, 0, 180);
                g.fillOval(10, 2, 5, 5);
                g.drawArc(7, 8, 11, 8, 0, 180);
                break;
            case "logout": // Mũi tên thoát
                g.drawRect(2, 2, 8, 14);
                g.drawLine(10, 9, 17, 9);
                g.drawLine(14, 5, 17, 9);
                g.drawLine(14, 13, 17, 9);
                break;
        }
        g.dispose();
        return new ImageIcon(img);
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
