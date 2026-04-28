package it.polimi.ingsw.model.exception;

/**
 * Enumeration of all possible error codes that can be raised during a game session.
 * Used by {@link GameException} to identify the type of violation and by the
 * controller to send a structured {@code ErrorMessage} to the client.
 *
 * @author Luca Grecchi
 */
public enum ErrorCode {
    UNKNOWN_PLAYER,
    UNKNOWN_CARD,
    NOT_YOUR_TURN,
    INVALID_PHASE,
    SLOT_OCCUPIED,
    INSUFFICIENT_FOOD,
    CARD_NOT_IN_ROW,
    INVALID_SELECTION,
    NICKNAME_TAKEN,
    COLOR_TAKEN,
    LOBBY_FULL,
    GAME_ALREADY_STARTED,
    LOBBY_NOT_CREATED
}
