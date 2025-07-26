package DatabaseInteraction;

import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.postgresql.xa.PGXADataSource;

import java.sql.*;

public class DatabaseInteraction {

    private Connection connection;
    public static String URL = null;
    public static final String USERNAME = "postgres";
    public static final String PASSWORD = "0000";
    private ResultSet rs = null;
    private PreparedStatement ps = null;

    public static void setUrl(String url){
        URL = url;
    }

    public void createConnection(){
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            //System.out.println("Connection.isValid(0) = " + connection.isValid(0));
        } catch (SQLException ex){
            ex.printStackTrace();
            closeConnection();
        }
    }
    public void closeConnection(){
        try {
            connection.close();
        }catch (SQLException ex) {
            ex.printStackTrace();
        }
        //System.out.println("Connection Closed");
    }
    public void closeResources() {
        try { rs.close(); } catch (Exception ex) { /* Ignored */ }
        try { ps.close(); } catch (Exception ex) { /* Ignored */ }
        //System.out.println("Resources Closed");
        closeConnection();
    }
    public ResultSet sendSelect(String query) {
        createConnection();
        try {
            ps = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = ps.executeQuery();
        } catch (SQLException e) {
            closeResources();
            e.printStackTrace();
        }finally {
            //System.out.println("QueryInteracter(select): "+query);
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
            throw e;
        }finally {
            try { ps.close(); } catch (Exception ex) {/*Ignored*/}
        }

        //System.out.println("QueryInteracter(update): "+query);
    }
}
