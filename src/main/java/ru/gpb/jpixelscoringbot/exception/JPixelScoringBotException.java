package ru.gpb.jpixelscoringbot.exception;

public class JPixelScoringBotException extends RuntimeException {

    public JPixelScoringBotException() {
        super();
    }

    public JPixelScoringBotException(String message) {
        super(message);
    }

    public JPixelScoringBotException(String message, Throwable cause) {
        super(message, cause);
    }

    public JPixelScoringBotException(Throwable cause) {
        super(cause);
    }
}
