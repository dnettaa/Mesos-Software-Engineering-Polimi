package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.game.DTO.*;

/**
 * The {@code GameListener} interface defines the observer pattern callbacks for the Game model.
 * Classes implementing this interface (e.g., the GameController) can listen to state changes
 * and events occurring during the match without coupling the Model to the network layer.
 *
 * @author Luca Grecchi
 */
public interface GameListener {

    /**
     * Invoked when the game starts. Provides the initial full state of the game.
     *
     * @param dto the snapshot containing the complete initial state of the game
     */
    void onGameStarted(GameStateSnapshot dto);

    /**
     * Invoked when a player places their totem on an offer slot.
     *
     * @param dto the data transfer object containing the totem placement details
     */
    void onTotemPlaced(TotemPlacedDTO dto);

    /**
     * Invoked when a player takes cards during the offer resolution phase.
     *
     * @param dto the data transfer object containing the details of the taken cards, food/PP deltas, and the freed slot
     */
    void onCardsTaken(CardsTakenDTO dto);

    /**
     * Invoked when a player takes an extra card (e.g., through a specific building effect).
     *
     * @param dto the data transfer object containing the details of the extra card taken
     */
    void onExtraCardTaken(ExtraCardTakenDTO dto);

    /**
     * Invoked when an event (Sustenance, Hunting, Shamanic Ritual, or Cave Paintings) is resolved.
     *
     * @param dto the data transfer object containing the point and food deltas for all affected players
     */
    void onEventResolved(EventResolvedDTO dto);

    /**
     * Invoked at the end of a round.
     *
     * @param dto the data transfer object containing information about discarded/shifted cards, new turn order, and potential era changes
     */
    void onRoundEnded(RoundEndedDTO dto);

    /**
     * Invoked when the game ends.
     *
     * @param dto the data transfer object containing the final prestige points, bonuses, and the final ranking
     */
    void onGameEnded(GameEndedDTO dto);
}