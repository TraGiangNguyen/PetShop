package is216.petshop.util;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class LogoPanel extends JPanel {
    
    private final int logoSize = 150;

    public LogoPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(logoSize, logoSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Bật khử răng cưa để nét vẽ mịn màng
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = (getWidth() - logoSize) / 2;
        int y = (getHeight() - logoSize) / 2;

        // 1. Vẽ nền hình tròn màu tím
        g2d.setColor(new Color(87, 75, 240)); 
        g2d.fillOval(x, y, logoSize, logoSize);

        // 2. Thiết lập cọ vẽ màu trắng, nét dày, bo tròn góc
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Tọa độ tâm của logo để dễ căn chỉnh
        int cx = x + logoSize / 2;
        int cy = y + logoSize / 2;

        // 3. Vẽ các bộ phận của mặt cún
        // Viền cằm dưới (hình vòng cung)
        g2d.drawArc(cx - 35, cy - 15, 70, 60, 180, 180); 
        
        // Đỉnh đầu nối 2 tai
        g2d.drawArc(cx - 15, cy - 40, 30, 20, 0, 180);

        // Tai trái
        g2d.drawArc(cx - 45, cy - 40, 25, 45, 60, 180);
        
        // Tai phải
        g2d.drawArc(cx + 20, cy - 40, 25, 45, -60, 180);

        // 4. Vẽ mắt (2 chấm tròn)
        g2d.fillOval(cx - 16, cy + 5, 8, 8); // Mắt trái
        g2d.fillOval(cx + 8, cy + 5, 8, 8);  // Mắt phải

        // 5. Vẽ mũi (Hình tam giác lật ngược hoặc trái tim nhỏ)
        int[] noseX = {cx - 6, cx + 6, cx};
        int[] noseY = {cy + 22, cy + 22, cy + 30};
        g2d.fillPolygon(noseX, noseY, 3);
    }
}
