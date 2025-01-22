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

public class UpdateWindow extends JFrame implements ActionListener {

    DatabaseInteraction database;
    String[] requiredValues;
    ArrayList<JPanel> fields;
    JPanel centerPanel;
    JPanel fieldsPanel;
    JButton updateButton;
    JButton resetButton;
    String selectedJwo;

    public UpdateWindow(String jwo){
        database = new DatabaseInteraction();
        selectedJwo = jwo;
        initializeComponents();
        addFields();

        centerPanel.add(fieldsPanel);
        centerPanel.add(updateButton);
        centerPanel.add(resetButton);

        this.add(centerPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setTitle("Update Job");
        this.setPreferredSize(new Dimension(500, 1000));
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

        updateButton = new JButton("Update");
        updateButton.setPreferredSize(new Dimension(100,40));
        updateButton.addActionListener(this);

        resetButton = new JButton("Reset");
        resetButton.setPreferredSize(new Dimension(100,40));
        resetButton.addActionListener(this);

        fields = new ArrayList<>();
    }

    private void addFields() {
        ResultSet rs = database.sendSelect("SELECT * FROM job_board where jwo = " + selectedJwo);
        ResultSetMetaData rsMeta = null;
        try {
            rsMeta = rs.getMetaData();
            int colCount = rsMeta.getColumnCount();
            if(rs.next()) {
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
                    text.setText(rs.getString(i));
                }
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
        if(e.getSource() == updateButton){
            UpdateQueryBuilder qb = new UpdateQueryBuilder();
            qb.updateTable("job_board");
            for(JPanel p : fields){
                JLabel label = (JLabel) p.getComponent(0);
                JTextField textField = (JTextField) p.getComponent(1);
                if(!textField.getText().isBlank()){
                    qb.setColNames(label.getText());
                    qb.setValues(textField.getText());
                }
            }
            JTextField jwoField = (JTextField) fields.get(0).getComponent(1);
            qb.where("jwo = "+selectedJwo);
            try {
                database.sendUpdate(qb.build());
            } catch (SQLException ex) {
                ex.printStackTrace();
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
