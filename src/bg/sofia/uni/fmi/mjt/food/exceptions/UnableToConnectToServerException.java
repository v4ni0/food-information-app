package bg.sofia.uni.fmi.mjt.food.exceptions;

public class UnableToConnectToServerException extends Exception {
    public UnableToConnectToServerException(String message) {
        super(message);
    }

    public UnableToConnectToServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
