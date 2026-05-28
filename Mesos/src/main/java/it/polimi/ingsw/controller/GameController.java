    package it.polimi.ingsw.controller;

    import it.polimi.ingsw.model.game.GameActions;
    import it.polimi.ingsw.model.player.TotemColor;
    import it.polimi.ingsw.network.VirtualView;
    import it.polimi.ingsw.model.game.GameListener;
    import it.polimi.ingsw.model.game.DTO.*;
    import it.polimi.ingsw.persistence.PersistenceManager;

    import it.polimi.ingsw.leaderboard.*;
    import it.polimi.ingsw.config.DBConfiguration;
    import it.polimi.ingsw.config.ConfigLoader;

    import java.util.HashMap;
    import java.util.ArrayList;
    import java.util.List;
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
         * Initializes the view map and configures the leaderboard service.
         * If a valid database connection is available, a SQL repository is used.
         * Otherwise, the game still runs but the global leaderboard is disabled.
         */
        public GameController() {
            this.views = new HashMap<>();

            RankingService service = null;

            DBConfiguration config = ConfigLoader.load();

            if (config != null) {
                SqlMatchResultRepository sqlRepo = new SqlMatchResultRepository(
                        config.dbUrl,
                        config.dbUser,
                        config.dbPassword
                );

                if (sqlRepo.testConnection()) {
                    service = new RankingService(sqlRepo);
                    System.out.println("Using SQL database");
                } else {
                    System.out.println("! Database non disponibile: leaderboard globale disabilitata");
                }

            } else {
                System.out.println("! Database non disponibile: leaderboard globale disabilitata");
            }

            this.rankingService = service;
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
         * Handles a player's reconnection request during server recovery.
         * Delegates the reconnect logic to the current controller phase.
         *
         * @param nickname the player's nickname
         * @param color the player's original totem color
         * @param view the reconnecting virtual view
         */
        public synchronized void reconnectPlayer(String nickname, TotemColor color, VirtualView view) {
            currentPhase.reconnect(this, nickname, color, view);
        }

        public synchronized void acceptRecovery(String nickname, TotemColor color, VirtualView view) {
            if (currentPhase != null) {
                currentPhase.acceptRecovery(this, nickname, color, view);
            }
        }

        public synchronized void declineRecovery(VirtualView view) {
            if (currentPhase != null) {
                currentPhase.declineRecovery(this, view);
            }
        }

        public synchronized void reset() {
            this.game = null;
            this.currentPhase = null;
            this.views.clear();
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
        public synchronized void takeCards(String nickname, java.util.List<String> upperIDs, java.util.List<String> lowerIDs, java.util.List<String> orderedIDs) {
            currentPhase.takeCards(nickname, upperIDs, lowerIDs, orderedIDs);
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
         * Disconnects all connected clients and clears the view map safely.
         */
        public void closeAll() {
            List<VirtualView> viewsToDisconnect = new ArrayList<>(views.values());

            views.clear();

            for (VirtualView view : viewsToDisconnect) {
                view.disconnect();
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

        // METODI DI GAME LISTENER

        /**
         * Invoked when the game starts.
         * Forwards the initial game snapshot to all connected views.
         *
         * @param snap the initial state of the game
         */
        @Override
        public void onGameStarted(GameStateSnapshot snap) {
            PersistenceManager.saveGame(this.game);
            for (VirtualView view : views.values()) {
                if (view.isConnected()) {
                    view.onGameStarted(snap);
                }
            }
        }

        /**
         * Handles a totem placement update from the model.
         *
         * @param dto DTO containing the placement information
         */
        @Override
        public void onTotemPlaced(TotemPlacedDTO dto) {
            PersistenceManager.saveGame(this.game);
            for (VirtualView view : views.values()) {
                if (view.isConnected()) {
                    view.onTotemPlaced(dto);
                }
            }
        }

        /**
         * Handles a card selection update from the model.
         *
         * @param dto DTO containing the taken-card information
         */
        @Override
        public void onCardsTaken(CardsTakenDTO dto) {
            PersistenceManager.saveGame(this.game);
            for (VirtualView view : views.values()) {
                if (view.isConnected()) {
                    view.onCardsTaken(dto);
                }
            }
        }

        /**
         * Handles an extra-card selection update from the model.
         *
         * @param dto DTO containing the extra-card information
         */
        @Override
        public void onExtraCardTaken(ExtraCardTakenDTO dto) {
            PersistenceManager.saveGame(this.game);
            for (VirtualView view : views.values()) {
                if (view.isConnected()) {
                    view.onExtraCardTaken(dto);
                }
            }
        }

        /**
         * Handles an event resolution update from the model.
         *
         * @param dto DTO containing the event resolution information
         */
        @Override
        public void onEventResolved(EventResolvedDTO dto) {
            PersistenceManager.saveGame(this.game);
            for (VirtualView view : views.values()) {
                if (view.isConnected()) {
                    view.onEventResolved(dto);
                }
            }
        }

        /**
         * Handles a round-end update from the model.
         *
         * @param dto DTO containing the round-end information
         */
        @Override
        public void onRoundEnded(RoundEndedDTO dto) {
            PersistenceManager.saveGame(this.game);
            for (VirtualView view : views.values()) {
                if (view.isConnected()) {
                    view.onRoundEnded(dto);
                }
            }
        }

        /**
         * Handles a game-end update from the model.
         * Stores results, computes the leaderboard, and notifies all clients
         * with both final game data and ranking information.
         *
         * @param dto contains final scores and ranking
         */
        @Override
        public void onGameEnded(GameEndedDTO dto) {
            PersistenceManager.deleteSave();

            List<MatchResult> ranking = List.of();
            if (rankingService != null) {
                rankingService.recordGame(dto, playerCount);
                ranking = rankingService.getRanking(playerCount);
            } else {
                System.out.println("Database non disponibile: leaderboard globale non salvata e non inviata");
            }

            for (VirtualView view : views.values()) {
                if (view.isConnected()) {

                    view.onGameEnded(dto);
                    if (rankingService != null) {
                        String nick = view.getNickname();
                        int position = rankingService.getPlayerPosition(nick, playerCount);
                        view.onLeaderboard(ranking, position);
                    }
                }
            }
        }
    }
