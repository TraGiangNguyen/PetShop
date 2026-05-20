package is216.petshop.Product;

import is216.petshop.util.GenericFormDialog;
import is216.petshop.util.TableActionEvent; // Đảm bảo bạn đã tạo file Interface này
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProductController {
    private ProductPanel view;
    // private ProductDAO modelDAO; 

    public ProductController(ProductPanel view) {
        this.view = view;

        // 1. Gắn sự kiện cho nút Thêm mới ở góc trên (Dùng lớp nội AddListener bên dưới)
        this.view.addAddProductListener(new AddListener());

        // 2. Gắn sự kiện cho các nút Sửa/Xóa nằm bên TRONG bảng thông qua Interface
        this.view.setTableActionEvent(new TableActionEvent() {
            @Override
            public void onEdit(int row) {
                // Lấy tên sản phẩm từ dòng bị bấm nút sửa
                String productName = view.getProductNameAt(row);
                
                // Mở form Sửa (Sử dụng lại GenericFormDialog nhưng với tiêu đề khác)
                String[] fields = {"Tên sản phẩm", "Danh mục", "Giá (VNĐ)", "Tồn kho", "URL hình ảnh"};
                GenericFormDialog editDialog = new GenericFormDialog(null, "Chỉnh sửa sản phẩm", fields);
                
                // Tương lai: Bạn sẽ dùng hàm để đổ dữ liệu cũ vào các ô text ở đây
                
                editDialog.setVisible(true);
            }

            @Override
            public void onDelete(int row) {
                String productName = view.getProductNameAt(row);
                
                int confirm = JOptionPane.showConfirmDialog(
                        null, 
                        "Bạn có chắc muốn xóa sản phẩm: " + productName + "?", 
                        "Xác nhận xóa", 
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    // TODO: Gọi modelDAO.delete(...) để xóa thật trong DB
                    view.showMessage("Đã xóa thành công sản phẩm: " + productName);
                    // TODO: Gọi hàm load lại bảng sau khi xóa
                }
            }
        });
    }

    // --- LUỒNG XỬ LÝ NÚT THÊM MỚI ---
// --- LUỒNG XỬ LÝ NÚT THÊM MỚI ---
    class AddListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] productFields = {"Tên sản phẩm", "Danh mục", "Giá (VNĐ)", "Tồn kho", "URL hình ảnh"};
            GenericFormDialog addDialog = new GenericFormDialog(null, "Thêm sản phẩm mới", productFields);
            
            addDialog.addAddButtonListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    // 1. Lấy toàn bộ dữ liệu từ Form
                    String name = addDialog.getFieldValue("Tên sản phẩm");
                    String category = addDialog.getFieldValue("Danh mục");
                    String price = addDialog.getFieldValue("Giá (VNĐ)");
                    String stock = addDialog.getFieldValue("Tồn kho"); 
                    
                    if(name.isEmpty() || price.isEmpty()) {
                        JOptionPane.showMessageDialog(addDialog, "Vui lòng nhập tối thiểu Tên và Giá!");
                        return;
                    }

                    // 2. Gói dữ liệu lại thành 1 mảng (Object[]) 
                    // Lưu ý: Thứ tự phải khớp với thứ tự cột: Hình ảnh, Tên, Danh mục, Giá, Tồn kho, Thao tác
                    Object[] newRow = {
                        "[Ảnh]",     // Cột 1: Hình ảnh
                        name,        // Cột 2: Tên sản phẩm
                        category,    // Cột 3: Danh mục
                        price + "đ", // Cột 4: Giá (thêm chữ đ cho đẹp)
                        stock,       // Cột 5: Tồn kho
                        ""           // Cột 6: Thao tác (để trống, bảng sẽ tự vẽ nút)
                    };

                    // 3. Đẩy mảng dữ liệu này xuống Panel để cập nhật lên bảng
                    view.addProductRow(newRow);

                    // 4. Đóng form và báo thành công
                    addDialog.closeDialog();
                    view.showMessage("Đã thêm thành công sản phẩm: " + name);
                }
            });

            addDialog.setVisible(true);
        }
    }
}