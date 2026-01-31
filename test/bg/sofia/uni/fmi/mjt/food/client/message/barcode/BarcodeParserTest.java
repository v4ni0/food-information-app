package bg.sofia.uni.fmi.mjt.food.client.message.barcode;

import bg.sofia.uni.fmi.mjt.food.client.message.barcode.BarcodeParser;
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
            BarcodeParser.parseBarcode(imagePath);
        });
    }
    @Test
    void testParseValidBarcode() throws Exception {
        String barcode = "725272730706";
        String imagePath = Path.of("barcodeImages", "barcode.gif").toAbsolutePath().toString();
        BarcodeParser parser = new BarcodeParser();
        String parsedBarcode = BarcodeParser.parseBarcode(imagePath);
        assertEquals(barcode, parsedBarcode);
    }


}