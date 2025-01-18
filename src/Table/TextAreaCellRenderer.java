package Table;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

public class TextAreaCellRenderer extends JTextArea implements TableCellRenderer {

    private final List<List<Integer>> rowAndCellHeights = new ArrayList<>();
    private HoverIndex hoverRow;
    private static Color HOVER_COLOR = new Color(18,18,18);
    private static Color ROW1_COLOR = new Color(40,40,40);
    private static Color ROW2_COLOR = new Color(64,64,64);

    public TextAreaCellRenderer(HoverIndex hoverRow){
        this.hoverRow = hoverRow;
        setWrapStyleWord(true);
        setLineWrap(true);
        setOpaque(true);
        setBorder(new EmptyBorder(8,10,8,10));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(Objects.toString(value, ""));
        adjustRowHeight(table, row, column);
        if(isSelected){
            setBackground(table.getSelectionBackground());
        }
        else{
            if(row == hoverRow.getIndex()){
                setBackground(HOVER_COLOR);
                setForeground(Color.white);
            }
            else{
                if(row % 2 == 0){
                    setBackground(ROW1_COLOR);
                    setForeground(Color.white);
                }
                else{
                    setBackground(ROW2_COLOR);
                    setForeground(Color.white);
                }
            }
        }
        setFont(table.getFont());
        return this;
    }

    private void adjustRowHeight(JTable table, int row, int column) {
        setBounds(table.getCellRect(row, column, false));
        int preferredHeight = getPreferredSize().height;
        while(rowAndCellHeights.size() <= row){
            rowAndCellHeights.add(new ArrayList<>(column));
        }
        List<Integer> list = rowAndCellHeights.get(row);
        while(list.size() <= column){
            list.add(0);
        }
        list.set(column, preferredHeight);
        int max = list.stream().max((x,y) -> Integer.compare(x,y)).get();
        if(table.getRowHeight(row) != max){
            table.setRowHeight(row, max);
        }
    }
}
