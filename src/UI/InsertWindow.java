package UI;

import DatabaseInteraction.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class InsertWindow extends JFrame implements ActionListener {

    DatabaseInteraction database;
    String[] requiredValues;
    ArrayList<JPanel> fields;
    JPanel centerPanel;
    JPanel fieldsPanel;
    JButton createButton;
    JButton resetButton;

    public InsertWindow(){
        database = new DatabaseInteraction();
        initializeComponents();
        addFields();

        centerPanel.add(fieldsPanel);
        centerPanel.add(createButton);
        centerPanel.add(resetButton);

        this.add(centerPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setTitle("Add Job");
        this.setPreferredSize(new Dimension(500, 850));
        this.setBackground(new Color(24,24,24));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    private void initializeComponents() {

        centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout());
        centerPanel.setBackground(new Color(24,24,24));
        centerPanel.setBorder(new EmptyBorder(10,10,10,10));

        fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridLayout(0, 1, 10,10));
        fieldsPanel.setBackground(new Color(40,40,40));

        createButton = new JButton("Create");
        createButton.setPreferredSize(new Dimension(100,40));
        createButton.addActionListener(this);

        resetButton = new JButton("Reset");
        resetButton.setPreferredSize(new Dimension(100,40));
        resetButton.addActionListener(this);

        fields = new ArrayList<>();
    }

    private void addFields() {
        ResultSet rs = database.sendSelect("SELECT * FROM job_board");
        ResultSetMetaData rsMeta = null;
        try {
            rsMeta = rs.getMetaData();
            int colCount = rsMeta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                JLabel label = new JLabel(rsMeta.getColumnName(i));
                label.setForeground(Color.white);
                label.setFont(new Font("SansSerif", Font.BOLD, 20));
                JTextField text = new JTextField();
                text.setPreferredSize(new Dimension(200, 30));
                text.setCaretColor(Color.white);
                text.setBackground(new Color(60, 60, 60));
                text.setForeground(Color.WHITE);
                text.setFont(new Font("SansSerif", Font.PLAIN, 20));
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                panel.setBackground(new Color(40, 40, 40));
                panel.add(label);
                panel.add(text);
                fields.add(panel);
                fieldsPanel.add(panel);
            }
        }catch (SQLException e){
            database.closeResources();
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
            boolean notEmpty = true;
            for(JPanel p : fields){
                JTextField text = (JTextField) p.getComponent(1);
                String str = text.getText();
                if(!str.isBlank())
                    qb.values(str);
                else
                    notEmpty = false;
            }
            try {
                if(notEmpty)
                    database.sendUpdate(qb.build());
                else
                    JOptionPane.showMessageDialog(null, "You have empty values", null, JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                    ex.printStackTrace();
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
