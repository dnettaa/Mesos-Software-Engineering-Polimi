package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.message.GameStateMessage;
import it.polimi.ingsw.network.message.ServerMessage;
import it.polimi.ingsw.network.message.ErrorMessage;
import it.polimi.ingsw.model.game.GameListener;
import it.polimi.ingsw.model.game.DTO.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Main controller of the server-side application.
 * Acts as the entry point for all client requests and coordinates
 * communication between the network layer, the model, and the current controller phase.
 * Uses the State pattern through {@link ControllerPhase} to delegate behavior
 * depending on the current stage of the game (e.g., lobby or in-game).
 * Also manages connected client views and handles message broadcasting.
 * Thread safety is ensured via synchronized methods, as multiple clients
 * may invoke actions concurrently.
 *
 * @author Andrea Markvukaj
 */
public class GameController implements GameListener {

    private GameActions game;
    private ControllerPhase currentPhase;
    private final Map<String, VirtualView> views;

    public GameController() {
        this.views = new HashMap<>();
    }

    // METODI DA DELEGARE ALLA LOBBY PHASE

    /**
     * Handles the creation of a new lobby.
     * If no phase is active, initializes a {@link LobbyPhase}.
     * Delegates the request to the current phase.
     *
     * @param nickname the nickname of the player creating the lobby
     * @param color the chosen totem color
     * @param expectedPlayers the number of players expected in the lobby
     * @param view the virtual view associated with the player
     */
    public synchronized void createLobby(String nickname, TotemColor color, int expectedPlayers, VirtualView view) {
        if (currentPhase == null) {
            currentPhase = new LobbyPhase(this, expectedPlayers);
        }
        currentPhase.createLobby(nickname, color, view);
    }

    /**
     * Handles a player's request to join an existing lobby.
     * Delegates the request to the current phase.
     *
     * @param nickname the nickname of the player
     * @param color the chosen totem color
     * @param view the virtual view associated with the player
     */
    public synchronized void joinLobby(String nickname, TotemColor color, VirtualView view) {
        currentPhase.joinLobby(nickname, color, view);
    }

    /**
     * Handles the disconnection of a player.
     * Delegates the logic to the current phase.
     *
     * @param nickname the nickname of the disconnected player
     */
    public synchronized void onDisconnect(String nickname) {
        if (currentPhase != null) {
            currentPhase.onDisconnect(nickname);
        }
    }

    // METODI DA DELEGARE ALLA IN GAME PHASE

    /**
     * Handles a totem placement request from a player.
     * Delegates the action to the current phase.
     *
     * @param nickname the nickname of the player
     * @param slotID the identifier of the chosen slot
     */
    public synchronized void placeTotem(String nickname, char slotID) {
        currentPhase.placeTotem(nickname, slotID);
    }

    /**
     * Handles a card selection request from a player.
     * Delegates the action to the current phase.
     *
     * @param nickname the nickname of the player
     * @param upperIDs identifiers of selected cards from the upper row
     * @param lowerIDs identifiers of selected cards from the lower row
     */
    public synchronized void takeCards(String nickname, java.util.List<String> upperIDs, java.util.List<String> lowerIDs) {
        currentPhase.takeCards(nickname, upperIDs, lowerIDs);
    }

    /**
     * Handles the selection of an extra card by a player.
     * Delegates the action to the current phase.
     *
     * @param nickname the nickname of the player
     * @param cardID the identifier of the selected card
     */
    public synchronized void takeExtraCard(String nickname, String cardID) {
        currentPhase.takeExtraCard(nickname, cardID);
    }

    // GESTIONE DELLE VIEW

    /**
     * Registers a new client view associated with a player.
     *
     * @param nickname the player's nickname
     * @param view the virtual view representing the client
     */
    public void registerView(String nickname, VirtualView view) {
        view.setNickname(nickname);
        views.put(nickname, view);
    }

    /**
     * Unregisters a client view when a player disconnects.
     *
     * @param nickname the player's nickname
     */
    public void unregisterView(String nickname) {
        views.remove(nickname);
    }

    // GESTIONE DELLA COMUNICAZIONE

    /**
     * Sends a message to all connected clients.
     *
     * @param msg the message to broadcast
     */
    public void broadcast(ServerMessage msg) {
        for (VirtualView view : views.values()) {
            if (view.isConnected()) {
                view.send(msg);
            }
        }
    }

    /**
     * Sends an error message to a specific player.
     *
     * @param nickname the recipient player
     * @param code the error code
     * @param desc the error description
     */
    public void sendError(String nickname, String code, String desc) {
        VirtualView view = views.get(nickname);
        if (view != null && view.isConnected()) {
            view.send(new ErrorMessage(code, desc));
        }
    }

    /**
     * Sends a specific message to a single player.
     *
     * @param nickname the recipient player
     * @param msg the message to send
     */
    public void sendTo(String nickname, ServerMessage msg) {
        VirtualView view = views.get(nickname);
        if (view != null && view.isConnected()) {
            view.send(msg);
        }
    }

    // GESTIONE DEL MODEL

    /**
     * Sets the game model instance.
     *
     * @param game the game to associate with this controller
     */
    public void setGame(GameActions game) {
        this.game = game;
        game.addListener(this);
    }

    /**
     * Returns the current game instance.
     *
     * @return the game model
     */
    public GameActions getGame() {
        return game;
    }

    /**
     * Transitions the controller to a new phase.
     *
     * @param phase the new controller phase
     */
    public void transitionTo(ControllerPhase phase) {
        this.currentPhase = phase;
    }

    /**
     * Returns the current controller phase.
     *
     * @return the active phase
     */
    public ControllerPhase getCurrentPhase() {
        return currentPhase;
    }

    // TERMINAZIONE DELLE VIEW

    /**
     * Disconnects all connected clients and clears the view map.
     */
    public void closeAll() {
        for (VirtualView view : views.values()) {
            view.disconnect();
        }
        views.clear();
    }

    // METODI DI GAME LISTENER

    /**
     * Invoked when the game starts.
     * Builds the initial game state message from the snapshot
     * and broadcasts it to all connected clients.
     *
     * @param snap the initial state of the game
     */
    @Override
    public void onGameStarted(GameStateSnapshot snap) {
        broadcast(new GameStateMessage(
                snap.currentRound(),
                snap.currentEra(),
                snap.currentPhaseName(),
                snap.currentPlayerNickname(),
                snap.placementOrder(),
                snap.resolutionOrder(),
                snap.turnOrder(),
                snap.tribeDeckRemaining(),
                snap.upperRowCardIDs(),
                snap.lowerRowCardIDs(),
                snap.offerSlots(),
                snap.players()
        ));
    }

    /**
     * Invoked when a player places a totem on the board.
     * Extracts the relevant data from the DTO and broadcasts
     * a corresponding message to all connected clients.
     *
     * @param dto contains information about the placement and next player
     */
    @Override
    public void onTotemPlaced(TotemPlacedDTO dto) {
        broadcast(new TotemPlacedMessage(
                dto.placerNickname(),
                dto.slotID(),
                dto.nextPlayerNickname()
        ));
    }

    /**
     * Invoked when a player takes cards from the board.
     * Extracts all state changes from the DTO and sends
     * a structured update message to all clients.
     *
     * @param dto contains details about the card selection and resulting changes
     */
    @Override
    public void onCardsTaken(CardsTakenDTO dto) {
        broadcast(new CardsTakenMessage(
                dto.nickname(),
                dto.takenUpperIDs(),
                dto.takenLowerIDs(),
                dto.addedTribeCardIDs(),
                dto.addedBuildingIDs(),
                dto.foodDelta(),
                dto.ppDelta(),
                dto.freedSlotID(),
                dto.turnOrderPosition(),
                dto.nextPlayerNickname()
        ));
    }

    /**
     * Invoked when a player takes an extra card.
     * Builds and broadcasts a message derived from the DTO.
     *
     * @param dto contains information about the extra card taken
     */
    @Override
    public void onExtraCardTaken(ExtraCardTakenDTO dto) {
        broadcast(new ExtraCardTakenMessage(
                dto.nickname(),
                dto.cardID(),
                dto.fromUpperRow(),
                dto.isBuilding(),
                dto.foodDelta()
        ));
    }

    /**
     * Invoked when an event card is resolved.
     * Extracts the effects from the DTO and broadcasts them to clients.
     *
     * @param dto contains the results of the resolved event
     */
    @Override
    public void onEventResolved(EventResolvedDTO dto) {
        broadcast(new EventResolvedMessage(
                dto.eventCardID(),
                dto.eventType(),
                dto.ppDeltaByPlayer(),
                dto.foodDeltaByPlayer()
        ));
    }

    /**
     * Invoked at the end of a round.
     * Extracts all updates from the DTO and broadcasts the new round setup.
     *
     * @param dto contains all state changes for the new round
     */
    @Override
    public void onRoundEnded(RoundEndedDTO dto) {
        broadcast(new RoundEndedMessage(
                dto.discardedLowerTribeIDs(),
                dto.discardedLowerEventIDs(),
                dto.movedUpperToLowerTribeIDs(),
                dto.discardedLowerBuildingIDs(),
                dto.movedUpperToLowerBuildingIDs(),
                dto.newUpperRowIDs(),
                dto.revealedBuildingIDs(),
                dto.newEra(),
                dto.newRound(),
                dto.newTurnOrder(),
                dto.firstPlayerNickname(),
                dto.tribeDeckRemaining()
        ));
    }

    /**
     * Invoked when the game ends.
     * Builds a final result message from the DTO, broadcasts it,
     * and closes all client connections.
     *
     * @param dto contains final scores and ranking
     */
    @Override
    public void onGameEnded(GameEndedDTO dto) {
        broadcast(new GameEndedMessage(
                dto.finalPPByPlayer(),
                dto.endGameBonusByPlayer(),
                dto.ranking()
        ));

        closeAll();
    }
}
