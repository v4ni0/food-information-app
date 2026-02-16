package bg.sofia.uni.fmi.mjt.food.client.parsing.barcode;

import bg.sofia.uni.fmi.mjt.food.client.parsing.barcode.BarcodeParser;
import bg.sofia.uni.fmi.mjt.food.exceptions.BarcodeParsingException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BarcodeParserTest {
    @Test
    void testParseInvalidBarcode() {
        String imagePath = Path.of("barcodeImages", "invalid").toAbsolutePath().toString();
        BarcodeParser parser = new BarcodeParser();
        assertThrows(BarcodeParsingException.class, () -> {
            BarcodeParser.parse(imagePath);
        });
    }

    @Test
    void testParseRealImageBarcodeFanta() throws Exception {
        String fantaImage = "D:/IntelliJ/java/Food Analyzer/barcodeImages/1e815412-a451-4d27-a3c3-a1555c1a8aba.jpg";
        String expectedBarcode = "5449000083128";
        String parsedBarcode = BarcodeParser.parse(fantaImage);
        assertEquals(expectedBarcode, parsedBarcode);

    }

    @Test
    void testParseRealImageBarcodeCola() throws Exception {
        String fantaImage = "D:/IntelliJ/java/Food Analyzer/barcodeImages/coca-cola.gif";
        String expectedBarcode = "490000289046";
        String parsedBarcode = BarcodeParser.parse(fantaImage);
        assertEquals(expectedBarcode, parsedBarcode);

    }

    @Test
    void testParseRealImageBarcodePepsi() throws Exception {
        String pepsiImage = "D:/IntelliJ/java/Food Analyzer/barcodeImages/pepsi.jpg";
        String expectedBarcode = "811572022082";
        String parsedBarcode = BarcodeParser.parse(pepsiImage);
        assertEquals(expectedBarcode, parsedBarcode);

    }


}