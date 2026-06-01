package is216.petshop.view;

import is216.petshop.dao.PayrollDAO;
import is216.petshop.model.HoSoLuongModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SalaryProfileDialog extends JDialog {
    private JTextField txtMucLuong;
    private JTextField txtGiamTru;
    private JTextField txtDependents;
    private JTextField txtTienGiamNpt;
    
    private JButton btnSave;
    private JButton btnCancel;
    private JButton btnClose;
    
    private PayrollDAO payrollDAO;
    private HoSoLuongModel profile;
    private boolean isSaved = false;

    public SalaryProfileDialog(Frame parent, HoSoLuongModel profile) {
        super(parent, "", true);
        this.profile = profile;
        this.payrollDAO = new PayrollDAO();
        
        setUndecorated(true);
        initComponents();
        setSize(520, 460);
        setLocationRelativeTo(parent);
        
        // Apply smooth rounded corners
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
            }
        });

        // Add window dragging capability
        WindowDragListener dragListener = new WindowDragListener();
        addMouseListener(dragListener);
        addMouseMotionListener(dragListener);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        // ─── HEADER PANEL ─────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(20, 24, 10, 24));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(new PayrollPanel.GearIcon(22, new Color(59, 130, 246)));
        titlePanel.add(iconLabel);

        JLabel titleLabel = new JLabel("Cấu hình hồ sơ lương: " + profile.getHoTen());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(15, 23, 42)); // Slate 900
        titlePanel.add(titleLabel);

        header.add(titlePanel, BorderLayout.WEST);

        // Close button "×"
        btnClose = new JButton("×");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        btnClose.setForeground(new Color(148, 163, 184)); // Slate 400
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnClose.setForeground(new Color(239, 68, 68)); // Red hover
            }
            public void mouseExited(java.awt.event.MouseAdapter e) {
                btnClose.setForeground(new Color(148, 163, 184));
            }
        });
        header.add(btnClose, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // ─── FORM PANEL (GridBagLayout) ───────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(10, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Color labelColor = new Color(100, 116, 139); // Slate 500
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Row 1: MỨC LƯƠNG CƠ BẢN (GROSS BASE) *
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        JLabel lblWage = new JLabel("MỨC LƯƠNG CƠ BẢN (GROSS BASE) *");
        lblWage.setFont(labelFont);
        lblWage.setForeground(labelColor);
        form.add(lblWage, gbc);

        gbc.gridy = 1;
        txtMucLuong = new JTextField(String.valueOf(profile.getMucLuong()));
        txtMucLuong.setPreferredSize(new Dimension(0, 38));
        txtMucLuong.setFont(fieldFont);
        txtMucLuong.putClientProperty("FlatLaf.style", "arc: 8; focusColor: #3b82f6");
        form.add(txtMucLuong, gbc);

        // Row 2: SỐ NGƯỜI PHỤ THUỘC (Left) & GIẢM TRỪ NGƯỜI PHỤ THUỘC (Right)
        gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.4;
        JLabel lblDeps = new JLabel("SỐ NGƯỜI PHỤ THUỘC");
        lblDeps.setFont(labelFont);
        lblDeps.setForeground(labelColor);
        form.add(lblDeps, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        JLabel lblDepDeduction = new JLabel("GIẢM TRỪ NGƯỜI PHỤ THUỘC / NGƯỜI");
        lblDepDeduction.setFont(labelFont);
        lblDepDeduction.setForeground(labelColor);
        form.add(lblDepDeduction, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.4;
        txtDependents = new JTextField(String.valueOf(profile.getSonguoiPhuThuoc()));
        txtDependents.setPreferredSize(new Dimension(0, 38));
        txtDependents.setFont(fieldFont);
        txtDependents.putClientProperty("FlatLaf.style", "arc: 8; focusColor: #3b82f6");
        form.add(txtDependents, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        long depRate = profile.getTienGiamNpt() > 0 ? profile.getTienGiamNpt() : 4400000L;
        txtTienGiamNpt = new JTextField(String.valueOf(depRate));
        txtTienGiamNpt.setPreferredSize(new Dimension(0, 38));
        txtTienGiamNpt.setFont(fieldFont);
        txtTienGiamNpt.putClientProperty("FlatLaf.style", "arc: 8; focusColor: #3b82f6");
        form.add(txtTienGiamNpt, gbc);

        // Row 3: GIẢM TRỪ GIA CẢNH BẢN THÂN
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 1.0;
        JLabel lblGiamTru = new JLabel("GIẢM TRỪ GIA CẢNH BẢN THÂN");
        lblGiamTru.setFont(labelFont);
        lblGiamTru.setForeground(labelColor);
        form.add(lblGiamTru, gbc);

        gbc.gridy = 5;
        txtGiamTru = new JTextField(String.valueOf(profile.getGiamTruBanThan() > 0 ? profile.getGiamTruBanThan() : 15500000L));
        txtGiamTru.setPreferredSize(new Dimension(0, 38));
        txtGiamTru.setFont(fieldFont);
        txtGiamTru.putClientProperty("FlatLaf.style", "arc: 8; focusColor: #3b82f6");
        form.add(txtGiamTru, gbc);

        root.add(form, BorderLayout.CENTER);

        // ─── FOOTER PANEL ─────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(0, 24, 20, 24));

        btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.setForeground(new Color(100, 116, 139)); // Slate 500
        btnCancel.setContentAreaFilled(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());
        btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnCancel.setForeground(new Color(15, 23, 42));
            }
            public void mouseExited(java.awt.event.MouseAdapter e) {
                btnCancel.setForeground(new Color(100, 116, 139));
            }
        });

        btnSave = new JButton("Lưu hồ sơ lương");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(new Color(37, 99, 235)); // Premium blue
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(150, 38));
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.putClientProperty("FlatLaf.style", "arc: 8");
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> doSave());
        btnSave.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnSave.setBackground(new Color(29, 78, 216)); // Darker blue
            }
            public void mouseExited(java.awt.event.MouseAdapter e) {
                btnSave.setBackground(new Color(37, 99, 235));
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void doSave() {
        String wageStr = txtMucLuong.getText().trim();
        String giamTruStr = txtGiamTru.getText().trim();
        String depsStr = txtDependents.getText().trim();
        String depRateStr = txtTienGiamNpt.getText().trim();

        if (wageStr.isEmpty() || giamTruStr.isEmpty() || depsStr.isEmpty() || depRateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ các thông tin hồ sơ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long wage, giamTru, depRate;
        int deps;

        try {
            wage = Long.parseLong(wageStr);
            giamTru = Long.parseLong(giamTruStr);
            depRate = Long.parseLong(depRateStr);
            if (wage < 0 || giamTru < 0 || depRate < 0) throw new NumberFormatException();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Mức lương và mức giảm trừ phải là số nguyên không âm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            deps = Integer.parseInt(depsStr);
            if (deps < 0) throw new NumberFormatException();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Số người phụ thuộc phải là số nguyên không âm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        profile.setMucLuong(wage);
        profile.setGiamTruBanThan(giamTru);
        profile.setSonguoiPhuThuoc(deps);
        profile.setTienGiamNpt(depRate);

        boolean success = payrollDAO.upsertSalaryProfile(profile);
        if (success) {
            JOptionPane.showMessageDialog(this, "Cập nhật cấu hình hồ sơ lương thành công!");
            isSaved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống khi cập nhật hồ sơ. Vui lòng kiểm tra lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return isSaved; }

    // Helper class to enable undecorated JDialog window dragging
    private class WindowDragListener extends java.awt.event.MouseAdapter {
        private Point posX = new Point(0, 0);

        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
            posX = e.getPoint();
        }

        @Override
        public void mouseDragged(java.awt.event.MouseEvent e) {
            if (posX != null) {
                Point curr = e.getLocationOnScreen();
                setLocation(curr.x - posX.x, curr.y - posX.y);
            }
        }
    }
}
