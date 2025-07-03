package Table;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;

public class DateTableCustomCellRenderer extends DefaultTableCellRenderer {

    private HoverIndex hoverRow;
    private static final Color HOVER_COLOR = new Color(18,18,18);
    private static final Color ROW1_COLOR = new Color(40,40,40);
    private static final Color ROW2_COLOR = new Color(64,64,64);
    private static final Color BUILD_COLOR = new Color(0,100,180);
    private static final Color FINISH_COLOR = new Color(200,170,0);
    private static final Color INSTALL_COLOR = new Color(200,40,40);
    private int colIndex = -1;
    private ArrayList<DateRange> dates;

    public DateTableCustomCellRenderer(HoverIndex hoverRow){
        this.hoverRow = hoverRow;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setBorder( new EmptyBorder(10,10,10,10));

        DateRange dateRange = dates.get(row);
        int dueCol = dateRange.getDueDateCol();
        int buildCol = dueCol - dateRange.getFinishDays() - dateRange.getBuildDays();
        int finishCol = dueCol - dateRange.getFinishDays();
        int installCol = dueCol + dateRange.getInstallDays()-1;
        boolean isSaturday = dateRange.isDueDateSaturday();


        if(isSelected){
            com.setBackground(table.getSelectionBackground());
        }
        else{
            if(row == hoverRow.getIndex()){
                com.setBackground(HOVER_COLOR);
                com.setForeground(Color.white);
            }
            else if(column >= buildCol && column < finishCol){
                com.setBackground(BUILD_COLOR);
                com.setForeground(Color.white);
            }
            else if(column >= finishCol && column < dueCol){
                com.setBackground(FINISH_COLOR);
                com.setForeground(Color.white);
            }
            else if(column >= dueCol && column <= installCol && dueCol != -1){
                com.setBackground(INSTALL_COLOR);
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
    public void setDates(ArrayList<DateRange> dates){
        this.dates = dates;
    }
}
