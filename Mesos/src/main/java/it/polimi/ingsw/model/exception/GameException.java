package it.polimi.ingsw.model.exception;

/**
 * Exception thrown when a game action violates the rules or the current state.
 * Wraps an {@link ErrorCode} to allow the controller to send a structured
 * error message back to the client.
 *
 * @author Luca Grecchi
 */
public class GameException extends RuntimeException {
    private final ErrorCode code;

    public GameException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
