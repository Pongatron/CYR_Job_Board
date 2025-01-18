package DatabaseInteraction;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseInteraction {

    private Connection connection;
    private ResultSet resultSet;
    private ResultSetMetaData rsmd;
    private DatabaseMetaData dbMeta;
    private ArrayList<String> tables;
    String tableNameQuery = "select table_name FROM information_schema.tables where table_schema='public';";
    String tableNameCountQuery = "select count(*) FROM information_schema.tables where table_schema='public';";

    public DatabaseInteraction(){
        try {
            tables = new ArrayList<>();
            createConnection();
            dbMeta = connection.getMetaData();
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

    public ResultSet sendSelect(String query) throws Exception{
        PreparedStatement ps = connection.prepareStatement(query);
        System.out.println("QueryInteracter(select): "+query);
        return ps.executeQuery();
    }
    public void sendUpdate(String query) throws Exception{
        PreparedStatement ps = connection.prepareStatement(query);
        System.out.println("QueryInteracter(update): "+query);
        ps.executeUpdate();
    }

    public String[] getTables() throws Exception {
        ResultSet rs = sendSelect(tableNameCountQuery);
        ResultSetMetaData rsMeta = rs.getMetaData();
        int tableCount = 0;
        while(rs.next()){
            tableCount = rs.getInt(1);
        }
        rs = sendSelect(tableNameQuery);
        rsMeta = rs.getMetaData();
        String[] colNameList = new String[tableCount+1];
        int i = 0;
        while(rs.next()){
            String str = rs.getString("table_name");
            colNameList[i] = str;
            i++;
        }

        return colNameList;
    }

    public ArrayList<String> getColumns(String colName)throws Exception{
        ArrayList<String> list = new ArrayList<>();
        ResultSet rs = sendSelect("select * from "+colName);
        ResultSetMetaData rsMeta = rs.getMetaData();
        int count = 1;
        while(count <= rsMeta.getColumnCount()){
            list.add(rsMeta.getColumnName(count));
//            System.out.println(rsMeta.getColumnName(count));
            count++;
        }
        return list;
    }


}
