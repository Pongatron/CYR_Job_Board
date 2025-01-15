package Table;

import javax.swing.*;
import java.awt.*;

public class BooleanCellRenderer extends TableCustomCellRenderer{

    public BooleanCellRenderer(HoverIndex hoverRow){
        super(hoverRow);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JCheckBox ch = new JCheckBox();
        ch.setHorizontalAlignment(SwingConstants.CENTER);
        ch.setOpaque(true);
        ch.setSelected((boolean) value);
        ch.setBackground(com.getBackground());
        return ch;
    }
}
