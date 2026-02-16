package bg.sofia.uni.fmi.mjt.food.server.cache;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Cache {
    private final Path root;
    private final Path reportsDir;
    private final Path barcodesDir;
    private final Path keywordsDir;
    private static final String JSON_EXTENSION = ".json";
    private static Cache instance;

    private Cache(Path rootPath) throws IOException {
        this.root = rootPath;
        this.reportsDir = root.resolve("reports");
        this.barcodesDir = root.resolve("barcodes");
        this.keywordsDir = root.resolve("keywords");
        Files.createDirectories(root);
        Files.createDirectories(reportsDir);
        Files.createDirectories(barcodesDir);
        Files.createDirectories(keywordsDir);

    }

    public static synchronized Cache getInstance() throws IOException {
        if (instance == null) {
            instance = new Cache(Path.of("cache"));
        }
        return instance;
    }

    // Using this method only for testing purposes
    static Cache createWithCustomPath(Path rootPath) throws IOException {
        return new Cache(rootPath);
    }

    public synchronized void saveReport(int id, String json) throws IOException {
        Path reportPath = reportsDir.resolve(id + JSON_EXTENSION);
        try (Writer writer = new BufferedWriter(new FileWriter(reportPath.toFile()))) {
            writer.write(json);
        }
    }

    public synchronized String loadReport(int id) throws IOException {
        Path reportPath = reportsDir.resolve(id + JSON_EXTENSION);
        if (!Files.exists(reportPath)) {
            return null;
        }
        return Files.readString(reportPath);
    }

    public synchronized void saveByBarcode(String barcode, String json) throws IOException {
        Path barcodePath = barcodesDir.resolve(barcode + JSON_EXTENSION);
        try (Writer writer = new BufferedWriter(new FileWriter(barcodePath.toFile()))) {
            writer.write(json);
        }
    }

    public synchronized String loadBarcode(String barcode) throws IOException {
        Path barcodePath = barcodesDir.resolve(barcode + JSON_EXTENSION);
        if (!Files.exists(barcodePath)) {
            return null;
        }
        return Files.readString(barcodePath);
    }

    public synchronized void saveByKeywords(String keywords, String json) throws IOException {
        String key = keywords.replace(" ", "_");
        Path keywordsPath = keywordsDir.resolve(key + JSON_EXTENSION);
        try (Writer writer = new BufferedWriter(new FileWriter(keywordsPath.toFile()))) {
            writer.write(json);
        }
    }

    public synchronized String loadByKeywords(String keywords) throws IOException {
        String sanitizedKey = keywords.replace(" ", "_");
        Path keywordsPath = keywordsDir.resolve(sanitizedKey + JSON_EXTENSION);
        if (!Files.exists(keywordsPath)) {
            return null;
        }
        return Files.readString(keywordsPath);
    }
}
