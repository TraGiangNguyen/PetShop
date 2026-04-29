/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.view;
import javax.swing.*;
import java.awt.*;
/**
 *
 * @author Admin
 */
public class MainFrame extends JFrame{
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    public MainFrame(){
        setTitle("Pet Shop");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);


        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        
        // Add các màn hình
        mainPanel.add(new ProductForm(), "product");
        mainPanel.add(new CustomerForm(), "customer");

        // Sidebar
        JPanel sidebar = new JPanel(new GridLayout(3, 1));

        JButton btnProduct = new JButton("Product");
        JButton btnCustomer = new JButton("Customer");
        JButton btnLogout = new JButton("Logout");

        sidebar.add(btnProduct);
        sidebar.add(btnCustomer);
        sidebar.add(btnLogout);

        // Event
        btnProduct.addActionListener(e -> cardLayout.show(mainPanel, "product"));
        btnCustomer.addActionListener(e -> cardLayout.show(mainPanel, "customer"));

        btnLogout.addActionListener(e -> {
            new LoginForm().setVisible(true);
            dispose();
        });

        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);
    }
}
