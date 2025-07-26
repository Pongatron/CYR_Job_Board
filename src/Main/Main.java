package Main;

import DatabaseInteraction.DatabaseInteraction;
import DatabaseInteraction.PropertiesManager;
import UI.MainWindow;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception{

        File f = new File(PropertiesManager.USER_PREF_PROPERTIES_FILE_PATH);
        if(!f.exists())
            f.createNewFile();

        Properties props = new Properties();
        FileInputStream input = new FileInputStream(PropertiesManager.USER_PREF_PROPERTIES_FILE_PATH);
        props.load(input);

        if(!"t".equals(props.getProperty("database.url.set"))){

            JPanel panel = new JPanel();
            JLabel label = new JLabel("First time setup. Enter server address: ");
            JTextField textField = new JTextField(15);
            panel.add(label);
            panel.add(textField);

            int result = JOptionPane.showConfirmDialog(null, panel, "User Input", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            String url = "";
            if(result == JOptionPane.OK_OPTION){
                url = "jdbc:postgresql://" + textField.getText() + ":5432/postgres";
                try{
                    DatabaseInteraction.setUrl(url);
                    PropertiesManager.queryPermissions();
                    OutputStream output = new FileOutputStream(PropertiesManager.USER_PREF_PROPERTIES_FILE_PATH);
                    props.setProperty("database.url.set", "t");
                    props.setProperty("database.url", url);
                    props.setProperty("column.order", "jwo,customer,po_date,cust_po,job_name,del,due_date,shops_submit,shops_app,finish_sample_submit,finish_sample_app,in_shop,mechanic,sub,finishing,notes,build,finish,extra,install,is_active");
                    props.store(output, "User Preferences");
                }catch (Exception e){
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Unsuccessful");
                    System.exit(0);
                }
            }
            else{
                JOptionPane.showMessageDialog(null, "Unsuccessful");
                System.exit(0);
            }


        }
        else{
            DatabaseInteraction.setUrl(props.getProperty("database.url"));
        }

        PropertiesManager.queryPermissions();
        PropertiesManager.loadColumnPermissions();
        PropertiesManager.loadUserPreferences();
        PropertiesManager.loadColumnDropdowns();

        SwingUtilities.invokeLater(()->{
            try {
                new MainWindow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }

}
