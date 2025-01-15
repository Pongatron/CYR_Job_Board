package UI;

import DatabaseInteraction.*;
import Table.TableCustom;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private JPanel centerPanel;
    private JPanel topPanel;
    private JPanel filterListPanel;

    private JOptionPane insertPane;

    private JButton insertButton;
    private JButton createTableButton;
    private JToolBar mainBar;
    private JScrollPane filterScroll;
    private JScrollPane tableScroll;

    private JLabel filtersLabel = new JLabel("Filters:");

    private JTable table;
    private JComboBox tablesComboBox;
    private InsertWindow insertWindow;
    private ArrayList<Filter> filters;

    private boolean sortOrder = true;

    public MainWindow() throws Exception{

        database = new DatabaseInteraction();
        initializeComponents();

        SelectQueryBuilder qb = new SelectQueryBuilder();
        qb.select("*");
        qb.from("jobs");
        loadTable(database.sendSelect(qb.build()));

        mainBar.add(tablesComboBox);
        mainBar.add(filterScroll);
        mainBar.add(insertButton);
        mainBar.add(createTableButton);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        filterListPanel.add(filtersLabel);

        this.add(mainBar, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(1000, 900));
        this.setMinimumSize(new Dimension(800, 100));
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

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        topPanel.setBackground(Color.lightGray);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(5,5,5,5));

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

        createTableButton = new JButton("Create Table");
        createTableButton.addActionListener(this);
        createTableButton.setBackground(Color.PINK);

        filters = new ArrayList<>();
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
        for(int i = colCount; i < table.getColumnCount(); i++){
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(30);
            col.setMaxWidth(30);
            col.setMinWidth(40);
        }

        TableCustom.apply(tableScroll, TableCustom.TableType.MULTI_LINE);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try{
            if(e.getSource() == tablesComboBox && !tablesComboBox.getSelectedItem().equals("[Select]")){
                filters.clear();
                filterListPanel.removeAll();
                filterListPanel.repaint();
                String selected = tablesComboBox.getSelectedItem().toString();
                SelectQueryBuilder qb = new SelectQueryBuilder();
                qb.select("*");
                qb.from(selected);
                loadTable(database.sendSelect(qb.build()));
            }
            if(e.getSource() == insertButton){
                insertWindow = new InsertWindow(database, tablesComboBox.getSelectedItem().toString());
            }
            if(e.getSource() == createTableButton){
                new CreateTableWindow();
            }
        } catch(Exception ee){
            ee.printStackTrace();
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() == 1){
            try {
                filterListPanel.removeAll();
                int col = table.columnAtPoint(e.getPoint());
                String name = table.getColumnName(col);
                SelectQueryBuilder qb = new SelectQueryBuilder();
                qb.select("*");
                qb.from(tablesComboBox.getSelectedItem().toString());

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
