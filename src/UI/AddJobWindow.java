package UI;

import DatabaseInteraction.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.util.Properties;

public class AddJobWindow extends JFrame implements ActionListener {

    private static  Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 15);
    private DatabaseInteraction database;
    private ResultSet jobBoardResultSet;
    private ArrayList<JPanel> fields;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel fieldsPanel;
    private JPanel buttonsPanel;
    private JButton createButton;
    private JButton cancelButton;
    private ArrayList<String> requiredCols;

    public AddJobWindow(DatabaseInteraction db, ResultSet rs){
        database = db;
        jobBoardResultSet = rs;
        initializeComponents();
        try {
            addFields();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        JLabel headingText = new JLabel("Add Job", SwingConstants.CENTER);
        headingText.setForeground(new Color(200,40,40));
        headingText.setFont(new Font(headingText.getFont().getFontName(), Font.BOLD, 30));
        topPanel.add(headingText);

        buttonsPanel.add(createButton);
        buttonsPanel.add(cancelButton);

        centerPanel.add(topPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(fieldsPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(buttonsPanel);

        this.add(centerPanel);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setTitle("Add Job");
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

        createButton = new JButton("Create");
        createButton.setPreferredSize(new Dimension(100,40));
        createButton.setBackground(new Color(0, 0, 0));
        createButton.setForeground(new Color(255,255,255));
        createButton.setFont(BUTTON_FONT);
        createButton.setFocusable(false);
        createButton.addActionListener(this);

        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100,40));
        cancelButton.setBackground(new Color(0, 0, 0));
        cancelButton.setForeground(new Color(255,255,255));
        cancelButton.setFont(BUTTON_FONT);
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(this);

        fields = new ArrayList<>();
    }

    private void addFields() throws IOException, SQLException {
        requiredCols = new ArrayList<>();
        String[] colOrder = PropertiesManager.getColumnOrder();

        Properties requiredProps = new Properties();
        FileInputStream reqInput = new FileInputStream(PropertiesManager.COLUMN_REQUIRED_PROPERTIES_FILE_PATH);
        requiredProps.load(reqInput);

        Properties visibleProps = new Properties();
        FileInputStream visibleInput = new FileInputStream(PropertiesManager.MENU_COLUMN_VISIBLE_PROPERTIES_FILE_PATH);
        visibleProps.load(visibleInput);

        Properties dropdownProps = new Properties();
        FileInputStream dropdownInput = new FileInputStream(PropertiesManager.MENU_COLUMN_DROPDOWN_PROPERTIES_FILE_PATH);
        dropdownProps.load(dropdownInput);

        Properties dropdownListProps = new Properties();
        FileInputStream dropdownListInput = new FileInputStream(PropertiesManager.DROPDOWN_OPTIONS_PROPERTIES_FILE_PATH);
        dropdownListProps.load(dropdownListInput);

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
//                if(columnName.contains("*"))
//                    label.setForeground(new Color(255, 50,50));
//                else
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
                    comboBox.setBorder(new EmptyBorder(5, 5, 5, 5));
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
            }
        }
        database.closeResources();
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == createButton){
            InsertQueryBuilder qb = new InsertQueryBuilder();
            qb.insertInto("job_board");
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
                        System.out.println("Empty required field: "+labelText);
                        missingFields += labelText + ", ";
                    }
                    else {
                        qb.setColumns(labelText);
                        qb.setValues(str);
                    }
                }
                else if(!str.isBlank()){
                    qb.setColumns(labelText);
                    qb.setValues(str);
                }
            }
            try {
                if(notEmpty) {
                    System.out.println("all required fields filled");
                    database.sendUpdate(qb.build());
                    dispose();
                }
                else
                    JOptionPane.showMessageDialog(null, "You have empty required values: "+missingFields, null, JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                String userMessage = ex.getMessage();
                if(userMessage.toLowerCase().contains("duplicate") && userMessage.toLowerCase().contains("key")){
                    userMessage = "A job with this JWO already exists.";
                }
                System.out.println(userMessage);
                JOptionPane.showMessageDialog(this, userMessage, null, JOptionPane.ERROR_MESSAGE);
            }finally {
                database.closeResources();
            }
        }
        if(e.getSource() == cancelButton){
            dispose();
        }
    }
}
