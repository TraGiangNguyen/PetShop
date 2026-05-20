package is216.petshop.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

public class GenericFormDialog extends JDialog {

    // Sử dụng Map để lưu trữ linh hoạt danh sách các ô nhập liệu
    // Key: Tên nhãn (VD: "Họ tên"), Value: Ô JTextField tương ứng
    private Map<String, JTextField> inputFields;
    private JButton btnCancel;
    private JButton btnAdd;

    /**
     * Constructor dùng chung cho mọi form thêm mới
     * @param parent Cửa sổ cha (MainView)
     * @param title Tiêu đề của form (VD: "Thêm nhân viên mới")
     * @param fieldNames Mảng chứa tên các trường cần nhập (VD: {"Họ tên", "Chức vụ", "Email"})
     */
    public GenericFormDialog(JFrame parent, String title, String[] fieldNames) {
        super(parent, title, true);
        inputFields = new LinkedHashMap<>(); // Dùng LinkedHashMap để giữ đúng thứ tự mảng truyền vào
        initComponents(title, fieldNames);
    }

    private void initComponents(String title, String[] fieldNames) {
        // Tự động tính toán chiều cao form dựa trên số lượng trường (Mỗi trường khoảng 80px) + Header/Footer
        int formHeight = 200 + (fieldNames.length * 85);
        setSize(450, formHeight);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // ==========================================
        // 1. PHẦN MAIN: CHỨA TIÊU ĐỀ VÀ CÁC Ô NHẬP
        // ==========================================
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setBackground(Color.WHITE);
        pnlMain.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        // Tiêu đề form
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(20, 30, 50));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlMain.add(lblTitle);
        pnlMain.add(Box.createRigidArea(new Dimension(0, 25)));

        // Vòng lặp: Đọc mảng fieldNames để tự động sinh ra các ô nhập liệu
        for (String fieldName : fieldNames) {
            JTextField textField = new JTextField();
            pnlMain.add(createFormGroup(fieldName, textField));
            
            // Lưu ô text này vào Map để lát sau Controller có thể lấy dữ liệu ra
            inputFields.put(fieldName, textField);
        }

        // ==========================================
        // 2. PHẦN BOTTOM: CHỨA 2 NÚT BẤM
        // ==========================================
        JPanel pnlBottom = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(BorderFactory.createEmptyBorder(15, 30, 25, 30));

        btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(Color.BLACK);
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.putClientProperty("JButton.buttonType", "roundRect");
        btnCancel.setPreferredSize(new Dimension(0, 45));

        btnAdd = new JButton("Thêm mới");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnAdd.setBackground(new Color(80, 72, 229)); // Màu tím xanh
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.setPreferredSize(new Dimension(0, 45));

        pnlBottom.add(btnCancel);
        pnlBottom.add(btnAdd);

        add(pnlMain, BorderLayout.CENTER);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private JPanel createFormGroup(String labelText, JTextField textField) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(40, 50, 70));
        
        textField.putClientProperty("JComponent.roundRect", true);
        textField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        textField.setMargin(new Insets(0, 10, 0, 10));

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(textField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        return panel;
    }

    // ========================================================
    // CÁC HÀM GETTER & LISTENER
    // ========================================================
    
    /**
     * Hàm lấy dữ liệu từ một ô nhập liệu bất kỳ dựa vào tên của nó
     */
    public String getFieldValue(String fieldName) {
        JTextField txt = inputFields.get(fieldName);
        return txt != null ? txt.getText().trim() : "";
    }

    public void addAddButtonListener(ActionListener listener) { btnAdd.addActionListener(listener); }
    public void addCancelButtonListener(ActionListener listener) { btnCancel.addActionListener(listener); }
    
    public void closeDialog() { this.dispose(); }
}
    
