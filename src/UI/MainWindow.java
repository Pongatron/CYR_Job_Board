package UI;

import DatabaseInteraction.*;
import Table.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static DatabaseInteraction.Filter.FilterStatus.*;

public class MainWindow extends JFrame implements ActionListener, MouseListener {

    private static  int ABSOLUTE_MIN_CELL_WIDTH = 50;
    private static  int MAX_CELL_WIDTH = 200;
    private static  int BASE_FONT_SIZE = 15;
    private static  Dimension TOP_PANEL_PREF_SIZE = new Dimension(0, 100);
    private static  Dimension BUTTON_PANEL_PREF_SIZE = new Dimension(400, 100);
    private static  Dimension TABLE_SCROLL_PREF_SIZE = new Dimension(500,0);
    private static  Font PLAIN_FONT = new Font("SansSerif", Font.PLAIN, BASE_FONT_SIZE);
    private static  Font BOLD_FONT = new Font("SansSerif", Font.BOLD, BASE_FONT_SIZE);
    private static  Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 15);

    private final DatabaseInteraction database;
    private ResultSet resultSet;
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
    private JButton archiveButton;
    private JButton plusZoomButton;
    private JButton minusZoomButton;
    private JScrollPane tableScroll;
    private JScrollPane datesScroll;
    private JTable dataTable;
    private JTable datesTable;
    private JSplitPane splitPane;
    private JToolBar mainBar;
    public static int totalWidth;
    private int todayCol = -1;

    DefaultTableModel dataTableModel;
    DefaultTableModel datesTableModel;

    public MainWindow() throws Exception{

        database = new DatabaseInteraction();
        updateDimensions();
        initializeComponents();
        syncScrollPanes();
        refreshTable();

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
        topContainerPanel.add(archiveButton);
        topContainerPanel.add(todayButton);
        topContainerPanel.add(minusZoomButton);
        topContainerPanel.add(plusZoomButton);


        mainBar.add(topContainerPanel);

        centerPanel.setPreferredSize(new Dimension(totalWidth, 200));
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        setDividerLocation();

        this.add(mainBar, BorderLayout.NORTH);
        this.add(splitPane, BorderLayout.CENTER);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                PropertiesManager.saveUserPreferences();
                super.windowClosing(e);
            }
        });

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Job Board");
        this.setPreferredSize(new Dimension(1440, 900));
        this.setMinimumSize(new Dimension(800, 100));
        this.setBackground(new Color(24,24,24));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void initializeComponents(){
        dataTable = new JTable();
        dataTable.getTableHeader().addMouseListener(this);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        dataTable.setDefaultEditor(Object.class, null);
        dataTable.getTableHeader().setReorderingAllowed(false);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataTable.setBackground(new Color(24,24,24));
        dataTable.setFont(new Font(dataTable.getFont().getFontName(), Font.PLAIN, 30));

        datesTable = new JTable();
        datesTable.getTableHeader().addMouseListener(this);
        datesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        datesTable.setDefaultEditor(Object.class, null);
        datesTable.getTableHeader().setReorderingAllowed(false);
        datesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        datesTable.setRowSelectionAllowed(false);
        datesTable.setColumnSelectionAllowed(false);
        datesTable.setCellSelectionEnabled(false);
        datesTable.setBackground(new Color(24,24,24));

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBackground(new Color(24,24,24));
        topPanel.setPreferredSize(TOP_PANEL_PREF_SIZE);
        topPanel.setBorder(new EmptyBorder(20,20,20,20));

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBackground(new Color(24,24,24));

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2,3, 10,10));
        buttonPanel.setPreferredSize(BUTTON_PANEL_PREF_SIZE);
        buttonPanel.setBorder(new EmptyBorder(20,20,20,20));
        buttonPanel.setBackground(new Color(24,24,24));

        addJobButton = new JButton("Add Job");
        addJobButton.setBackground(new Color(220, 46, 35));
        addJobButton.setForeground(new Color(0,0,0));
        addJobButton.setFont(BUTTON_FONT);
        addJobButton.setFocusable(false);
        addJobButton.addActionListener(this);

        updateJobButton = new JButton("Update");
        updateJobButton.setBackground(new Color(0, 52, 191));
        updateJobButton.setForeground(new Color(255,255,255));
        updateJobButton.setFont(BUTTON_FONT);
        updateJobButton.setFocusable(false);
        updateJobButton.addActionListener(this);

        deleteButton = new JButton("Delete");
        deleteButton.setBackground(new Color(240, 232, 5));
        deleteButton.setForeground(new Color(0,0,0));
        deleteButton.setFont(BUTTON_FONT);
        deleteButton.setFocusable(false);
        deleteButton.addActionListener(this);

        jwoFilterButton = new JButton("JWO");
        jwoFilterButton.setBackground(new Color(44, 123, 201));
        jwoFilterButton.setForeground(new Color(255,255,255));
        jwoFilterButton.setFont(BUTTON_FONT);
        jwoFilterButton.setFocusable(false);
        jwoFilterButton.addActionListener(this);

        customerFilterButton = new JButton("Customer");
        customerFilterButton.setBackground(new Color(44, 123, 201));
        customerFilterButton.setForeground(new Color(255,255,255));
        customerFilterButton.setFont(BUTTON_FONT);
        customerFilterButton.setFocusable(false);
        customerFilterButton.addActionListener(this);

        dateFilterButton = new JButton("Due Date");
        dateFilterButton.setBackground(new Color(44, 123, 201));
        dateFilterButton.setForeground(new Color(255,255,255));
        dateFilterButton.setFont(BUTTON_FONT);
        dateFilterButton.setFocusable(false);
        dateFilterButton.addActionListener(this);

        todayButton = new JButton("Today");
        todayButton.setBackground(new Color(0, 0, 0));
        todayButton.setForeground(new Color(255,255,255));
        todayButton.setFont(BUTTON_FONT);
        todayButton.setFocusable(false);
        todayButton.addActionListener(this);

        resetViewButton = new JButton("Reset View");
        resetViewButton.setBackground(new Color(0, 0, 0));
        resetViewButton.setForeground(new Color(255,255,255));
        resetViewButton.setFont(BUTTON_FONT);
        resetViewButton.setFocusable(false);
        resetViewButton.addActionListener(this);

        archiveButton = new JButton("Archive");
        archiveButton.setBackground(new Color(0, 0, 0));
        archiveButton.setForeground(new Color(255,255,255));
        archiveButton.setFont(BUTTON_FONT);
        archiveButton.setFocusable(false);
        archiveButton.addActionListener(this);

        plusZoomButton = new JButton("+");
        plusZoomButton.setBackground(new Color(0, 0, 0));
        plusZoomButton.setForeground(new Color(255,255,255));
        plusZoomButton.setFont(BUTTON_FONT);
        plusZoomButton.setFocusable(false);
        plusZoomButton.addActionListener(this);

        minusZoomButton = new JButton("-");
        minusZoomButton.setBackground(new Color(0, 0, 0));
        minusZoomButton.setForeground(new Color(255,255,255));
        minusZoomButton.setFont(BUTTON_FONT);
        minusZoomButton.setFocusable(false);
        minusZoomButton.addActionListener(this);

        mainBar = new JToolBar();
        mainBar.setFloatable(false);
        mainBar.setPreferredSize(new Dimension(0, 100));
        mainBar.setBackground(new Color(24,24,24));

        tableScroll = new JScrollPane(dataTable);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setPreferredSize(TABLE_SCROLL_PREF_SIZE);
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
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        TableCustom.apply(tableScroll, TableCustom.TableType.DEFAULT);
        TableCustom.apply(datesScroll, TableCustom.TableType.VERTICAL);
    }

    public void loadTable()  throws SQLException{
        dataTableModel = new DefaultTableModel();
        datesTableModel = new DefaultTableModel();
        createDataTable();
        createDatesTable();
        setTableFontsAndSizes();
        populateDatesTable();
        resetDatesScrollBar();

        database.closeResources();
    }

    public void createDataTable() throws SQLException{
        //---------- create datatable
        ResultSetMetaData rsMeta = resultSet.getMetaData();
        int colCount = rsMeta.getColumnCount();

        ArrayList<Integer> visibleIndexes = new ArrayList<>();

        for (int i = 1; i <= colCount; i++) {
            String columnName = rsMeta.getColumnLabel(i);
            if(PropertiesManager.getKeyValue(columnName).equals("t")) {
                System.out.println(columnName+": "+i);
                visibleIndexes.add(i);
                dataTableModel.addColumn(columnName.replace("_", " "));
            }
        }


        while (resultSet.next()) {
            Object[] row = new Object[visibleIndexes.size()];
            Object[] emptyRow = new Object[visibleIndexes.size()];

            for (int i = 0; i < visibleIndexes.size(); i++) {
                int databaseIndex = visibleIndexes.get(i);
                Object item = resultSet.getObject(databaseIndex);
                if (item instanceof java.sql.Date) {
                    LocalDate cellDate = ((Date) item).toLocalDate();
                    row[i] = cellDate;
                }
                else{
                    row[i] = item;
                }
            }
            dataTableModel.addRow(row);
            datesTableModel.addRow(emptyRow);
        }
        resultSet.beforeFirst();

        dataTable.setModel(dataTableModel);
        //-------------------
    }

    public void createDatesTable() throws SQLException {
        //--------------------------- create dates table
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("E- dd- MMM");
        LocalDate today = LocalDate.now();
        LocalDate ninetyDaysAgo = today.minusDays(90);
        LocalDate oneYearFromNow = today.plusDays(365);
        LocalDate currentDate = ninetyDaysAgo;
        ArrayList<LocalDate> saturdayList = new ArrayList<>();

        while(resultSet.next()){
            Date sqlDate = resultSet.getDate("due_date");
            LocalDate date = sqlDate.toLocalDate();
            if(date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                if(!saturdayList.contains(date)) {
                    saturdayList.add(date);
                }
            }
        }
        resultSet.beforeFirst();

        while(!currentDate.isAfter(oneYearFromNow)){
            if(currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                if(currentDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    for(LocalDate d : saturdayList){
                        if(d.equals(currentDate)) {
                            datesTableModel.addColumn(currentDate.format(dateFormat));
                        }
                    }
                }
                else
                    datesTableModel.addColumn(currentDate.format(dateFormat));
            }
            if(currentDate.isEqual(today)){
                datesTableModel.addColumn(currentDate.format(dateFormat));
                todayCol = datesTableModel.getColumnCount() - 1;
            }
            System.out.println(currentDate);
            System.out.println(today);

            currentDate = currentDate.plusDays(1);
        }
        datesTable.setModel(datesTableModel);
        //------------------------------
    }

    public void setTableFontsAndSizes(){
        // set both tables' fonts
        dataTable.getTableHeader().setFont(BOLD_FONT);
        datesTable.getTableHeader().setFont(BOLD_FONT);
        dataTable.setFont(PLAIN_FONT);
        datesTable.setFont(PLAIN_FONT);

        // calculate each column width and set it as the preferred size so nothing looks cut off
        totalWidth = 0;
        int minHeight = 0;
        for(int col = 0; col < dataTable.getColumnCount(); col++){
            TableColumn column = dataTable.getColumnModel().getColumn(col);
            TableCellRenderer headerRenderer = dataTable.getTableHeader().getDefaultRenderer();
            JLabel headerComp = (JLabel)headerRenderer.getTableCellRendererComponent(dataTable, column.getHeaderValue(), false, false, -1, col);

            int minWidth = 0;

            for(int i = 0; i < dataTable.getRowCount(); i++){
                TableCellRenderer cellRenderer = dataTable.getCellRenderer(i, col);
                Component comp = dataTable.prepareRenderer(cellRenderer, i, col);
                int cellWidth = comp.getPreferredSize().width;
                if(cellWidth < MAX_CELL_WIDTH)
                    minWidth = Math.max(minWidth, cellWidth + (int)(8 * ZoomManager.getZoom()));
            }
            if(headerComp.getPreferredSize().width > minWidth && !headerComp.getText().contains("html")){
                column.setHeaderRenderer(new RotatedHeaderRenderer(dataTable));
                minWidth = Math.max(minWidth, headerComp.getPreferredSize().height + (int)(8 * ZoomManager.getZoom()));
            }
            else{
                column.setHeaderRenderer(new TableHeaderCustomCellRenderer(dataTable));
                minWidth = Math.max(minWidth, headerComp.getPreferredSize().width + (int)(8 * ZoomManager.getZoom()));
            }

            column.setPreferredWidth(minWidth);
            column.setMaxWidth(MAX_CELL_WIDTH);
            column.setMinWidth(minWidth);
            totalWidth += minWidth;

            int minHeadHeight = headerComp.getPreferredSize().height;
            minHeight = Math.max(minHeight, minHeadHeight);

        }
        datesTable.getTableHeader().setDefaultRenderer(new RotatedHeaderRenderer(datesTable));

        // set datesTable cell sizes
        for (int i = 0; i < datesTable.getColumnModel().getColumnCount(); i++) {
            datesTable.getColumnModel().getColumn(i).setMinWidth((int)(30 * ZoomManager.getZoom()));
            datesTable.getColumnModel().getColumn(i).setMaxWidth((int)(30 * ZoomManager.getZoom()));
            datesTable.getColumnModel().getColumn(i).setPreferredWidth((int)(30 * ZoomManager.getZoom()));
            TableColumn column = datesTable.getColumnModel().getColumn(i);
            TableCellRenderer headerRenderer = datesTable.getTableHeader().getDefaultRenderer();
            Component headerComp = headerRenderer.getTableCellRendererComponent(datesTable, column.getHeaderValue(), false, false, -1, i);
            minHeight = Math.max(minHeight, headerComp.getPreferredSize().width);
        }




        // set both tables preferred sizes

        dataTable.getTableHeader().setPreferredSize(new Dimension(dataTable.getTableHeader().getPreferredSize().width, minHeight));
        datesTable.getTableHeader().setPreferredSize(new Dimension(datesTable.getTableHeader().getPreferredSize().width, minHeight));

        dataTable.setRowHeight((int)(30 * ZoomManager.getZoom()));
        datesTable.setRowHeight((int)(30 * ZoomManager.getZoom()));
    }

    // Populate dates table with colored days
    public void populateDatesTable() throws SQLException {
        // TODO find a way to get column index without exact number


        ArrayList<DateRange> dates = new ArrayList<>();
        int buildIndex = 1;
        int finishIndex = 1;
        int installIndex = 1;
        int dueDateColumn = 1;
        while(resultSet.next()){
            buildIndex = resultSet.findColumn("build");
            finishIndex = resultSet.findColumn("finish");
            installIndex = resultSet.findColumn("install");
            dueDateColumn = resultSet.findColumn("due_date");

            Date sqlDate = resultSet.getDate("due_date");
            LocalDate dueDate = sqlDate.toLocalDate();

            // get amount of days for each section of time from data table
            int buildDays = (int) resultSet.getInt(buildIndex);
            int finishDays = (int) resultSet.getInt(finishIndex);
            int installDays = (int) resultSet.getInt(installIndex);
            int daysBack = buildDays + finishDays;
            int daysForward = installDays - 1;

            // check if the due date is a saturday
            boolean isDueDateSaturday = false;
            if (dueDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                isDueDateSaturday = true;
            }

            // calculate what the start/end date for all sections of time
            // this calculation will only count weekdays as working days unless the due date is a saturday
            LocalDate buildStart = dueDate;
            LocalDate finishStart = dueDate;
            LocalDate installEnd = dueDate;
            // calculate buildStart
            for (int j = 0; j >= -(daysBack); ) {
                buildStart = buildStart.minusDays(1);
                if (!buildStart.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !buildStart.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                    j--;
                }
            }
            // calculate finishStart
            for (int j = 0; j >= -(daysBack - buildDays); ) {
                finishStart = finishStart.minusDays(1);
                if (!finishStart.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !finishStart.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                    j--;
                }
            }
            // calculate installEnd (since install start is due date)
            // for now this is unnecessary, but it could be useful in future
            for (int j = 0; j < daysForward; ) {
                installEnd = installEnd.plusDays(1);
                if (!installEnd.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !installEnd.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                    j++;
                } else if (installEnd.getDayOfWeek().equals(DayOfWeek.SATURDAY) && dueDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                    j++;
                }
            }

            // Starting from each time period, add all the weekdays to a respective list based on when each period starts
            // Only weekdays will be added based on the amount of days in each time period
            // Add a saturday to that list only if the saturday is a due date
            ArrayList<LocalDate> buildDates = new ArrayList<>();
            ArrayList<LocalDate> finishDates = new ArrayList<>();
            ArrayList<LocalDate> installDates = new ArrayList<>();
            LocalDate tempDatePopulator = buildStart;
            // Populate the buildDates
            for (int j = 0; j < (daysBack - finishDays); ) {
                tempDatePopulator = tempDatePopulator.plusDays(1);
                if (!tempDatePopulator.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !tempDatePopulator.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                    j++;
                    buildDates.add(tempDatePopulator);
                }
            }
            // Populate the finishDates
            tempDatePopulator = finishStart;
            for (int j = 0; j < (finishDays); ) {
                tempDatePopulator = tempDatePopulator.plusDays(1);
                if (!tempDatePopulator.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !tempDatePopulator.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                    j++;
                    finishDates.add(tempDatePopulator);
                }
            }
            // Populate the installDates
            // the due date is the first install day and if the due date is a saturday it will be added to the list
            tempDatePopulator = dueDate;
            for (int j = 0; j < (installDays); ) {
                if (!tempDatePopulator.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !tempDatePopulator.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                    j++;
                    installDates.add(tempDatePopulator);
                } else if (tempDatePopulator.getDayOfWeek().equals(DayOfWeek.SATURDAY) && dueDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                    j++;
                    installDates.add(tempDatePopulator);
                }
                tempDatePopulator = tempDatePopulator.plusDays(1);
            }
            dates.add(new DateRange(dueDate, buildDates, finishDates, installDates, isDueDateSaturday));
        }
        resultSet.beforeFirst();

        // Apply the dates for each time period to the custom renderer so it can draw the colored squares
        TableCustom.applyDates(datesTable, dates);
    }

    // syncs both scroll bars in dataTable and datesTable to move together
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

    // set dates scroll bar to today
    public void resetDatesScrollBar() {
        if (todayCol != -1) {
            Rectangle cellRect = datesTable.getCellRect(0, todayCol, true);
            JScrollBar hScrollBar = datesScroll.getHorizontalScrollBar();
            hScrollBar.setValue(cellRect.x);
            datesTable.scrollRectToVisible(cellRect);
        }
    }

    public void updateDimensions(){
        ABSOLUTE_MIN_CELL_WIDTH = (int)(50 * ZoomManager.getZoom());
        MAX_CELL_WIDTH = (int)(125 * ZoomManager.getZoom());
        BASE_FONT_SIZE = (int)(15 * ZoomManager.getZoom());
        TOP_PANEL_PREF_SIZE = new Dimension(0, 100);
        BUTTON_PANEL_PREF_SIZE = new Dimension(400, 100);
        TABLE_SCROLL_PREF_SIZE = new Dimension(500,0);
        PLAIN_FONT = new Font("SansSerif", Font.PLAIN, BASE_FONT_SIZE);
        BOLD_FONT = new Font("SansSerif", Font.BOLD, BASE_FONT_SIZE);
    }

    public void refreshTable() {
        SelectQueryBuilder qb = new SelectQueryBuilder();
        qb.select("*");
        qb.from("job_board");
        qb.where("is_active = true");
        try {
            resultSet = database.sendSelect(qb.build());
            loadTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createDropdown(String columnName){

    }

    public void setDividerLocation(){
        splitPane.setDividerLocation(totalWidth);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == addJobButton){
            new AddJobWindow();
        }
        if(e.getSource() == updateJobButton){
            int selectedRow = dataTable.getSelectedRow();
            if(selectedRow != -1) {
                String selectedJwo = dataTable.getValueAt(selectedRow, 0).toString();
                new UpdateJobWindow(selectedJwo);
            }
        }
        if(e.getSource() == deleteButton){
            int selectedRow = dataTable.getSelectedRow();
            if(selectedRow != -1) {
                String selectedJwo = dataTable.getValueAt(selectedRow, 0).toString();
                new DeleteJobWindow(selectedJwo);
            }
        }
        if(e.getSource() == jwoFilterButton){
            SelectQueryBuilder qb = new SelectQueryBuilder();
            qb.select("*");
            qb.from("job_board");
            Filter f = new Filter("jwo", DESC);
            qb.orderBy(f);
            try {
                resultSet = database.sendSelect(qb.build());
                loadTable();
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
                resultSet = database.sendSelect(qb.build());
                loadTable();
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
                resultSet = database.sendSelect(qb.build());
                loadTable();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if(e.getSource() == todayButton) {
            resetDatesScrollBar();
        }
        if(e.getSource() == resetViewButton) {
            setDividerLocation();
            dataTable.setSelectionModel(new DefaultListSelectionModel());
            refreshTable();
        }
        if(e.getSource() == archiveButton) {
            setDividerLocation();
            SelectQueryBuilder qb = new SelectQueryBuilder();
            qb.select("*");
            qb.from("job_board");
            qb.where("is_active = false");
            try {
                resultSet = database.sendSelect(qb.build());
                loadTable();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            dataTable.setSelectionModel(new DefaultListSelectionModel() {
                @Override
                public void setSelectionInterval(int index0, int index1) {
                    // Do nothing, effectively preventing selection
                }

                @Override
                public void addSelectionInterval(int index0, int index1) {
                    // Do nothing
                }

                @Override
                public void removeSelectionInterval(int index0, int index1) {
                    // Do nothing
                }
            });
        }
        if(e.getSource() == plusZoomButton) {
            ZoomManager.increaseZoom();
            updateDimensions();
            //refreshTable();
            setTableFontsAndSizes();
            setDividerLocation();

            SwingUtilities.invokeLater(()->{
                setTableFontsAndSizes();
                resetDatesScrollBar();
            });


            //JOptionPane.showMessageDialog(this, "current zoom: "+ZoomManager.getZoom(), "", JOptionPane.INFORMATION_MESSAGE);
        }
        if(e.getSource() == minusZoomButton) {
            ZoomManager.decreaseZoom();
            updateDimensions();
            //refreshTable();
            setTableFontsAndSizes();
            setDividerLocation();

            SwingUtilities.invokeLater(()->{
                setTableFontsAndSizes();
                resetDatesScrollBar();
            });
            //JOptionPane.showMessageDialog(this, "current zoom: "+ZoomManager.getZoom(), "", JOptionPane.INFORMATION_MESSAGE);
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
