package Exceptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorWriting {

    public static void logError(Throwable e){
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("] ");
        sb.append(e.toString()).append("\n");

        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("    at ").append(element.toString()).append("\n");
        }
        sb.append("\n");

        try {
            Files.write(
                    Path.of("error.log"),
                    sb.toString().getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ioException) {
            // If even the logger fails, print to stderr
            ioException.printStackTrace();
        }
    }
}
