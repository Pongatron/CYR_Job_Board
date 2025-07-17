package Table;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TableCustomCellRenderer extends DefaultTableCellRenderer {

    private HoverIndex hoverRow;
    private static final Color HOVER_COLOR = new Color(18,18,18);
    private static final Color ROW1_COLOR = new Color(40,40,40);
    private static final Color ROW2_COLOR = new Color(64,64,64);

    public TableCustomCellRenderer(HoverIndex hoverRow){
        this.hoverRow = hoverRow;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setBorder( new EmptyBorder(10,10,10,10));
        if(isSelected){
            label.setBackground(table.getSelectionBackground());
        }
        else{
            if(row == hoverRow.getIndex()){
                label.setBackground(HOVER_COLOR);
                label.setForeground(Color.white);
            }
            else{
                if(row % 2 == 0){
                    label.setBackground(ROW1_COLOR);
                    label.setForeground(Color.white);
                }
                else{
                    label.setBackground(ROW2_COLOR);
                    label.setForeground(Color.white);
                }
            }
        }
        label.setFont(table.getFont());

        // Shows tooltip only if the text is too wide for the cell
        if (value != null) {
            String text = value.toString();
            label.setText(text);

            // Calculate available space
            FontMetrics fm = label.getFontMetrics(label.getFont());
            int textWidth = fm.stringWidth(text);

            int columnWidth = table.getColumnModel().getColumn(column).getWidth();

            // Subtract padding/insets (label padding + border)
            Insets insets = label.getInsets();
            int availableWidth = columnWidth - insets.left - insets.right;

            if (textWidth > availableWidth) {
                label.setToolTipText(text);
            } else {
                label.setToolTipText(null);
            }
        } else {
            label.setToolTipText(null);
            label.setText("");
        }
        return label;
    }
}
