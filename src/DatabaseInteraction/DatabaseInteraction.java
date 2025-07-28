package DatabaseInteraction;

import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.postgresql.xa.PGXADataSource;

import javax.swing.*;
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
        } catch (SQLException ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
            closeConnection();
        }
    }
    public void closeConnection(){
        try {
            connection.close();
        }catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }
    public void closeResources() {
        try { rs.close(); } catch (Exception ex) { /* Ignored */ }
        try { ps.close(); } catch (Exception ex) { /* Ignored */ }
        closeConnection();
    }
    public ResultSet sendSelect(String query) {
        createConnection();
        try {
            ps = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = ps.executeQuery();
        } catch (SQLException ex) {
            closeResources();
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        return rs;
    }
    public void sendUpdate(String query) throws SQLException{
        createConnection();
        try {
            ps = connection.prepareStatement(query);
            ps.executeUpdate();
        } catch (SQLException ex) {
            closeResources();
            throw ex;
        }finally {
            try { ps.close(); } catch (Exception ex) {/*Ignored*/}
        }
    }
}
