package DatabaseInteraction;

import java.io.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Properties;

public class PropertiesManager {

    public static final String PROPERTIES_FILE_PATH = "resources/column-visibility.properties";

    public static ArrayList<Boolean> ReadColumnVisibility(ArrayList<String> columnNames){
        Properties props = new Properties();
        ArrayList<Boolean> columnVisible = new ArrayList<>();
        try{
            InputStream input = PropertiesManager.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_PATH);
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

    public static void checkColumns() throws Exception{

        DatabaseInteraction database = new DatabaseInteraction();
        SelectQueryBuilder qb = new SelectQueryBuilder();
        qb.select("*");
        qb.from("job_board");

        Properties props = new Properties();
        InputStream input = new FileInputStream(PROPERTIES_FILE_PATH);
        props.load(input);

        boolean propertiesChanged = false;

        ResultSet rs = database.sendSelect(qb.build());
        ResultSetMetaData rsMeta = rs.getMetaData();
        for(int i = 1; i <= rsMeta.getColumnCount(); i++){
            String colName = rsMeta.getColumnName(i);
            if(!props.containsKey(colName)){
                props.setProperty(colName, "true");
                propertiesChanged = true;
                System.out.println("property set");
            }
        }

        if(propertiesChanged){
            OutputStream output = new FileOutputStream(PROPERTIES_FILE_PATH);
            props.store(output, "column visibility properties");
            System.out.println("properties added");
        }
    }

    public static String[] getColumnOrder() {
        Properties prop = new Properties();
        try(FileInputStream input = new FileInputStream(PropertiesManager.PROPERTIES_FILE_PATH)){
            prop.load(input);

            String columnOrderString = prop.getProperty("column.order");
            String[] orderedColumnNames = columnOrderString.split(",");
            return orderedColumnNames;

        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static String getKeyValue(String key){
        Properties prop = new Properties();
        String value = "true";
        try(FileInputStream input = new FileInputStream(PropertiesManager.PROPERTIES_FILE_PATH)){
            prop.load(input);
            value = prop.getProperty(key);
            System.out.println(value);
            return value;
        } catch (IOException e){
            e.printStackTrace();
        }
        return value;
    }

}
