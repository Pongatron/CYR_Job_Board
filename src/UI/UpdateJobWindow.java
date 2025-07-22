package UI;

import DatabaseInteraction.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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

public class UpdateJobWindow extends JFrame implements ActionListener {

    private DatabaseInteraction database;
    private ResultSet jobBoardResultSet;
    private ArrayList<JPanel> fields;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel fieldsPanel;
    private JPanel buttonsPanel;
    private JButton updateButton;
    private JButton resetButton;
    private String selectedJwo;
    private static final Color UPDATE_PANEL_COLOR = new Color(40, 40, 40);

    public UpdateJobWindow(DatabaseInteraction db, ResultSet rs,String jwo){
        database = db;
        jobBoardResultSet = rs;
        selectedJwo = jwo;
        initializeComponents();
        try {
            addFields();
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }

        JLabel headingText = new JLabel("Update Job", SwingConstants.CENTER);
        headingText.setForeground(new Color(0,100,180));
        headingText.setFont(new Font(headingText.getFont().getFontName(), Font.BOLD, 30));
        topPanel.add(headingText);

        buttonsPanel.add(updateButton);
        buttonsPanel.add(resetButton);

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
        updateButton.addActionListener(this);

        resetButton = new JButton("Reset");
        resetButton.setPreferredSize(new Dimension(100,40));
        resetButton.addActionListener(this);

        fields = new ArrayList<>();
    }

    private void addFields() throws IOException, SQLException {

        jobBoardResultSet.beforeFirst();
        ArrayList<String> requiredCols = new ArrayList<>();
        String[] colOrder = PropertiesManager.getColumnOrder();

        Properties requiredProps = new Properties();
        FileInputStream reqInput = new FileInputStream(PropertiesManager.COLUMN_REQUIRED_PROPERTIES_FILE_PATH);
        requiredProps.load(reqInput);

        Properties visibleProps = new Properties();
        FileInputStream visibleInput = new FileInputStream(PropertiesManager.MENU_COLUMN_VISIBLE_PROPERTIES_FILE_PATH);
        visibleProps.load(visibleInput);
        ResultSetMetaData rsMeta = null;

        while(jobBoardResultSet.next()) {
            if(!jobBoardResultSet.getObject("jwo").toString().equals(selectedJwo)){
                System.out.println("matching jwo");
                continue;
            }

            try {
                rsMeta = jobBoardResultSet.getMetaData();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


            for (String columnName : colOrder) {
                String requiredValue = requiredProps.getProperty(columnName);
                String visibleValue = visibleProps.getProperty(columnName);
                String requiredColName = columnName;
                if ("t".equals(visibleValue)) {
                    if ("t".equals(requiredValue)) {
                        requiredColName += "*";
                        requiredCols.add(columnName);
                    }
                    JLabel label = new JLabel(requiredColName);
                    label.setForeground(Color.white);
                    label.setFont(new Font("SansSerif", Font.BOLD, 20));

                    JTextField text = new JTextField();
                    text.setPreferredSize(new Dimension(200, 30));
                    text.setCaretColor(Color.white);
                    text.setBackground(new Color(60, 60, 60));
                    text.setForeground(Color.WHITE);
                    text.setFont(new Font("SansSerif", Font.PLAIN, 20));
                    text.setBorder(new EmptyBorder(5, 5, 5, 5));

                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
                    panel.setBackground(new Color(40, 40, 40));
                    panel.add(label);
                    panel.add(text);
                    fields.add(panel);
                    fieldsPanel.add(panel);
                    int databaseColIndex = jobBoardResultSet.findColumn(columnName);
                    Object value = jobBoardResultSet.getObject(databaseColIndex);
                    text.setText(value != null ? value.toString() : "");
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == updateButton){
            UpdateQueryBuilder qb = new UpdateQueryBuilder();
            qb.updateTable("job_board");
            for(JPanel p : fields){
                JLabel label = (JLabel) p.getComponent(0);
                JTextField textField = (JTextField) p.getComponent(1);
                if(!textField.getText().isBlank()){
                    qb.setColNames(label.getText().replace("*", ""));
                    qb.setValues(textField.getText());
                }
            }
            JTextField jwoField = (JTextField) fields.get(0).getComponent(1);
            qb.where("jwo = "+selectedJwo);
            try {
                database.sendUpdate(qb.build());
                dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, ex.getMessage(), null, JOptionPane.ERROR_MESSAGE);
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
