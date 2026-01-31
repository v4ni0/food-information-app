package bg.sofia.uni.fmi.mjt.food.client.message;

import bg.sofia.uni.fmi.mjt.food.exceptions.BarcodeParsingException;
import bg.sofia.uni.fmi.mjt.food.exceptions.InvalidClientMessageException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MessageParserTest {

    @Test
    void testParseMessageWithNullThrows() {
        assertThrows(InvalidClientMessageException.class, () -> MessageParser.parseMessage(null),
            "Should throw when message is null");
    }

    @Test
    void testParseMessageWithBlankThrows() {
        assertThrows(InvalidClientMessageException.class, () -> MessageParser.parseMessage("   "),
            "Should throw when message is blank");
    }

    @Test
    void testParseMessageWithEmptyThrows() {
        assertThrows(InvalidClientMessageException.class, () -> MessageParser.parseMessage(""),
            "Should throw when message is empty");
    }

    @Test
    void testParseMessageNonBarcodeCommandReturnsUnchanged() throws Exception {
        String message = "get-food-report 2543615";
        String result = MessageParser.parseMessage(message);
        assertEquals(message, result, "Non-barcode commands should be returned unchanged");
    }

    @Test
    void testParseMessageGetFoodReportReturnsUnchanged() throws Exception {
        String message = "get-food-report 415269";
        String result = MessageParser.parseMessage(message);
        assertEquals(message, result, "get-food-report command should be returned unchanged");
    }

    @Test
    void testParseMessageWithCodePrefixReturnsUnchanged() throws Exception {
        String message = "get-food-by-barcode --code=009800146130";
        String result = MessageParser.parseMessage(message);
        assertEquals(message, result, "Message with --code= should be returned unchanged");
    }

    @Test
    void testParseMessageWithCodePrefixIgnoresImg() throws Exception {
        String imagePath = Path.of("barcodeImages", "barcode.gif").toAbsolutePath().toString();
        String message = "get-food-by-barcode --code=009800146130 --img=" + imagePath;
        String result = MessageParser.parseMessage(message);
        assertEquals(message, result, "When --code= is present, --img= should be ignored");
    }

    @Test
    void testParseMessageWithValidImagePathConvertsToBarcode() throws Exception {
        String imagePath = Path.of("barcodeImages", "barcode.gif").toAbsolutePath().toString();
        String message = "get-food-by-barcode --img=" + imagePath;
        String result = MessageParser.parseMessage(message);

        assertTrue(result.startsWith("get-food-by-barcode --code="),
            "Result should start with 'get-food-by-barcode --code='");
        assertFalse(result.contains("--img="), "Result should not contain --img=");
        assertEquals("get-food-by-barcode --code=725272730706", result,
            "Should convert image path to barcode code");
    }

    @Test
    void testParseMessageWithInvalidImagePathThrows() {
        String imagePath = Path.of("barcodeImages", "invalid").toAbsolutePath().toString();
        String message = "get-food-by-barcode --img=" + imagePath;

        assertThrows(BarcodeParsingException.class, () -> MessageParser.parseMessage(message),
            "Should throw when barcode cannot be parsed from image");
    }

    @Test
    void testParseMessageWithoutPrefixReturnsUnchanged() throws Exception {
        String message = "get-food-by-barcode 009800146130";
        String result = MessageParser.parseMessage(message);
        assertEquals(message, result, "Message without prefix should be returned unchanged");
    }

    @Test
    void testParseMessageBarcodeCommandWithoutArgumentsReturnsUnchanged() throws Exception {
        String message = "get-food-by-barcode";
        String result = MessageParser.parseMessage(message);
        assertEquals(message, result, "Command without arguments should be returned unchanged");
    }

    @Test
    void testParseMessageWithExtraWhitespace() throws Exception {
        String imagePath = Path.of("barcodeImages", "barcode.gif").toAbsolutePath().toString();
        String message = "get-food-by-barcode   --img=" + imagePath;
        String result = MessageParser.parseMessage(message);

        assertEquals("get-food-by-barcode --code=725272730706", result,
            "Should handle extra whitespace correctly");
    }

    @Test
    void testParseMessageCaseInsensitiveCommandCheck() throws Exception {
        String message = "GET-FOOD pizza";
        String result = MessageParser.parseMessage(message);
        assertEquals(message, result, "Command check should be case-sensitive (only exact match)");
    }

    @Test
    void testParseMessagePartialCommandMatch() throws Exception {
        String message = "get-food-by-barcode-extra --img=test.gif";
        String result = MessageParser.parseMessage(message);
        assertEquals(message, result, "Should not match partial command names");
    }
}