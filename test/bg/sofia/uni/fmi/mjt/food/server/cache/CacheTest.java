package bg.sofia.uni.fmi.mjt.food.server.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CacheTest {

    @TempDir
    Path tempDir;

    private Cache cache;

    @BeforeEach
    void setUp() throws IOException {
        cache = Cache.createWithCustomPath(tempDir);
    }

    @Test
    void testSaveReportCreatesFile() throws IOException {
        int id = 11111;
        String json = """
            {"fdcId":11111}""";

        cache.saveReport(id, json);
        Path reportPath = tempDir.resolve("reports").resolve(id + ".json");
        assertTrue(Files.exists(reportPath), "Report file should be created");
    }

    @Test
    void testLoadReportCorrrect() throws IOException {
        int id = 12345;
        String json = """
            {
                "fdcId":12345,
                "description":"Test Food"
            }""";

        cache.saveReport(id, json);
        String loaded = cache.loadReport(id);

        assertEquals(json, loaded, "Loaded report should match saved report");
    }

    @Test
    void testLoadReportNotFound() throws IOException {
        int id = 1;
        String loaded = cache.loadReport(id);

        assertNull(loaded, "Loading non-existent report should return null");
    }

    @Test
    void testSaveAndLoadBarcode() throws IOException {
        String barcode = "111";
        String json = """
            {"fdcId":1,"description":"RAFFAELLO","gtinUpc":"111"}""";

        cache.saveByBarcode(barcode, json);
        String loaded = cache.loadBarcode(barcode);

        assertEquals(json, loaded, "Loaded barcode should match saved barcode");
    }

    @Test
    void testSaveBarcodeCreatesFile() throws IOException {
        String barcode = "123";
        String json = """
            {"gtinUpc":"123"}""";

        cache.saveByBarcode(barcode, json);

        Path barcodePath = tempDir.resolve("barcodes").resolve(barcode + ".json");
        assertTrue(Files.exists(barcodePath), "Barcode file should be created");
    }

    @Test
    void testLoadBarcodeNotFound() throws IOException {
        String barcode = "0";
        String loaded = cache.loadBarcode(barcode);

        assertNull(loaded, "Loading non-existent barcode should return null");
    }

    @Test
    void testSaveKeywordsCreatesFile() throws IOException {
        String keywords = "test food";
        String json = """
            {"foods":[]}""";

        cache.saveByKeywords(keywords, json);

        Path keywordsPath = tempDir.resolve("keywords").resolve("test_food.json");
        assertTrue(Files.exists(keywordsPath), "Keywords file should be created with sanitized name");
    }

    @Test
    void testLoadKeywordsCorrect() throws IOException {
        String keywords = "beef noodle soup";
        String json = """
            {"foods":[{"fdcId":123,"description":"Beef Noodle Soup"}]}""";

        cache.saveByKeywords(keywords, json);
        String loaded = cache.loadByKeywords(keywords);

        assertEquals(json, loaded, "Loaded keywords should match saved keywords");
    }

    @Test
    void testLoadKeywordsNotFoundNull() throws IOException {
        String nonExistentKeywords = "food";
        String loaded = cache.loadByKeywords(nonExistentKeywords);

        assertNull(loaded, "Non-existent keywords should return null");
    }

    @Test
    void testGetInstanceOnlyOneInstance() throws IOException {
        Cache instance1 = Cache.getInstance();
        Cache instance2 = Cache.getInstance();

        assertSame(instance1, instance2, "getInstance should return the same singleton instance");
    }

}

