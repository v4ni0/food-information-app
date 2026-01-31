package bg.sofia.uni.fmi.mjt.food.validation;

import bg.sofia.uni.fmi.mjt.food.exceptions.InvalidClientMessageException;

import java.util.List;

public class Validator {
    private static final int MINIMUM_NUMBER_OF_ARGS = 2;

    public static void validateMessageNotBlankOrEmpty(String message) throws InvalidClientMessageException {
        if (message == null || message.isBlank())
            throw new InvalidClientMessageException("message cannot be null or blank");
    }

    public static void validateArgumentsLength(String... arguments)
        throws InvalidClientMessageException {
        if (arguments == null || arguments.length < MINIMUM_NUMBER_OF_ARGS)
            throw new InvalidClientMessageException("message should contain at least 2 arguments");
    }

    public static void validateKeywordsLength(List<String> keywords)
        throws InvalidClientMessageException {
        if (keywords == null || keywords.isEmpty())
            throw new InvalidClientMessageException("there should be at least one keyword");
    }

}
