/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.view;

import is216.petshop.bus.NhanVienBUS;
import is216.petshop.model.NhanVien;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class NhanVienPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private NhanVienBUS nhanVienBUS;

    public NhanVienPanel() {
        nhanVienBUS = new NhanVienBUS();
        initComponents();
        loadDataToTable();
    }

    private void initComponents() {
        // Thiết lập nền tổng thể
        this.setBackground(Color.decode("#F4F6F9"));
        this.setLayout(new BorderLayout(20, 20));
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30)); // Padding

        // --- PHẦN HEADER (TIÊU ĐỀ & NÚT) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#F4F6F9"));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(Color.decode("#F4F6F9"));
        JLabel lblTitle = new JLabel("Quản lý nhân viên");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        JLabel lblSubtitle = new JLabel("Quản lý danh sách nhân viên cửa hàng");
        lblSubtitle.setForeground(Color.GRAY);
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);

        JButton btnAdd = new JButton("+ Thêm nhân viên");
        btnAdd.setBackground(Color.decode("#6C5DD3"));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setFont(new Font("Arial", Font.BOLD, 14));
        btnAdd.setPreferredSize(new Dimension(160, 40));
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(btnAdd, BorderLayout.EAST);

        // --- PHẦN BẢNG (JTABLE) ---
        String[] columnNames = {"Họ tên", "Chức vụ", "Số điện thoại", "Email", "Lương", "Thao tác"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp trên ô
            }
        };
        table = new JTable(tableModel);
        
        // Tuỳ chỉnh giao diện bảng cho giống thiết kế
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(Color.decode("#E2E5FF"));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Bỏ viền Scrollpane

        // Thêm vào Panel chính
        this.add(headerPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0); // Xoá dữ liệu cũ
        List<NhanVien> list = nhanVienBUS.getDanhSachNhanVien();
        DecimalFormat formatter = new DecimalFormat("###,###,###đ");
        
        for (NhanVien nv : list) {
            Object[] row = {
                nv.getHoTen(),
                nv.getChucVu(),
                nv.getSoDienThoai(),
                nv.getEmail(),
                formatter.format(nv.getLuong()), // Format tiền tệ
                "✎  🗑" // Tạm dùng Unicode thay cho Icon. Trong thực tế bạn dùng JLabel chứa ImageIcon.
            };
            tableModel.addRow(row);
        }
    }
}