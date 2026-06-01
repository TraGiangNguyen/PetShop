package is216.petshop.Login;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class LoginForm extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginForm() {
        setTitle("Quản Lý Thú Cưng - Đăng Nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
        }

    private void initComponents() {
        // ... (Giữ nguyên toàn bộ code cấu hình giao diện JPanel, JLabel, Layout của bạn ở đây)
        JPanel root = new JPanel();
        root.setBackground(Color.WHITE);
        root.setLayout(new GridBagLayout());
        setContentPane(root);

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 48, 48, 48));
        card.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));

        JLabel iconLabel = new JLabel(createDogIcon(72));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(20));

        JLabel lblTitle = new JLabel("Quản Lý Thú Cưng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(30, 30, 50));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(6));

        JLabel lblSub = new JLabel("Đăng nhập để tiếp tục");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(130, 130, 150));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSub);
        card.add(Box.createVerticalStrut(36));

        int formWidth = 300;
        
        // ── Username label ──
        JLabel lblUser = new JLabel("Tên đăng nhập");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(new Color(50, 50, 70));
        // Sửa ở đây: Căn giữa theo trục X của Panel, set kích thước bằng với ô nhập
        lblUser.setMaximumSize(new Dimension(formWidth, 20)); 
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblUser);
        card.add(Box.createVerticalStrut(4));

        // ── Username field ──
        txtUsername = new JTextField();
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tên đăng nhập");
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setPreferredSize(new Dimension(formWidth, 48));
        // Sửa ở đây: Giới hạn chiều rộng ở mức 300 thay vì Integer.MAX_VALUE
        txtUsername.setMaximumSize(new Dimension(formWidth, 48)); 
        txtUsername.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; borderWidth: 1.5; " +
                "background: #FFFFFF; foreground: #1e1e32;");
        // Sửa ở đây: Căn giữa
        txtUsername.setAlignmentX(Component.CENTER_ALIGNMENT); 
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(16));

        // ── Password label ──
        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(new Color(50, 50, 70));

        // Sửa ở đây tương tự username
        lblPass.setMaximumSize(new Dimension(formWidth, 20)); 
        lblPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblPass);
        card.add(Box.createVerticalStrut(4));

        // ── Password field ──
        txtPassword = new JPasswordField();
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu");
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(formWidth, 48));
        // Sửa ở đây
        txtPassword.setMaximumSize(new Dimension(formWidth, 48)); 
        txtPassword.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; borderWidth: 1.5; " +
                "background: #FFFFFF; foreground: #1e1e32;");
        txtPassword.putClientProperty(FlatClientProperties.STYLE_CLASS, "passwordField");
        // Sửa ở đây
        txtPassword.setAlignmentX(Component.CENTER_ALIGNMENT); 
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(32));

        // ── Login button (Giữ nguyên phần vẽ nút của bạn) ──
        btnLogin = new JButton("Đăng nhập") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = new Color(91, 91, 214);
                Color hover = new Color(72, 72, 190);
                g2.setColor(getModel().isRollover() ? hover : base);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(formWidth, 52));
        // Sửa ở đây
        btnLogin.setMaximumSize(new Dimension(formWidth, 52)); 
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Sửa ở đây
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT); 

        // Các thiết lập phím tắt...
        txtUsername.addActionListener(e -> txtPassword.requestFocus());
        card.add(btnLogin);
        
        root.add(card);
    }

    // ── CÁC HÀM GETTER & HELPER CHO CONTROLLER GỌI ──

    public String getUsername() {
        return txtUsername.getText().trim();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword());
    }

    public void clearPassword() {
        txtPassword.setText("");
        txtPassword.requestFocus();
    }

    public void showMessage(String msg, boolean isError) {
        int messageType = isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
        String title = isError ? "Lỗi đăng nhập" : "Thành công";
        JOptionPane.showMessageDialog(this, msg, title, messageType);
    }

    // Cung cấp hàm để Controller gắn sự kiện (Cho cả nút bấm và khi ấn Enter ở text pass)
    public void addLoginListener(ActionListener listener) {
        btnLogin.addActionListener(listener);
        txtPassword.addActionListener(listener);
    }

    // T\u1ea3i logo t\u1eeb file \u1ea3nh trong resources
    private ImageIcon createDogIcon(int size) {
        try {
            java.net.URL imgURL = getClass().getClassLoader().getResource("images/dog_logo.png");
            if (imgURL != null) {
                ImageIcon original = new ImageIcon(imgURL);
                Image scaled = original.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Fallback: v\u1ebd icon \u0111\u01a1n gi\u1ea3n n\u1ebfu kh\u00f4ng t\u00ecm th\u1ea5y \u1ea3nh
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(91, 91, 214));
        g.fillOval(0, 0, size, size);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("P", (size - fm.stringWidth("P")) / 2, (size + fm.getAscent() - fm.getDescent()) / 2);
        g.dispose();
        return new ImageIcon(img);
    }
}