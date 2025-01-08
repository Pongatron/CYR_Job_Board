package DatabaseInteraction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseInteraction {

    private Connection connection;
    private ResultSet resultSet;
    private ResultSetMetaData rsmd;
    private DatabaseMetaData dbmd;
    private ArrayList<String> tables;
    String tableNameQuery = "select table_name FROM information_schema.tables where table_schema='public';";

    public DatabaseInteraction(JTable jt){
        try {
            tables = new ArrayList<>();
            createConnection();

            //for(String s : tables)
                //System.out.println(s);

        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void createConnection(){
        try {
            String url = "jdbc:postgresql://localhost:5432/postgres";
            String username = "postgres";
            String password = "0000";
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connection.isValid(0) = " + connection.isValid(0));
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void closeConnection()throws Exception{
        connection.close();
        System.out.println("Connection closed");
    }

    public ResultSet interact(String query) throws Exception{
        PreparedStatement ps = connection.prepareStatement(query);

        ArrayList<String> list = new ArrayList<>();
        resultSet = ps.executeQuery();

        return resultSet;
    }

    public void getTables(){

    }

}
