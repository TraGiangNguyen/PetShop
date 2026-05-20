package is216.petshop.Product;

import is216.petshop.util.TableActionCellEditor;
import is216.petshop.util.TableActionCellRender;
import is216.petshop.util.TableActionEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class ProductPanel extends JPanel {

    // Khai báo các thành phần ra ngoài (Biến toàn cục) để Controller có thể lấy dữ liệu
    private JButton btnAdd;
    private JTable tblProduct;
    private DefaultTableModel tableModel;

    public ProductPanel() {
        // Hàm khởi tạo sẽ gọi hàm vẽ giao diện
        initComponents();
    }

    private void initComponents() {
        // Cài đặt layout chính và khoảng cách viền 20px cho thoáng
        setLayout(new BorderLayout(15, 15));
        setOpaque(false); // Trong suốt để lấy nền xám từ MainView
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); 

        // ==========================================
        // 1. PHẦN HEADER: TIÊU ĐỀ & NÚT THÊM
        // ==========================================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        
        // Tiêu đề bám sát ảnh thiết kế
        JLabel lblTitle = new JLabel("<html><h2 style='margin:0; font-size:22px'>Quản lý sản phẩm</h2><p style='color:gray; margin:0; padding-top:5px'>Quản lý danh sách sản phẩm thú cưng</p></html>");
        
        // Nút Thêm sản phẩm ở góc phải
        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlAction.setOpaque(false);

        btnAdd = new JButton("+ Thêm sản phẩm");
        btnAdd.setBackground(new Color(80, 72, 229)); // Màu tím xanh hiện đại giống ảnh
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.putClientProperty("JButton.buttonType", "roundRect"); // Bo góc FlatLaf
        btnAdd.setPreferredSize(new Dimension(170, 45));
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlAction.add(btnAdd);

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(pnlAction, BorderLayout.EAST);

        // ==========================================
        // 2. PHẦN BẢNG DỮ LIỆU (TABLE)
        // ==========================================
        // Khai báo các cột giống hệt ảnh thiết kế
        String[] columns = {"Hình ảnh", "Tên sản phẩm", "Danh mục", "Giá", "Tồn kho", "Thao tác"};
        
        // Dữ liệu mẫu (Tạm thời dùng chữ [Ảnh] thay cho hình ảnh thực tế)
        Object[][] data = {
            {"[Ảnh]", "Thức ăn cho chó Royal Canin 5kg", "Thức ăn", "450.000đ", "50", ""},
            {"[Ảnh]", "Thức ăn cho mèo Whiskas 1kg", "Thức ăn", "120.000đ", "80", ""}
        };

        // Khởi tạo Model và CHẶN chỉnh sửa dữ liệu trực tiếp trên bảng
        tableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Chỉ cho phép tương tác với cột cuối cùng (Cột số 5 - Thao tác)
                return column == 5; 
            }
        };

        tblProduct = new JTable(tableModel);
        
        // Cấu hình giao diện bảng chuẩn FlatLaf
        tblProduct.setRowHeight(70); // Hàng cao lên để chứa nút và ảnh cho đẹp
        tblProduct.setShowVerticalLines(false); // Bỏ đường kẻ dọc
        tblProduct.setIntercellSpacing(new Dimension(0, 0));
        tblProduct.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tblProduct.getTableHeader().setBackground(Color.WHITE);
        tblProduct.getTableHeader().setPreferredSize(new Dimension(0, 45));
        tblProduct.setSelectionBackground(new Color(235, 235, 250)); // Màu khi click chọn dòng

        // Gắn bộ vẽ (Render) và bộ sự kiện (Editor) cho cột "Thao tác" (cột số 5)
        // Lưu ý: Bạn cần có 2 file TableActionCellRender.java và TableActionCellEditor.java đã tạo ở bước trước
        tblProduct.getColumnModel().getColumn(5).setCellRenderer(new TableActionCellRender());

        // Bọc bảng vào thanh cuộn (ScrollPane)
        JScrollPane scrollPane = new JScrollPane(tblProduct);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230))); // Viền nhẹ nhàng
        scrollPane.getViewport().setBackground(Color.WHITE); // Nền bảng trắng tinh

        // ==========================================
        // 3. LẮP RÁP VÀO PANEL CHÍNH
        // ==========================================
        add(pnlHeader, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ========================================================
    // CÁC HÀM "CẦU NỐI" CHO CONTROLLER
    // ========================================================
    
    // Gắn sự kiện lắng nghe cho nút Thêm sản phẩm
    public void addAddProductListener(ActionListener listener) { 
        btnAdd.addActionListener(listener); 
    }

    // Hàm lấy tên sản phẩm đang được chọn (Dùng tạm tên ở cột 1 thay vì Mã SP)
    public String getSelectedProductName() {
        int row = tblProduct.getSelectedRow();
        if (row >= 0) {
            return tblProduct.getValueAt(row, 1).toString();
        }
        return null;
    }

    // Hàm hiển thị thông báo chung
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
    
    // Hàm này sẽ nhận các khối lệnh từ Controller truyền vào
    public void setTableActionEvent(TableActionEvent event) {
        // Gắn Editor có chứa sự kiện vào cột thứ 5 (Cột Thao tác)
        tblProduct.getColumnModel().getColumn(5).setCellEditor(new TableActionCellEditor(event));
    }
    
    // Thêm hàm này để lấy tên sản phẩm dựa vào vị trí dòng (row)
    public String getProductNameAt(int row) {
        return tblProduct.getValueAt(row, 1).toString(); // Cột 1 là Tên sản phẩm
    }
    // Thêm hàm này xuống cuối file ProductPanel
    public void addProductRow(Object[] rowData) {
        tableModel.addRow(rowData);
    }
}