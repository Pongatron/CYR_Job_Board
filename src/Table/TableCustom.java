package Table;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class TableCustom {

    public static enum TableType {MULTI_LINE, DEFAULT}

    public static void apply(JScrollPane scroll, TableType type){
        JTable table = (JTable) scroll.getViewport().getComponent(0);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(new TableHeaderCustomCellRenderer(table));
        table.setRowHeight(30);
        HoverIndex hoverRow = new HoverIndex();
        TableCellRenderer cellRender;
        if(type == TableType.DEFAULT){
            cellRender = new TableCustomCellRenderer(hoverRow);
        }
        else{
            cellRender = new TextAreaCellRenderer(hoverRow);
        }

        table.setDefaultRenderer(Object.class, cellRender);
        table.setDefaultRenderer(Boolean.class, new BooleanCellRenderer(hoverRow));

        table.setShowVerticalLines(true);
        table.setGridColor(new Color(220,220,220));
        table.setForeground(new Color(51,51,51));
        table.setSelectionForeground(new Color(51,51,51));
        scroll.setBorder(new LineBorder(new Color(220,220,220)));

        JPanel panel = new JPanel(){
            @Override
            public void paint(Graphics g){
                super.paint(g);
                g.setColor(new Color(220,220,220));
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        panel.setBackground(new Color(250,250,250));
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, panel);
        scroll.getViewport().setBackground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(250,250,250));
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoverRow.setIndex(-1);
                table.repaint();
            }
        });
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if(row != hoverRow.getIndex()){
                    hoverRow.setIndex(row);
                    table.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if(row != hoverRow.getIndex()){
                    hoverRow.setIndex(row);
                    table.repaint();
                }
            }
        });

        adjustColumnWidths(table);
    }

    private static void adjustColumnWidths(JTable table) {

        for(int col = 0; col < table.getColumnCount(); col++){
            TableColumn tableColumn = table.getColumnModel().getColumn(col);
            int preferredWidth = getMaxColumnWidth(table, col);
            int minWidth = 60;
            tableColumn.setPreferredWidth(preferredWidth + 10);
            tableColumn.setMinWidth(minWidth);
        }
    }

    private static int getMaxColumnWidth(JTable table, int col) {

        int maxWidth = 0;

        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
        Component headerComponent = headerRenderer.getTableCellRendererComponent(table, table.getColumnModel().getColumn(col).getHeaderValue(), false,false, 0, col);
        maxWidth = Math.max(maxWidth, headerComponent.getPreferredSize().width);

        for(int row = 0; row < table.getRowCount(); row++){
            TableCellRenderer cellRenderer = table.getCellRenderer(row,col);
            Component cellComponent = table.prepareRenderer(cellRenderer, row, col);
            maxWidth = Math.max(maxWidth, cellComponent.getPreferredSize().width);
        }
        return maxWidth;
    }

}
