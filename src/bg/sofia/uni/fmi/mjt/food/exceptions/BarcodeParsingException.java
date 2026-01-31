package bg.sofia.uni.fmi.mjt.food.exceptions;

public class BarcodeParsingException extends Exception {
    public BarcodeParsingException(String message) {
        super(message);
    }

    public BarcodeParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
