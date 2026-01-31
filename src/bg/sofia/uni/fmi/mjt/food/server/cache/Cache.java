package bg.sofia.uni.fmi.mjt.food.server.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Cache {
    private static final Path ROOT = Path.of("cache");
    private static final Path REPORTS_DIR = ROOT.resolve("reports");
    private static final Path BARCODES_DIR = ROOT.resolve("barcodes");
    private static final String JSON_EXTENSION = ".json";

    public Cache() {
        try {
            Files.createDirectories(ROOT);
            Files.createDirectories(REPORTS_DIR);
            Files.createDirectories(BARCODES_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Could not create cache directories", e);
        }
    }

    public void saveReport(int id, String json) {
        Path reportPath = REPORTS_DIR.resolve(id + JSON_EXTENSION);
        try (Writer writer = new BufferedWriter(new FileWriter(reportPath.toFile()))) {
            writer.write(json);
        } catch (IOException e) {
            throw new RuntimeException("Could not save report to cache", e);
        }
    }

    public String loadReport(int id) {
        Path reportPath = REPORTS_DIR.resolve(id + JSON_EXTENSION);
        if (!Files.exists(reportPath)) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(reportPath.toFile()))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            return json.toString();

        } catch (IOException e) {
            throw new RuntimeException("Could not load report from cache", e);
        }
    }

    public void saveBarcode(String barcode, String json) {
        Path barcodePath = BARCODES_DIR.resolve(barcode + JSON_EXTENSION);
        try (Writer writer = new BufferedWriter(new FileWriter(barcodePath.toFile()))) {
            writer.write(json);
        } catch (IOException e) {
            throw new RuntimeException("Could not save barcode to cache", e);
        }
    }

    public String loadBarcode(String barcode) {
        Path barcodePath = BARCODES_DIR.resolve(barcode + JSON_EXTENSION);
        if (!Files.exists(barcodePath)) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(barcodePath.toFile()))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            return json.toString();

        } catch (IOException e) {
            throw new RuntimeException("Could not load barcode from cache", e);
        }
    }
}
