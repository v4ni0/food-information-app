package bg.sofia.uni.fmi.mjt.food.client.message;

import bg.sofia.uni.fmi.mjt.food.client.message.barcode.BarcodeParser;
import bg.sofia.uni.fmi.mjt.food.exceptions.BarcodeParsingException;
import bg.sofia.uni.fmi.mjt.food.exceptions.InvalidClientMessageException;
import bg.sofia.uni.fmi.mjt.food.validation.Validator;

public class MessageParser {
    private static final String BARCODE_PREFIX = "--code=";
    private static final String IMG_PREFIX = "--img=";
    private static final String GET_FOOD_BY_BARCODE = "get-food-by-barcode";

    public static String parseMessage(String message) throws BarcodeParsingException, InvalidClientMessageException {
        Validator.validateMessageNotBlankOrEmpty(message);

        if (!message.startsWith(GET_FOOD_BY_BARCODE)) {
            return message;
        }

        String[] parts = message.split("\\s+");
        boolean hasCode = false;
        boolean hasImg = false;
        String imgPath = null;

        for (String part : parts) {
            if (part.startsWith(BARCODE_PREFIX)) {
                hasCode = true;
                break;
            } else if (part.startsWith(IMG_PREFIX)) {
                hasImg = true;
                imgPath = part.substring(IMG_PREFIX.length());
                break;
            }
        }

        if (hasCode || !hasImg) {
            return message;
        }

        String barcode = BarcodeParser.parseBarcode(imgPath);
        return GET_FOOD_BY_BARCODE + " " + BARCODE_PREFIX + barcode;
    }
}