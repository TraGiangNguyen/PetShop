package is216.petshop.view;

import javax.swing.*;
import java.awt.*;

public class PlaceholderPanel extends JPanel {
    public PlaceholderPanel(String message) {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        
        JLabel lblMessage = new JLabel(message);
        lblMessage.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblMessage.setForeground(new Color(150, 150, 150));
        lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
        
        add(lblMessage, BorderLayout.CENTER);
    }
}
