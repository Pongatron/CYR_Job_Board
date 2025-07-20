package Main;

import DatabaseInteraction.PropertiesManager;
import UI.MainWindow;

public class Main {

    public static void main(String[] args) throws Exception{

        PropertiesManager.queryPermissions();
        PropertiesManager.loadColumnPermissions();

        new MainWindow();
    }

}
