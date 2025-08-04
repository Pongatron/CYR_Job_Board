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

public class ArchiveJobWindow extends JFrame implements ActionListener {

    private MainWindow.JobBoardMode currentBoardMode;
    private static  Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 15);
    private static final String HIDE_PASSWORD = "1991wood";
    private DatabaseInteraction database;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel fieldsPanel;
    private JPanel buttonsPanel;
    private JButton confirmButton;
    private JButton cancelButton;
    private String selectedJwo;
    private JPasswordField passwordText;
    private JLabel wrongPasswordLabel;
    private JComboBox comboBox;

    public ArchiveJobWindow(JFrame owner, String jwo, MainWindow.JobBoardMode currentBoardMode){
        database = new DatabaseInteraction();
        selectedJwo = jwo;
        this.currentBoardMode = currentBoardMode;
        initializeComponents();

        JLabel headingText = new JLabel("", SwingConstants.CENTER);
        if(currentBoardMode == MainWindow.JobBoardMode.ACTIVE_JOBS){
            headingText.setText("Archive Job");
        }
        else{
            headingText.setText("Un-Archive Job");
        }

        headingText.setForeground(new Color(140,100,0));
        headingText.setFont(new Font(headingText.getFont().getFontName(), Font.BOLD, 30));
        topPanel.add(headingText);

        buttonsPanel.add(confirmButton);
        buttonsPanel.add(cancelButton);

        populateFieldsPanel();

        centerPanel.add(topPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(fieldsPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(buttonsPanel);

        this.add(centerPanel);


        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setTitle("Archive/Un-Archive Job");
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
        fieldsPanel.setLayout(new GridLayout(0, 1, 0,10));
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

        JRootPane rootPane = this.getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "confirm");
        rootPane.getActionMap().put("confirm", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmButton.doClick(); // simulate button press
            }
        });

    }

    public void populateFieldsPanel(){

        JLabel confirmationLabel = new JLabel("Set job as Active or Archived");
        confirmationLabel.setForeground(Color.white);
        confirmationLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        confirmationLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel jwoLabel = new JLabel("JWO: " + selectedJwo);
        jwoLabel.setForeground(Color.white);
        jwoLabel.setBackground(new Color(140,100,0));
        jwoLabel.setOpaque(true);
        jwoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        jwoLabel.setBorder(new EmptyBorder(0,0,0,0));
        jwoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel isActiveLabel = new JLabel("Status :");
        isActiveLabel.setForeground(Color.white);
        isActiveLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        isActiveLabel.setHorizontalAlignment(SwingConstants.CENTER);

        String[] options = new String[]{"Active", "Archived"};
        if(currentBoardMode == MainWindow.JobBoardMode.ARCHIVE){options = new String[]{"Archived", "Active"};}
        comboBox = new JComboBox<>(options);
        comboBox.setPreferredSize(new Dimension(200, 30));
        comboBox.setEditable(false);
        comboBox.setBackground(new Color(60, 60, 60));
        comboBox.setForeground(Color.WHITE);
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 20));
        comboBox.setBorder(new EmptyBorder(0, 5, 0, 0));
        comboBox.setMaximumRowCount(2);

        JPanel isActivePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        isActivePanel.setBackground(new Color(40, 40, 40));
        isActivePanel.add(isActiveLabel);
        isActivePanel.add(comboBox);

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

        fieldsPanel.add(confirmationLabel);
        fieldsPanel.add(jwoLabel);
        fieldsPanel.add(isActivePanel);
        if(currentBoardMode == MainWindow.JobBoardMode.ARCHIVE) {
            fieldsPanel.add(passwordPanel);
            fieldsPanel.add(wrongPasswordLabel);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == confirmButton){
            String selectedItem = comboBox.getSelectedItem().toString();
            String enteredPassword = new String(passwordText.getPassword());
            if(currentBoardMode == MainWindow.JobBoardMode.ARCHIVE && !enteredPassword.equals(HIDE_PASSWORD)){
                wrongPasswordLabel.setVisible(true);
            }
            else if(selectedItem.equals("Active") && currentBoardMode == MainWindow.JobBoardMode.ACTIVE_JOBS ||
                    selectedItem.equals("Archived") && currentBoardMode == MainWindow.JobBoardMode.ARCHIVE){
                JOptionPane.showMessageDialog(this, "Job Active status is unchanged. Set status or cancel", null, JOptionPane.ERROR_MESSAGE);
            }
            else{
                String activeStatus = "t";
                if(selectedItem.equals("Active")){
                    activeStatus = "t";
                }
                else{
                    activeStatus = "f";
                }

                UpdateQueryBuilder qb = new UpdateQueryBuilder();
                qb.updateTable("job_board");
                qb.setColNames("is_active");
                qb.setValues(activeStatus);
                qb.where("jwo = " + selectedJwo);
                try {
                    database.sendUpdate(qb.build());
                    JOptionPane.showMessageDialog(this, "JWO: "+ selectedJwo +" Active status changed to: "+selectedItem);
                    dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                    dispose();

                }
            }
        }

        if(e.getSource() == cancelButton){
            dispose();
        }

    }
}
