package UI;

import DatabaseInteraction.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Properties;
import java.util.Set;

public class AddJobWindow extends JFrame implements ActionListener {

    private DatabaseInteraction database;
    private ResultSet jobBoardResultSet;
    private ArrayList<JPanel> fields;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel fieldsPanel;
    private JPanel buttonsPanel;
    private JButton createButton;
    private JButton resetButton;
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
        buttonsPanel.add(resetButton);

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
        createButton.addActionListener(this);

        resetButton = new JButton("Reset");
        resetButton.setPreferredSize(new Dimension(100,40));
        resetButton.addActionListener(this);

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

        for(String columnName : colOrder){
            String requiredValue = requiredProps.getProperty(columnName);
            String visibleValue = visibleProps.getProperty(columnName);
            if("t".equals(visibleValue)){
                if("t".equals(requiredValue)){
                    requiredCols.add(columnName);
                    columnName += "*";
                }
                JLabel label = new JLabel(columnName);
                label.setForeground(Color.white);
                label.setFont(new Font("SansSerif", Font.BOLD, 20));

                JTextField text = new JTextField();
                text.setPreferredSize(new Dimension(200, 30));
                text.setCaretColor(Color.white);
                text.setBackground(new Color(60, 60, 60));
                text.setForeground(Color.WHITE);
                text.setFont(new Font("SansSerif", Font.PLAIN, 20));
                text.setBorder(new EmptyBorder(5,5,5,5));

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
                JTextField text = (JTextField) p.getComponent(1);
                String str = text.getText();

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
            } catch (SQLException ex) {
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
        if(e.getSource() == resetButton){
            for(JPanel p : fields){
                JTextField textField = (JTextField) p.getComponent(1);
                textField.setText(null);
            }
        }
    }
}
