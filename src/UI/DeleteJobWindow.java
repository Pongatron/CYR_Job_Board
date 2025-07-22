package UI;

import DatabaseInteraction.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class DeleteJobWindow extends JFrame implements ActionListener {

    private static  Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 15);
    private static final String DELETE_PASSWORD = "password";
    private DatabaseInteraction database;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel fieldsPanel;
    private JPanel buttonsPanel;
    private JButton deleteJobButton;
    private JButton cancelButton;
    private String selectedJwo;
    private JTextField passwordText;
    private JLabel wrongPasswordLabel;

    public DeleteJobWindow(String jwo){
        database = new DatabaseInteraction();
        selectedJwo = jwo;
        initializeComponents();

        JLabel headingText = new JLabel("Delete Job", SwingConstants.CENTER);
        headingText.setForeground(new Color(200,40,40));
        headingText.setFont(new Font(headingText.getFont().getFontName(), Font.BOLD, 30));
        topPanel.add(headingText);

        buttonsPanel.add(deleteJobButton);
        buttonsPanel.add(cancelButton);

        populateFieldsPanel();

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
        fieldsPanel.setBorder(new EmptyBorder(5,5,5,5));

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsPanel.setBackground(new Color(40,40,40));

        deleteJobButton = new JButton("Delete");
        deleteJobButton.setPreferredSize(new Dimension(100,40));
        deleteJobButton.setBackground(new Color(0, 0, 0));
        deleteJobButton.setForeground(new Color(255,255,255));
        deleteJobButton.setFont(BUTTON_FONT);
        deleteJobButton.setFocusable(false);
        deleteJobButton.addActionListener(this);

        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100,40));
        cancelButton.setBackground(new Color(0, 0, 0));
        cancelButton.setForeground(new Color(255,255,255));
        cancelButton.setFont(BUTTON_FONT);
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(this);

    }

    public void populateFieldsPanel(){

        JLabel confirmationLabel = new JLabel("Are you sure you want to delete this job?");
        confirmationLabel.setForeground(Color.white);
        confirmationLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        confirmationLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel jwoLabel = new JLabel("JWO: " + selectedJwo);
        jwoLabel.setForeground(Color.white);
        jwoLabel.setBackground(new Color(140,100,0));
        jwoLabel.setOpaque(true);
        jwoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        jwoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel passwordLabel = new JLabel("Password: ");
        passwordLabel.setForeground(Color.white);
        passwordLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        passwordText = new JTextField();
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

        fieldsPanel.add(confirmationLabel);
        fieldsPanel.add(jwoLabel);
        fieldsPanel.add(passwordPanel);
        fieldsPanel.add(wrongPasswordLabel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == deleteJobButton){
            if(!passwordText.getText().toString().equals(DELETE_PASSWORD)){
                wrongPasswordLabel.setVisible(true);
            }
            else{
                UpdateQueryBuilder qb = new UpdateQueryBuilder();
                qb.updateTable("job_board");
                qb.setColNames("is_active");
                qb.setValues("false");
                qb.where("jwo = " + selectedJwo);
                qb.where("is_active = true");
                try {
                    database.sendUpdate(qb.build());
                    JOptionPane.showMessageDialog(this, "Job sent to Archive");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                dispose();
            }
        }

        if(e.getSource() == cancelButton){
            dispose();
        }

    }
}
