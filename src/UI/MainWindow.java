package UI;

import DatabaseInteraction.*;
import Table.*;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Properties;

import static DatabaseInteraction.DatabaseInteraction.*;
import static DatabaseInteraction.Filter.FilterStatus.*;

public class MainWindow extends JFrame implements ActionListener {

    public enum JobBoardMode {ACTIVE_JOBS, ARCHIVE}


    private static  int ABSOLUTE_MIN_CELL_WIDTH = 50;
    private static  int MAX_CELL_WIDTH = 400;
    private static  int BASE_FONT_SIZE = 15;
    private static  Dimension TOP_PANEL_PREF_SIZE = new Dimension(0, 100);
    private static  Dimension BUTTON_PANEL_PREF_SIZE = new Dimension(400, 50);
    private static  Dimension TABLE_SCROLL_PREF_SIZE = new Dimension(500,0);
    private static  Font PLAIN_FONT = new Font("SansSerif", Font.PLAIN, BASE_FONT_SIZE);
    private static  Font BOLD_FONT = new Font("SansSerif", Font.BOLD, BASE_FONT_SIZE);
    private static  Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 15);

    float currentZoom = ZoomManager.getZoom();
    int zoomedDateCellWidth = (int)(30 * currentZoom);
    int zoomedCellBuffer = (int)(8 * currentZoom);

    public JobBoardMode currentBoardMode = JobBoardMode.ACTIVE_JOBS;
    private final DatabaseInteraction database;
    private ResultSet jobBoardResultSet;
    private ResultSet customerListResultSet;
    private JPanel leftPanel;
    private JPanel topContainerPanel;
    private JPanel buttonPanel;
    private JPanel topTablePanel;
    private JPanel centerPanel;
    private JButton addJobButton;
    private JButton updateJobButton;
    private JButton deleteButton;
    private JButton jwoFilterButton;
    private JButton customerFilterButton;
    private JButton dateFilterButton;
    private JButton todayButton;
    private JButton resetViewButton;
    private JButton timeOffButton;
    private JButton archiveButton;
    private JButton plusZoomButton;
    private JButton minusZoomButton;
    private JScrollPane tableScroll;
    private JScrollPane datesScroll;
    private JScrollPane timeOffScroll;
    private JTable dataTable;
    private JTable datesTable;
    private JTable timeOffTable;
    private JSplitPane splitPane;
    public static int totalWidth;
    private int todayCol = -1;
    private boolean isArchiveHidden = false;

    ArrayList<Integer> visibleIndexes = new ArrayList<>();

    DefaultTableModel dataTableModel;
    DefaultTableModel datesTableModel;

    public MainWindow() throws Exception{
        database = new DatabaseInteraction();
        refreshResultSets("due_date");
        initializeComponents();
        syncScrollPanes();
        loadTable();
        resetDatesScrollBar();




        buttonPanel.add(addJobButton);
        buttonPanel.add(updateJobButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(jwoFilterButton);
        buttonPanel.add(customerFilterButton);
        buttonPanel.add(dateFilterButton);

        topContainerPanel.add(buttonPanel);
        topContainerPanel.add(resetViewButton);
        topContainerPanel.add(archiveButton);
        topContainerPanel.add(timeOffButton);
        topContainerPanel.add(todayButton);
        topContainerPanel.add(minusZoomButton);
        topContainerPanel.add(plusZoomButton);

        leftPanel.setPreferredSize(new Dimension(totalWidth, 100));
        leftPanel.add(tableScroll, BorderLayout.CENTER);
        setDividerLocation();

        topTablePanel.add(timeOffScroll, BorderLayout.EAST);
        centerPanel.add(topTablePanel, BorderLayout.NORTH);
        centerPanel.add(splitPane, BorderLayout.CENTER);

        this.add(centerPanel, BorderLayout.CENTER);
        this.add(topContainerPanel, BorderLayout.NORTH);

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

    private static String channel = "table_update";
    private static boolean running = true;

    public static void listenToDatabase(){
        Thread listenerThread = new Thread(() -> {
            Connection conn = null; // Declare outside try-with-resources to use in finally
            try {
                conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                PGConnection pgconn = conn.unwrap(PGConnection.class);

                if (pgconn == null) {
                    System.err.println("Not a PostgreSQL connection or cannot unwrap to PGConnection.");
                    return;
                }

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("LISTEN " + channel);
                    System.out.println("Listening for notifications on channel: " + channel);
                }

                while (running) {
                    // Option 1: Using the connection's wait method (more efficient)
                    // This will block until a notification arrives or the timeout occurs
                    // You need to synchronize on the connection object itself
                    synchronized (conn) {
                        conn.wait(5000); // Wait for up to 5 seconds for a notification
                        // If 0, it waits indefinitely until notified
                    }

                    // Option 2: Polling with a sleep (less efficient but often simpler to get working initially)
                    // You only need this if conn.wait() isn't working or you prefer it.
                    // If no notifications, you need to sleep to prevent busy-waiting.
                    // This line would go outside the `if (notifications != null)` block if you use it.
                    // Thread.sleep(500); // Sleep for 500ms if no notifications received

                    PGNotification[] notifications = pgconn.getNotifications();

                    if (notifications != null && notifications.length > 0) {
//                        for (PGNotification notification : notifications) {
//                            System.out.println("NOTIFICATION: Channel=" + notification.getName() +
//                                    ", Payload=" + notification.getParameter() +
//                                    ", PID=" + notification.getPID());
//
//                            // Execute UI update on the Event Dispatch Thread (EDT)
//
//                        }
                        SwingUtilities.invokeLater(()->{});
                    }
                    // If using Thread.sleep(), it would go here for Option 2.
                }
            } catch (SQLException e) {
                System.err.println("Database connection error or LISTEN/NOTIFY issue: " + e.getMessage());
                e.printStackTrace();
                // IMPORTANT: Implement reconnect/retry logic here for robustness
                try { Thread.sleep(5000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Listener thread interrupted.");
            } finally {
                // Ensure the connection is closed even if an exception occurs
                if (conn != null) {
                    try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
                }
                System.out.println("PostgreSQL Listener stopped.");
            }
        }, "Postgres-Listener-Thread"); // Give your thread a name for easier debugging
        listenerThread.setDaemon(true); // Make it a daemon thread so it doesn't prevent JVM exit
        listenerThread.start();
    }

    public void initializeComponents(){
        dataTable = new JTable();
        //dataTable.getTableHeader().addMouseListener(this);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        dataTable.setDefaultEditor(Object.class, null);
        dataTable.getTableHeader().setReorderingAllowed(false);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataTable.setBackground(new Color(24,24,24));
        dataTable.setFont(new Font(dataTable.getFont().getFontName(), Font.PLAIN, 30));

        datesTable = new JTable();
        //datesTable.getTableHeader().addMouseListener(this);
        datesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        datesTable.setDefaultEditor(Object.class, null);
        datesTable.getTableHeader().setReorderingAllowed(false);
        datesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        datesTable.setRowSelectionAllowed(false);
        datesTable.setColumnSelectionAllowed(false);
        datesTable.setCellSelectionEnabled(false);
        datesTable.setBackground(new Color(24,24,24));

        timeOffTable = new JTable();
        //timeOffTable.getTableHeader().addMouseListener(this);
        timeOffTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        timeOffTable.setDefaultEditor(Object.class, null);
        timeOffTable.getTableHeader().setReorderingAllowed(false);
        timeOffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timeOffTable.setRowSelectionAllowed(false);
        timeOffTable.setColumnSelectionAllowed(false);
        timeOffTable.setCellSelectionEnabled(false);
        timeOffTable.setBackground(new Color(24,24,24));

        topContainerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topContainerPanel.setBackground(new Color(24,24,24));

        leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBackground(new Color(24,24,24));

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBackground(new Color(24,24,24));

        topTablePanel = new JPanel();
        topTablePanel.setLayout(new BorderLayout());
        topTablePanel.setBackground(new Color(24,24,24));

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2,3, 10,10));
        buttonPanel.setPreferredSize(BUTTON_PANEL_PREF_SIZE);
        buttonPanel.setBorder(new EmptyBorder(0,20,0,20));
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

        timeOffButton = new JButton("Time Off");
        timeOffButton.setBackground(new Color(0, 0, 0));
        timeOffButton.setForeground(new Color(255,255,255));
        timeOffButton.setFont(BUTTON_FONT);
        timeOffButton.setFocusable(false);
        timeOffButton.addActionListener(this);

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

        timeOffScroll = new JScrollPane(timeOffTable);
        timeOffScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        timeOffScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        timeOffScroll.getViewport().setBackground(new Color(24,24,24));
        timeOffScroll.getVerticalScrollBar().setBackground(new Color(24,24,24));
        timeOffScroll.getHorizontalScrollBar().setBackground(new Color(24,24,24));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, datesScroll);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        TableCustom.apply(tableScroll, TableCustom.TableType.DEFAULT);
        TableCustom.apply(datesScroll, TableCustom.TableType.VERTICAL);
        TableCustom.apply(timeOffScroll, TableCustom.TableType.TIMEOFF);

        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
                    SwingUtilities.invokeLater(()->{
                        timeOffScroll.setPreferredSize(new Dimension(datesScroll.getWidth(), timeOffTable.getPreferredSize().height));
                        timeOffScroll.revalidate();
                        timeOffScroll.repaint();
                        topTablePanel.revalidate();
                        topTablePanel.repaint();
                    });

                }
            }
        });
    }

    public void loadTable()  throws SQLException{
        dataTableModel = new DefaultTableModel();
        datesTableModel = new DefaultTableModel();
        createDataTable();
        createDatesTable();
        populateDatesTable();
        populateTimeOffTable();
        setTableFontsAndSizes();
        setDividerLocation();
        applyZoom();
    }

    public void refreshData(String filter){
        refreshResultSets(filter);
        try {
            loadTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void refreshResultSets(String filter){
        String isActiveValue = "true";
        if(currentBoardMode == JobBoardMode.ARCHIVE){
            isActiveValue = "false";
        }

        SelectQueryBuilder qbJobBoard = new SelectQueryBuilder();
        qbJobBoard.select("*");
        qbJobBoard.from("job_board");
        qbJobBoard.where("is_active = "+isActiveValue);
        qbJobBoard.orderBy(new Filter(filter, ASC));
        jobBoardResultSet = database.sendSelect(qbJobBoard.build());

        SelectQueryBuilder qbCustomerList = new SelectQueryBuilder();
        qbCustomerList.select("*");
        qbCustomerList.from("customer_list");
        customerListResultSet = database.sendSelect(qbCustomerList.build());
    }

    public void createDataTable() throws SQLException{
        //---------- create datatable
        ResultSetMetaData rsMeta = jobBoardResultSet.getMetaData();
        int colCount = rsMeta.getColumnCount();

        visibleIndexes = new ArrayList<>();

        for (int i = 1; i <= colCount; i++) {
            String columnName = rsMeta.getColumnLabel(i);
            if("t".equals(PropertiesManager.getKeyValue(columnName, PropertiesManager.VISIBILITY_PROPERTIES_FILE_PATH))) {
                visibleIndexes.add(i);
                dataTableModel.addColumn(columnName.replace("_", " "));
            }
        }

        while (jobBoardResultSet.next()) {
            Object[] row = new Object[visibleIndexes.size()];
            Object[] emptyRow = new Object[visibleIndexes.size()];

            for (int i = 0; i < visibleIndexes.size(); i++) {
                int databaseIndex = visibleIndexes.get(i);
                Object item = jobBoardResultSet.getObject(databaseIndex);
                if (item instanceof Date) {
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
        jobBoardResultSet.beforeFirst();
        dataTable.setModel(dataTableModel);
        //-------------------
    }

    public void createDatesTable() throws SQLException {
        //--------------------------- create dates table
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("E- dd- MMM");
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusDays(365);
        LocalDate oneYearFromNow = today.plusDays(365);
        LocalDate currentDate = oneYearAgo;
        ArrayList<LocalDate> saturdayList = new ArrayList<>();

        DefaultTableModel timeOffTableModel = new DefaultTableModel();

        while(jobBoardResultSet.next()){
            Date sqlDate = jobBoardResultSet.getDate("due_date");
            LocalDate date = sqlDate.toLocalDate();
            if(date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                if(!saturdayList.contains(date)) {
                    saturdayList.add(date);
                }
            }
        }
        jobBoardResultSet.beforeFirst();

        while(!currentDate.isAfter(oneYearFromNow)){
            if(currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                if(currentDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    for(LocalDate d : saturdayList){
                        if(d.equals(currentDate)) {
                            datesTableModel.addColumn(currentDate.format(dateFormat));
                            timeOffTableModel.addColumn(currentDate.format(dateFormat));
                        }
                    }
                }
                else {
                    datesTableModel.addColumn(currentDate.format(dateFormat));
                    timeOffTableModel.addColumn(currentDate.format(dateFormat));
                }
                if(currentDate.isEqual(today)){
                    todayCol = datesTableModel.getColumnCount() - 1;
                }
            }
            else if(currentDate.isEqual(today)){
                datesTableModel.addColumn(currentDate.format(dateFormat));
                timeOffTableModel.addColumn(currentDate.format(dateFormat));
                todayCol = datesTableModel.getColumnCount() - 1;
            }

            currentDate = currentDate.plusDays(1);
        }
        datesTable.setModel(datesTableModel);
        timeOffTableModel.addRow(new Object[visibleIndexes.size()]);
        timeOffTable.setModel(timeOffTableModel);
        timeOffTable.setTableHeader(null);
        //------------------------------
    }

    public void setTableFontsAndSizes(){
        // set both tables' fonts
        dataTable.getTableHeader().setFont(BOLD_FONT);
        datesTable.getTableHeader().setFont(BOLD_FONT);
        dataTable.setFont(PLAIN_FONT);
        datesTable.setFont(PLAIN_FONT);
        timeOffTable.setFont(PLAIN_FONT);

        // calculate each column width and set it as the preferred size so nothing looks cut off
        totalWidth = 0;
        int minHeight = 0;
        int rowHeight = zoomedDateCellWidth;
        for(int col = 0; col < dataTable.getColumnCount(); col++){
            TableColumn column = dataTable.getColumnModel().getColumn(col);
            TableCellRenderer headerRenderer = dataTable.getTableHeader().getDefaultRenderer();
            JLabel headerComp = (JLabel)headerRenderer.getTableCellRendererComponent(dataTable, column.getHeaderValue(), false, false, -1, col);

            int minWidth = 0;

            for(int row = 0; row < dataTable.getRowCount(); row++){
                TableCellRenderer cellRenderer = dataTable.getCellRenderer(row, col);
                Component comp = dataTable.prepareRenderer(cellRenderer, row, col);

                FontMetrics fm = comp.getFontMetrics(PLAIN_FONT);
                String cellText = dataTable.getValueAt(row, col) != null ? dataTable.getValueAt(row, col).toString() : "";

                Border border = ((JComponent)comp).getBorder();
                Insets borderInsets = border.getBorderInsets(comp);

                int textWidth = fm.stringWidth(cellText) + borderInsets.left + borderInsets.right + zoomedCellBuffer;
                int cellWidth = textWidth;
                if(headerComp.getText().contains("date"))
                    cellWidth = comp.getPreferredSize().width;
                if(cellWidth < MAX_CELL_WIDTH)
                    minWidth = Math.max(minWidth, cellWidth + zoomedCellBuffer);
                else
                    minWidth = MAX_CELL_WIDTH;
            }
            if(headerComp.getPreferredSize().width > minWidth && !headerComp.getText().contains("html")){
                column.setHeaderRenderer(new RotatedHeaderRenderer(dataTable));
                minWidth = Math.max(minWidth, headerComp.getPreferredSize().height + zoomedCellBuffer);
            }
            else{
                column.setHeaderRenderer(new TableHeaderCustomCellRenderer(dataTable));
                minWidth = Math.max(minWidth, headerComp.getPreferredSize().width + zoomedCellBuffer);
            }

            minWidth += (int)(5*currentZoom);
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
            datesTable.getColumnModel().getColumn(i).setMinWidth(zoomedDateCellWidth);
            datesTable.getColumnModel().getColumn(i).setMaxWidth(zoomedDateCellWidth);
            datesTable.getColumnModel().getColumn(i).setPreferredWidth(zoomedDateCellWidth);
            timeOffTable.getColumnModel().getColumn(i).setMinWidth(zoomedDateCellWidth);
            timeOffTable.getColumnModel().getColumn(i).setMaxWidth(zoomedDateCellWidth);
            timeOffTable.getColumnModel().getColumn(i).setPreferredWidth(zoomedDateCellWidth);
            TableColumn column = datesTable.getColumnModel().getColumn(i);
            TableCellRenderer headerRenderer = datesTable.getTableHeader().getDefaultRenderer();
            Component headerComp = headerRenderer.getTableCellRendererComponent(datesTable, column.getHeaderValue(), false, false, -1, i);
            minHeight = Math.max(minHeight, headerComp.getPreferredSize().width);
        }
        // set both tables preferred sizes
        dataTable.getTableHeader().setPreferredSize(new Dimension(dataTable.getTableHeader().getPreferredSize().width, minHeight));
        datesTable.getTableHeader().setPreferredSize(new Dimension(datesTable.getTableHeader().getPreferredSize().width, minHeight));

        int zoomedCellHeight = (int)(30 * ZoomManager.getZoom());
        dataTable.setRowHeight(rowHeight);
        datesTable.setRowHeight(rowHeight);
        timeOffTable.setRowHeight(zoomedDateCellWidth * 2);
    }

    public void applyZoom(){
        currentZoom = ZoomManager.getZoom();

        zoomedDateCellWidth = (int)(30 * currentZoom);
        zoomedCellBuffer = (int)(8 * currentZoom);
        BASE_FONT_SIZE = (int)(15 * currentZoom);
        PLAIN_FONT = new Font("SansSerif", Font.PLAIN, BASE_FONT_SIZE);
        BOLD_FONT = new Font("SansSerif", Font.BOLD, BASE_FONT_SIZE);
        MAX_CELL_WIDTH = (int)(300 * currentZoom);

        dataTable.setFont(PLAIN_FONT);
        dataTable.getTableHeader().setFont(BOLD_FONT);
        datesTable.setFont(PLAIN_FONT);
        datesTable.getTableHeader().setFont(BOLD_FONT);
        timeOffTable.setFont(PLAIN_FONT);
        //timeOffTable.getTableHeader().setFont(BOLD_FONT);

        addJobButton.setFont(BUTTON_FONT);
        updateJobButton.setFont(BUTTON_FONT);
        deleteButton.setFont(BUTTON_FONT);
        jwoFilterButton.setFont(BUTTON_FONT);
        customerFilterButton.setFont(BUTTON_FONT);
        dateFilterButton.setFont(BUTTON_FONT);
        todayButton.setFont(BUTTON_FONT);
        resetViewButton.setFont(BUTTON_FONT);
        archiveButton.setFont(BUTTON_FONT);
        timeOffButton.setFont(BUTTON_FONT);
        plusZoomButton.setFont(BUTTON_FONT);
        minusZoomButton.setFont(BUTTON_FONT);

        setTableFontsAndSizes();
        setTableFontsAndSizes();
        this.revalidate();
        this.repaint();
        setTableFontsAndSizes();


        setDividerLocation();
        syncScrollPanes();
        resetDatesScrollBar();
    }

    // Populate dates table with colored days
    public void populateDatesTable() throws SQLException {
        ArrayList<DateRange> dates = new ArrayList<>();
        int buildIndex = 1;
        int finishIndex = 1;
        int installIndex = 1;
        while(jobBoardResultSet.next()){
            buildIndex = jobBoardResultSet.findColumn("build");
            finishIndex = jobBoardResultSet.findColumn("finish");
            installIndex = jobBoardResultSet.findColumn("install");

            Date sqlDate = jobBoardResultSet.getDate("due_date");
            LocalDate dueDate = sqlDate.toLocalDate();

            // get amount of days for each section of time from data table
            int buildDays = (int) jobBoardResultSet.getInt(buildIndex);
            int finishDays = (int) jobBoardResultSet.getInt(finishIndex);
            int installDays = (int) jobBoardResultSet.getInt(installIndex);
            int daysBack = buildDays + finishDays;
            int daysForward = installDays - 1;

            // check if the due date is a saturday
            boolean isDueDateSaturday = (dueDate.getDayOfWeek() == DayOfWeek.SATURDAY);

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
        jobBoardResultSet.beforeFirst();

        // Apply the dates for each time period to the custom renderer so it can draw the colored squares
        TableCustom.applyDates(datesTable, dates);
    }

    public void populateTimeOffTable() throws SQLException {
        SelectQueryBuilder qb = new SelectQueryBuilder();
        qb.select("*");
        qb.from("time_off");
        ResultSet rs = database.sendSelect(qb.build());

        ArrayList<TimeOffDates> timeOffDatesList = new ArrayList<>();

        int personIndex = 1;
        while(rs.next()){
            personIndex = rs.findColumn("worker");

            Date sqlDate = rs.getDate("start_date");
            LocalDate startDate = sqlDate.toLocalDate();

            sqlDate = rs.getDate("end_date");
            LocalDate endDate = sqlDate.toLocalDate();

            ArrayList<LocalDate> timeOffDates = new ArrayList<>();
            LocalDate currentDate = startDate;

            while(currentDate.isBefore(endDate)){
                if(currentDate.getDayOfWeek() != DayOfWeek.SATURDAY && currentDate.getDayOfWeek() != DayOfWeek.SUNDAY){
                    timeOffDates.add(currentDate);
                }
                currentDate = currentDate.plusDays(1);
            }
            timeOffDates.add(currentDate);
            timeOffDatesList.add(new TimeOffDates(rs.getString(personIndex), timeOffDates));
        }
        TableCustom.applyTimeOffDates(timeOffTable, timeOffDatesList);
    }

    // syncs both scroll bars in dataTable and datesTable to move together
    public void syncScrollPanes() {
        JScrollBar vScrollData = tableScroll.getVerticalScrollBar();
        JScrollBar vScrollDates = datesScroll.getVerticalScrollBar();
        JScrollBar hScrolldates = datesScroll.getHorizontalScrollBar();
        JScrollBar hScrollTimeoff = timeOffScroll.getHorizontalScrollBar();

        vScrollData.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                vScrollDates.setValue(vScrollData.getValue());
            }
        });
        vScrollDates.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                vScrollData.setValue(vScrollDates.getValue());
            }
        });
        hScrolldates.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                hScrollTimeoff.setValue(hScrolldates.getValue());
            }
        });
        hScrollTimeoff.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                hScrolldates.setValue(hScrollTimeoff.getValue());
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







    public void setDividerLocation(){
        splitPane.setDividerLocation(totalWidth);
    }

    public void setEditableAndDropdownColumns(ArrayList<Integer> visibleIndexes) throws IOException, SQLException {
        Properties editableProps = new Properties();
        FileInputStream editableInput = new FileInputStream(PropertiesManager.CELL_EDITABLE_PROPERTIES_FILE_PATH);
        editableProps.load(editableInput);

        Properties dropdownProps = new Properties();
        FileInputStream dropdownInput = new FileInputStream(PropertiesManager.CELL_DROPDOWN_PROPERTIES_FILE_PATH);
        dropdownProps.load(dropdownInput);

        Properties dropdownListProps = new Properties();
        FileInputStream dropdownListInput = new FileInputStream(PropertiesManager.DROPDOWN_OPTIONS_PROPERTIES_FILE_PATH);
        dropdownListProps.load(dropdownListInput);

        DefaultCellEditor editor = null;

        jobBoardResultSet.beforeFirst();
        for(int i = 0; i < dataTableModel.getColumnCount(); i++){
            String columnLabel = jobBoardResultSet.getMetaData().getColumnLabel(visibleIndexes.get(i));
            String isEditableValue = editableProps.getProperty(columnLabel);
            String isDropdownValue = dropdownProps.getProperty(columnLabel);
            JComponent text = new JComponent(){};

            if("t".equals(isEditableValue)){
                JTextField editorField = new JTextField();
                editorField.setBackground(new Color(24, 24, 24));
                editorField.setForeground(new Color(255,255,255));
                editorField.setFont(PLAIN_FONT);
                editorField.setBorder(new LineBorder(Color.GREEN, 2));
                editorField.setCaretPosition(editorField.getText().length());
                editorField.setCaretColor(new Color(255,255,255));

                final boolean[] enterPressed = {false};
                final int[] currentRow = {0};
                final int[] currentCol = {0};
                final String[] currentValue = {""};
                editor = new DefaultCellEditor(editorField){
                    @Override
                    public boolean isCellEditable(EventObject anEvent) {
                        return super.isCellEditable(anEvent);
                    }

                    @Override
                    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                        currentRow[0] = row;
                        currentCol[0] = column;
                        currentValue[0] = value != null ? value.toString() : "";

                        SwingUtilities.invokeLater(()->{
                            editorField.setCaretPosition(editorField.getText().length());
                        });

                        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
                    }
                    @Override
                    public boolean stopCellEditing() {
                        if (enterPressed[0]) {
                            UpdateQueryBuilder qb = new UpdateQueryBuilder();
                            qb.updateTable("job_board");
                            try {
                                qb.setColNames(dataTable.getColumnName(currentCol[0]).replace(" ", "_"));
                                qb.setValues(editorField.getText());
                                TableColumn targetColumn = dataTable.getColumn("jwo");
                                TableColumnModel columnModel = dataTable.getColumnModel();
                                int columnIndex = columnModel.getColumnIndex(targetColumn.getIdentifier());
                                qb.where("jwo = "+ dataTable.getValueAt(currentRow[0], columnIndex));
                                database.sendUpdate(qb.build());
                            } catch (SQLException e) {
                                enterPressed[0] = false;
                                throw new RuntimeException(e);
                            }
                            enterPressed[0] = false;
                            try {
                                loadTable();
                            } catch (SQLException e) {
                                enterPressed[0] = false;
                                throw new RuntimeException(e);
                            }
                            setDividerLocation();
                            return super.stopCellEditing();
                        } else {
                            cancelCellEditing(); // Explicitly cancel if Enter wasn't pressed
                            return false; // Indicate that editing was not successfully stopped (committed)
                        }
                    }

                    @Override
                    public void cancelCellEditing() {
                        super.cancelCellEditing();
                    }
                };
                editor.setClickCountToStart(1);
                DefaultCellEditor finalEditor = editor;
                editorField.addActionListener(e -> {
                    enterPressed[0] = true;
                    finalEditor.stopCellEditing();
                });
                // Assign the editor to the column
                text = editorField;
            }
            else if("t".equals(isDropdownValue)){
                String columnName = columnLabel;
                if(columnLabel.equals("mechanic"))
                    columnName = "worker";
                String dropdownList = dropdownListProps.getProperty(columnName + "_list.dropdown.options");
                if(dropdownList == null)
                    dropdownList = ",none";

                JComboBox comboBox = new JComboBox<>(dropdownList.split(","));
                Component editorComp = comboBox.getEditor().getEditorComponent();

                if(editorComp instanceof JTextField textField){
                    textField.setPreferredSize(new Dimension(200, 30));
                    textField.setCaretColor(Color.white);
                    textField.setBackground(new Color(60, 60, 60));
                    textField.setForeground(Color.WHITE);
                    textField.setFont(PLAIN_FONT);
                    textField.setBorder(new EmptyBorder(0,0,0,0));
                }

                comboBox.setPreferredSize(new Dimension(200, 30));
                comboBox.setEditable(true);
                comboBox.setBackground(new Color(60, 60, 60));
                comboBox.setForeground(Color.WHITE);
                comboBox.setFont(PLAIN_FONT);
                comboBox.setBorder(new EmptyBorder(5, 5, 5, 5));
                comboBox.setMaximumRowCount(5);


                final boolean[] enterPressed = {false};
                final int[] currentRow = {0};
                final int[] currentCol = {0};
                final String[] currentValue = {""};
                editor = new DefaultCellEditor(comboBox){
                    @Override
                    public boolean isCellEditable(EventObject anEvent) {
                        return super.isCellEditable(anEvent);
                    }

                    @Override
                    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                        currentRow[0] = row;
                        currentCol[0] = column;
                        currentValue[0] = value != null ? value.toString() : "";
                        enterPressed[0] = false;

//                        SwingUtilities.invokeLater(()->{
//                            comboBox.setCaretPosition(comboBox.getText().length());
//                        });

                        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
                    }
                    @Override
                    public boolean stopCellEditing() {
                        if (enterPressed[0]) {
                            UpdateQueryBuilder qb = new UpdateQueryBuilder();
                            qb.updateTable("job_board");
                            try {
                                qb.setColNames(dataTable.getColumnName(currentCol[0]).replace(" ", "_"));
                                qb.setValues(comboBox.getEditor().getItem().toString());
                                TableColumn targetColumn = dataTable.getColumn("jwo");
                                TableColumnModel columnModel = dataTable.getColumnModel();
                                int columnIndex = columnModel.getColumnIndex(targetColumn.getIdentifier());
                                qb.where("jwo = "+ dataTable.getValueAt(currentRow[0], columnIndex));
                                database.sendUpdate(qb.build());
                                loadTable();
                                setDividerLocation();
                                return super.stopCellEditing();
                            } catch (SQLException e) {
                                enterPressed[0] = false;
                                throw new RuntimeException(e);
                            }
                        } else if(!enterPressed[0]){
                            return super.stopCellEditing();
                            // Explicitly cancel if Enter wasn't pressed
                            //return false; // Indicate that editing was not successfully stopped (committed)
                        }
                        return super.stopCellEditing();
                    }
                };
                editor.setClickCountToStart(1);
                DefaultCellEditor finalEditor = editor;
//                comboBox.addActionListener(e -> {
//                    enterPressed[0] = true;
//                    finalEditor.stopCellEditing();
//                });
                comboBox.addPopupMenuListener(new PopupMenuListener() {
                    private Object previousSelection = null;
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                        previousSelection = comboBox.getSelectedItem();
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        Object currentSelection = comboBox.getSelectedItem();
                        if (currentSelection != null && !currentSelection.equals(previousSelection)) {
                            enterPressed[0] = true;
                            finalEditor.stopCellEditing();
                        }
                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                        enterPressed[0] = false;
                    }
                });
                text = comboBox;
            }

            dataTable.getColumnModel().getColumn(i).setCellEditor(editor);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == addJobButton){
            new AddJobWindow(database, jobBoardResultSet);
        }
        if(e.getSource() == updateJobButton){
            int selectedRow = dataTable.getSelectedRow();
            if(selectedRow != -1) {
                String selectedJwo = dataTable.getValueAt(selectedRow, dataTable.getColumnModel().getColumnIndex("jwo")).toString();
                new UpdateJobWindow(database, jobBoardResultSet, selectedJwo);
            }
        }
        if(e.getSource() == deleteButton){
            int selectedRow = dataTable.getSelectedRow();
            if(selectedRow != -1) {
                String selectedJwo = dataTable.getValueAt(selectedRow, dataTable.getColumnModel().getColumnIndex("jwo")).toString();
                new DeleteJobWindow(selectedJwo);
            }
        }
        if(e.getSource() == jwoFilterButton){
            refreshData("jwo");
        }
        if(e.getSource() == customerFilterButton){
            refreshData("customer");
        }
        if(e.getSource() == dateFilterButton){
            refreshData("due_date");
        }
        if(e.getSource() == todayButton) {
            resetDatesScrollBar();
        }
        if(e.getSource() == resetViewButton) {

            currentBoardMode = JobBoardMode.ACTIVE_JOBS;
            refreshData("due_date");

        }
        if(e.getSource() == archiveButton) {
            currentBoardMode = JobBoardMode.ARCHIVE;
            refreshData("due_date");
        }

        if(e.getSource() == timeOffButton){
            new AddTimeOffWindow(database);
        }

        if(e.getSource() == plusZoomButton) {
            ZoomManager.increaseZoom();
            applyZoom();
        }
        if(e.getSource() == minusZoomButton) {
            ZoomManager.decreaseZoom();
            applyZoom();
        }
    }

}
