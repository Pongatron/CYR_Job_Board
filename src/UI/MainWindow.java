package UI;

import javax.swing.*;

public class MainWindow extends JFrame {

    public MainWindow(){

        MainPanel panel = new MainPanel();

        this.add(panel);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 500);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

}
