package Table;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class TableCustom {

    public static enum TableType {MULTI_LINE, DEFAULT, VERTICAL}

    public static final Color GRIDLINE_COLOR = new Color(70,70,70);
    public static final Color HEADERTEXT_COLOR = new Color(255,255,255);
    public static final Color HEADERCELL_COLOR = new Color(50,50,50);
    public static final Color SELECTION_COLOR = new Color(213, 240, 242);

    public static void apply(JScrollPane scroll, TableType type){
        JTable table = (JTable) scroll.getViewport().getComponent(0);
        table.setSelectionBackground(SELECTION_COLOR);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(new TableHeaderCustomCellRenderer(table));
        table.setRowHeight(30);
        HoverIndex hoverRow = new HoverIndex();
        TableCellRenderer cellRender;
        if(type == TableType.DEFAULT){
            cellRender = new TableCustomCellRenderer(hoverRow);
        }
        else if(type == TableType.VERTICAL){
            cellRender = new TableCustomCellRenderer(hoverRow);
        }
        else{
            cellRender = new TextAreaCellRenderer(hoverRow);
        }

        table.setDefaultRenderer(Object.class, cellRender);
        table.setDefaultRenderer(Boolean.class, new BooleanCellRenderer(hoverRow));

        table.setShowVerticalLines(true);
        table.setGridColor(GRIDLINE_COLOR);
        table.setForeground(HEADERTEXT_COLOR);
        table.setSelectionForeground(HEADERTEXT_COLOR);
        scroll.setBorder(new LineBorder(GRIDLINE_COLOR));

        JPanel panel = new JPanel(){
            @Override
            public void paint(Graphics g){
                super.paint(g);
                g.setColor(GRIDLINE_COLOR);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        panel.setBackground(HEADERCELL_COLOR);
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, panel);
        table.getTableHeader().setBackground(HEADERCELL_COLOR);
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
