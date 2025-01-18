package UI;

import DatabaseInteraction.*;
import Table.RotatedHeaderRenderer;
import Table.TableCustom;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static DatabaseInteraction.Filter.FilterStatus.*;

public class MainWindow extends JFrame implements ActionListener, MouseListener {

    private static final Font PLAIN_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 12);

    private DatabaseInteraction database;
    private JPanel centerPanel;
    private JPanel topPanel;
    private JPanel filterListPanel;

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


    private JButton addJobButton;
    private JButton updateJobButton;
    private JButton deleteButton;
    private JPanel buttonPanel;
    private JButton jwoFilterButton;
    private JButton customerFilterButton;
    private JButton dateFilterButton;


    public MainWindow() throws Exception{

        database = new DatabaseInteraction();
        initializeComponents();

        SelectQueryBuilder qb = new SelectQueryBuilder();
        qb.select("*");
        qb.from("job_board");
        loadTable(database.sendSelect(qb.build()));


        buttonPanel.add(addJobButton);
        buttonPanel.add(updateJobButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(jwoFilterButton);
        buttonPanel.add(customerFilterButton);
        buttonPanel.add(dateFilterButton);

        JPanel topContainerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topContainerPanel.setBackground(new Color(24,24,24));
        topContainerPanel.add(buttonPanel);
        topContainerPanel.add(tablesComboBox);

        // Add this topContainerPanel to the mainBar (or directly to the frame if preferred)
        mainBar.add(topContainerPanel);


        centerPanel.add(tableScroll, BorderLayout.CENTER);

        this.add(mainBar, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Job Board");
        this.setPreferredSize(new Dimension(1000, 900));
        this.setMinimumSize(new Dimension(800, 100));
        this.setBackground(new Color(24,24,24));
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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(new Color(24,24,24));

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBackground(new Color(24,24,24));
        topPanel.setPreferredSize(new Dimension(0, 100));
        topPanel.setBorder(new EmptyBorder(20,20,20,20));

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(5,5,5,5));
        centerPanel.setBackground(new Color(24,24,24));

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2,3, 10,10));
        buttonPanel.setPreferredSize(new Dimension(400,topPanel.getPreferredSize().height));
        buttonPanel.setBorder(new EmptyBorder(20,20,20,20));
        buttonPanel.setBackground(new Color(24,24,24));

        addJobButton = new JButton("Add Job");
        addJobButton.setBackground(new Color(220, 46, 35));
        addJobButton.setForeground(new Color(0,0,0));
        addJobButton.setFont(BOLD_FONT);
        addJobButton.setFocusable(false);
        addJobButton.addActionListener(this);


        updateJobButton = new JButton("Update");
        updateJobButton.setBackground(new Color(0, 52, 191));
        updateJobButton.setForeground(new Color(255,255,255));
        updateJobButton.setFont(BOLD_FONT);
        updateJobButton.setFocusable(false);
        updateJobButton.addActionListener(this);

        deleteButton = new JButton("Delete");
        deleteButton.setBackground(new Color(240, 232, 5));
        deleteButton.setForeground(new Color(0,0,0));
        deleteButton.setFont(BOLD_FONT);
        deleteButton.setFocusable(false);
        deleteButton.addActionListener(this);

        jwoFilterButton = new JButton("JWO");
        jwoFilterButton.setBackground(new Color(44, 123, 201));
        jwoFilterButton.setForeground(new Color(255,255,255));
        jwoFilterButton.setFont(BOLD_FONT);
        jwoFilterButton.setFocusable(false);
        jwoFilterButton.addActionListener(this);

        customerFilterButton = new JButton("Customer");
        customerFilterButton.setBackground(new Color(44, 123, 201));
        customerFilterButton.setForeground(new Color(255,255,255));
        customerFilterButton.setFont(BOLD_FONT);
        customerFilterButton.setFocusable(false);
        customerFilterButton.addActionListener(this);

        dateFilterButton = new JButton("Due Date");
        dateFilterButton.setBackground(new Color(44, 123, 201));
        dateFilterButton.setForeground(new Color(255,255,255));
        dateFilterButton.setFont(BOLD_FONT);
        dateFilterButton.setFocusable(false);
        dateFilterButton.addActionListener(this);

        filterListPanel = new JPanel();
        filterListPanel.setLayout(new BoxLayout(filterListPanel,BoxLayout.Y_AXIS));

        mainBar = new JToolBar();
        mainBar.setFloatable(false);
        mainBar.setPreferredSize(new Dimension(0, 100));
        mainBar.setBackground(new Color(24,24,24));

        filterScroll = new JScrollPane(filterListPanel);
        filterScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        filterScroll.setMaximumSize(new Dimension(200, 100));

        tableScroll = new JScrollPane(table);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        tableScroll.setPreferredSize(new Dimension(500,0));
        tableScroll.getViewport().setBackground(new Color(24,24,24));
        tableScroll.getVerticalScrollBar().setBackground(new Color(24,24,24));
        tableScroll.getHorizontalScrollBar().setBackground(new Color(24,24,24));

        tablesComboBox = new JComboBox(database.getTables());
        tablesComboBox.addActionListener(this);
        tablesComboBox.setMaximumSize(tablesComboBox.getPreferredSize());

        insertButton = new JButton("Insert");
        insertButton.addActionListener(this);

        createTableButton = new JButton("Create Table");
        createTableButton.addActionListener(this);
        createTableButton.setBackground(Color.PINK);

        filters = new ArrayList<>();

        TableCustom.apply(tableScroll, TableCustom.TableType.DEFAULT);
    }

    public void loadTable(ResultSet rs) throws SQLException {

        ResultSetMetaData rsMeta = rs.getMetaData();
        DefaultTableModel tableModel = new DefaultTableModel();
        int colCount = rsMeta.getColumnCount();

        for (int i = 1; i <= colCount; i++) {
            tableModel.addColumn(rsMeta.getColumnLabel(i));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("E-dd-MMM");
        Calendar calendar = Calendar.getInstance();


        for (int i = 0; i < 365; i++) {
            Date date = calendar.getTime();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if(dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                String day = dateFormat.format(date);
                tableModel.addColumn(day);
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Object[] row = new Object[colCount];
        while (rs.next()) {
            for (int i = 0; i < colCount; i++) {
                row[i] = rs.getObject(i + 1);
            }
            tableModel.addRow(row);
        }
        table.setModel(tableModel);
        table.getTableHeader().setFont(new Font(table.getTableHeader().getFont().getFontName(), Font.BOLD, table.getTableHeader().getFont().getSize()));

        FontMetrics fontMetrics = table.getFontMetrics(table.getFont());
        for(int col = 0; col < colCount; col++){
            int maxWidth = 0;

            int headerWidth = fontMetrics.stringWidth(tableModel.getColumnName(col));
            maxWidth = headerWidth;

            for(int i = 0; i < table.getRowCount(); i++){
                TableCellRenderer cellRenderer = table.getCellRenderer(i, col);
                Component comp = table.prepareRenderer(cellRenderer, i, col);
                int cellWidth = comp.getPreferredSize().width;
                maxWidth = Math.max(maxWidth, cellWidth);
            }
            table.getColumnModel().getColumn(col).setPreferredWidth(maxWidth+25);
        }


        TableCellRenderer headerRenderer = new RotatedHeaderRenderer(table);
        for (int i = colCount; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            table.getColumnModel().getColumn(i).setMinWidth(30);
            table.getColumnModel().getColumn(i).setMaxWidth(30);
            table.getColumnModel().getColumn(i).setPreferredWidth(30);
        }

        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getPreferredSize().width, 100));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == addJobButton){
            try {
                new InsertWindow(database);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        if(e.getSource() == updateJobButton){

        }
        if(e.getSource() == deleteButton){

        }
        if(e.getSource() == jwoFilterButton){

        }
        if(e.getSource() == customerFilterButton){

        }
        if(e.getSource() == dateFilterButton){

        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

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
