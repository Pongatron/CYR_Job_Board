package DatabaseInteraction;

import UI.ZoomManager;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PropertiesManager {
    public static final String APP_DATA_DIR = getAppDirectory() + File.separator + "config";
    public static final String VISIBILITY_PROPERTIES_FILE_PATH = APP_DATA_DIR + "/column-visibility.properties";
    public static final String CELL_DROPDOWN_PROPERTIES_FILE_PATH = APP_DATA_DIR + "/cell-column-dropdown.properties";
    public static final String CELL_EDITABLE_PROPERTIES_FILE_PATH = APP_DATA_DIR + "/cell-column-editable.properties";
    public static final String MENU_COLUMN_DROPDOWN_PROPERTIES_FILE_PATH = APP_DATA_DIR + "/menu-column-dropdown.properties";
    public static final String MENU_COLUMN_EDITABLE_PROPERTIES_FILE_PATH = APP_DATA_DIR + "/menu-column-editable.properties";
    public static final String MENU_COLUMN_VISIBLE_PROPERTIES_FILE_PATH = APP_DATA_DIR + "/menu-column-visible.properties";
    public static final String COLUMN_REQUIRED_PROPERTIES_FILE_PATH = APP_DATA_DIR + "/column-required.properties";
    public static final String USER_PREF_PROPERTIES_FILE_PATH = APP_DATA_DIR + "/user-preferences.properties";
    public static final String DROPDOWN_OPTIONS_PROPERTIES_FILE_PATH = APP_DATA_DIR + "/dropdown-options.properties";
    private static ResultSet resultSet;

    // helper class that stores information about a property file so it can be quickly iterated in loadColumnPermissions method
    private static class PropertyConfig {
        String filePath;
        int keyColIndex;
        int valueColIndex;
        String comment;
        public PropertyConfig(String filePath, int keyColIndex, int valueColIndex, String comment){
            this.filePath = filePath;
            this.keyColIndex = keyColIndex;
            this.valueColIndex = valueColIndex;
            this.comment = comment;
        }
    }

    // query database for all column permissions e.g. visibility, isDropdown, isEditable
    public static void queryPermissions() throws SQLException{
        DatabaseInteraction database = new DatabaseInteraction();
        SelectQueryBuilder qb = new SelectQueryBuilder();
        qb.select("*");
        qb.from("column_permissions");
        resultSet = database.sendSelect(qb.build());
    }

    // set the app data directory for all property files
    public static String getAppDirectory(){
        try {
            File jarFile = new File(PropertiesManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return jarFile.getParentFile().getAbsolutePath();
        } catch (URISyntaxException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return ".";
        }
    }

    // sets up the user preferences file if it didn't exist. otherwise just load from it
    public static void loadUserPreferences()  {
        File f = new File(USER_PREF_PROPERTIES_FILE_PATH);
        if(!f.exists()) {
            try {
                f.createNewFile();

                Properties props = new Properties();
                String columnOrderString = "jwo,customer,po_date,cust_po,job_name,del,due_date,shops_submit,shops_app,finish_sample_submit,finish_sample_app,in_shop,mechanic,sub,finishing,notes,build,finish,extra,install,is_active\n";
                String url = "";
                String zoom = "1.0";

                props.setProperty("column.order", columnOrderString);
                props.setProperty("database.url", url);
                props.setProperty("database.url.set", "f");
                props.setProperty("zoom", zoom);

                OutputStream output = new FileOutputStream(USER_PREF_PROPERTIES_FILE_PATH);
                props.store(output, "User Preferences");
            }catch (IOException e){
                JOptionPane.showMessageDialog(null, e.getMessage());
                System.exit(1);
            }
        }

        Properties props = new Properties();
        FileInputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(PropertiesManager.USER_PREF_PROPERTIES_FILE_PATH);
            props.load(input);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            e.printStackTrace();
        }
        // if propertied file says url isn't set create a window to type in the address of the database
        if(!"t".equals(props.getProperty("database.url.set"))){
            JPanel panel = new JPanel();
            JLabel label = new JLabel("First time setup. Enter server address: ");
            JTextField textField = new JTextField(15);
            panel.add(label);
            panel.add(textField);

            int result = JOptionPane.showConfirmDialog(null, panel, "User Input", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            String url = "";
            if(result == JOptionPane.OK_OPTION) {
                url = "jdbc:postgresql://" + textField.getText() + ":5432/postgres";
                DatabaseInteraction.setUrl(url);

                String columnOrderString = "jwo,customer,po_date,cust_po,job_name,del,due_date,shops_submit,shops_app,finish_sample_submit,finish_sample_app,in_shop,mechanic,sub,finishing,notes,build,finish,extra,install,is_active\n";
                String zoom = "1.0";

                props.setProperty("column.order", columnOrderString);
                props.setProperty("database.url", url);
                props.setProperty("database.url.set", "t");
                props.setProperty("zoom", zoom);
                try {
                    output = new FileOutputStream(USER_PREF_PROPERTIES_FILE_PATH);
                    props.store(output, "User Preferences");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage());
                    System.exit(1);
                }
            }
            else{
                JOptionPane.showMessageDialog(null, "Unsuccessful");
                System.exit(1);
            }
        }
        else{
            DatabaseInteraction.setUrl(props.getProperty("database.url"));
            ZoomManager.setZoom(Float.parseFloat(props.getProperty("zoom")));
        }
        try {
            PropertiesManager.queryPermissions();
        }catch (SQLException e){
            JOptionPane.showMessageDialog(null, "Unsuccessful");
            try {
                output = new FileOutputStream(USER_PREF_PROPERTIES_FILE_PATH);
                props.setProperty("database.url.set", "f");
                props.store(output, "User Preferences");
            } catch (IOException ex) {
                System.exit(1);
            }
            System.exit(1);
        }
    }

    public static void resetDatabaseConnectionStatus(){
        Properties props = new Properties();
        OutputStream output = null;
        try {
            output = new FileOutputStream(PropertiesManager.USER_PREF_PROPERTIES_FILE_PATH);
            props.setProperty("database.url.set", "f");
            props.store(output, "User Preferences");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            e.printStackTrace();
        }
    }

    // create and load properties files that have permissions of certain columns in the database e.g. visibility, isEditable, isDropdown
    public static void loadColumnPermissions(){
        int columnNameCol = 0;
        int isVisibleCol = 0;
        int hasDropdownCol = 0;
        int cellsEditableCol = 0;
        int menuHasDropdownCol = 0;
        int menuEditableCol = 0;
        int menuVisibleCol = 0;
        int isRequiredCol = 0;
        try {
            columnNameCol = resultSet.findColumn("column_name");
            isVisibleCol = resultSet.findColumn("is_visible");
            hasDropdownCol = resultSet.findColumn("has_dropdown");
            cellsEditableCol = resultSet.findColumn("cells_editable");
            menuHasDropdownCol = resultSet.findColumn("menu_column_has_dropdown");
            menuEditableCol = resultSet.findColumn("menu_column_editable");
            menuVisibleCol = resultSet.findColumn("menu_column_visible");
            isRequiredCol = resultSet.findColumn("is_required");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            throw new RuntimeException(e);
        }

        List<PropertyConfig> configs = Arrays.asList(
                new PropertyConfig(VISIBILITY_PROPERTIES_FILE_PATH, columnNameCol, isVisibleCol, "Cell Column Visibility Properties"),
                new PropertyConfig(CELL_DROPDOWN_PROPERTIES_FILE_PATH, columnNameCol, hasDropdownCol, "Cell Dropdown Properties"),
                new PropertyConfig(CELL_EDITABLE_PROPERTIES_FILE_PATH, columnNameCol, cellsEditableCol, "Cell Editable Properties"),
                new PropertyConfig(MENU_COLUMN_DROPDOWN_PROPERTIES_FILE_PATH, columnNameCol, menuHasDropdownCol, "Menu Column Dropdown Properties"),
                new PropertyConfig(MENU_COLUMN_EDITABLE_PROPERTIES_FILE_PATH, columnNameCol, menuEditableCol, "Menu Column Editable Properties"),
                new PropertyConfig(MENU_COLUMN_VISIBLE_PROPERTIES_FILE_PATH, columnNameCol, menuVisibleCol, "Menu Column Visibility Properties"),
                new PropertyConfig(COLUMN_REQUIRED_PROPERTIES_FILE_PATH, columnNameCol, isRequiredCol, "Required Column Properties")
        );
        for(PropertyConfig config : configs){
            File f = new File(config.filePath);

            if(!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage());
                    System.exit(0);
                }
            }

            Properties props = new Properties();
            FileInputStream input = null;
            OutputStream output = null;
            try {
                input = new FileInputStream(config.filePath);
                props.load(input);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }

            boolean propertiesChanged = false;
            try {
                resultSet.beforeFirst();
                while (resultSet.next()) {
                    String key = resultSet.getString(config.keyColIndex);
                    String value = resultSet.getString(config.valueColIndex);
                    if (!value.equals(props.getProperty(key))) {
                        props.setProperty(key, value);
                        propertiesChanged = true;
                    }
                }
            }catch (SQLException e){
                JOptionPane.showMessageDialog(null, e.getMessage());
            }

            if(propertiesChanged){
                try {
                    output = new FileOutputStream(config.filePath);
                    props.store(output, config.comment);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage());
                }
            }
        }
    }

    public static void loadColumnDropdowns()  {
        File f = new File(DROPDOWN_OPTIONS_PROPERTIES_FILE_PATH);
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                System.exit(0);
            }
        }

        Properties props = new Properties();
        FileInputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(DROPDOWN_OPTIONS_PROPERTIES_FILE_PATH);
            props.load(input);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }


        try {
            DatabaseInteraction database = new DatabaseInteraction();
            ResultSet rsTables = database.sendSelect("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'");
            ArrayList<String> dropdownTablesList = new ArrayList<>();
            while (rsTables.next()) {
                String tableName = rsTables.getString(1);
                if (tableName.contains("list")) {
                    dropdownTablesList.add(tableName);
                }
            }

            for (String tableName : dropdownTablesList) {
                SelectQueryBuilder qb = new SelectQueryBuilder();
                qb.select("*");
                qb.from(tableName);
                ResultSet rs = database.sendSelect(qb.build());

                String dropdownOptions = "";
                while (rs.next()) {
                    dropdownOptions += "," + rs.getString(1);
                }
                props.setProperty(tableName + ".dropdown.options", dropdownOptions);
            }
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        try {
            output = new FileOutputStream(DROPDOWN_OPTIONS_PROPERTIES_FILE_PATH);
            props.store(output, "Dropdown Options Lists");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }


    public static void saveUserPreferences(){
        Properties props = new Properties();
        try(FileInputStream input = new FileInputStream(USER_PREF_PROPERTIES_FILE_PATH)){
            props.load(input);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        try(FileOutputStream output = new FileOutputStream(USER_PREF_PROPERTIES_FILE_PATH)){
            props.setProperty("zoom", String.valueOf(ZoomManager.getZoom()));
            props.store(output, "User Preferences");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }


    public static String[] getColumnOrder() {
        Properties prop = new Properties();
        try(FileInputStream input = new FileInputStream(PropertiesManager.USER_PREF_PROPERTIES_FILE_PATH)){
            prop.load(input);

            String columnOrderString = prop.getProperty("column.order");
            String[] orderedColumnNames = columnOrderString.split(",");
            return orderedColumnNames;
        } catch (IOException e){
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

}
