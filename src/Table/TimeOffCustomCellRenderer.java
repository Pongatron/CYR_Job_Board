package Table;

import UI.ZoomManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TimeOffCustomCellRenderer extends DefaultTableCellRenderer {

    private HoverIndex hoverRow;
    private static final Color HOVER_COLOR = new Color(18,18,18);
    private static final Color ROW1_COLOR = new Color(40,40,40);
    private static final Color ROW2_COLOR = new Color(64,64,64);
    private static Border cellBorder = new EmptyBorder((int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()));

    private int colIndex = -1;
    private ArrayList<TimeOffDates> dates;

    public TimeOffCustomCellRenderer(HoverIndex hoverRow){
        this.hoverRow = hoverRow;
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        //cellBorder = new EmptyBorder((int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()));

        if(table.getColumnName(column).contains("Mon"))
            colIndex = column;

        if(row % 2 == 0){
            label.setBackground(ROW1_COLOR);
        }
        else{
            label.setBackground(ROW2_COLOR);
        }
        if (row == hoverRow.getIndex()) {
            label.setBackground(HOVER_COLOR);
        }
        label.setForeground(Color.white);

        //label.setText("OP");
        //System.out.println(dates.get(1));

        if(dates != null) {
            for(int i = 0; i < dates.size(); i++) {
                TimeOffDates dateRange = dates.get(i);
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("E- dd- MMM");

                for (LocalDate date : dateRange.getOffDays()) {
                    String dateString = date.format(dateFormat);
                    if (dateString.equals(table.getColumnName(column))) {
                        label.setText(dateRange.getPerson());
                        label.setBackground(new Color(24,80,24));
                        //System.out.println(dateString + " " + dateRange.getPerson());
                    }
                }
            }
        }
        return label;
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

    public void setTimeOffDates(ArrayList<TimeOffDates> dates){
        this.dates = dates;
    }
}
