/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.view;

import is216.petshop.model.Product;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import is216.petshop.dao.ProductDAO;

/**
 *
 * @author Admin
 */
public class ProductForm extends JPanel {

    private JTextField txtName, txtPrice;
    private JComboBox<String> cbType;
    private JTable table;
    private DefaultTableModel tableModel;

    private ProductDAO dao = new ProductDAO();
    private ArrayList<Product> list = new ArrayList<>();
    private int selectedIndex = -1;

    public ProductForm() {
        setLayout(new BorderLayout());

        // ===== FORM =====
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        formPanel.add(new JLabel("Name:"));
        txtName = new JTextField();
        formPanel.add(txtName);

        formPanel.add(new JLabel("Type:"));
        cbType = new JComboBox<>(new String[]{"Dog", "Cat", "Accessory"});
        formPanel.add(cbType);

        formPanel.add(new JLabel("Price:"));
        txtPrice = new JTextField();
        formPanel.add(txtPrice);

        add(formPanel, BorderLayout.NORTH);

        // ===== BUTTON =====
        JPanel buttonPanel = new JPanel();

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);

        add(buttonPanel, BorderLayout.CENTER);

        // ===== TABLE =====
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Type", "Price"}, 0
        );

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.SOUTH);

        // ===== LOAD DATA =====
        loadTable();

        // ===== EVENTS =====
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());

        table.getSelectionModel().addListSelectionListener(e -> fillForm());
    }

    // ===== METHODS =====

    private void loadTable() {
        list = dao.getAll();
        tableModel.setRowCount(0);

        for (Product p : list) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getType(),
                    p.getPrice()
            });
        }
    }

    private void addProduct() {
        try {
            Product p = new Product(
                    txtName.getText(),
                    cbType.getSelectedItem().toString(),
                    Double.parseDouble(txtPrice.getText())
            );

            dao.insert(p);
            loadTable();
            clearForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input!");
        }
    }

    private void updateProduct() {
        if (selectedIndex >= 0) {
            try {
                Product p = list.get(selectedIndex);

                p.setName(txtName.getText());
                p.setType(cbType.getSelectedItem().toString());
                p.setPrice(Double.parseDouble(txtPrice.getText()));

                dao.update(p);
                loadTable();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        }
    }

    private void deleteProduct() {
        if (selectedIndex >= 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete this product?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                dao.delete(list.get(selectedIndex).getId());
                loadTable();
                clearForm();
            }
        }
    }

    private void fillForm() {
        selectedIndex = table.getSelectedRow();

        if (selectedIndex >= 0) {
            Product p = list.get(selectedIndex);

            txtName.setText(p.getName());
            cbType.setSelectedItem(p.getType());
            txtPrice.setText(String.valueOf(p.getPrice()));
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtPrice.setText("");
        selectedIndex = -1;
    }
}
