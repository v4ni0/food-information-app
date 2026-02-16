package bg.sofia.uni.fmi.mjt.food.client.logging;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class Logger {
    private static final Path LOG_DIR = Path.of("logs");
    private static Logger instance = new Logger("clientLogs.txt");
    private final Path logFile;

    private Logger(String fileName) {
        try {
            Files.createDirectories(LOG_DIR);
        } catch (IOException e) {
            System.err.println("Could not create log directory: " + e.getMessage());
        }
        this.logFile = LOG_DIR.resolve(fileName);
    }

    public static Logger getInstance() {
        return instance;
    }

    static Logger createWithCustomFileName(String fileName) {
        return new Logger(fileName);
    }

    private String stackTraceToString(Throwable t) {
        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter)) {
            t.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            System.err.println("Error while converting stack trace " + e.getMessage());
            return "Error while converting stack trace: " + e.getMessage();
        }
    }

    public synchronized void log(String message, Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(LocalDateTime.now()).append("--")
            .append(System.lineSeparator())
            .append(message)
            .append(System.lineSeparator());
        if (throwable != null) {
            sb.append(stackTraceToString(throwable)).append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        try {
            Files.writeString(logFile, sb.toString(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error while logging" + e.getMessage());
        }
    }
}