package Main;

import DatabaseInteraction.DatabaseInteraction;
import DatabaseInteraction.PropertiesManager;
import UI.MainWindow;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import static DatabaseInteraction.PropertiesManager.APP_DATA_DIR;
import static DatabaseInteraction.PropertiesManager.queryPermissions;

public class Main {

    private static final String CURRENT_VERSION = "0.0.1";
    private static final String REPO = "Pongatron/CYR_Job_Board";

    public static void main(String[] args) throws Exception{

        if(isUpdateAvailable()){

        }
        else {
            // create a directory for properties files
            File dir = new File(APP_DATA_DIR);
            if (!dir.exists()) dir.mkdirs();
            try {
                PropertiesManager.loadUserPreferences();
                PropertiesManager.loadColumnPermissions();
                PropertiesManager.loadColumnDropdowns();

                SwingUtilities.invokeLater(() -> {
                    new MainWindow();
                });
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "A critical error has occured.\n" +
                        "Make sure column permissions have been set. Otherwise, uh oh");
                PropertiesManager.resetDatabaseConnectionStatus();
            }
        }

    }

    private static boolean isUpdateAvailable() throws Exception{
        String apiUrl = "https://api.github.com/repos/" + REPO + "/releases/latest";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/vnd.github+json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

        String latestTag = json.get("tag_name").getAsString().replaceFirst("^v", "");

        System.out.println("Current version: " + CURRENT_VERSION);
        System.out.println("Latest version: " + latestTag);

        return isNewerVersion(CURRENT_VERSION, latestTag);
    }

    private static boolean isNewerVersion(String current, String latest){
        String[] currentParts = current.split("\\.");
        String[] latestParts = latest.split("\\.");
        int length = Math.max(currentParts.length, latestParts.length);
        for(int i = 0; i < length; i++){
            int currVal = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            int latestVal = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
            if (latestVal > currVal) return true;
            if (latestVal < currVal) return false;
        }
        return false;
    }

    private static void runUpdate() throws Exception{
        String apiUrl = "https://api.github.com/repos/" + REPO + "/releases/latest";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/vnd.github+json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

        JsonArray assets = json.getAsJsonArray("assets");
        String downloadUrl = null;

        for(JsonElement elem : assets){
            JsonObject asset = elem.getAsJsonObject();
            String name = asset.get("name").getAsString();
            if(name.endsWith(".msi")){
                downloadUrl = asset.get("browser)download_url").getAsString();
                break;
            }
        }
        if(downloadUrl == null){
            System.err.println("No MSI installer found in latest release.");
            return;
        }

        System.out.println("Downloading MSI from: " + downloadUrl);

        Path tempMsi = Files.createTempFile("update", "msi");

        try(InputStream in = new URL(downloadUrl).openStream()){
            Files.copy(in, tempMsi, StandardCopyOption.REPLACE_EXISTING);
        }

        System.out.println("Downloaded MSI to: " + tempMsi);

        String command = "msiexec /i \"" + tempMsi.toAbsolutePath() + "\" REINSTALL=ALL REINSTALLMODE=vomus /qn";

        Process process = Runtime.getRuntime().exec(command);
        int exitCode = process.waitFor();
        System.out.println("Installer exited with code: " + exitCode);

    }

}
