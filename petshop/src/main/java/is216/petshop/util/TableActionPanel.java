package is216.petshop.util;

import javax.swing.*;
import java.awt.*;

public class TableActionPanel extends JPanel {
    public JButton btnEdit;
    public JButton btnDelete;

    public TableActionPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        setOpaque(false); // Để hiện màu nền của dòng khi được chọn

        btnEdit = new JButton("✏️");
        btnEdit.setToolTipText("Sửa");
        btnEdit.setPreferredSize(new Dimension(35, 30));
        btnEdit.setMargin(new Insets(2, 2, 2, 2)); // Ép lề nhỏ lại để chữ không bị che
        
        btnDelete = new JButton("🗑️");
        btnDelete.setToolTipText("Xóa");
        btnDelete.setPreferredSize(new Dimension(35, 30));
        btnDelete.setMargin(new Insets(2, 2, 2, 2));
        add(btnEdit);
        add(btnDelete);
    }
}