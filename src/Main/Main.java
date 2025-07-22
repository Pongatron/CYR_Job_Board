package Main;

import DatabaseInteraction.PropertiesManager;
import UI.MainWindow;

import javax.swing.*;

public class Main {

    public static void main(String[] args) throws Exception{

        PropertiesManager.queryPermissions();
        PropertiesManager.loadColumnPermissions();
        PropertiesManager.loadUserPreferences();
        PropertiesManager.loadColumnDropdowns();

        SwingUtilities.invokeLater(()->{
            try {
                new MainWindow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }

}
