package Table;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TableHeaderCustomCellRenderer extends DefaultTableCellRenderer {

    private JTable table;
    private TableCellRenderer oldCellRenderer;

    public TableHeaderCustomCellRenderer(JTable table){
        this.table = table;
        oldCellRenderer = table.getTableHeader().getDefaultRenderer();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String originalText = value.toString();

        String wrappedText = "<html><center style='text-align: left'>" + originalText.replace(" ", "<br>") + "</center></html>";
        label.setText(wrappedText);

        Component oldHeader = oldCellRenderer.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

        JLabel oldLabel = (JLabel) oldHeader;
        label.setHorizontalTextPosition(oldLabel.getHorizontalTextPosition());
        label.setIcon(oldLabel.getIcon());
        label.setVerticalAlignment(SwingConstants.CENTER);
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
        label.setFont(table.getTableHeader().getFont());
        label.setBackground(table.getTableHeader().getBackground());
        label.setOpaque(true);

        return label;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(table.getGridColor());
        g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
        g2.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
        g2.dispose();
    }
}
