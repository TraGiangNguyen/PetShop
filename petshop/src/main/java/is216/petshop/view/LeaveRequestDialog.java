package is216.petshop.view;

import is216.petshop.dao.DonNghiPhepDAO;
import is216.petshop.model.DonNghiPhepModel;
import is216.petshop.model.QuanLyPhepModel;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LeaveRequestDialog extends JDialog {
    private JComboBox<String> cbType;
    private JTextField txtFrom;
    private JTextField txtTo;
    private JTextField txtDays;
    private JTextField txtReason;
    
    private JButton btnSubmit;
    private JButton btnCancel;
    
    private DonNghiPhepDAO donNghiPhepDAO;
    private int maNhanVien;
    private boolean isSubmitted = false;

    public LeaveRequestDialog(Frame parent, int maNhanVien) {
        super(parent, "Đơn xin nghỉ phép", true);
        this.maNhanVien = maNhanVien;
        this.donNghiPhepDAO = new DonNghiPhepDAO();
        
        initComponents();
        setSize(450, 560);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Title
        JLabel lblTitle = new JLabel("Tạo đơn xin nghỉ phép");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // Font
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Row 1: LoaiNghi
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        JLabel lblType = new JLabel("Loại nghỉ phép:");
        lblType.setFont(labelFont);
        form.add(lblType, gbc);

        gbc.gridy = 1;
        String[] types = { "Nghỉ phép năm", "Nghỉ ốm", "Việc riêng", "Khác" };
        cbType = new JComboBox<>(types);
        cbType.setPreferredSize(new Dimension(0, 36));
        cbType.putClientProperty("FlatLaf.style", "arc: 8");
        cbType.setFont(fieldFont);
        form.add(cbType, gbc);

        // Row 2: TuNgay
        gbc.gridy = 2;
        JLabel lblFrom = new JLabel("Từ ngày (dd/MM/yyyy):");
        lblFrom.setFont(labelFont);
        form.add(lblFrom, gbc);

        gbc.gridy = 3;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        txtFrom = new JTextField(sdf.format(new Date()), 15);
        txtFrom.setPreferredSize(new Dimension(0, 36));
        txtFrom.putClientProperty("FlatLaf.style", "arc: 8");
        txtFrom.setFont(fieldFont);
        form.add(txtFrom, gbc);

        // Row 3: DenNgay
        gbc.gridy = 4;
        JLabel lblTo = new JLabel("Đến ngày (dd/MM/yyyy):");
        lblTo.setFont(labelFont);
        form.add(lblTo, gbc);

        gbc.gridy = 5;
        txtTo = new JTextField(sdf.format(new Date()), 15);
        txtTo.setPreferredSize(new Dimension(0, 36));
        txtTo.putClientProperty("FlatLaf.style", "arc: 8");
        txtTo.setFont(fieldFont);
        form.add(txtTo, gbc);

        // Row 4: SoNgay
        gbc.gridy = 6;
        JLabel lblDays = new JLabel("Số ngày xin nghỉ:");
        lblDays.setFont(labelFont);
        form.add(lblDays, gbc);

        gbc.gridy = 7;
        txtDays = new JTextField("1", 5);
        txtDays.setPreferredSize(new Dimension(0, 36));
        txtDays.putClientProperty("FlatLaf.style", "arc: 8");
        txtDays.setFont(fieldFont);
        form.add(txtDays, gbc);

        // Row 5: LyDo
        gbc.gridy = 8;
        JLabel lblReason = new JLabel("Lý do nghỉ phép:");
        lblReason.setFont(labelFont);
        form.add(lblReason, gbc);

        gbc.gridy = 9;
        txtReason = new JTextField(20);
        txtReason.setPreferredSize(new Dimension(0, 36));
        txtReason.putClientProperty("FlatLaf.style", "arc: 8");
        txtReason.setFont(fieldFont);
        form.add(txtReason, gbc);

        root.add(form, BorderLayout.CENTER);

        // Bottom buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttons.setBackground(Color.WHITE);

        btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setPreferredSize(new Dimension(80, 36));
        btnCancel.addActionListener(e -> dispose());

        btnSubmit = new JButton("Gửi đơn");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSubmit.setBackground(new Color(108, 93, 211));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setPreferredSize(new Dimension(100, 36));
        btnSubmit.setFocusPainted(false);
        btnSubmit.setBorderPainted(false);
        btnSubmit.putClientProperty("FlatLaf.style", "arc: 8");
        btnSubmit.addActionListener(e -> doSubmit());

        buttons.add(btnCancel);
        buttons.add(btnSubmit);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void doSubmit() {
        String type = (String) cbType.getSelectedItem();
        String fromStr = txtFrom.getText().trim();
        String toStr = txtTo.getText().trim();
        String daysStr = txtDays.getText().trim();
        String reason = txtReason.getText().trim();

        if (fromStr.isEmpty() || toStr.isEmpty() || daysStr.isEmpty() || reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ các trường thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        Date fromDate, toDate;
        int days;

        try {
            fromDate = sdf.parse(fromStr);
            toDate = sdf.parse(toStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ! Vui lòng dùng dd/MM/yyyy.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            days = Integer.parseInt(daysStr);
            if (days <= 0) throw new NumberFormatException();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Số ngày nghỉ phép phải là số nguyên dương!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (toDate.before(fromDate)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc không thể trước ngày bắt đầu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate annual leave balance
        if ("Nghỉ phép năm".equals(type)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromDate);
            int year = cal.get(Calendar.YEAR);
            QuanLyPhepModel balance = donNghiPhepDAO.getLeaveBalance(maNhanVien, year);
            if (balance.getConLai() < days) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                        "Số ngày phép còn lại của bạn trong năm " + year + " là " + balance.getConLai() + " ngày, " +
                        "ít hơn số ngày bạn muốn xin nghỉ (" + days + "). Bạn vẫn muốn gửi đơn xin nghỉ chứ?", 
                        "Cảnh báo số dư phép", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
            }
        }

        DonNghiPhepModel req = new DonNghiPhepModel();
        req.setMaNhanVien(maNhanVien);
        req.setLoaiNghi(type);
        req.setTuNgay(fromDate);
        req.setDenNgay(toDate);
        req.setSoNgay(days);
        req.setLyDo(reason);

        boolean success = donNghiPhepDAO.submitRequest(req);
        if (success) {
            JOptionPane.showMessageDialog(this, "Gửi đơn xin nghỉ phép thành công! Đang chờ quản lý phê duyệt.");
            isSubmitted = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống khi lưu đơn nghỉ phép. Vui lòng thử lại sau!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSubmitted() { return isSubmitted; }
}
