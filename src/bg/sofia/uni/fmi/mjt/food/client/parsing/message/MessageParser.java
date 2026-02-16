package bg.sofia.uni.fmi.mjt.food.client.parsing.message;

import bg.sofia.uni.fmi.mjt.food.client.parsing.barcode.BarcodeParser;
import bg.sofia.uni.fmi.mjt.food.exceptions.BarcodeParsingException;
import bg.sofia.uni.fmi.mjt.food.exceptions.InvalidClientMessageException;
import bg.sofia.uni.fmi.mjt.food.validation.Validator;

public class MessageParser {
    private static final String BARCODE_PREFIX = "--code=";
    private static final String IMG_PREFIX = "--img=";
    private static final String GET_FOOD_BY_BARCODE = "get-food-by-barcode";

    public static String parse(String message) throws BarcodeParsingException, InvalidClientMessageException {
        Validator.validateMessageNotBlankOrEmpty(message);
        if (!message.startsWith(GET_FOOD_BY_BARCODE)) {
            return message;
        }
        String[] parts = message.split("\\s+");
        String imgPath = null;

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith(BARCODE_PREFIX)) {
                return message;
            } else if (parts[i].startsWith(IMG_PREFIX)) {
                StringBuilder pathBuilder = new StringBuilder(parts[i].substring(IMG_PREFIX.length()));
                for (int j = i + 1; j < parts.length; j++) {
                    pathBuilder.append(" ").append(parts[j]);
                }
                imgPath = pathBuilder.toString();
                break;
            }
        }

        if (imgPath == null) {
            throw new InvalidClientMessageException("Image path and code is missing for barcode parsing");
        }

        String barcode = BarcodeParser.parse(imgPath);
        return GET_FOOD_BY_BARCODE + " " + BARCODE_PREFIX + barcode;
    }
}