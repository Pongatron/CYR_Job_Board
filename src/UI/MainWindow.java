package UI;

import DatabaseInteraction.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import static DatabaseInteraction.Filter.FilterStatus.*;

public class MainWindow extends JFrame implements ActionListener, MouseListener {

    private DatabaseInteraction database;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel centerPanel;
    private JPanel topPanel;
    private JPanel filterListPanel;

    private JOptionPane insertPane;

    private JButton insertButton;
    private JToolBar mainBar;
    private JScrollPane filterScroll;
    private JScrollPane tableScroll;

    private JLabel filtersLabel = new JLabel("Filters:");

    private JTable table;
    private JComboBox tablesComboBox;
    private ArrayList<String> filterList;
    private InsertWindow insertWindow;

    private boolean sortOrder = true;

    public MainWindow() throws Exception{

        database = new DatabaseInteraction();
        initializeComponents();

        mainBar.add(tablesComboBox);
        mainBar.add(filterScroll);
        mainBar.add(insertButton);
        centerPanel.add(tableScroll);
        filterListPanel.add(filtersLabel);

        this.add(mainBar, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.EAST);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(1000, 600));
        this.setMinimumSize(new Dimension(800, 600));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    public void initializeComponents()throws Exception{
        table = new JTable();
        table.getTableHeader().addMouseListener(this);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setDefaultEditor(Object.class, null);
        table.getTableHeader().setReorderingAllowed(false);

        filterList = new ArrayList<>();

        leftPanel = new JPanel();
        leftPanel.setBackground(Color.PINK);
        leftPanel.setPreferredSize(new Dimension(20, 100));

        rightPanel = new JPanel();
        rightPanel.setBackground(Color.PINK);
        rightPanel.setPreferredSize(new Dimension(20, 100));

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        topPanel.setBackground(Color.lightGray);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBackground(Color.GRAY);

        filterListPanel = new JPanel();
        filterListPanel.setLayout(new BoxLayout(filterListPanel,BoxLayout.Y_AXIS));

        mainBar = new JToolBar();
        mainBar.setFloatable(false);
        mainBar.setPreferredSize(new Dimension(500, 100));
        mainBar.setMinimumSize(new Dimension(10, 100));

        filterScroll = new JScrollPane(filterListPanel);
        filterScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        filterScroll.setMaximumSize(new Dimension(200, 100));

        tableScroll = new JScrollPane(table);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        tablesComboBox = new JComboBox(database.getTables());
        tablesComboBox.addActionListener(this);
        tablesComboBox.setMaximumSize(tablesComboBox.getPreferredSize());

        insertButton = new JButton("Insert");
        insertButton.addActionListener(this);
        filters = new ArrayList<>();
    }
/*
    public void refreshFilters(String tableName)throws Exception{
        filterList = database.getColumns(tableName);
        filterListPanel.add(filtersLabel);
        for(String s : filterList){
            JCheckBox check = new JCheckBox(s);
            check.setFocusable(false);
            filterListPanel.add(check);
        }
        this.validate();
        this.repaint();
    }
*/
    public void clearFilters(){
        filterListPanel.removeAll();
    }

    public void loadTable(ResultSet rs) throws SQLException {

        ResultSetMetaData rsMeta = rs.getMetaData();
        DefaultTableModel tableModel = new DefaultTableModel();
        int colCount = rsMeta.getColumnCount();

        for(int i = 1; i <= colCount; i++){
            tableModel.addColumn(rsMeta.getColumnLabel(i));
        }

        Object[] row = new Object[colCount];
        while (rs.next()){
            for(int i = 0; i < colCount; i++){
                row[i] = rs.getObject(i + 1);
            }
            tableModel.addRow(row);
        }

        table.setModel(tableModel);
        modifyTable();
        for(int i = colCount; i < table.getColumnCount(); i++){
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(30);
            col.setMaxWidth(30);
            col.setMinWidth(40);
        }
    }

    public void modifyTable(){

        TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int maxWidth = 0;

            TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
            Component headerComp = headerRenderer.getTableCellRendererComponent(table, table.getColumnName(column), false, false, -1, column);
            maxWidth = Math.max(maxWidth, headerComp.getPreferredSize().width);
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
            }
            // Set the minimum width of the column
            TableColumn tableColumn = columnModel.getColumn(column);
            tableColumn.setMinWidth(maxWidth + 5); // 5 is for padding
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try{
            if(e.getSource() == tablesComboBox && !tablesComboBox.getSelectedItem().equals("[Select]")){
                SelectQueryBuilder qb = new SelectQueryBuilder();
                qb.select("*");
                qb.from((String) tablesComboBox.getSelectedItem());
                loadTable(database.sendSelect(qb.build()));
                clearFilters();
                //refreshFilters(tablesComboBox.getSelectedItem().toString());
                this.validate();
                this.repaint();
            }
            if(e.getSource() == insertButton){

                insertWindow = new InsertWindow(database);

            }


        } catch(Exception ee){
            ee.printStackTrace();
        }

    }

    ArrayList<Filter> filters;
    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() == 1){
            try {
                filterListPanel.removeAll();
                int col = table.columnAtPoint(e.getPoint());
                String name = table.getColumnName(col);
                SelectQueryBuilder qb = new SelectQueryBuilder();
                qb.select("*");
                qb.from("jobs");



                boolean found = false;
                if(!filters.isEmpty()) {
                    for (Filter f : filters) {
                        if(f.getFilterName().equals(name)) {
                            found = true;
                            switch (f.getFilterStatus()) {
                                case ASC:
                                    f.setFilterStatus(DESC);
                                    break;
                                case DESC:
                                    filters.remove(f);
                                    break;
                            }
                        }
                        if(found){
                            break;
                        }
                    }
                    if(!found){
                        filters.add(new Filter(name, ASC));
                    }

                }
                else{
                    filters.add(new Filter(name, ASC));
                }


                if(!filters.isEmpty()) {
                    qb.orderBy(filters);
                }

                filterListPanel.add(filtersLabel);
                for(Filter f : filters){
                    JLabel label = new JLabel(f.getFilterName() + " " + f.getFilterStatus());
                    label.setFocusable(false);
                    filterListPanel.add(label);
                }
                loadTable(database.sendSelect(qb.build()));
                this.validate();
                this.repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
