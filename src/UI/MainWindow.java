package UI;

import DatabaseInteraction.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainWindow extends JFrame implements ActionListener{

    DatabaseInteraction database;
    JPanel leftPanel;
    JPanel rightPanel;
    JPanel centerPanel;
    JPanel topPanel;
    JPanel filterListPanel;

    JButton insertButton;
    JToolBar mainBar;
    JScrollPane filterScroll;
    JScrollPane tableScroll;

    JLabel filtersLabel = new JLabel("Filters:");

    JTable table;
    JComboBox tablesComboBox;
    ArrayList<String> filterList;

    public MainWindow() throws Exception{

        database = new DatabaseInteraction();
        initializeComponents();

        mainBar.add(tablesComboBox);
        mainBar.add(filterScroll);
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
        leftPanel = new JPanel();
        leftPanel.setBackground(Color.PINK);
        leftPanel.setPreferredSize(new Dimension(20, 100));

        rightPanel = new JPanel();
        rightPanel.setBackground(Color.PINK);
        rightPanel.setPreferredSize(new Dimension(20, 100));

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        topPanel.setBackground(Color.lightGray);
        //topPanel.setPreferredSize(new Dimension(50, 100));

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBackground(Color.GRAY);
        //centerPanel.setPreferredSize(new Dimension(500, 50));

        filterListPanel = new JPanel();
        filterListPanel.setLayout(new BoxLayout(filterListPanel,BoxLayout.Y_AXIS));
        //filterListPanel.setPreferredSize(topPanel.getPreferredSize());

        table = new JTable();

        mainBar = new JToolBar();

        filterScroll = new JScrollPane(filterListPanel);
        filterScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        filterScroll.setPreferredSize(new Dimension(100, 100));

        tableScroll = new JScrollPane(table);

        tablesComboBox = new JComboBox(database.getTables());
        tablesComboBox.addActionListener(this);

        filterList = new ArrayList<>();

    }

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

    public void clearFilters(){
        filterListPanel.removeAll();
    }

    public void setTable(ResultSet resultSet) throws SQLException {

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setDefaultEditor(Object.class, null);
        ResultSetMetaData rsMetaData = resultSet.getMetaData();
        DefaultTableModel tableModel = new DefaultTableModel();
        int colCount = rsMetaData.getColumnCount();

        for(int i = 1; i <= colCount; i++){
            tableModel.addColumn(rsMetaData.getColumnLabel(i));
        }

        Object[] row = new Object[colCount];
        while (resultSet.next()){
            for(int i = 0; i < colCount; i++){
                row[i] = resultSet.getObject(i + 1);
            }
            tableModel.addRow(row);
        }

        //table.setModel(tableModel);
        for(int i = 0; i < 5; i++){
            tableModel.addColumn("M");
            tableModel.addColumn("T");
            tableModel.addColumn("W");
            tableModel.addColumn("TH");
            tableModel.addColumn("F");
        }

        table.setModel(tableModel);

        //table.setRowSorter(ts);
        table.getTableHeader().setReorderingAllowed(false);
        modifyTable();for(int i = colCount; i < table.getColumnCount(); i++){
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
                System.out.println(tablesComboBox.getSelectedItem().toString());
                System.out.println("SelectedItem: "+ tablesComboBox.getSelectedItem());
                SelectQueryBuilder qb = new SelectQueryBuilder();
                qb.select("*");
                qb.from((String) tablesComboBox.getSelectedItem());
                setTable(database.sendSelect(qb.build()));
                clearFilters();
                refreshFilters(tablesComboBox.getSelectedItem().toString());
                this.validate();
                this.repaint();
            }
            if(e.getSource() == insertButton){
                InsertQueryBuilder ib = new InsertQueryBuilder();
                ib.insertInto("jobs");
                ib.values("6", "customer 9", "now()", "desk", "1-2-2026");
                database.sendUpdate(ib.build());
            }

        } catch(Exception ee){
            ee.printStackTrace();
        }

    }
}
