package bg.sofia.uni.fmi.mjt.food.exceptions;

public class MissingBarcodeException extends Exception {
    public MissingBarcodeException(String message) {
        super(message);
    }

    public MissingBarcodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
