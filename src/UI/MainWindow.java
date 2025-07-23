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

import static DatabaseInteraction.Filter.FilterStatus.*;

public class MainWindow extends JFrame implements ActionListener, MouseListener {

    private static  int ABSOLUTE_MIN_CELL_WIDTH = 50;
    private static  int MAX_CELL_WIDTH = 400;
    private static  int BASE_FONT_SIZE = 15;
    private static  Dimension TOP_PANEL_PREF_SIZE = new Dimension(0, 100);
    private static  Dimension BUTTON_PANEL_PREF_SIZE = new Dimension(400, 100);
    private static  Dimension TABLE_SCROLL_PREF_SIZE = new Dimension(500,0);
    private static  Font PLAIN_FONT = new Font("SansSerif", Font.PLAIN, BASE_FONT_SIZE);
    private static  Font BOLD_FONT = new Font("SansSerif", Font.BOLD, BASE_FONT_SIZE);
    private static  Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 15);

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

    ArrayList<Integer> visibleIndexes = new ArrayList<>();

    DefaultTableModel dataTableModel;
    DefaultTableModel datesTableModel;

    public MainWindow() throws Exception{

        database = new DatabaseInteraction();
        updateDimensions();
        initializeComponents();
        syncScrollPanes();
        refreshResultSets();
        loadTable();

        buttonPanel.add(addJobButton);
        buttonPanel.add(updateJobButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(jwoFilterButton);
        buttonPanel.add(customerFilterButton);
        buttonPanel.add(dateFilterButton);

        topContainerPanel.add(buttonPanel);
        topContainerPanel.add(resetViewButton);
        topContainerPanel.add(archiveButton);
        topContainerPanel.add(todayButton);
        topContainerPanel.add(minusZoomButton);
        topContainerPanel.add(plusZoomButton);

        leftPanel.setPreferredSize(new Dimension(totalWidth, 100));
        leftPanel.add(tableScroll, BorderLayout.CENTER);
        setDividerLocation();

        topTablePanel.add(timeOffScroll, BorderLayout.EAST);
        //topTablePanel.setMinimumSize(new Dimension(100, 10));
        centerPanel.add(topTablePanel, BorderLayout.NORTH);
        centerPanel.add(splitPane, BorderLayout.CENTER);

        this.add(centerPanel, BorderLayout.CENTER);
        this.add(topContainerPanel, BorderLayout.NORTH);
//        this.add(splitPane, BorderLayout.CENTER);

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

        timeOffTable = new JTable();
        timeOffTable.getTableHeader().addMouseListener(this);
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
                        System.out.println("guh");
                        timeOffScroll.setPreferredSize(new Dimension(datesScroll.getWidth(), timeOffTable.getPreferredSize().height));
                        System.out.println(splitPane.getRightComponent().getWidth());
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
        setTableFontsAndSizes();
        populateDatesTable();
        resetDatesScrollBar();
        populateTimeOffTable();

        database.closeResources();
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
                //System.out.println("database: "+databaseIndex+" table: "+i);
                Object item = jobBoardResultSet.getObject(databaseIndex);
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
        jobBoardResultSet.beforeFirst();


        dataTable.setModel(dataTableModel);
        try {
            setEditableAndDropdownColumns(visibleIndexes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //-------------------
    }

    public void setEditableAndDropdownColumns(ArrayList<Integer> visibleIndexes) throws IOException, SQLException {
        Properties props = new Properties();
        FileInputStream input = new FileInputStream(PropertiesManager.CELL_EDITABLE_PROPERTIES_FILE_PATH);
        props.load(input);

        for(int i = 0; i < dataTableModel.getColumnCount(); i++){
            String columnLabel = jobBoardResultSet.getMetaData().getColumnLabel(visibleIndexes.get(i));
            String isEditable = props.getProperty(columnLabel);

            if("t".equals(isEditable)){
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
                DefaultCellEditor editor = new DefaultCellEditor(editorField){
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
                                qb.setColNames(jobBoardResultSet.getMetaData().getColumnLabel(currentCol[0]+1));
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
                            refreshResultSets();
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
                        System.out.println("Canceling cell editing.");
                        super.cancelCellEditing();
                    }
                };
                editor.setClickCountToStart(1);
                editorField.addActionListener(e -> {
                    enterPressed[0] = true;
                    editor.stopCellEditing();
                });
                // Assign the editor to the column
                dataTable.getColumnModel().getColumn(i).setCellEditor(editor);
            }
        }

    }


    public void createDatesTable() throws SQLException {
        //--------------------------- create dates table
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("E- dd- MMM");
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusDays(365);
        LocalDate oneYearFromNow = today.plusDays(365);
        LocalDate currentDate = oneYearAgo;
        ArrayList<LocalDate> saturdayList = new ArrayList<>();

        DefaultTableModel tm = new DefaultTableModel();

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
                            tm.addColumn(currentDate.format(dateFormat));
                        }
                    }
                }
                else {
                    datesTableModel.addColumn(currentDate.format(dateFormat));
                    tm.addColumn(currentDate.format(dateFormat));
                }
                if(currentDate.isEqual(today)){
                    todayCol = datesTableModel.getColumnCount() - 1;
                }
            }
            else if(currentDate.isEqual(today)){
                datesTableModel.addColumn(currentDate.format(dateFormat));
                tm.addColumn(currentDate.format(dateFormat));
                todayCol = datesTableModel.getColumnCount() - 1;
            }

            currentDate = currentDate.plusDays(1);
        }
        datesTable.setModel(datesTableModel);
        tm.addRow(new Object[visibleIndexes.size()]);
        System.out.println(tm.getRowCount());
        timeOffTable.setModel(tm);
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
        for(int col = 0; col < dataTable.getColumnCount(); col++){
            TableColumn column = dataTable.getColumnModel().getColumn(col);
            TableCellRenderer headerRenderer = dataTable.getTableHeader().getDefaultRenderer();
            JLabel headerComp = (JLabel)headerRenderer.getTableCellRendererComponent(dataTable, column.getHeaderValue(), false, false, -1, col);

            int minWidth = 0;

            for(int row = 0; row < dataTable.getRowCount(); row++){
                TableCellRenderer cellRenderer = dataTable.getCellRenderer(row, col);
                Component comp = dataTable.prepareRenderer(cellRenderer, row, col);
                int cellWidth = comp.getPreferredSize().width;
                if(cellWidth < MAX_CELL_WIDTH)
                    minWidth = Math.max(minWidth, cellWidth + (int)(8 * ZoomManager.getZoom()));
                else
                    minWidth = MAX_CELL_WIDTH;
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
            timeOffTable.getColumnModel().getColumn(i).setMinWidth((int)(30 * ZoomManager.getZoom()));
            timeOffTable.getColumnModel().getColumn(i).setMaxWidth((int)(30 * ZoomManager.getZoom()));
            timeOffTable.getColumnModel().getColumn(i).setPreferredWidth((int)(30 * ZoomManager.getZoom()));
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
        timeOffTable.setRowHeight((int)(30 * ZoomManager.getZoom()));
//        centerPanel.setMinimumSize(new Dimension(totalWidth, 0));
//        datesScroll.setMinimumSize(new Dimension(totalWidth, 0));

//        timeOffScroll.setPreferredSize(new Dimension((int)( 1/ZoomManager.getZoom() * splitPane.getRightComponent().getPreferredSize().width), 50));
//        timeOffScroll.revalidate();
//        timeOffScroll.repaint();
//        topTablePanel.revalidate();
//        topTablePanel.repaint();
    }

    // Populate dates table with colored days
    public void populateDatesTable() throws SQLException {
        // TODO find a way to get column index without exact number


        ArrayList<DateRange> dates = new ArrayList<>();
        int buildIndex = 1;
        int finishIndex = 1;
        int installIndex = 1;
        int dueDateColumn = 1;
        while(jobBoardResultSet.next()){
            buildIndex = jobBoardResultSet.findColumn("build");
            finishIndex = jobBoardResultSet.findColumn("finish");
            installIndex = jobBoardResultSet.findColumn("install");
            dueDateColumn = jobBoardResultSet.findColumn("due_date");

            Date sqlDate = jobBoardResultSet.getDate("due_date");
            LocalDate dueDate = sqlDate.toLocalDate();

            // get amount of days for each section of time from data table
            int buildDays = (int) jobBoardResultSet.getInt(buildIndex);
            int finishDays = (int) jobBoardResultSet.getInt(finishIndex);
            int installDays = (int) jobBoardResultSet.getInt(installIndex);
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
        jobBoardResultSet.beforeFirst();

        // Apply the dates for each time period to the custom renderer so it can draw the colored squares
        TableCustom.applyDates(datesTable, dates);
        //populateTimeOffTable();
    }

    public void populateTimeOffTable() throws SQLException {
        SelectQueryBuilder qb = new SelectQueryBuilder();
        qb.select("*");
        qb.from("time_off");
        ResultSet rs = database.sendSelect(qb.build());

        ArrayList<TimeOffDates> timeOffDatesList = new ArrayList<>();

        int personIndex = 1;
        int startDateIndex = 1;
        int endDateIndex = 1;
        while(rs.next()){
            personIndex = rs.findColumn("person");
            startDateIndex = rs.findColumn("start_date");
            endDateIndex = rs.findColumn("end_date");

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

    public void updateDimensions(){
        ABSOLUTE_MIN_CELL_WIDTH = (int)(50 * ZoomManager.getZoom());
        MAX_CELL_WIDTH = (int)(300 * ZoomManager.getZoom());
        BASE_FONT_SIZE = (int)(15 * ZoomManager.getZoom());
        TOP_PANEL_PREF_SIZE = new Dimension(0, 100);
        BUTTON_PANEL_PREF_SIZE = new Dimension(400, 100);
        TABLE_SCROLL_PREF_SIZE = new Dimension(500,0);
        PLAIN_FONT = new Font("SansSerif", Font.PLAIN, BASE_FONT_SIZE);
        BOLD_FONT = new Font("SansSerif", Font.BOLD, BASE_FONT_SIZE);
    }

    public void refreshTable() {

    }

    public void refreshResultSets(){
        SelectQueryBuilder qbJB = new SelectQueryBuilder();
        qbJB.select("*");
        qbJB.from("job_board");
        qbJB.where("is_active = true");
        qbJB.orderBy(new Filter("due_date", ASC));
        SelectQueryBuilder qbCL = new SelectQueryBuilder();
        qbCL.select("*");
        qbCL.from("customer_list");
        jobBoardResultSet = database.sendSelect(qbJB.build());
        customerListResultSet = database.sendSelect(qbCL.build());
    }

    public void createDropdown(String columnName){

    }

    public void setDividerLocation(){
        splitPane.setDividerLocation(totalWidth);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == addJobButton){
            new AddJobWindow(database, jobBoardResultSet);
        }
        if(e.getSource() == updateJobButton){
            int selectedRow = dataTable.getSelectedRow();
            if(selectedRow != -1) {
                String selectedJwo = dataTable.getValueAt(selectedRow, 0).toString();
                new UpdateJobWindow(database, jobBoardResultSet, selectedJwo);
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
                refreshResultSets();
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
                refreshResultSets();
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
                refreshResultSets();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if(e.getSource() == todayButton) {
            resetDatesScrollBar();
        }
        if(e.getSource() == resetViewButton) {
            setDividerLocation();
            //dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            refreshResultSets();

            try {
                setEditableAndDropdownColumns(visibleIndexes);
                loadTable();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        if(e.getSource() == archiveButton) {
//            setDividerLocation();
//            SelectQueryBuilder qb = new SelectQueryBuilder();
//            qb.select("*");
//            qb.from("job_board");
//            qb.where("is_active = false");
//            SelectQueryBuilder qbCL = new SelectQueryBuilder();
//            qbCL.select("*");
//            qbCL.from("customer_list");
//
//            try {
//                jobBoardResultSet = database.sendSelect(qb.build());
//                customerListResultSet = database.sendSelect(qbCL.build());
//                loadTable();
//            } catch (SQLException ex) {
//                ex.printStackTrace();
//            }
//
//            dataTable.setSelectionModel(new DefaultListSelectionModel() {
//                @Override
//                public void setSelectionInterval(int index0, int index1) {
//                    // Do nothing, effectively preventing selection
//                }
//
//                @Override
//                public void addSelectionInterval(int index0, int index1) {
//                    // Do nothing
//                }
//
//                @Override
//                public void removeSelectionInterval(int index0, int index1) {
//                    // Do nothing
//                }
//            });
        }
        if(e.getSource() == plusZoomButton) {
            ZoomManager.increaseZoom();
            updateDimensions();
            //refreshTable();
            setTableFontsAndSizes();
            setDividerLocation();
            try {
                setEditableAndDropdownColumns(visibleIndexes);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            SwingUtilities.invokeLater(()->{
                //setTableFontsAndSizes();
                resetDatesScrollBar();
            });
        }
        if(e.getSource() == minusZoomButton) {
            ZoomManager.decreaseZoom();
            updateDimensions();
            //refreshTable();
            setTableFontsAndSizes();
            setDividerLocation();
            try {
                setEditableAndDropdownColumns(visibleIndexes);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            SwingUtilities.invokeLater(()->{
                //setTableFontsAndSizes();
                resetDatesScrollBar();
            });
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
