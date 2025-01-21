package Table;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class DateTableCustomCellRenderer extends DefaultTableCellRenderer {

    private HoverIndex hoverRow;
    private static final Color HOVER_COLOR = new Color(18,18,18);
    private static final Color ROW1_COLOR = new Color(40,40,40);
    private static final Color ROW2_COLOR = new Color(64,64,64);
    private int colIndex = -1;

    public DateTableCustomCellRenderer(HoverIndex hoverRow){
        this.hoverRow = hoverRow;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setBorder( new EmptyBorder(10,10,10,10));
        if(isSelected){
            com.setBackground(table.getSelectionBackground());
        }
        else{
            if(row == hoverRow.getIndex()){
                com.setBackground(HOVER_COLOR);
                com.setForeground(Color.white);
            }
            else{
                if(row % 2 == 0){
                    com.setBackground(ROW1_COLOR);
                    com.setForeground(Color.white);
                }
                else{
                    com.setBackground(ROW2_COLOR);
                    com.setForeground(Color.white);
                }
            }
        }
        com.setFont(table.getFont());

        if(table.getColumnName(column).contains("Mon"))
            colIndex = column;

        return com;
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        if (colIndex != -1) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1));
            g2.drawLine(1, 0, 1, getHeight());
        }
        colIndex = -1;
        g2.dispose();
    }
}
