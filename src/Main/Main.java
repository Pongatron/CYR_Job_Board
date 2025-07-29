package Main;

import DatabaseInteraction.DatabaseInteraction;
import DatabaseInteraction.PropertiesManager;
import UI.MainWindow;

import javax.swing.*;
import java.io.*;
import java.util.Properties;

import static DatabaseInteraction.PropertiesManager.APP_DATA_DIR;
import static DatabaseInteraction.PropertiesManager.queryPermissions;

public class Main {

    public static void main(String[] args) {

        // create a directory for properties files
        File dir = new File(APP_DATA_DIR);
        if (!dir.exists()) dir.mkdirs();
        try {
            PropertiesManager.loadUserPreferences();
            PropertiesManager.loadColumnPermissions();
            PropertiesManager.loadColumnDropdowns();

            SwingUtilities.invokeLater(()->{
                new MainWindow();
            });
        }catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "A critical error has occured.\n" +
                    "Make sure column permissions have been set. Otherwise, uh oh");
            PropertiesManager.resetDatabaseConnectionStatus();
        }

    }

}
