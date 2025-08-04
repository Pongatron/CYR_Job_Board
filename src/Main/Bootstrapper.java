package Main;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Bootstrapper {
    public static final String APP_DATA_DIR = System.getProperty("user.home") + File.separator + ".CYR_Job_Board";
    private static final String REPO_OWNER = "Pongatron";
    private static final String REPO_NAME = "CYR_Job_Board";
    // Use File.separator for cross-platform compatibility
    private static final String VERSION_FILE = APP_DATA_DIR + File.separator + "version.txt";
    private static final String MAIN_APP_PATH = "C:\\Program Files\\CYR_Job_Board\\CYR_Job_Board.exe";
    private static final Path MSI_DOWNLOAD_PATH = Paths.get( APP_DATA_DIR + File.separator + "update.msi");

    private static final String GITHUB_TOKEN = "";

    public static void main(String[] args) {

        File dir = new File(APP_DATA_DIR);
        if (!dir.exists()) dir.mkdirs();

        try {
            System.out.println("[Bootstrapper] Checking for updates...");

            String currentVersion = readLocalVersion();

            System.out.println(currentVersion);
            JsonObject releaseDetails = fetchLatestMainAppRelease();

            if (releaseDetails == null) {
                System.err.println("[Bootstrapper] Could not retrieve release details. Launching existing app.");
                launchMainApp();
                return;
            }

            String latestVersion = releaseDetails.get("tag_name").getAsString();
            System.out.println(latestVersion);

            if (!currentVersion.equals(latestVersion) && latestVersion.contains("Main-App-")) {
                System.out.println("[Bootstrapper] Update found: " + latestVersion);

                int result = JOptionPane.showConfirmDialog(null, "A newer version was found. Do you want to install it?", null, JOptionPane.YES_NO_OPTION);
                if(result == JOptionPane.YES_OPTION) {

                    String msiUrl = getMsiDownloadUrlFromDetails(releaseDetails);
                    if (msiUrl == null) {
                        System.err.println("[Bootstrapper] No MSI found in release assets. Launching existing app.");
                        launchMainApp();
                        return;
                    }

                    downloadFile(msiUrl, MSI_DOWNLOAD_PATH);
                    runInstaller(MSI_DOWNLOAD_PATH);
                    writeLocalVersion(latestVersion);

                    System.out.println("[Bootstrapper] Deleting temporary update file...");
                    Files.deleteIfExists(MSI_DOWNLOAD_PATH);
                }
            } else {
                System.out.println("[Bootstrapper] Already up to date.");
            }

            launchMainApp();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[Bootstrapper] An error occurred. Launching existing app.");
            launchMainApp();
        }
    }

    private static JsonObject fetchLatestMainAppRelease() throws IOException, InterruptedException {
        String url = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/releases";
        HttpRequest request = null;
        if(!GITHUB_TOKEN.isBlank()){
            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("Authorization", "Bearer " + GITHUB_TOKEN)
                    .build();
        }else{
            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/vnd.github.v3+json")
                    .build();
        }


        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Failed to fetch release details. Status code: " + response.statusCode());
            return null;
        }

        JsonArray releases = JsonParser.parseString(response.body()).getAsJsonArray();

        for(JsonElement releaseElement : releases){
            JsonObject release = releaseElement.getAsJsonObject();
            String tagName = release.get("tag_name").getAsString();
            if(tagName.startsWith("Main-App-")){
                return release;
            }
        }

        return null;
    }

    private static String getMsiDownloadUrlFromDetails(JsonObject releaseDetails) {
        JsonArray assets = releaseDetails.getAsJsonArray("assets");
        for(JsonElement assetElement : assets){
            JsonObject asset = assetElement.getAsJsonObject();
            String name = asset.get("name").getAsString();
            if(name.endsWith(".msi")){
                return asset.get("browser_download_url").getAsString();
            }
        }
        return null;
    }

    private static boolean mainAppExists(){
        File mainApp = new File(MAIN_APP_PATH);
        return mainApp.exists();
    }

    private static String readLocalVersion(){
        Path versionPath = Path.of(VERSION_FILE);
        String defaultVersion = "Main-App-v0.0.0";

        System.out.println(mainAppExists());

        try {
            if (!mainAppExists() || !Files.exists(versionPath)) {
                Files.writeString(versionPath, defaultVersion, StandardOpenOption.CREATE);
                return defaultVersion;
            }

            return Files.readString(versionPath).trim();
        }catch (IOException e){
            System.err.println("[Bootstrapper] Failed to read or create version file.");
            e.printStackTrace();
            return defaultVersion;
        }

    }

    private static void writeLocalVersion(String version) throws IOException{
        Files.writeString(Path.of(VERSION_FILE), version, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void downloadFile(String url, Path outputPath) throws IOException, InterruptedException {
        System.out.println("[Bootstrapper] Downloading update from: " + url);

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();


        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(outputPath));

        if (response.statusCode() == 200) {
            System.out.println("[Bootstrapper] Download complete.");
        } else {
            throw new IOException("Failed to download MSI. HTTP " + response.statusCode());
        }
    }

    private static void runInstaller(Path msiPath) throws IOException, InterruptedException {
        System.out.println("[Bootstrapper] Running MSI installer...");

        ProcessBuilder builder = new ProcessBuilder("msiexec", "/i", msiPath.toAbsolutePath().toString(), "/passive", "/norestart");

        builder.inheritIO(); // Optional: shows installer UI in terminal

        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("MSI installation failed. Exit code: " + exitCode);
        }

        System.out.println("[Bootstrapper] Installation complete.");
    }

    private static void launchMainApp(){
        try {
            System.out.println("[Bootstrapper] Launching main app...");
            new ProcessBuilder(MAIN_APP_PATH).start();
        } catch (Exception e) {
            System.err.println("[Bootstrapper] Failed to launch main app.");
            e.printStackTrace();
        }
    }

}