    package it.polimi.ingsw.controller;

    import it.polimi.ingsw.model.game.GameActions;
    import it.polimi.ingsw.model.player.TotemColor;
    import it.polimi.ingsw.network.VirtualView;
    import it.polimi.ingsw.model.game.GameListener;
    import it.polimi.ingsw.model.game.DTO.*;
    import it.polimi.ingsw.leaderboard.*;

    import java.util.HashMap;
    import java.util.Map;

    /**
     * Main controller of the server-side application.
     * Acts as the entry point for all client requests and coordinates
     * communication between the network layer, the model, and the current controller phase.
     * Uses the State pattern through {@link ControllerPhase} to delegate behavior
     * depending on the current stage of the game (e.g., lobby or in-game).
     * Also manages connected client views and forwards model updates directly to VirtualView callbacks
     * Thread safety is ensured via synchronized methods, as multiple clients
     * may invoke actions concurrently.
     *
     * @author Andrea Markvukaj
     */
    public class GameController implements GameListener {

        private GameActions game;
        private ControllerPhase currentPhase;
        private final Map<String, VirtualView> views;
        private int playerCount;
        private final RankingService rankingService;

        /**
         * Constructs a new GameController.
         * Initializes the view map and sets up the leaderboard service
         * using a SQL-based repository connected to a PostgreSQL database.
         */
        public GameController() {
            this.views = new HashMap<>();

            MatchResultRepository repo = new SqlMatchResultRepository(
                    "jdbc:postgresql://localhost:5432/mesos",
                    "postgres",
                    "postgres123"
            );

            this.rankingService = new RankingService(repo);
        }

        public Map<String, VirtualView> getViews() {
            return views;
        }

        public void setPlayerCount(int playerCount) {
            this.playerCount = playerCount;
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
                view.onError(code, desc);
            }
        }

        /**
         * Disconnects all connected clients and clears the view map.
         */
        public void closeAll() {
            for (VirtualView view : views.values()) {
                view.disconnect();
            }
            views.clear();
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

        // METODI DI GAME LISTENER

        /**
         * Invoked when the game starts.
         * Forwards the initial game snapshot to all connected views.
         *
         * @param snap the initial state of the game
         */
        @Override
        public void onGameStarted(GameStateSnapshot snap) {
            for (VirtualView view : views.values()) {
                if (view.isConnected()) {
                    view.onGameStarted(snap);
                }
            }
        }

        /**
         * Invoked when a player places a totem on the board.
         * Forwards the corresponding update DTO to all connected views.
         *
         * @param dto contains information about the placement and next player
         */
        @Override
        public void onTotemPlaced(TotemPlacedDTO dto) {
            for (VirtualView view : views.values()) {
                if (view.isConnected()) {
                    view.onTotemPlaced(dto);
                }
            }
        }

        /**
         * Invoked when a player takes cards from the board.
         * Forwards the DTO containing all state changes to all connected views.
         *
         * @param dto contains details about the card selection and resulting changes
         */
        @Override
        public void onCardsTaken(CardsTakenDTO dto) {
            for (VirtualView view : views.values()) {
                if (view.isConnected()) {
                    view.onCardsTaken(dto);
                }
            }
        }

        /**
         * Invoked when a player takes an extra card.
         * Forwards the update DTO to all connected views.
         *
         * @param dto contains information about the extra card taken
         */
        @Override
        public void onExtraCardTaken(ExtraCardTakenDTO dto) {
            for (VirtualView view : views.values()) {
                if (view.isConnected()) {
                    view.onExtraCardTaken(dto);
                }
            }
        }

        /**
         * Invoked when an event is resolved.
         * Forwards the resulting DTO to all connected views.
         *
         * @param dto contains the effects of the resolved event
         */
        @Override
        public void onEventResolved(EventResolvedDTO dto) {
            for (VirtualView view : views.values()) {
                if (view.isConnected()) {
                    view.onEventResolved(dto);
                }
            }
        }

        /**
         * Invoked at the end of a round.
         * Forwards the DTO describing the new round setup to all connected views.
         *
         * @param dto contains all state changes for the new round
         */
        @Override
        public void onRoundEnded(RoundEndedDTO dto) {
            for (VirtualView view : views.values()) {
                if (view.isConnected()) {
                    view.onRoundEnded(dto);
                }
            }
        }

        /**
         * Invoked when the game ends.
         * Stores the final results in the leaderboard system and
         * forwards the GameEnded DTO to all connected views.
         *
         * @param dto contains final scores and ranking
         */
        @Override
        public void onGameEnded(GameEndedDTO dto) {

            rankingService.recordGame(dto, playerCount);

            for (VirtualView view : views.values()) {
                if (view.isConnected()) {
                    view.onGameEnded(dto);
                }
            }
        }
    }
