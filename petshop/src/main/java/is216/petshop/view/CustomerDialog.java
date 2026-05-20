package is216.petshop.view;

import is216.petshop.dao.CustomerDAO;
import is216.petshop.model.Customer;
import is216.petshop.view.Customerpanel.FlatBtn;
import is216.petshop.view.Customerpanel.RoundedBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomerDialog extends JDialog {
    private static final Color WHITE = Color.WHITE;
    private static final Color PRIMARY = new Color(79, 70, 229);
    private static final Color BORDER = new Color(229, 231, 235);

    private final CustomerDAO dao;
    private final Customer customer;
    private final boolean isEdit;
    private final Runnable onSuccess;

    public CustomerDialog(Frame parent, Customer customer, CustomerDAO dao, Runnable onSuccess) {
        super(parent, customer != null ? "Sửa khách hàng" : "Thêm khách hàng", true);
        this.dao = dao;
        this.customer = customer;
        this.isEdit = customer != null;
        this.onSuccess = onSuccess;

        initUI();
    }

    private void initUI() {
        setSize(480, 680);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(WHITE);

        JPanel dTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        dTop.setBackground(PRIMARY);
        JLabel dLbl = new JLabel(isEdit ? "Sửa thông tin khách hàng" : "Thêm khách hàng mới");
        dLbl.setFont(new Font("Helvetica Neue", Font.BOLD, 18));
        dLbl.setForeground(WHITE);
        dTop.add(dLbl);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1.0;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        JTextField tfN = mkFld(isEdit ? customer.getName() : "");
        JTextField tfP = mkFld(isEdit ? customer.getPhone() : "");
        JTextField tfE = mkFld(isEdit ? customer.getEmail() : "");
        JTextField tfA = mkFld(isEdit ? customer.getAddress() : "");

        String[] types = {"Khách hàng", "Nhà cung cấp", "Cả hai"};
        String[] typeKeys = {"KHACH_HANG", "NHA_CUNG_CAP", "CA_HAI"};
        JComboBox<String> cbType = new JComboBox<>(types);
        cbType.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        cbType.setPreferredSize(new Dimension(0, 40));
        if (isEdit) {
            for (int i = 0; i < typeKeys.length; i++) {
                if (typeKeys[i].equals(customer.getPartnerType())) {
                    cbType.setSelectedIndex(i);
                    break;
                }
            }
        }

        JTextField tfPts = mkFld(isEdit ? String.valueOf(customer.getLoyaltyPoints()) : "0");
        JTextField tfDate = mkFld(isEdit ? sdf.format(customer.getJoinDate()) : sdf.format(new Date()));

        int row = 0;
        gbc.gridy = row++; form.add(new JLabel("Họ tên:"), gbc);
        gbc.gridy = row++; form.add(tfN, gbc);
        gbc.gridy = row++; form.add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridy = row++; form.add(tfP, gbc);
        gbc.gridy = row++; form.add(new JLabel("Email:"), gbc);
        gbc.gridy = row++; form.add(tfE, gbc);
        gbc.gridy = row++; form.add(new JLabel("Địa chỉ:"), gbc);
        gbc.gridy = row++; form.add(tfA, gbc);
        gbc.gridy = row++; form.add(new JLabel("Phân loại đối tác:"), gbc);
        gbc.gridy = row++; form.add(cbType, gbc);
        gbc.gridy = row++; form.add(new JLabel("Điểm tích lũy:"), gbc);
        gbc.gridy = row++; form.add(tfPts, gbc);
        gbc.gridy = row++; form.add(new JLabel("Ngày tham gia (dd/MM/yyyy):"), gbc);
        gbc.gridy = row++; form.add(tfDate, gbc);

        FlatBtn bSave = new FlatBtn(isEdit ? "Cập nhật" : "Lưu lại");
        bSave.setPreferredSize(new Dimension(0, 50));
        bSave.addActionListener(e -> {
            String name = tfN.getText().trim();
            String phone = tfP.getText().trim();
            String email = tfE.getText().trim();
            String addr = tfA.getText().trim();
            String pType = typeKeys[cbType.getSelectedIndex()];
            String ptsStr = tfPts.getText().trim();
            String dateStr = tfDate.getText().trim();

            if (name.isEmpty()) { showError("Tên khách hàng không được để trống"); return; }
            if (phone.isEmpty()) { showError("Số điện thoại không được để trống"); return; }
            if (!phone.matches("^[0-9]+$")) { showError("Số điện thoại chỉ được chứa chữ số"); return; }
            if (phone.length() < 10 || phone.length() > 11) { showError("Số điện thoại phải từ 10-11 số"); return; }
            if (!email.isEmpty() && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                showError("Định dạng email không hợp lệ"); return;
            }

            int points = 0;
            try {
                points = Integer.parseInt(ptsStr);
                if (points < 0) { showError("Điểm không được âm"); return; }
            } catch (NumberFormatException ex) {
                showError("Điểm phải là số nguyên"); return;
            }

            Date joinDate;
            try {
                joinDate = sdf.parse(dateStr);
            } catch (Exception ex) {
                showError("Ngày tham gia sai định dạng (dd/MM/yyyy)"); return;
            }

            try {
                Customer c = isEdit ? customer : new Customer();
                c.setName(name);
                c.setPhone(phone);
                c.setEmail(email);
                c.setAddress(addr);
                c.setPartnerType(pType);
                c.setLoyaltyPoints(points);
                c.setJoinDate(joinDate);

                boolean success = isEdit ? dao.update(c) : dao.insert(c);
                if (success) {
                    if (onSuccess != null) onSuccess.run();
                    dispose();
                }
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        add(dTop, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(bSave, BorderLayout.SOUTH);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private JTextField mkFld(String v) {
        JTextField tf = new JTextField(v);
        tf.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(10, 12, 10, 12)));
        return tf;
    }
}
