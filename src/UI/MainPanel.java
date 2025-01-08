package UI;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {

    public MainPanel(){

        JLabel label = new JLabel("Gambit prime 2");

        this.setPreferredSize(new Dimension(100, 100));

        this.setLayout(null);
        this.add(label);
        this.setOpaque(true);
        this.setVisible(true);


    }

}
