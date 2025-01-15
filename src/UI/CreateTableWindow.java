package UI;

import DatabaseInteraction.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class CreateTableWindow extends JFrame implements ActionListener {

    private DatabaseInteraction database;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel tableNamePanel;
    private JPanel addRemovePanel;
    private JPanel insideCenter;

    private JLabel windowLabel;
    private JTextField tableNameField;
    private JButton addButton;
    private JButton removeButton;
    private JButton createButton;
    private JScrollPane scroll;

    private static final String[] DATATYPES = {"None", "Integer", "Decimal", "Characters", "Date"};
    private ArrayList<JPanel> panels = new ArrayList<>();

    public CreateTableWindow()throws Exception{
        database = new DatabaseInteraction();

        createComponents();

        JLabel nameLabel = new JLabel("Column Name:");
        nameLabel.setPreferredSize(new Dimension(100,30));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel typeLabel = new JLabel("Select Type:");
        typeLabel.setPreferredSize(new Dimension(100,30));
        typeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel labelPanel = new JPanel();
        labelPanel.add(nameLabel);
        labelPanel.add(typeLabel);
        insideCenter.add(labelPanel);

        tableNamePanel.add(new JLabel("Table Name: "));
        tableNamePanel.add(tableNameField);
        topPanel.add(windowLabel, BorderLayout.NORTH);
        topPanel.add(tableNamePanel, BorderLayout.CENTER);
        topPanel.add(addRemovePanel, BorderLayout.SOUTH);

        addRemovePanel.add(addButton);
        addRemovePanel.add(removeButton);
        addRemovePanel.add(createButton);
        centerPanel.add(scroll, BorderLayout.CENTER);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setMinimumSize(new Dimension(400, 200));
        this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void createComponents() {
        this.setLayout(new BorderLayout());

        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(Color.lightGray);
        topPanel.setPreferredSize(new Dimension(50,115));
        topPanel.setBorder(new EmptyBorder(10,10,10,10));

        centerPanel = new JPanel();
        centerPanel.setBackground(Color.darkGray);
        centerPanel.setPreferredSize(new Dimension(50,200));
        centerPanel.setLayout(new BorderLayout());

        insideCenter = new JPanel();
        insideCenter.setLayout(new BoxLayout(insideCenter, BoxLayout.Y_AXIS));

        windowLabel = new JLabel("CREATE TABLE:", SwingConstants.CENTER);

        tableNamePanel = new JPanel();

        addRemovePanel = new JPanel();

        tableNameField = new JTextField();
        tableNameField.setPreferredSize(new Dimension(100,25));
        tableNameField.setMinimumSize(new Dimension(100,25));

        addButton = new JButton("Add");
        addButton.addActionListener(this);
        addButton.setPreferredSize(new Dimension(100,30));
        addButton.setMaximumSize(new Dimension(100,30));

        removeButton = new JButton("Remove");
        removeButton.addActionListener(this);
        removeButton.setPreferredSize(new Dimension(100,30));
        removeButton.setMaximumSize(new Dimension(100,30));

        createButton = new JButton("Create");
        createButton.addActionListener(this);
        createButton.setPreferredSize(new Dimension(100,30));
        createButton.setMaximumSize(new Dimension(100,30));

        scroll = new JScrollPane(insideCenter);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    public void createRow(){

        JPanel panel = new JPanel();
        panel.setSize(new Dimension(200,30));

        JTextField colNameField = new JTextField();
        colNameField.setPreferredSize(new Dimension(100,30));

        JComboBox typeComboBox = new JComboBox(DATATYPES);
        typeComboBox.setPreferredSize(new Dimension(100,30));

        panel.add(colNameField);
        panel.add(typeComboBox);

        insideCenter.add(panel);
        insideCenter.revalidate();
        insideCenter.repaint();

        panels.add(panel);
    }

    public void removeRow(){
        insideCenter.remove(panels.removeLast());
        insideCenter.revalidate();
        insideCenter.repaint();
    }

    public boolean verifyTable(){
        if(tableNameField.getText().isEmpty() || tableNameField.getText().isBlank())
            return false;
        if(!panels.isEmpty()) {
            for (JPanel p : panels) {
                JTextField tx = (JTextField) p.getComponent(0);
                JComboBox cb = (JComboBox) p.getComponent(1);
                if (tx.getText().isEmpty() || tx.getText().isBlank() || cb.getSelectedItem().toString().equals("None")) {
                    return false;
                }
            }
            return true;
        }
        else
            return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == addButton){
            createRow();
        }
        if(e.getSource() == removeButton){
            if(!panels.isEmpty())
                removeRow();
        }
        if(e.getSource() == createButton){
            ArrayList<String> colNames = new ArrayList<>();
            ArrayList<String> datatypes = new ArrayList<>();
            for (JPanel p : panels){
                JTextField field = (JTextField) p.getComponent(0);
                JComboBox comboBox = (JComboBox) p.getComponent(1);
                String datatypeName = comboBox.getSelectedItem().toString();
                String parsedName = "";
                switch (datatypeName){
                    case "None":
                        System.out.println("bad boy");
                        break;
                    case "Integer":
                        parsedName = "int";
                        break;
                    case "Decimal":
                        parsedName = "float";
                        break;
                    case "Characters":
                        parsedName = "varchar(50)";
                        break;
                    case "Date":
                        parsedName = "date";
                        break;

                }
                colNames.add(field.getText());
                datatypes.add(parsedName);
            }

            if(verifyTable()) {

                int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to create a new Table?", null, JOptionPane.YES_NO_OPTION);

                if(choice == JOptionPane.YES_OPTION) {
                    DatabaseInteraction interact = new DatabaseInteraction();
                    CreateTableQueryBuilder qb = new CreateTableQueryBuilder(colNames, datatypes);
                    qb.createTable(tableNameField.getText());
                    try {
                        interact.sendUpdate(qb.build());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else{
                JOptionPane.showMessageDialog(null,"Invalid Table. Missing Parameters.");
            }
        }

    }
}
