package is216.petshop.Product;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ProductCard extends JPanel {
    private int productId;
    private String productName;
    private long price;
    
    private JLabel lblImage;
    private JLabel lblName;
    private JLabel lblPrice;
    private JButton btnAddToCart;

    public ProductCard(int id, String name, long price) {
        this.productId = id;
        this.productName = name;
        this.price = price;
        
        initComponents();
    }

    private void initComponents() {
        // Thiết lập kích thước và khung (Border) cho thẻ
        setPreferredSize(new Dimension(200, 250));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 1. Hình ảnh sản phẩm (Dùng icon mặc định tạm thời)
        lblImage = new JLabel("🛒", SwingConstants.CENTER);
        lblImage.setFont(new Font("Segoe UI", Font.PLAIN, 50));
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblImage.setPreferredSize(new Dimension(180, 120));
        
        // 2. Tên sản phẩm
        lblName = new JLabel(productName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 3. Giá tiền
        lblPrice = new JLabel(String.format("%,.0f VNĐ", price));
        lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPrice.setForeground(new Color(220, 53, 69)); // Màu đỏ cho giá tiền
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 4. Nút Thêm vào giỏ
        btnAddToCart = new JButton("+ Thêm vào giỏ");
        btnAddToCart.setBackground(new Color(60, 45, 130));
        btnAddToCart.setForeground(Color.WHITE);
        btnAddToCart.setFocusPainted(false);
        btnAddToCart.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAddToCart.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Lắp ráp các thành phần vào thẻ
        add(Box.createRigidArea(new Dimension(0, 15)));
        add(lblImage);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(lblName);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(lblPrice);
        add(Box.createVerticalGlue()); // Đẩy nút bấm xuống đáy
        add(btnAddToCart);
        add(Box.createRigidArea(new Dimension(0, 15)));
    }

    // Hàm public để Controller có thể gắn sự kiện vào nút bấm của thẻ này
    public void addAddToCartListener(ActionListener listener) {
        btnAddToCart.addActionListener(listener);
    }
    
    // Các hàm Getters để lấy thông tin khi bấm nút
    public long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public long getPrice() { return price; }
}