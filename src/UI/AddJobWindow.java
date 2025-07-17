package UI;

import DatabaseInteraction.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class AddJobWindow extends JFrame implements ActionListener {

    DatabaseInteraction database;
    String[] requiredValues;
    ArrayList<JPanel> fields;
    JPanel topPanel;
    JPanel centerPanel;
    JPanel fieldsPanel;
    JPanel buttonsPanel;
    JButton createButton;
    JButton resetButton;
    ArrayList<String> requiredCols;

    int widest = 0;
    int shortest = 0;

    public AddJobWindow(){
        database = new DatabaseInteraction();
        initializeComponents();
        addFields();

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

    private void addFields()  {
        ResultSet rsRequired = database.sendSelect("SELECT * FROM required_columns");
        requiredCols = new ArrayList<>();

        SelectQueryBuilder qb = new SelectQueryBuilder();
        try {

            while (rsRequired.next()) {
                requiredCols.add(rsRequired.getObject(1).toString());
            }
        }catch (SQLException ex){
            ex.printStackTrace();
        }

        ResultSet rs = null;
        try {
            rs = database.sendSelect("SELECT * FROM job_board");
            ResultSetMetaData rsMeta = null;

            rsMeta = rs.getMetaData();
            int colCount = rsMeta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                String labelName = rsMeta.getColumnName(i);

                for(String s : requiredCols){
                    if(s.equals(labelName))
                        labelName += "*";
                }

                JLabel label = new JLabel(labelName);
                label.setForeground(Color.white);
                label.setFont(new Font("SansSerif", Font.BOLD, 20));

                JTextField text = new JTextField();
                text.setPreferredSize(new Dimension(200, 30));
                text.setCaretColor(Color.white);
                text.setBackground(new Color(60, 60, 60));
                text.setForeground(Color.WHITE);
                text.setFont(new Font("SansSerif", Font.PLAIN, 20));

                JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
                panel.setBackground(new Color(40, 40, 40));
                panel.add(label);
                panel.add(text);
                fields.add(panel);
                fieldsPanel.add(panel);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            database.closeResources();
        }
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == createButton){
            InsertQueryBuilder qb = new InsertQueryBuilder();
            qb.insertInto("job_board");
//            for(String s : requiredCols) {
//                qb.setColumns(s);
//            }
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
