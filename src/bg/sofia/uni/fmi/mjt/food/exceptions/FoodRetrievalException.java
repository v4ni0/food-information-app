package bg.sofia.uni.fmi.mjt.food.exceptions;

public class FoodRetrievalException extends Exception {
    public FoodRetrievalException(String message) {
        super(message);
    }

    public FoodRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
