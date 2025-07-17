package UI;

import DatabaseInteraction.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class UpdateJobWindow extends JFrame implements ActionListener {

    DatabaseInteraction database;
    String[] requiredValues;
    ArrayList<JPanel> fields;
    JPanel topPanel;
    JPanel centerPanel;
    JPanel fieldsPanel;
    JPanel buttonsPanel;
    JButton updateButton;
    JButton resetButton;
    String selectedJwo;
    private static final Color UPDATE_PANEL_COLOR = new Color(40, 40, 40);

    public UpdateJobWindow(String jwo){
        database = new DatabaseInteraction();
        selectedJwo = jwo;
        initializeComponents();
        addFields();

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

    private void addFields() {
        ResultSet rs = null;

        try {
            rs = database.sendSelect("SELECT * FROM job_board where jwo = " + selectedJwo);
            ResultSetMetaData rsMeta = null;

            if(rs.next()) {
                rsMeta = rs.getMetaData();
                int colCount = rsMeta.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
                    String labelName = rsMeta.getColumnName(i);

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
                    text.setText(rs.getString(i));
                }
            }
            else{
                JOptionPane.showMessageDialog(this, "Error updating job: can't find jwo", null, JOptionPane.ERROR_MESSAGE);
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
            dispose();
        }
        if(e.getSource() == resetButton){
            for(JPanel p : fields){
                JTextField textField = (JTextField) p.getComponent(1);
                textField.setText(null);
            }
        }
    }
}
