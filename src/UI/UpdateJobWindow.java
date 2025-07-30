package UI;

import DatabaseInteraction.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Properties;

public class UpdateJobWindow extends JFrame implements ActionListener {

    private static  Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 15);
    private DatabaseInteraction database;
    private ResultSet jobBoardResultSet;
    private ArrayList<JPanel> fields;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel fieldsPanel;
    private JPanel buttonsPanel;
    private JButton updateButton;
    private JButton cancelButton;
    private String selectedJwo;
    ArrayList<String> requiredCols;
    private static final Color UPDATE_PANEL_COLOR = new Color(40, 40, 40);
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public UpdateJobWindow(DatabaseInteraction db, ResultSet rs, String jwo){
        database = db;
        jobBoardResultSet = rs;
        selectedJwo = jwo;
        initializeComponents();
        addFields();

        JLabel headingText = new JLabel("Update Job", SwingConstants.CENTER);
        headingText.setForeground(new Color(0,100,180));
        headingText.setFont(new Font(headingText.getFont().getFontName(), Font.BOLD, 30));
        topPanel.add(headingText);

        buttonsPanel.add(updateButton);
        buttonsPanel.add(cancelButton);

        centerPanel.add(topPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(fieldsPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(buttonsPanel);

        this.add(centerPanel);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setTitle("Update Job");
        this.setBackground(new Color(24,24,24));
        this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initializeComponents() {

        topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.setBackground(new Color(40,40,40));

        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(24,24,24));
        centerPanel.setBorder(new EmptyBorder(10,10,10,10));

        fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridLayout(0, 1, 0,10));
        fieldsPanel.setBackground(new Color(40,40,40));
        fieldsPanel.setBorder(new EmptyBorder(5,5,5,5));

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsPanel.setBackground(new Color(40,40,40));

        updateButton = new JButton("Update");
        updateButton.setPreferredSize(new Dimension(100,40));
        updateButton.setBackground(new Color(0, 0, 0));
        updateButton.setForeground(new Color(255,255,255));
        updateButton.setFont(BUTTON_FONT);
        updateButton.setFocusable(false);
        updateButton.addActionListener(this);

        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100,40));
        cancelButton.setBackground(new Color(0, 0, 0));
        cancelButton.setForeground(new Color(255,255,255));
        cancelButton.setFont(BUTTON_FONT);
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(this);

        fields = new ArrayList<>();

        JRootPane rootPane = this.getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "confirm");
        rootPane.getActionMap().put("confirm", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateButton.doClick(); // simulate button press
            }
        });
    }

    private void addFields()  {

        requiredCols = new ArrayList<>();
        String[] colOrder = PropertiesManager.getColumnOrder();

        Properties requiredProps = new Properties();
        Properties visibleProps = new Properties();
        Properties dropdownProps = new Properties();
        Properties dropdownListProps = new Properties();
        FileInputStream reqInput = null;
        FileInputStream visibleInput = null;
        FileInputStream dropdownInput = null;
        FileInputStream dropdownListInput = null;
        OutputStream output = null;
        try {
            reqInput = new FileInputStream(PropertiesManager.COLUMN_REQUIRED_PROPERTIES_FILE_PATH);
            requiredProps.load(reqInput);

            visibleInput = new FileInputStream(PropertiesManager.MENU_COLUMN_VISIBLE_PROPERTIES_FILE_PATH);
            visibleProps.load(visibleInput);

            dropdownInput = new FileInputStream(PropertiesManager.MENU_COLUMN_DROPDOWN_PROPERTIES_FILE_PATH);
            dropdownProps.load(dropdownInput);

            dropdownListInput = new FileInputStream(PropertiesManager.DROPDOWN_OPTIONS_PROPERTIES_FILE_PATH);
            dropdownListProps.load(dropdownListInput);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        ResultSetMetaData rsMeta = null;
        try {
            jobBoardResultSet.beforeFirst();
            while (jobBoardResultSet.next()) {
                if (jobBoardResultSet.getObject("jwo").toString().equals(selectedJwo)) {
                    break;
                }
            }
        }catch (SQLException e){
            JOptionPane.showMessageDialog(null, e.getMessage() + "\n for safety...");
            System.exit(1);
        }

        for(String columnName : colOrder){
            String requiredValue = requiredProps.getProperty(columnName);
            String visibleValue = visibleProps.getProperty(columnName);
            String dropdownValue = dropdownProps.getProperty(columnName);
            if("t".equals(visibleValue)){
                if("t".equals(requiredValue)){
                    requiredCols.add(columnName);
                    columnName += "*";
                }
                JLabel label = new JLabel(columnName);
                if(columnName.contains("*"))
                    label.setForeground(new Color(255, 50,50));
                else
                    label.setForeground(Color.white);
                label.setFont(new Font("SansSerif", Font.BOLD, 20));

                JComponent text;
                if("t".equals(dropdownValue)){
                    String dropdownList = dropdownListProps.getProperty(columnName.replace("*", "") + "_list.dropdown.options");
                    if(dropdownList == null)
                        dropdownList = ",none";

                    JComboBox comboBox = new JComboBox<>(dropdownList.split(","));
                    Component editorComp = comboBox.getEditor().getEditorComponent();

                    if(editorComp instanceof JTextField textField){
                        textField.setPreferredSize(new Dimension(200, 30));
                        textField.setCaretColor(Color.white);
                        textField.setBackground(new Color(60, 60, 60));
                        textField.setForeground(Color.WHITE);
                        textField.setFont(new Font("SansSerif", Font.PLAIN, 20));
                        textField.setBorder(new EmptyBorder(0,0,0,0));
                    }

                    comboBox.setPreferredSize(new Dimension(200, 30));
                    comboBox.setEditable(true);
                    comboBox.setBackground(new Color(60, 60, 60));
                    comboBox.setForeground(Color.WHITE);
                    comboBox.setFont(new Font("SansSerif", Font.PLAIN, 20));
                    comboBox.setBorder(new EmptyBorder(0, 5, 0, 0));
                    comboBox.setMaximumRowCount(5);

                    text = comboBox;
                }
                else {
                    JTextField textField = new JTextField();
                    textField.setPreferredSize(new Dimension(200, 30));
                    textField.setCaretColor(Color.white);
                    textField.setBackground(new Color(60, 60, 60));
                    textField.setForeground(Color.WHITE);
                    textField.setFont(new Font("SansSerif", Font.PLAIN, 20));
                    textField.setBorder(new EmptyBorder(5, 5, 5, 5));
                    text = textField;
                }

                JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
                panel.setBackground(new Color(40, 40, 40));
                panel.add(label);
                panel.add(text);
                fields.add(panel);
                fieldsPanel.add(panel);


                Object value = null;
                try {
                    int databaseColIndex = jobBoardResultSet.findColumn(columnName.replace("*", ""));
                    value = jobBoardResultSet.getObject(databaseColIndex);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage());
                    System.exit(1);
                }

                if(value instanceof java.sql.Date){
                    java.sql.Date sqlDate = (java.sql.Date) value;
                    LocalDate localDate = sqlDate.toLocalDate();
                    value = dateFormat.format(localDate);
                }

                if(text instanceof JComboBox<?> comboBox){
                    comboBox.setSelectedItem(value != null ? value.toString() : "");
                }
                else if(text instanceof JTextField textField){
                    textField.setText(value != null ? value.toString() : "");
                }
            }
        }
        database.closeResources();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == updateButton){
            UpdateQueryBuilder qb = new UpdateQueryBuilder();
            qb.updateTable("job_board");
            boolean notEmpty = true;
            String missingFields = "";
            for(JPanel p : fields){
                JLabel label = (JLabel) p.getComponent(0);
                String labelText = label.getText().replace("*", "");
                Component text =  p.getComponent(1);
                String str = "OLD";
                if(text instanceof JComboBox<?> comboBox){
                    str = comboBox.getEditor().getItem().toString();
                }
                else if(text instanceof JTextField textField){
                    str = textField.getText();
                }


                boolean isRequired = requiredCols.contains(labelText);

                if(isRequired){
                    if(str.isBlank()) {
                        notEmpty = false;
                        missingFields += labelText + ", ";
                    }
                    else {
                        qb.setColNames(labelText);
                        qb.setValues(str);
                    }
                }
                else{
                    qb.setColNames(labelText);
                    qb.setValues(str);
                }
            }

            if(notEmpty) {
                try {
                    qb.where("jwo = "+ selectedJwo);
                    database.sendUpdate(qb.build());
                    dispose();
                } catch (SQLException ex) {
                    String msg = ex.getMessage();

                    if(msg.toLowerCase().contains("syntax") && msg.toLowerCase().contains("integer")){
                        msg = "JWO can only be a number";
                    }
                    else if(msg.toLowerCase().contains("syntax") && msg.toLowerCase().contains("date")){
                        msg = "Fields with 'date' can only be in date format\ne.g. 12/12/12, 12-12-12, 12/12/2012, 12-12-2012";
                    }
                    else if(msg.toLowerCase().contains("date") && msg.toLowerCase().contains("range")){
                        msg = "A date you entered is out of range";
                    }

                    JOptionPane.showMessageDialog(this, msg, null, JOptionPane.ERROR_MESSAGE);
                }finally {
                    database.closeResources();
                }
            }
            else{
                JOptionPane.showMessageDialog(this, "You're missing required values: " + missingFields, null, JOptionPane.ERROR_MESSAGE);
            }
        }
        if(e.getSource() == cancelButton){
            dispose();
        }
    }
}
