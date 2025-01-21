package DatabaseInteraction;

import java.sql.*;

public class DatabaseInteraction {

    private Connection connection;
    private final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private final String USERNAME = "postgres";
    private final String PASSWORD = "0000";
    private ResultSet rs = null;
    private PreparedStatement ps = null;

    public void createConnection(){
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connection.isValid(0) = " + connection.isValid(0));
        } catch (SQLException ex){
            closeConnection();
            ex.printStackTrace();
        }
    }
    public void closeConnection(){
        try {
            connection.close();
        }catch (SQLException ex) {
            ex.printStackTrace();
        }
        System.out.println("Connection Closed");
    }
    public void closeResources() {
        try { rs.close(); } catch (Exception ex) { /* Ignored */ }
        try { ps.close(); } catch (Exception ex) { /* Ignored */ }
        System.out.println("Resources Closed");
        closeConnection();
    }
    public ResultSet sendSelect(String query) {
        createConnection();
        try {
            ps = connection.prepareStatement(query);
            rs = ps.executeQuery();
        } catch (SQLException e) {
            closeResources();
            e.printStackTrace();
        }finally {
            System.out.println("QueryInteracter(select): "+query);
        }
        return rs;
    }
    public void sendUpdate(String query) throws SQLException{
        createConnection();
        try {
            ps = connection.prepareStatement(query);
            ps.executeUpdate();
        } catch (SQLException e) {
            closeResources();
            e.printStackTrace();
        }finally {
            try { ps.close(); } catch (Exception ex) {/*Ignored*/}
        }

        System.out.println("QueryInteracter(update): "+query);
    }
}
