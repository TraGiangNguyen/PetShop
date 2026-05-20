package is216.petshop.util;

import is216.petshop.util.TableActionPanel;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TableActionCellRender extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        TableActionPanel action = new TableActionPanel();
        if (isSelected) {
            action.setBackground(com.getBackground());
        } else {
            action.setBackground(Color.WHITE);
        }
        return action;
    }
}   