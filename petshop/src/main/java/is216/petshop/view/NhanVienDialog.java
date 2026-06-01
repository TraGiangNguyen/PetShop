package is216.petshop.view;

import is216.petshop.dao.NhanVienDAO;
import is216.petshop.model.NhanVienModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NhanVienDialog extends JDialog {
    private JTextField txtHoTen, txtEmail, txtSdt, txtNgayVaoLam;
    private JComboBox<String> cbChucVu;
    private boolean isEditMode;
    private NhanVienModel currentNv;
    private NhanVienDAO dao;
    private NhanVienPanel parentPanel;

    public NhanVienDialog(Frame parent, boolean modal, NhanVienPanel panel, NhanVienModel nv) {
        super(parent, modal);
        this.parentPanel = panel;
        this.dao = new NhanVienDAO();
        this.isEditMode = (nv != null);
        this.currentNv = isEditMode ? nv : new NhanVienModel();

        initComponents();
        if (isEditMode) {
            loadData();
            txtNgayVaoLam.setEnabled(false);
        } else {
            txtNgayVaoLam.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        }
    }

    private void initComponents() {
        setTitle(isEditMode ? "Cập nhật nhân viên" : "Thêm nhân viên mới");
        setSize(550, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(15, 20, 5, 20));
        JLabel lblTitle = new JLabel(isEditMode ? "Cập nhật nhân viên" : "Thêm nhân viên mới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 25, 15));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        // Row 1: Họ tên & Chức vụ
        txtHoTen = createField(formPanel, "HỌ TÊN", "Nhập họ và tên");
        String[] vaitro = { "Quản lý", "Nhân viên bán hàng", "Nhân viên dịch vụ", "Nhân viên kho" };
        cbChucVu = createCombo(formPanel, "CHỨC VỤ", vaitro);

        // Row 2: Email & SĐT
        txtEmail = createField(formPanel, "EMAIL", "ví dụ@petstore.com");
        txtSdt = createField(formPanel, "SỐ ĐIỆN THOẠI", "Nhập số điện thoại");

        // NGÀY VÀO LÀM
        txtNgayVaoLam = createField(formPanel, "NGÀY VÀO LÀM (DD/MM/YYYY)", "20/05/2024");

        add(formPanel, BorderLayout.CENTER);

        // Bottom Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(249, 250, 251));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnCancel = new JButton("Hủy");
        styleButton(btnCancel, new Color(226, 232, 240), new Color(15, 23, 42));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu thông tin");
        styleButton(btnSave, new Color(0, 104, 116), Color.WHITE);
        btnSave.addActionListener(e -> saveNhanVien());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JTextField createField(JPanel parent, String label, String placeholder) {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 116, 139));
        JTextField txt = new JTextField();
        txt.putClientProperty("JTextField.placeholderText", placeholder);
        txt.putClientProperty("FlatLaf.style", "arc: 8; margin: 4,8,4,8");
        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(txt, BorderLayout.CENTER);
        parent.add(pnl);
        return txt;
    }

    private JComboBox<String> createCombo(JPanel parent, String label, String[] items) {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 116, 139));
        JComboBox<String> cb = new JComboBox<>(items);
        cb.putClientProperty("FlatLaf.style", "arc: 8; margin: 4,8,4,8");
        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(cb, BorderLayout.CENTER);
        parent.add(pnl);
        return cb;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("FlatLaf.style", "arc: 8");
    }

    private void loadData() {
        txtHoTen.setText(currentNv.getHoTen());
        txtEmail.setText(currentNv.getEmail());
        txtSdt.setText(currentNv.getSdt());

        String cv = currentNv.getChucVu();
        if (cv != null) {
            for (int i = 0; i < cbChucVu.getItemCount(); i++) {
                if (cbChucVu.getItemAt(i).equalsIgnoreCase(cv)) {
                    cbChucVu.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void saveNhanVien() {
        try {
            String hoTen = txtHoTen.getText().trim();
            String email = txtEmail.getText().trim();
            String sdt = txtSdt.getText().trim();
            String ngayVaoLamStr = txtNgayVaoLam.getText().trim();

            if (hoTen.isEmpty() || email.isEmpty() || sdt.isEmpty() || ngayVaoLamStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            currentNv.setHoTen(hoTen);
            currentNv.setEmail(email);
            currentNv.setSdt(sdt);
            currentNv.setChucVu(cbChucVu.getSelectedItem().toString());

            boolean success = false;
            if (isEditMode) {
                success = dao.updateNhanVien(currentNv);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                currentNv.setNgayVaoLam(sdf.parse(ngayVaoLamStr));
                currentNv.setTrangThai("Đang làm việc"); // set active status for new employee
                success = dao.addNhanVien(currentNv, "", ""); // trigger handles auto account creation
            }

            if (success) {
                JOptionPane.showMessageDialog(this, "Lưu thành công!");
                parentPanel.refreshData();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu vào CSDL!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Dữ liệu nhập vào không hợp lệ: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
