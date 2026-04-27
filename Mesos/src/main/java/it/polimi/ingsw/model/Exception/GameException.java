package it.polimi.ingsw.model.Exception;

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
