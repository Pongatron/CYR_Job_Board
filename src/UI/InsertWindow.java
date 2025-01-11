package UI;

import DatabaseInteraction.DatabaseInteraction;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;

public class InsertWindow extends JFrame {

    ArrayList<JLabel> labels;
    ArrayList<JTextField> fields;

    public InsertWindow(DatabaseInteraction database)throws Exception{

        labels = new ArrayList<>();
        fields = new ArrayList<>();
        ResultSet rs = database.sendSelect("select * from customers");
        createComponents(rs);

        for(int i = 0; i < labels.size(); i++){
            this.add(labels.get(i));
            this.add(fields.get(i));
        }


        this.setLayout(new GridLayout(labels.size(), 2, 5, 5));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //this.setPreferredSize(new Dimension(500, 200));
        this.setMinimumSize(new Dimension(500, 200));
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    private void createComponents(ResultSet rs) throws Exception{
        ResultSetMetaData rsMeta = rs.getMetaData();
        int colCount = rsMeta.getColumnCount();
        for(int i = 1; i <= colCount; i++){
            JLabel label = new JLabel(rsMeta.getColumnName(i));
            JTextField textField = new JTextField();
            textField.setMinimumSize(new Dimension(100, 30));
            labels.add(label);
            fields.add(textField);
        }
    }

}
