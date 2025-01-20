package Table;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class RotatedHeaderRenderer extends DefaultTableCellRenderer {

    private JTable table;
    private TableCellRenderer oldCellRenderer;

    public RotatedHeaderRenderer(JTable table){
        this.table = table;
        oldCellRenderer = table.getTableHeader().getDefaultRenderer();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component com = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
        Component oldHeader = oldCellRenderer.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
        JLabel oldLabel = (JLabel) oldHeader;
        JLabel label = (JLabel) com;
        label.setHorizontalTextPosition(oldLabel.getHorizontalTextPosition());
        label.setIcon(oldLabel.getIcon());
        this.setBorder(new EmptyBorder(8,10,8,10));
        com.setFont(table.getTableHeader().getFont());
        com.setBackground(table.getTableHeader().getBackground());
        return com;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(table.getGridColor());
        g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
        g2.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
        g2.dispose();
        Graphics2D g2d = (Graphics2D) g.create();

        int width = getWidth();
        int height = getHeight();
        g2d.rotate(-Math.PI / 2, width / 2, height / 2);
        g2d.translate(0,5);
        g2d.setFont(new Font(getFont().getFontName(), Font.BOLD, getFont().getSize()));
        String truncatedText = getText();
        FontMetrics fm = g2d.getFontMetrics();
        int stringWidth = fm.stringWidth(truncatedText);
        g2d.drawString(truncatedText, (width - stringWidth) / 2, height / 2);
    }
}
