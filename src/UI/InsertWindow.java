package UI;

import DatabaseInteraction.DatabaseInteraction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class InsertWindow extends JFrame {

    JPanel containerPanel;

    public InsertWindow(DatabaseInteraction database)throws Exception{

        ResultSet rs = database.sendSelect("select * from job_board");

        containerPanel = new JPanel();
        containerPanel.setLayout(new GridLayout(0,2));
        containerPanel.setBorder(new EmptyBorder(5,5,5,5));
        containerPanel.setBackground(new Color(24,24,24));
        createComponents(rs);

        this.add(containerPanel);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    private void createComponents(ResultSet rs) throws Exception{
        ResultSetMetaData rsMeta = rs.getMetaData();
        int colCount = rsMeta.getColumnCount();
        for(int i = 1; i <= colCount; i++){

            JLabel label = new JLabel(rsMeta.getColumnName(i));
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            label.setForeground(Color.white);

            JTextField textField = new JTextField();
            textField.setMinimumSize(new Dimension(100, 30));
            textField.setMaximumSize(new Dimension(100, 30));
            textField.setPreferredSize(new Dimension(100, 30));
            textField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            textField.setBackground(new Color(50,50,50));
            textField.setForeground(Color.white);
            textField.setCaretColor(Color.white);

            containerPanel.add(label);
            containerPanel.add(textField);
        }
    }

}
