package is216.petshop.util;

import is216.petshop.util.TableActionEvent;
import is216.petshop.util.TableActionPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TableActionCellEditor extends DefaultCellEditor {
    private TableActionPanel action;
    private JTable table; // Thêm biến lưu table để lấy dòng đang chọn
    public TableActionCellEditor(TableActionEvent event) {
        super(new JCheckBox());
        action = new TableActionPanel();
        
        // Khi bấm nút Sửa
        action.btnEdit.addActionListener(e -> {
            stopCellEditing(); // Bắt buộc phải có để bảng nhả trạng thái đang edit
            event.onEdit(table.getSelectedRow()); // Truyền vị trí dòng ra ngoài
        });
        
        // Khi bấm nút Xóa
        action.btnDelete.addActionListener(e -> {
            stopCellEditing();
            event.onDelete(table.getSelectedRow());
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table; // Lưu lại table
        return action;
    }
}