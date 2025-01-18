package Main;

import DatabaseInteraction.*;
import org.postgresql.util.PSQLException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

public class FirstTimeSetup extends JFrame implements ActionListener {

    DatabaseInteraction database;
    private JButton initializeButton;
    private ArrayList<String> colNames = new ArrayList<>(Arrays.asList("JWO","Customer","PO_Date","Cust_PO","Job_Name","Del","Start_Date","Shops_Submit","Shops_App"));
    private ArrayList<String> datatypes = new ArrayList<>(Arrays.asList("int","varchar(255)","date","varchar(255)","varchar(255)","varchar(255)","date","date","date"));

    public FirstTimeSetup(){
        database = new DatabaseInteraction();
        initializeComponents();

        this.add(initializeButton);
        this.setLayout(new FlowLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("First Time Setup");
        this.setPreferredSize(new Dimension(300, 100));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void initializeComponents(){
        initializeButton = new JButton("Initialize Table?");
        initializeButton.setFocusable(false);
        initializeButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == initializeButton){
            try{
                CreateTableQueryBuilder qb = new CreateTableQueryBuilder(colNames, datatypes);
                qb.nameTable("job_board");
                database.sendUpdate(qb.build());
                JOptionPane.showMessageDialog(null, "Table successfully created. You may close the window and open the regular program.","", JOptionPane.INFORMATION_MESSAGE);
            }
            catch (Exception ex){
                JOptionPane.showMessageDialog(null, "Table already exists. You may close this window and open the regular program.","", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    public static void main(String[] args) throws Exception{
        new FirstTimeSetup();
    }
}
