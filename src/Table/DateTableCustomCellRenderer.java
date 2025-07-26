package Table;

import UI.ZoomManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DateTableCustomCellRenderer extends DefaultTableCellRenderer {

    private HoverIndex hoverRow;
    private DateRange dateRange;
    private static final Color HOVER_COLOR = new Color(18,18,18);
    private static final Color ROW1_COLOR = new Color(40,40,40);
    private static final Color ROW2_COLOR = new Color(64,64,64);
    private static final Color BUILD_COLOR = new Color(0,100,180);
    private static final Color FINISH_COLOR = new Color(200,170,0);
    private static final Color EXTRA_COLOR = new Color(120, 50,120);
    private static final Color INSTALL_COLOR = new Color(200,40,40);
    private static Border cellBorder = new EmptyBorder((int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()));

    private int colIndex = -1;
    private ArrayList<DateRange> dates;

    public DateTableCustomCellRenderer(HoverIndex hoverRow){
        this.hoverRow = hoverRow;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JComponent com = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        cellBorder = new EmptyBorder((int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()));
        com.setBorder(cellBorder);
        com.setFont(table.getFont());

        if(table.getColumnName(column).contains("Mon"))
            colIndex = column;

        if(row % 2 == 0){
            com.setBackground(ROW1_COLOR);
        }
        else{
            com.setBackground(ROW2_COLOR);
        }
        com.setForeground(Color.white);
        if(dates != null) {

            dateRange = dates.get(row);
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("E- dd- MMM");
            if (isSelected) {
                com.setBackground(table.getSelectionBackground());
            } else {
                if (row == hoverRow.getIndex()) {
                    com.setBackground(HOVER_COLOR);
                }
                for (LocalDate date : dateRange.getBuildDays()) {
                    String dateString = date.format(dateFormat);
                    if (dateString.equals(table.getColumnName(column))) {
                        com.setBackground(BUILD_COLOR);
                        break;
                    }
                }
                for (LocalDate date : dateRange.getFinishDays()) {
                    String dateString = date.format(dateFormat);
                    if (dateString.equals(table.getColumnName(column))) {
                        com.setBackground(FINISH_COLOR);
                        break;
                    }
                }
                for (LocalDate date : dateRange.getExtraDays()) {
                    String dateString = date.format(dateFormat);
                    if (dateString.equals(table.getColumnName(column))) {
                        com.setBackground(EXTRA_COLOR);
                        break;
                    }
                }
                for (LocalDate date : dateRange.getInstallDays()) {
                    String dateString = date.format(dateFormat);
                    if (dateString.equals(table.getColumnName(column))) {
                        com.setBackground(INSTALL_COLOR);
                        break;
                    }
                }
            }
        }
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
