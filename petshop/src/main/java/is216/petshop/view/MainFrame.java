/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author Admin
 */
public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public MainFrame() {
        setTitle("Pet Shop - Quản lý khách hàng");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Add các màn hình
        mainPanel.add(new Customerpanel(), "customer");

        // Sidebar
        JPanel sidebar = new JPanel(new GridLayout(2, 1));

        JButton btnCustomer = new JButton("Customer");
        JButton btnLogout = new JButton("Logout");

        sidebar.add(btnCustomer);
        sidebar.add(btnLogout);

        // Event
        btnCustomer.addActionListener(e -> cardLayout.show(mainPanel, "customer"));

        btnLogout.addActionListener(e -> {
            new LoginForm().setVisible(true);
            dispose();
        });

        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);
    }
}
