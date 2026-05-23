package is216.petshop.view;

import is216.petshop.model.NhanVienModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;

public class UserProfileDialog extends JDialog {
    private String username;
    private NhanVienModel nv;

    private static final Color COLOR_PRIMARY = new Color(108, 93, 211);
    private static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_BG = new Color(248, 250, 252);

    public UserProfileDialog(Frame parent, String username, NhanVienModel nv) {
        super(parent, "Thông tin cá nhân", true);
        this.username = username;
        this.nv = nv;
        
        initComponents();
        setSize(420, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- 1. HEADER PANEL ---
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw elegant top gradient header background
                GradientPaint gp = new GradientPaint(0, 0, new Color(108, 93, 211), 0, getHeight(), new Color(75, 55, 150));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 130);
                
                // Draw a decorative subtle circle pattern in the header
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillOval(getWidth() - 100, -30, 150, 150);
                g2.fillOval(-50, 40, 100, 100);

                // Draw centered circular avatar base
                int avatarSize = 80;
                int ax = (getWidth() - avatarSize) / 2;
                int ay = 130 - (avatarSize / 2);

                // Shadow/white border for avatar
                g2.setColor(Color.WHITE);
                g2.fillOval(ax - 4, ay - 4, avatarSize + 8, avatarSize + 8);

                // Inner avatar circle with gradient
                GradientPaint avatarGp = new GradientPaint(ax, ay, new Color(108, 93, 211).brighter(), ax, ay + avatarSize, new Color(108, 93, 211));
                g2.setPaint(avatarGp);
                g2.fillOval(ax, ay, avatarSize, avatarSize);

                // Initial character inside avatar
                String initial = (nv != null && nv.getHoTen() != null && !nv.getHoTen().isEmpty()) 
                        ? nv.getHoTen().substring(0, 1).toUpperCase() 
                        : username.substring(0, 1).toUpperCase();
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
                FontMetrics fm = g2.getFontMetrics();
                int tx = ax + (avatarSize - fm.stringWidth(initial)) / 2;
                int ty = ay + ((avatarSize - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initial, tx, ty);

                g2.dispose();
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 200));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);

        // Struts to align user labels below avatar
        headerPanel.add(Box.createVerticalStrut(175));

        JLabel lblName = new JLabel(nv != null ? nv.getHoTen() : username);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblName.setForeground(COLOR_TEXT_PRIMARY);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(lblName);

        JLabel lblRole = new JLabel(nv != null ? nv.getChucVu() : "Tài khoản hệ thống");
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRole.setForeground(COLOR_TEXT_SECONDARY);
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(lblRole);

        add(headerPanel, BorderLayout.NORTH);

        // --- 2. DETAILS PANEL ---
        JPanel detailsContainer = new JPanel();
        detailsContainer.setLayout(new BoxLayout(detailsContainer, BoxLayout.Y_AXIS));
        detailsContainer.setBackground(Color.WHITE);
        detailsContainer.setBorder(new EmptyBorder(10, 25, 10, 25));

        // Let's format and display fields neatly
        addInfoRow(detailsContainer, "TÊN ĐĂNG NHẬP", username);

        if (nv != null) {
            addInfoRow(detailsContainer, "SỐ ĐIỆN THOẠI", nv.getSdt() != null ? nv.getSdt() : "---");
            addInfoRow(detailsContainer, "EMAIL", nv.getEmail() != null ? nv.getEmail() : "---");
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String hireDate = nv.getNgayVaoLam() != null ? sdf.format(nv.getNgayVaoLam()) : "---";
            addInfoRow(detailsContainer, "NGÀY VÀO LÀM", hireDate);

            // Create a custom styled panel for Status badge
            JPanel statusRow = new JPanel(new BorderLayout());
            statusRow.setBackground(Color.WHITE);
            statusRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
            statusRow.setPreferredSize(new Dimension(0, 48));
            
            JLabel lblTitle = new JLabel("TRẠNG THÁI");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblTitle.setForeground(COLOR_TEXT_SECONDARY);
            statusRow.add(lblTitle, BorderLayout.WEST);

            // Elegant Pill Badge
            String statusText = nv.getTrangThai() != null ? nv.getTrangThai() : "Đang làm việc";
            JPanel pnlBadge = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    Color pillBg = new Color(220, 252, 231); // light green
                    Color textCol = new Color(22, 163, 74); // dark green
                    
                    if (statusText.toLowerCase().contains("nghỉ việc")) {
                        pillBg = new Color(254, 226, 226); // light red
                        textCol = new Color(220, 38, 38); // dark red
                    }

                    gBoBo(g2, getWidth(), getHeight(), pillBg, textCol);
                    g2.dispose();
                }

                private void gBoBo(Graphics2D g2, int w, int h, Color bg, Color fg) {
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 2, w, h - 4, h - 4, h - 4);
                    g2.setColor(fg);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (w - fm.stringWidth(statusText)) / 2;
                    int ty = ((h - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(statusText, tx, ty);
                }
            };
            pnlBadge.setOpaque(false);
            pnlBadge.setPreferredSize(new Dimension(110, 24));
            
            JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
            rightWrap.setOpaque(false);
            rightWrap.add(pnlBadge);
            statusRow.add(rightWrap, BorderLayout.EAST);
            
            detailsContainer.add(statusRow);
        } else {
            addInfoRow(detailsContainer, "LOẠI TÀI KHOẢN", "Quản trị viên Hệ thống");
            addInfoRow(detailsContainer, "MÔ TẢ", "Tài khoản quản trị gốc của ứng dụng");
        }

        add(detailsContainer, BorderLayout.CENTER);

        // --- 3. BOTTOM BUTTON PANEL ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        bottomPanel.setBackground(COLOR_BG);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));
        
        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setBackground(COLOR_PRIMARY);
        btnClose.setForeground(Color.WHITE);
        btnClose.setPreferredSize(new Dimension(160, 38));
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.putClientProperty("FlatLaf.style", "arc: 12");
        btnClose.addActionListener(e -> dispose());
        
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addInfoRow(JPanel parent, String title, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        row.setPreferredSize(new Dimension(0, 48));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(COLOR_TEXT_SECONDARY);
        row.add(lblTitle, BorderLayout.WEST);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValue.setForeground(COLOR_TEXT_PRIMARY);
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(lblValue, BorderLayout.EAST);

        parent.add(row);
    }
}
