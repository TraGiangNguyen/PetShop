/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.view;
import javax.swing.*;

/**
 *
 * @author Admin
 */
public class CustomerForm extends JPanel{
        public CustomerForm() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(new JLabel("CUSTOMER MANAGEMENT"));

        add(new JLabel("Name:"));
        add(new JTextField());

        add(new JLabel("Phone:"));
        add(new JTextField());

        add(new JButton("Add"));
    }
}
