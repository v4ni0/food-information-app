package bg.sofia.uni.fmi.mjt.food.exceptions;

public class InvalidClientMessageException extends Exception {
    public InvalidClientMessageException(String message) {
        super(message);
    }

    public InvalidClientMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
