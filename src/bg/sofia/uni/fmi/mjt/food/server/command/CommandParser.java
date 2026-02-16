package bg.sofia.uni.fmi.mjt.food.server.command;

import bg.sofia.uni.fmi.mjt.food.exceptions.InvalidClientMessageException;
import bg.sofia.uni.fmi.mjt.food.server.command.model.Command;
import bg.sofia.uni.fmi.mjt.food.server.command.model.Type;
import bg.sofia.uni.fmi.mjt.food.validation.Validator;

import java.util.Arrays;
import java.util.List;

public class CommandParser {
    private static final String BARCODE_PREFIX = "--code=";

    private static Type parseType(String command) throws InvalidClientMessageException {
        for (Type type : Type.values()) {
            if (type.getValue().equals(command)) {
                return type;
            }
        }
        throw new InvalidClientMessageException("Invalid command type");
    }

    private static Command parseGetFoodCommand(String... parts) throws InvalidClientMessageException {
        List<String> keywords = Arrays.stream(parts)
            .map(String::strip)
            .skip(1)
            .toList();

        Validator.validateKeywordsLength(keywords);

        return Command.builder(Type.GET_FOOD)
            .setKeywords(keywords)
            .build();
    }

    private static Command parseGetFoodReportCommand(String... parts) throws InvalidClientMessageException {
        Validator.validateArgumentsLength(parts);

        int id;
        try {
            id = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new InvalidClientMessageException("The id should be a valid integer", e);
        }

        return Command.builder(Type.GET_FOOD_REPORT)
            .setId(id)
            .build();
    }

    private static Command parseGetFoodByBarcodeCommand(String... parts) throws InvalidClientMessageException {
        String barcode;
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].startsWith(BARCODE_PREFIX)) {
                barcode = parts[i].substring(BARCODE_PREFIX.length()).strip();
                if (barcode.isBlank()) {
                    throw new InvalidClientMessageException("barcode should not be blank");
                }
                return Command.builder(Type.GET_FOOD_BY_BARCODE)
                    .setBarcode(barcode)
                    .build();
            }
        }
        throw new InvalidClientMessageException("Barcode argument is missing");
    }

    public static Command parse(String clientMessage) throws InvalidClientMessageException {
        Validator.validateMessageNotBlankOrEmpty(clientMessage);

        String[] parts = clientMessage.split("\\s+");

        Validator.validateArgumentsLength(parts);

        Type commandType = parseType(parts[0]);
        return switch (commandType) {
            case GET_FOOD -> parseGetFoodCommand(parts);
            case GET_FOOD_REPORT -> parseGetFoodReportCommand(parts);
            case GET_FOOD_BY_BARCODE -> parseGetFoodByBarcodeCommand(parts);
        };
    }
}
