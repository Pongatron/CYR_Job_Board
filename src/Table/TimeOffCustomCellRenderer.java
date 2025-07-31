package Table;

import UI.MainWindow;
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
import java.util.Collections;

public class TimeOffCustomCellRenderer extends DefaultTableCellRenderer {

    private HoverIndex hoverRow;
    private static final Color HOVER_COLOR = new Color(18,18,18);
    private static final Color ROW1_COLOR = new Color(40,40,40);
    private static final Color ROW2_COLOR = new Color(64,64,64);
    private static Border cellBorder = new EmptyBorder((int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()),(int)(10* ZoomManager.getZoom()));

    private int colIndex = -1;

    public TimeOffCustomCellRenderer(HoverIndex hoverRow){
        this.hoverRow = hoverRow;
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.BOTTOM);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//        cellBorder = new EmptyBorder(0,0,0,0);
//        label.setBorder(cellBorder);

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

        Color oldColor = label.getBackground();
        ArrayList<String> people = new ArrayList<>();

        String allPeople = "";
        for(TimeOffValue tov : MainWindow.timeOffValues){
            if(tov.getCol() == column){
                people.add(tov.getWorker());
            }
        }
        Collections.sort(people, String.CASE_INSENSITIVE_ORDER);

        if(!people.isEmpty()) {
            StringBuilder sb = new StringBuilder("<html><center style='text-align: left'>");
            for(String p : people){
                sb.append(p).append("<br>");
            }
            sb.append("</center></html>");
            label.setText(sb.toString());
            label.setBackground(new Color(24, 80, 24));
        }
        else{
            label.setText("");
            label.setBackground(oldColor);
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

}
