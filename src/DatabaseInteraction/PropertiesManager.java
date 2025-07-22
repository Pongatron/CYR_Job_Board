package DatabaseInteraction;

import UI.ZoomManager;

import java.io.*;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PropertiesManager {
    public static final String VISIBILITY_PROPERTIES_FILE_PATH = "resources/column-visibility.properties";
    public static final String CELL_DROPDOWN_PROPERTIES_FILE_PATH = "resources/cell-column-dropdown.properties";
    public static final String CELL_EDITABLE_PROPERTIES_FILE_PATH = "resources/cell-column-editable.properties";
    public static final String MENU_COLUMN_DROPDOWN_PROPERTIES_FILE_PATH = "resources/menu-column-dropdown.properties";
    public static final String MENU_COLUMN_EDITABLE_PROPERTIES_FILE_PATH = "resources/menu-column-editable.properties";
    public static final String MENU_COLUMN_VISIBLE_PROPERTIES_FILE_PATH = "resources/menu-column-visible.properties";
    public static final String COLUMN_REQUIRED_PROPERTIES_FILE_PATH = "resources/column-required.properties";
    public static final String USER_PREF_PROPERTIES_FILE_PATH = "resources/user-preferences.properties";
    private static ResultSet resultSet;

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

    public static ArrayList<Boolean> ReadColumnVisibility(ArrayList<String> columnNames){
        Properties props = new Properties();
        ArrayList<Boolean> columnVisible = new ArrayList<>();
        try{
            InputStream input = PropertiesManager.class.getClassLoader().getResourceAsStream(VISIBILITY_PROPERTIES_FILE_PATH);
            props.load(input);

            for(String s : columnNames){
                System.out.println(s);
                String isVisible = props.getProperty(s);
                System.out.println(isVisible);
                if(isVisible == null)
                    isVisible = "true";
                if(isVisible.equals("false")){
                    columnVisible.add(false);
                }
                else{
                    columnVisible.add(true);
                }
            }


        } catch (Exception e){
            e.printStackTrace();
        }
        return columnVisible;
    }

    public static void WriteProperty(){
        Properties props = new Properties();
    }

    public static void queryPermissions(){
        DatabaseInteraction database = new DatabaseInteraction();
        SelectQueryBuilder qb = new SelectQueryBuilder();
        qb.select("*");
        qb.from("column_permissions");
        resultSet = database.sendSelect(qb.build());
    }

    public static void loadColumnPermissions() throws Exception{
        int columnNameCol = resultSet.findColumn("column_name");
        int isVisibleCol = resultSet.findColumn("is_visible");
        int hasDropdownCol = resultSet.findColumn("has_dropdown");
        int cellsEditableCol = resultSet.findColumn("cells_editable");
        int menuHasDropdownCol = resultSet.findColumn("menu_column_has_dropdown");
        int menuEditableCol = resultSet.findColumn("menu_column_editable");
        int menuVisibleCol = resultSet.findColumn("menu_column_visible");
        int isRequiredCol = resultSet.findColumn("is_required");

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
            Properties props = new Properties();
            FileInputStream input = new FileInputStream(config.filePath);
            boolean propertiesChanged = false;
            props.load(input);

            resultSet.beforeFirst();
            while(resultSet.next()){
                String key = resultSet.getString(config.keyColIndex);
                String value = resultSet.getString(config.valueColIndex);
                if(!value.equals(props.getProperty(key))){
                    props.setProperty(key, value);
                    propertiesChanged = true;
                }
            }
            input.close();
            if(propertiesChanged){
                OutputStream output = new FileOutputStream(config.filePath);
                props.store(output, config.comment);
                System.out.println("Updated: "+config.filePath);
                output.close();
            }
        }
    }

    public static void loadUserPreferences(){
        Properties props = new Properties();
        try(FileInputStream input = new FileInputStream(USER_PREF_PROPERTIES_FILE_PATH)){
            props.load(input);

            float zoomLevel = Float.parseFloat(props.getProperty("zoom", "1.0"));
            ZoomManager.setZoom(zoomLevel);

            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void saveUserPreferences(){
        Properties props = new Properties();
        try(FileInputStream input = new FileInputStream(USER_PREF_PROPERTIES_FILE_PATH)){
            props.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(FileOutputStream output = new FileOutputStream(USER_PREF_PROPERTIES_FILE_PATH)){
            props.setProperty("zoom", String.valueOf(ZoomManager.getZoom()));
            props.store(output, "User Preferences");

        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return null;
    }
    public static String getKeyValue(String key, String filename){
        Properties prop = new Properties();
        String value = "t";
        try(FileInputStream input = new FileInputStream(filename)){
            prop.load(input);
            value = prop.getProperty(key);
            //System.out.println(key+": "+value);
            return value;
        } catch (IOException e){
            e.printStackTrace();
        }
        return value;
    }

}
