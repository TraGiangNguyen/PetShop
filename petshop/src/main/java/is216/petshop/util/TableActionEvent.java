package is216.petshop.util;

public interface TableActionEvent {
    void onEdit(int row);   // Hàm chạy khi bấm Sửa
    void onDelete(int row); // Hàm chạy khi bấm Xóa
    
}