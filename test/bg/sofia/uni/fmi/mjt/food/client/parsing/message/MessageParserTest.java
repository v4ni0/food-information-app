package bg.sofia.uni.fmi.mjt.food.client.parsing.message;

import bg.sofia.uni.fmi.mjt.food.exceptions.BarcodeParsingException;
import bg.sofia.uni.fmi.mjt.food.exceptions.InvalidClientMessageException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MessageParserTest {

    @Test
    void testParseMessageWithNullThrows() {
        assertThrows(InvalidClientMessageException.class, () -> MessageParser.parse(null),
            "Should throw when message is null");
    }

    @Test
    void testParseMessageWithBlankThrows() {
        assertThrows(InvalidClientMessageException.class, () -> MessageParser.parse("   "),
            "Should throw when message is blank");
    }

    @Test
    void testParseMessageWithEmptyThrows() {
        assertThrows(InvalidClientMessageException.class, () -> MessageParser.parse(""),
            "Should throw when message is empty");
    }

    @Test
    void testParseMessageNonBarcodeCommandReturnsUnchanged() throws Exception {
        String message = "get-food-report 2543615";
        String result = MessageParser.parse(message);
        assertEquals(message, result, "Non-barcode commands should be returned unchanged");
    }

    @Test
    void testParseMessageGetFoodReportReturnsUnchanged() throws Exception {
        String message = "get-food-report 415269";
        String result = MessageParser.parse(message);
        assertEquals(message, result, "get-food-report command should be returned unchanged");
    }

    @Test
    void testParseMessageWithCodePrefixReturnsUnchanged() throws Exception {
        String message = "get-food-by-barcode --code=009800146130";
        String result = MessageParser.parse(message);
        assertEquals(message, result, "Message with --code= should be returned unchanged");
    }

    @Test
    void testParseMessageWithCodePrefixIgnoresImg() throws Exception {
        String message = "get-food-by-barcode   --img=D:/IntelliJ/java/Food Analyzer/barcodeImages/pepsi.jpg";
        String result = MessageParser.parse(message);
        String expected = "get-food-by-barcode --code=811572022082";
        assertEquals(expected, result, "Message with img should turn path to code");
    }
    @Test
    void testParseMessageWithImgPrefix() throws Exception {
        String imagePath = Path.of("barcodeImages", "barcode.gif").toAbsolutePath().toString();
        String message = "get-food-by-barcode --code=009800146130 --img=" + imagePath;
        String result = MessageParser.parse(message);
        assertEquals(message, result, "When --code= is present, --img= should be ignored");
    }

    @Test
    void testParseMessageNoCodeAndNoBarcodePrefixThrows() throws Exception {
        String message = "get-food-by-barcode ";
        assertThrows(InvalidClientMessageException.class, () -> MessageParser.parse(message),
            "Should throw when get-food-by-barcode is missing both --code= and --img=");
    }

    @Test
    void testParseMessageWithInvalidImagePathThrows() {
        String imagePath = Path.of("barcodeImages", "invalid").toAbsolutePath().toString();
        String message = "get-food-by-barcode --img=" + imagePath;

        assertThrows(BarcodeParsingException.class, () -> MessageParser.parse(message),
            "Should throw when barcode cannot be parsed from image");
    }

    @Test
    void testParseMessageCaseInsensitiveCommandCheck() throws Exception {
        String message = "GET-FOOD pizza";
        String result = MessageParser.parse(message);
        assertEquals(message, result, "Command check should be case-sensitive (only exact match)");
    }

}