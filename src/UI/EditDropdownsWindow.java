package UI;

import DatabaseInteraction.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.util.Properties;

public class EditDropdownsWindow extends JFrame implements ActionListener {

    private static Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 15);
    private static final String EDIT_DROPDOWNS_PASSWORD = "1991wood";
    private DatabaseInteraction database;
    private ResultSet jobBoardResultSet;
    private ArrayList<JPanel> fields;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel fieldsPanel;
    private JPanel buttonsPanel;
    private JButton confirmButton;
    private JButton cancelButton;
    private ArrayList<String> requiredCols;
    private String[] oldOptions;
    private JComboBox comboBox;
    private JPasswordField passwordText;
    private JLabel wrongPasswordLabel;

    public EditDropdownsWindow(JFrame owner){
        database = new DatabaseInteraction();
        initializeComponents();
        addFields();

        JLabel headingText = new JLabel("Edit Dropdowns", SwingConstants.CENTER);
        headingText.setForeground(new Color(200,40,40));
        headingText.setFont(new Font(headingText.getFont().getFontName(), Font.BOLD, 30));
        topPanel.add(headingText);

        buttonsPanel.add(confirmButton);
        buttonsPanel.add(cancelButton);

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
        this.setLocationRelativeTo(owner);
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
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBackground(new Color(40,40,40));
        fieldsPanel.setBorder(new EmptyBorder(5,5,5,5));

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsPanel.setBackground(new Color(40,40,40));

        confirmButton = new JButton("Confirm");
        confirmButton.setPreferredSize(new Dimension(100,40));
        confirmButton.setBackground(new Color(0, 0, 0));
        confirmButton.setForeground(new Color(255,255,255));
        confirmButton.setFont(BUTTON_FONT);
        confirmButton.setFocusable(false);
        confirmButton.addActionListener(this);

        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100,40));
        cancelButton.setBackground(new Color(0, 0, 0));
        cancelButton.setForeground(new Color(255,255,255));
        cancelButton.setFont(BUTTON_FONT);
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(this);

        fields = new ArrayList<>();

        JRootPane rootPane = this.getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "confirm");
        rootPane.getActionMap().put("confirm", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmButton.doClick(); // simulate button press
            }
        });
    }

    private void addFields() {

        ResultSet isDropdownResultSet = database.sendSelect("SELECT * FROM column_permissions WHERE has_dropdown = true OR menu_column_has_dropdown = true;");
        ArrayList<String> dropdownColumns = new ArrayList<>();
        dropdownColumns.add("");

        try {
            while (isDropdownResultSet.next()) {
                dropdownColumns.add(isDropdownResultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        Properties props = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream(PropertiesManager.DROPDOWN_OPTIONS_PROPERTIES_FILE_PATH);
            props.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(new Color(40, 40, 40));
        optionsPanel.setBorder(new EmptyBorder(10,0,10,0));

        JButton addOptionButton = new JButton("+");
        addOptionButton.setForeground(Color.GREEN);
        addOptionButton.setBackground(new Color(0,0,0));
        addOptionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addOptionButton.setPreferredSize(new Dimension(45, 30));
        addOptionButton.setFocusable(false);

        addOptionButton.addActionListener(e -> {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            panel.setBackground(new Color(40, 40, 40));
            panel.setBorder(new EmptyBorder(5,0,0,5));

            JButton removeButton = new JButton("-");
            removeButton.setForeground(Color.red);
            removeButton.setBackground(new Color(0,0,0));
            removeButton.setPreferredSize(new Dimension(45, 30));
            removeButton.setFocusable(false);

            removeButton.addActionListener(ev->{
                fields.remove(panel);
                optionsPanel.remove(panel);
                optionsPanel.revalidate();
                optionsPanel.repaint();
            });

            JTextField textField = new JTextField();
            textField.setPreferredSize(new Dimension(250, 30));
            textField.setCaretColor(Color.white);
            textField.setBackground(new Color(60, 60, 60));
            textField.setForeground(Color.WHITE);
            textField.setFont(new Font("SansSerif", Font.PLAIN, 20));
            textField.setBorder(new EmptyBorder(0, 5, 0, 0));
            textField.setAlignmentX(Component.LEFT_ALIGNMENT);

            panel.add(removeButton);
            panel.add(textField);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

            fields.add(panel);
            optionsPanel.add(panel, (optionsPanel.getComponentCount() - 1));
            optionsPanel.revalidate();
            optionsPanel.repaint();
        });

        comboBox = new JComboBox<>(dropdownColumns.toArray());
        //comboBox.setPreferredSize(new Dimension(200, 30));
        comboBox.setEditable(false);
        comboBox.setBackground(new Color(60, 60, 60));
        comboBox.setForeground(Color.WHITE);
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 20));
        comboBox.setBorder(new EmptyBorder(0, 0, 0, 0));
        comboBox.setMaximumRowCount(10);
        comboBox.addActionListener(e -> {
            fields.clear();
            String selected = (String)comboBox.getSelectedItem();
            if(selected.equals("mechanic"))
                selected = "worker";

            optionsPanel.removeAll();
            oldOptions = props.getProperty(selected+"_list.dropdown.options") != null ? props.getProperty(selected+"_list.dropdown.options").split(",") : new String[]{""};
            for(String s : oldOptions){
                if(!s.isBlank()){
                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
                    panel.setBackground(new Color(40, 40, 40));
                    panel.setBorder(new EmptyBorder(5,0,0,5));

                    JButton removeButton = new JButton("-");
                    removeButton.setForeground(Color.red);
                    removeButton.setBackground(new Color(0,0,0));
                    removeButton.setPreferredSize(new Dimension(45, 30));
                    removeButton.setFocusable(false);

                    removeButton.addActionListener(ev->{
                        fields.remove(panel);
                        optionsPanel.remove(panel);
                        optionsPanel.revalidate();
                        optionsPanel.repaint();
                    });

                    JTextField textField = new JTextField();
                    textField.setPreferredSize(new Dimension(250, 30));
                    textField.setCaretColor(Color.white);
                    textField.setBackground(new Color(60, 60, 60));
                    textField.setForeground(Color.WHITE);
                    textField.setFont(new Font("SansSerif", Font.PLAIN, 20));
                    textField.setBorder(new EmptyBorder(0, 5, 0, 0));
                    textField.setAlignmentX(Component.LEFT_ALIGNMENT);
                    textField.setText(s);

                    panel.add(removeButton);
                    panel.add(textField);
                    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

                    fields.add(panel);
                    optionsPanel.add(panel);
                }
            }
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            panel.setBackground(new Color(40, 40, 40));
            panel.setBorder(new EmptyBorder(5,0,0,5));
            panel.add(addOptionButton);
            optionsPanel.add(panel);
            optionsPanel.revalidate();
            optionsPanel.repaint();

        });



        JLabel headingLabel = new JLabel("Select a list to Edit");
        headingLabel.setForeground(Color.white);
        headingLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(optionsPanel);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(new EmptyBorder(0,0,0,0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);

        JLabel passwordLabel = new JLabel("Password: ");
        passwordLabel.setForeground(Color.white);
        passwordLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        passwordText = new JPasswordField();
        passwordText.setPreferredSize(new Dimension(200, 30));
        passwordText.setCaretColor(Color.white);
        passwordText.setBackground(new Color(60, 60, 60));
        passwordText.setForeground(Color.WHITE);
        passwordText.setFont(new Font("SansSerif", Font.PLAIN, 20));
        passwordText.setBorder(new EmptyBorder(5,5,5,5));
        passwordText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                wrongPasswordLabel.setVisible(false);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                wrongPasswordLabel.setVisible(false);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {}
        });

        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        passwordPanel.setBackground(new Color(40, 40, 40));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordText);

        wrongPasswordLabel = new JLabel("Wrong Password");
        wrongPasswordLabel.setForeground(new Color(200,40,40));
        wrongPasswordLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        wrongPasswordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wrongPasswordLabel.setVisible(false);


        fieldsPanel.add(headingLabel);
        fieldsPanel.add(comboBox);
        fieldsPanel.add(Box.createVerticalStrut(10));
        fieldsPanel.add(scrollPane);
        fieldsPanel.add(Box.createVerticalStrut(10));
        fieldsPanel.add(passwordPanel);
        fieldsPanel.add(Box.createVerticalStrut(10));
        fieldsPanel.add(wrongPasswordLabel);
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == confirmButton){

            String enteredPassword = new String(passwordText.getPassword());
            if(!enteredPassword.equals(EDIT_DROPDOWNS_PASSWORD)){
                wrongPasswordLabel.setVisible(true);
            }
            else {
                String selectedList = (String) comboBox.getSelectedItem();
                if(selectedList.isBlank()){
                    JOptionPane.showMessageDialog(this, "No list was selected to Edit. Select a list or cancel");
                    return;
                }

                ArrayList<String> newOptionsList = new ArrayList<>();
                for (JPanel p : fields) {
                    JTextField textField = (JTextField) p.getComponent(1);
                    String newOption = textField.getText();

                    for (String s : oldOptions) {
                        if (!s.equals(newOption) && !newOption.isBlank()) {
                            newOptionsList.add(newOption);
                            break;
                        }
                    }

                }


                if (selectedList.equals("mechanic"))
                    selectedList = "worker";

                if (!selectedList.isBlank()) {
                    try {
                        database.sendUpdate("delete from " + selectedList + "_list;");
                        InsertQueryBuilder qb = new InsertQueryBuilder();
                        qb.insertInto(selectedList + "_list");
                        for (String s : newOptionsList) {
                            qb.setValues(s);
                        }
                        database.sendUpdate(qb.build());
                        JOptionPane.showMessageDialog(this, selectedList + " dropdown options have been updated.");
                        dispose();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        if(e.getSource() == cancelButton){
            dispose();
        }
    }
}
