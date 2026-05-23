package is216.petshop.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

public class UserButton extends JButton {
    private String username;
    private boolean isHovered = false;
    private final Color startColor = new Color(108, 93, 211); // COLOR_PRIMARY from NhanVienPanel
    private final Color endColor = new Color(140, 120, 240); // A sleek vibrant lighter purple

    public UserButton(String username) {
        this.username = username != null ? username : "User";
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Xem thông tin tài khoản: " + this.username);
        
        // Match sidebar sizing (perfectly circular button, e.g. 42x42 pixels)
        setPreferredSize(new Dimension(42, 42));
        setMinimumSize(new Dimension(42, 42));
        setMaximumSize(new Dimension(42, 42));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        // Draw circular background gradient
        GradientPaint gp;
        if (getModel().isPressed()) {
            gp = new GradientPaint(x, y, startColor.darker(), x, y + size, endColor.darker());
        } else if (isHovered) {
            gp = new GradientPaint(x, y, startColor.brighter(), x, y + size, endColor.brighter());
        } else {
            gp = new GradientPaint(x, y, startColor, x, y + size, endColor);
        }
        
        g2.setPaint(gp);
        g2.fillOval(x, y, size, size);

        // Draw elegant hover glow border
        if (isHovered) {
            g2.setColor(new Color(255, 255, 255, 200));
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawOval(x + 1, y + 1, size - 2, size - 2);
        } else {
            g2.setColor(new Color(255, 255, 255, 100));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(x + 1, y + 1, size - 2, size - 2);
        }

        // Draw the initial character of username in the center
        String initial = username.substring(0, 1).toUpperCase();
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (size - fm.stringWidth(initial)) / 2;
        int textY = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
        
        g2.drawString(initial, textX, textY);

        g2.dispose();
    }
}
