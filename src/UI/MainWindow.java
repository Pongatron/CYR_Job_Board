package UI;

import DatabaseInteraction.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class MainWindow extends JFrame implements ActionListener{

    JButton searchButton;
    DatabaseInteraction db;
    JPanel centerPanel;
    JLabel centerPanelLabel;
    JTable jt;
    JTextField textField;
    JTextField sqlTableField;

    public MainWindow() throws Exception{

        db = new DatabaseInteraction(jt);

        this.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();

        leftPanel.setBackground(Color.PINK);
        rightPanel.setBackground(Color.PINK);
        leftPanel.setPreferredSize(new Dimension(20, 100));
        rightPanel.setPreferredSize(new Dimension(20, 100));


        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.lightGray);
        topPanel.setPreferredSize(new Dimension(50, 80));

        searchButton = new JButton("Search");
        searchButton.setSize(100, 20);
        searchButton.setFocusable(false);
        searchButton.addActionListener(this);

        sqlTableField = new JTextField();
        sqlTableField.setPreferredSize(new Dimension(80, 25));
        sqlTableField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try{
                    warn();
                } catch (Exception ex) {
                    System.out.println(e.toString());
                }            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                try{
                    warn();
                } catch (Exception ex) {
                    System.out.println(e.toString());
                }            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                try{
                    warn();
                } catch (Exception ex) {
                    System.out.println(e.toString());                }
            }
            public void warn() throws Exception{

                    SelectQueryBuilder qb = new SelectQueryBuilder();
                    qb.select(sqlTableField.getText());
                    qb.from("jobs");
                    qb.where("job_name = \'desk\'");
                    setTable(db.interact(qb.build()));
            }
        });

        topPanel.add(sqlTableField);
        topPanel.add(searchButton);



        centerPanel = new JPanel();
        centerPanel.setBackground(Color.GRAY);
        centerPanel.setPreferredSize(new Dimension(500, 50));
        centerPanelLabel = new JLabel();



        jt = new JTable();
        JScrollPane js = new JScrollPane(jt);

        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(js);




        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.EAST);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(1000, 600));
        this.setMinimumSize(new Dimension(800, 600));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    private void setTable(ResultSet resultSet) throws SQLException {


        ResultSetMetaData rsmd = resultSet.getMetaData();

        DefaultTableModel tableModel = new DefaultTableModel();

        int colCount = rsmd.getColumnCount();

        for(int i = 1; i <= colCount; i++){
            tableModel.addColumn(rsmd.getColumnLabel(i));
        }

        Object[] row = new Object[colCount];

        while (resultSet.next()){
            for(int i = 0; i < colCount; i++){
                row[i] = resultSet.getObject(i + 1);
            }
            tableModel.addRow(row);
        }

        jt.setModel(tableModel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try{
            if(e.getSource() == searchButton){
                SelectQueryBuilder qb = new SelectQueryBuilder();
                qb.select("*");
                qb.from("jobs");
                //qb.where("job_name = \'desk\'");
                setTable(db.interact(qb.build()));
            }
        } catch(Exception ee){
            ee.printStackTrace();
        }

    }
}
