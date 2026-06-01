package is216.petshop.Customer;

import is216.petshop.Customer.CustomerDAO;
import is216.petshop.Customer.Customer;
import is216.petshop.Customer.Customerpanel.FlatBtn;
import is216.petshop.Customer.Customerpanel.RoundedBorder;

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
                c.setPartnerType(typeKeys[cbType.getSelectedIndex()]);
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

        if (isEdit) {
            setSize(980, 680);
            setLocationRelativeTo(getParent());

            JPanel splitContainer = new JPanel(new GridLayout(1, 2, 25, 0));
            splitContainer.setBackground(WHITE);
            splitContainer.setBorder(new EmptyBorder(15, 20, 20, 20));

            JPanel leftCol = new JPanel(new BorderLayout(0, 12));
            leftCol.setBackground(WHITE);
            leftCol.add(form, BorderLayout.CENTER);
            leftCol.add(bSave, BorderLayout.SOUTH);

            JPanel rightCol = new JPanel(new BorderLayout(0, 12));
            rightCol.setBackground(WHITE);
            rightCol.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER),
                new EmptyBorder(0, 20, 0, 0)
            ));

            JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
            rightHeader.setBackground(WHITE);
            JLabel rightTitle = new JLabel("🐾 Hồ sơ thú cưng");
            rightTitle.setFont(new Font("Helvetica Neue", Font.BOLD, 16));
            rightTitle.setForeground(PRIMARY);
            rightHeader.add(rightTitle);
            rightCol.add(rightHeader, BorderLayout.NORTH);

            petModel = new javax.swing.table.DefaultTableModel(
                new Object[]{"Tên thú cưng", "Loài", "Giới tính", "Thao tác"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return c == 3; }
            };

            JTable petTable = new JTable(petModel);
            petTable.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
            petTable.setRowHeight(38);
            petTable.setShowGrid(true);
            petTable.setGridColor(BORDER);
            petTable.setBackground(WHITE);
            petTable.setSelectionBackground(new Color(245, 243, 255));
            petTable.setSelectionForeground(new Color(17, 24, 39));
            petTable.setIntercellSpacing(new Dimension(0, 0));
            petTable.setFocusable(false);

            javax.swing.table.JTableHeader petHdr = petTable.getTableHeader();
            petHdr.setReorderingAllowed(false);
            petHdr.setResizingAllowed(false);
            petHdr.setBackground(WHITE);
            petHdr.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
            petHdr.setForeground(new Color(107, 114, 128));

            petTable.getColumnModel().getColumn(0).setPreferredWidth(130);
            petTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            petTable.getColumnModel().getColumn(2).setPreferredWidth(90);
            petTable.getColumnModel().getColumn(3).setPreferredWidth(70);

            petTable.getColumnModel().getColumn(3).setCellRenderer(new PetActionRender());
            petTable.getColumnModel().getColumn(3).setCellEditor(new PetActionEditor());

            JScrollPane petScroll = new JScrollPane(petTable);
            petScroll.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
            petScroll.getViewport().setBackground(WHITE);

            JPanel addPetPanel = new JPanel(new GridBagLayout());
            addPetPanel.setBackground(WHITE);
            addPetPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
            ));
            
            GridBagConstraints pGbc = new GridBagConstraints();
            pGbc.fill = GridBagConstraints.HORIZONTAL;
            pGbc.insets = new Insets(4, 0, 4, 0);
            pGbc.weightx = 1.0;

            JLabel addPetTitle = new JLabel("Thêm thú cưng mới:");
            addPetTitle.setFont(new Font("Helvetica Neue", Font.BOLD, 13));
            addPetTitle.setForeground(new Color(75, 85, 99));
            
            pGbc.gridy = 0; pGbc.gridwidth = 2;
            addPetPanel.add(addPetTitle, pGbc);

            JTextField tfPetN = mkFld("");
            tfPetN.putClientProperty("JTextField.placeholderText", "Ví dụ: Milu, LuLu...");
            pGbc.gridy = 1; pGbc.gridwidth = 1; pGbc.weightx = 0.3;
            addPetPanel.add(new JLabel("Tên:"), pGbc);
            pGbc.gridx = 1; pGbc.weightx = 0.7;
            addPetPanel.add(tfPetN, pGbc);

            JTextField tfPetT = mkFld("");
            tfPetT.putClientProperty("JTextField.placeholderText", "Chó, Mèo...");
            pGbc.gridx = 0; pGbc.gridy = 2; pGbc.weightx = 0.3;
            addPetPanel.add(new JLabel("Loài:"), pGbc);
            pGbc.gridx = 1; pGbc.weightx = 0.7;
            addPetPanel.add(tfPetT, pGbc);

            JComboBox<String> cbPetG = new JComboBox<>(new String[]{"Đực", "Cái", "Chưa rõ"});
            cbPetG.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
            cbPetG.setPreferredSize(new Dimension(0, 36));
            pGbc.gridx = 0; pGbc.gridy = 3; pGbc.weightx = 0.3;
            addPetPanel.add(new JLabel("Giới tính:"), pGbc);
            pGbc.gridx = 1; pGbc.weightx = 0.7;
            addPetPanel.add(cbPetG, pGbc);

            FlatBtn bAddPet = new FlatBtn("+ Thêm thú cưng");
            bAddPet.setPreferredSize(new Dimension(0, 42));
            bAddPet.addActionListener(ev -> {
                String name = tfPetN.getText().trim();
                String type = tfPetT.getText().trim();
                String gender = cbPetG.getSelectedItem().toString();

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Tên thú cưng không được để trống", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    if (dao.insertPet(customer.getId(), name, type, gender)) {
                        loadPetData();
                        tfPetN.setText("");
                        tfPetT.setText("");
                        cbPetG.setSelectedIndex(0);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm thú cưng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
            pGbc.gridx = 0; pGbc.gridy = 4; pGbc.gridwidth = 2; pGbc.weightx = 1.0;
            pGbc.insets = new Insets(8, 0, 0, 0);
            addPetPanel.add(bAddPet, pGbc);

            JPanel rightCenter = new JPanel(new BorderLayout(0, 15));
            rightCenter.setBackground(WHITE);
            rightCenter.add(petScroll, BorderLayout.CENTER);
            rightCenter.add(addPetPanel, BorderLayout.SOUTH);

            rightCol.add(rightCenter, BorderLayout.CENTER);

            splitContainer.add(leftCol);
            splitContainer.add(rightCol);

            add(splitContainer, BorderLayout.CENTER);

            loadPetData();
        } else {
            add(form, BorderLayout.CENTER);
            add(bSave, BorderLayout.SOUTH);
        }
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

    // --- PET PANEL HELPERS ---

    private java.util.ArrayList<CustomerDAO.Pet> petList = new java.util.ArrayList<>();
    private javax.swing.table.DefaultTableModel petModel;

    private void loadPetData() {
        if (customer == null) return;
        petList = dao.getPetsByCustomerId(customer.getId());
        petModel.setRowCount(0);
        for (CustomerDAO.Pet p : petList) {
            petModel.addRow(new Object[]{
                p.getName(),
                p.getType() == null || p.getType().isEmpty() ? "Chưa rõ" : p.getType(),
                p.getGender() == null || p.getGender().isEmpty() ? "Chưa rõ" : p.getGender(),
                ""
            });
        }
    }

    class PetActionRender extends javax.swing.table.DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
            p.setBackground(sel ? new Color(245, 243, 255) : Color.WHITE);
            JLabel lbl = new JLabel("Xóa 🗑️");
            lbl.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
            lbl.setForeground(new Color(220, 38, 38));
            p.add(lbl);
            return p;
        }
    }

    class PetActionEditor extends DefaultCellEditor {
        private final JButton btn;
        private int curRow = -1;

        public PetActionEditor() {
            super(new JCheckBox());
            btn = new JButton("Xóa");
            btn.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(220, 38, 38));
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                fireEditingStopped();
                if (curRow >= 0 && curRow < petList.size()) {
                    CustomerDAO.Pet p = petList.get(curRow);
                    int reply = JOptionPane.showConfirmDialog(btn, "Bạn có chắc chắn muốn xóa thú cưng " + p.getName() + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.YES_OPTION) {
                        try {
                            if (dao.deletePet(p.getId())) {
                                loadPetData();
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(btn, "Lỗi khi xóa thú cưng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            curRow = r;
            return btn;
        }

        @Override public Object getCellEditorValue() { return ""; }
    }
}
