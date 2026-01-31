package bg.sofia.uni.fmi.mjt.food.server.command;

import bg.sofia.uni.fmi.mjt.food.exceptions.InvalidClientMessageException;
import bg.sofia.uni.fmi.mjt.food.server.command.model.Command;
import bg.sofia.uni.fmi.mjt.food.server.command.model.Type;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {

    @Test
    void testParseGetFoodCommandWithSingleKeyword() throws InvalidClientMessageException {
        String input = "get-food pizza";
        Command command = CommandParser.parse(input);
        assertEquals(Type.GET_FOOD, command.type(), "command type should be GET_FOOD");
        assertIterableEquals(List.of("pizza"), command.keywords(), "keywords should be pizza");
    }

    @Test
    void testParseGetFoodCommandWithManyKeywords() throws InvalidClientMessageException {
        String input = "get-food   beef    noodle soup";
        Command command = CommandParser.parse(input);

        assertEquals(Type.GET_FOOD, command.type(), "command type should be GET_FOOD");
        assertIterableEquals(List.of("beef", "noodle", "soup"), command.keywords(),
            "keywords should be beef, noodle, soup");
    }

    @Test
    void testParseGetFoodCommandWithoutKeywordsThrows() {
        String input = "get-food  ";
        assertThrows(InvalidClientMessageException.class, () -> CommandParser.parse(input),
            "There should be at least one keyword");
    }

    @Test
    void testParseGetFoodReportCommandValid() throws InvalidClientMessageException {
        String input = "get-food-report 415269";
        Command command = CommandParser.parse(input);

        assertEquals(Type.GET_FOOD_REPORT, command.type(), "Command type should be GET_FOOD_REPORT");
        assertEquals(415269, command.id(), "ID should be 415269");
    }

    @Test
    void testParseGetFoodReportCommandWithNoNumberThrows() {
        String input = "get-food-report a";
        assertThrows(InvalidClientMessageException.class, () -> CommandParser.parse(input),
            "Should throw when id is not a number");
    }

    @Test
    void testParseGetFoodReportCommandWithoutIdThrows() {
        String input = "get-food-report   ";
        assertThrows(InvalidClientMessageException.class, () -> CommandParser.parse(input),
            "Should throw when id is missing");
    }

    @Test
    void testParseGetFoodReportCommandWithMultipleArgumentsOnlyFirstCounts() throws InvalidClientMessageException {
        String input = "get-food-report 415269 1";
        Command command = CommandParser.parse(input);
        assertEquals(Type.GET_FOOD_REPORT, command.type(), "Command type should be GET_FOOD_REPORT");
        assertEquals(415269, command.id(), "ID should be 415269");
    }

    @Test
    void testParseGetFoodByBarcodeCommandValid() throws InvalidClientMessageException {
        String input = "get-food-by-barcode   --code=009800146130";
        Command command = CommandParser.parse(input);

        assertEquals(Type.GET_FOOD_BY_BARCODE, command.type(),
            "Command type should be GET_FOOD_BY_BARCODE");
        assertEquals("009800146130", command.barcode(), "barcode should be 009800146130");
    }


    @Test
    void testParseGetFoodByBarcodeCommandWithoutBarcodeThrows() {
        String input = "get-food-by-barcode";
        assertThrows(InvalidClientMessageException.class, () -> CommandParser.parse(input),
            "Should throw when barcode is missing");
    }

    @Test
    void testParseGetFoodByBarcodeNoPrefixThrows() {
        String input = "get-food-by-barcode 009800146130";
        assertThrows(InvalidClientMessageException.class, () -> CommandParser.parse(input),
            "Should throw when there is no --code= prefix");
    }

    @Test
    void testParseNullStringThrows() {
        assertThrows(InvalidClientMessageException.class, () -> CommandParser.parse(null),
            "Should throw when input is null");
    }

    @Test
    void testParseBlankStringThrows() {
        String input = "   ";
        assertThrows(InvalidClientMessageException.class, () -> CommandParser.parse(input),
            "Should throw when input is blank");
    }

    @Test
    void testParseGetFoodByBarcodeCommandEmptyBarcodeValueThrows() {
        String input = "get-food-by-barcode --code=  ";
        assertThrows(InvalidClientMessageException.class, () -> CommandParser.parse(input),
            "Should throw when barcode is blank");
    }

    @Test
    void testParseCommandWithOnlyCommandName() {
        String input = "get-food-report";
        assertThrows(InvalidClientMessageException.class, () -> CommandParser.parse(input),
            "Should throw when only one argument is provided");
    }

    @Test
    void testParseCommandInvalidCommand() {
        String input = "command invalid";
        assertThrows(InvalidClientMessageException.class, () -> CommandParser.parse(input),
            "Should throw when non-existing command is provided");
    }
}