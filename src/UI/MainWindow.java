package UI;

import DatabaseInteraction.*;
import Table.RotatedHeaderRenderer;
import Table.TableCustom;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

import static DatabaseInteraction.Filter.FilterStatus.*;

public class MainWindow extends JFrame implements ActionListener, MouseListener {

    private static final Font PLAIN_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 12);
    private DatabaseInteraction database;
    private JPanel centerPanel;
    private JPanel topPanel;
    private JPanel buttonPanel;
    private JButton addJobButton;
    private JButton updateJobButton;
    private JButton deleteButton;
    private JButton jwoFilterButton;
    private JButton customerFilterButton;
    private JButton dateFilterButton;
    private JButton todayButton;
    private JButton resetViewButton;
    private JScrollPane tableScroll;
    private JScrollPane datesScroll;
    private JTable table;
    private JTable datesTable;
    private JSplitPane splitPane;
    private JToolBar mainBar;
    private int totalWidth;
    private int todayCol = -1;

    public MainWindow() throws Exception{

        database = new DatabaseInteraction();
        initializeComponents();
        syncScrollPanes();

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
        topContainerPanel.add(resetViewButton);
        topContainerPanel.add(todayButton);

        mainBar.add(topContainerPanel);

        centerPanel.setPreferredSize(new Dimension(totalWidth, 200));
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        splitPane.setDividerLocation(centerPanel.getPreferredSize().width);

        this.add(mainBar, BorderLayout.NORTH);
        this.add(splitPane, BorderLayout.CENTER);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Job Board");
        this.setPreferredSize(new Dimension(1440, 900));
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

        datesTable = new JTable();
        datesTable.getTableHeader().addMouseListener(this);
        datesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        datesTable.setDefaultEditor(Object.class, null);
        datesTable.getTableHeader().setReorderingAllowed(false);
        datesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        datesTable.setBackground(new Color(24,24,24));

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBackground(new Color(24,24,24));
        topPanel.setPreferredSize(new Dimension(0, 100));
        topPanel.setBorder(new EmptyBorder(20,20,20,20));

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
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

        todayButton = new JButton("Today");
        todayButton.setBackground(new Color(0, 0, 0));
        todayButton.setForeground(new Color(255,255,255));
        todayButton.setFont(BOLD_FONT);
        todayButton.setFocusable(false);
        todayButton.addActionListener(this);

        resetViewButton = new JButton("Reset View");
        resetViewButton.setBackground(new Color(0, 0, 0));
        resetViewButton.setForeground(new Color(255,255,255));
        resetViewButton.setFont(BOLD_FONT);
        resetViewButton.setFocusable(false);
        resetViewButton.addActionListener(this);

        mainBar = new JToolBar();
        mainBar.setFloatable(false);
        mainBar.setPreferredSize(new Dimension(0, 100));
        mainBar.setBackground(new Color(24,24,24));

        tableScroll = new JScrollPane(table);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setPreferredSize(new Dimension(500,0));
        tableScroll.getViewport().setBackground(new Color(24,24,24));
        tableScroll.getVerticalScrollBar().setBackground(new Color(24,24,24));
        tableScroll.getHorizontalScrollBar().setBackground(new Color(24,24,24));

        datesScroll = new JScrollPane(datesTable);
        datesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        datesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        datesScroll.getViewport().setBackground(new Color(24,24,24));
        datesScroll.getVerticalScrollBar().setBackground(new Color(24,24,24));
        datesScroll.getHorizontalScrollBar().setBackground(new Color(24,24,24));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerPanel,datesScroll);

        TableCustom.apply(tableScroll, TableCustom.TableType.DEFAULT);
        TableCustom.apply(datesScroll, TableCustom.TableType.VERTICAL);
    }

    public void syncScrollPanes() {
        JScrollBar vScroll1 = tableScroll.getVerticalScrollBar();
        JScrollBar vScroll2 = datesScroll.getVerticalScrollBar();

        vScroll1.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                vScroll2.setValue(vScroll1.getValue());
            }
        });
        vScroll2.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                vScroll1.setValue(vScroll2.getValue());
            }
        });
    }

    public void loadTable(ResultSet rs) throws SQLException {
        ResultSetMetaData rsMeta = rs.getMetaData();
        DefaultTableModel tableModel = new DefaultTableModel();
        DefaultTableModel dateTableModel = new DefaultTableModel();
        int colCount = rsMeta.getColumnCount();

        for (int i = 1; i <= colCount; i++) {
            tableModel.addColumn(rsMeta.getColumnLabel(i));
        }

        Object[] row = new Object[colCount];
        Object[] emptyRow = new Object[colCount];
        while (rs.next()) {
            for (int i = 0; i < colCount; i++) {
                row[i] = rs.getObject(i + 1);
            }
            tableModel.addRow(row);
            dateTableModel.addRow(emptyRow);
        }
        table.setModel(tableModel);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("E-dd-MMM");
        LocalDate today = LocalDate.now();
        LocalDate ninetyDaysAgo = today.minusDays(90);
        LocalDate oneYearFromNow = today.plusDays(365);
        LocalDate currentDate = ninetyDaysAgo;
        ArrayList<LocalDate> saturdayList = new ArrayList<>();
        for(int i = 0; i < table.getRowCount(); i++){
            java.sql.Date sqlDate = (java.sql.Date) table.getValueAt(i,6);
            LocalDate date = sqlDate.toLocalDate();
            if(date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                saturdayList.add(date);
            }
        }
        while(!currentDate.isAfter(oneYearFromNow)){
            if(currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                if(currentDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    for(LocalDate d : saturdayList){
                        if(d.equals(currentDate)) {
                            dateTableModel.addColumn(currentDate.format(dateFormat));
                        }
                    }
                }
                else
                    dateTableModel.addColumn(currentDate.format(dateFormat));
                if (currentDate.equals(today))
                    todayCol = dateTableModel.getColumnCount() - 1;
            }
            currentDate = currentDate.plusDays(1);
        }
        datesTable.setModel(dateTableModel);

        table.getTableHeader().setFont(new Font(table.getTableHeader().getFont().getFontName(), Font.BOLD, table.getTableHeader().getFont().getSize()));
        datesTable.getTableHeader().setFont(new Font(table.getTableHeader().getFont().getFontName(), Font.BOLD, table.getTableHeader().getFont().getSize()));

        totalWidth = 0;
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
            totalWidth += maxWidth+25;
        }

        datesTable.getTableHeader().setDefaultRenderer(new RotatedHeaderRenderer(datesTable));
        for (int i = 0; i < datesTable.getColumnModel().getColumnCount(); i++) {
            datesTable.getColumnModel().getColumn(i).setMinWidth(30);
            datesTable.getColumnModel().getColumn(i).setMaxWidth(30);
            datesTable.getColumnModel().getColumn(i).setPreferredWidth(30);
        }

        if(todayCol != 1) {
            Rectangle cellRect = datesTable.getCellRect(0, todayCol, true);
            JScrollBar hScrollBar = datesScroll.getHorizontalScrollBar();
            hScrollBar.setValue(cellRect.x);
            datesTable.scrollRectToVisible(cellRect);
        }

        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getPreferredSize().width, 100));
        datesTable.getTableHeader().setPreferredSize(new Dimension(datesTable.getTableHeader().getPreferredSize().width, 100));

        database.closeResources();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == addJobButton){
            new InsertWindow();
        }
        if(e.getSource() == updateJobButton){
            int selectedRow = table.getSelectedRow();
            if(selectedRow != -1) {
                String selectedJwo = table.getValueAt(selectedRow, 0).toString();
                new UpdateWindow(selectedJwo);
            }
        }
        if(e.getSource() == deleteButton){
            SelectQueryBuilder qb = new SelectQueryBuilder();
            qb.select("*");
            qb.from("job_board");
            try {
                loadTable(database.sendSelect(qb.build()));
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        if(e.getSource() == jwoFilterButton){
            SelectQueryBuilder qb = new SelectQueryBuilder();
            qb.select("*");
            qb.from("job_board");
            Filter f = new Filter("jwo", DESC);
            qb.orderBy(f);
            try {
                loadTable(database.sendSelect(qb.build()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if(e.getSource() == customerFilterButton){
            SelectQueryBuilder qb = new SelectQueryBuilder();
            qb.select("*");
            qb.from("job_board");
            Filter f = new Filter("customer", ASC);
            qb.orderBy(f);
            try {
                loadTable(database.sendSelect(qb.build()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if(e.getSource() == dateFilterButton){
            SelectQueryBuilder qb = new SelectQueryBuilder();
            qb.select("*");
            qb.from("job_board");
            Filter f = new Filter("due_date", DESC);
            qb.orderBy(f);
            try {
                loadTable(database.sendSelect(qb.build()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if(e.getSource() == todayButton) {
            if(todayCol != 1) {
                Rectangle cellRect = datesTable.getCellRect(0, todayCol, true);
                JScrollBar hScrollBar = datesScroll.getHorizontalScrollBar();
                hScrollBar.setValue(cellRect.x);
                datesTable.scrollRectToVisible(cellRect);
            }
        }
        if(e.getSource() == resetViewButton) {
            splitPane.setDividerLocation(centerPanel.getPreferredSize().width);
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
