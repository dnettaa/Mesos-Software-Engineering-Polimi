package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.leaderboard.MatchResult;
import it.polimi.ingsw.model.exception.ErrorCode;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.model.game.DTO.CardsTakenDTO;
import it.polimi.ingsw.model.game.DTO.EventResolvedDTO;
import it.polimi.ingsw.model.game.DTO.ExtraCardTakenDTO;
import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.model.game.DTO.TotemPlacedDTO;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Server-side RMI adapter.
 * <p>
 * This remote object is registered in the RMI registry and receives direct
 * domain-specific method calls from RMI clients.
 * <p>
 * Unlike the Socket implementation, this class does not receive or send
 * generic message objects. Client actions are forwarded directly to the
 * {@link GameController}, while server notifications are forwarded directly
 * to the remote {@link ClientRMI} callback object.
 *
 * @author Diana
 */
public class RMIServerAdapter extends UnicastRemoteObject implements ServerRMI{

    private final GameController controller;
    private final Map<String, RMIClientConnection> connectionsByNickname;

    /**
     * Creates a new RMI server adapter.
     *
     * @param controller the game controller used to handle remote client actions
     * @throws RemoteException if the remote object cannot be exported
     */
    public RMIServerAdapter(GameController controller) throws RemoteException{
        super();
        this.controller = controller;
        this.connectionsByNickname = new ConcurrentHashMap<>();
    }

    /**
     * Handles a remote request to create a new lobby.
     * <p>
     * The provided {@link ClientRMI} stub is wrapped in a {@link VirtualView}
     * implementation and passed to the controller. From this point on, the
     * controller can notify the RMI client exactly as it notifies a Socket client,
     * through the {@link VirtualView} abstraction.
     *
     * @param nickname        nickname of the player creating the lobby
     * @param color           chosen totem color
     * @param expectedPlayers number of players required to start the game
     * @param client          remote callback object of the client
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void createLobby(String nickname, TotemColor color, int expectedPlayers, ClientRMI client)
            throws RemoteException{
        RMIClientConnection connection = registerConnection(nickname, client);
        controller.createLobby(nickname, color, expectedPlayers, connection);
    }

    /**
     * Handles a remote request to join an existing lobby.
     *
     * @param nickname nickname of the joining player
     * @param color    chosen totem color
     * @param client   remote callback object of the client
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void joinLobby(String nickname, TotemColor color, ClientRMI client)
            throws RemoteException{
        RMIClientConnection connection = registerConnection(nickname, client);
        controller.joinLobby(nickname, color, connection);
    }

    /**
     * Handles a remote request to place a totem.
     *
     * @param nickname nickname of the player performing the action
     * @param slotID   identifier of the chosen offer slot
     * @throws RemoteException if the player is not connected through RMI
     */
    @Override
    public void placeTotem(String nickname, char slotID) throws RemoteException{
        ensureConnected(nickname);
        controller.placeTotem(nickname, slotID);
    }

    /**
     * Handles a remote request to take cards.
     *
     * @param nickname nickname of the player performing the action
     * @param upperIDs identifiers of the selected upper-row cards
     * @param lowerIDs identifiers of the selected lower-row cards
     * @throws RemoteException if the player is not connected through RMI
     */
    @Override
    public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs)
            throws RemoteException{
        ensureConnected(nickname);
        controller.takeCards(nickname, upperIDs, lowerIDs);
    }

    /**
     * Handles a remote request to take an extra card.
     *
     * @param nickname nickname of the player performing the action
     * @param cardID   identifier of the selected card
     * @throws RemoteException if the player is not connected through RMI
     */
    @Override
    public void takeExtraCard(String nickname, String cardID) throws RemoteException{
        ensureConnected(nickname);
        controller.takeExtraCard(nickname, cardID);
    }

    /**
     * Handles a remote disconnection request.
     *
     * @param nickname nickname of the disconnecting player
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void disconnect(String nickname) throws RemoteException{
        RMIClientConnection connection = connectionsByNickname.remove(nickname);

        if(connection != null){
            connection.disconnect();
            controller.onDisconnect(nickname);
        }
    }

    /**
     * Checks whether the remote server object is reachable.
     *
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void ping() throws RemoteException{
        // Empty by design: a successful remote invocation is enough to prove reachability.
    }

    /**
     * Creates and stores the server-side virtual view associated with an RMI client.
     *
     * @param nickname nickname associated with the connection
     * @param client   remote callback object of the client
     * @return the virtual view representing the RMI client
     * @throws RemoteException if the nickname is already associated with an active RMI connection
     */
    private RMIClientConnection registerConnection(String nickname, ClientRMI client){
        RMIClientConnection connection = new RMIClientConnection(client);
        connection.setNickname(nickname);
        connectionsByNickname.put(nickname, connection);
        return connection;
    }

    /**
     * Ensures that a nickname is associated with an active RMI connection.
     *
     * @param nickname nickname to check
     * @throws RemoteException if no active RMI connection is associated with the nickname
     */
    private void ensureConnected(String nickname) throws RemoteException{
        RMIClientConnection connection = connectionsByNickname.get(nickname);

        if(connection == null || !connection.isConnected()){
            throw new RemoteException("Unknown or disconnected RMI client: " + nickname);
        }
    }

    /**
     * Server-side virtual view associated with one RMI client.
     * <p>
     * The controller only depends on {@link VirtualView}. This class hides the
     * RMI-specific callback object behind that abstraction.
     * <p>
     * Every notification is forwarded through a direct remote method call on
     * {@link ClientRMI}. No {@code ServerMessage}, queue or writer thread is used.
     */
    private class RMIClientConnection implements VirtualView{

        private final ClientRMI clientStub;
        private volatile String nickname;
        private volatile boolean connected;
        private final BlockingQueue<RemoteCallback> outbox = new LinkedBlockingQueue<>();
        private final Thread writerThread;

        /**
         * Creates a virtual view for one RMI client.
         *
         * @param clientStub remote callback object of the client
         */
        private RMIClientConnection(ClientRMI clientStub){
            this.clientStub = clientStub;
            this.connected = true;
            this.writerThread = new Thread(this::writerLoop, "rmi-writer");
            this.writerThread.setDaemon(true);
            this.writerThread.start();
        }

        private void writerLoop() {
            while (connected) {
                try {
                    RemoteCallback call = outbox.take();
                    if (!connected) return;
                    try {
                        call.call();
                    } catch (RemoteException e) {
                        handleClientFailure();
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        private void enqueue(RemoteCallback call) {
            if (connected) outbox.offer(call);
        }

        /**
         * Returns the nickname associated with this virtual view.
         *
         * @return the player nickname
         */
        @Override
        public String getNickname(){
            return nickname;
        }

        /**
         * Sets the nickname associated with this virtual view.
         *
         * @param nickname the player nickname
         */
        @Override
        public void setNickname(String nickname){
            this.nickname = nickname;
        }

        /**
         * Checks whether this RMI virtual view is active.
         *
         * @return {@code true} if the client is connected, {@code false} otherwise
         */
        @Override
        public boolean isConnected(){
            return connected;
        }

        /**
         * Marks this RMI virtual view as disconnected.
         */
        @Override
        public void disconnect(){
            connected = false;
            outbox.offer(() -> {});
        }

        /**
         * Notifies the remote client that the join operation succeeded.
         *
         * @param nickname assigned nickname
         * @param color    assigned totem color
         */
        @Override
        public void onJoinSuccess(String nickname, TotemColor color){
            enqueue(() -> clientStub.onJoinSuccess(nickname, color));
        }

        /**
         * Notifies the remote client that the lobby state changed.
         *
         * @param players        nicknames of the players currently in the lobby
         * @param colorsByPlayer selected color for each player
         * @param expected       number of players required to start the game
         */
        @Override
        public void onLobbyUpdate(List<String> players, Map<String, TotemColor> colorsByPlayer, int expected){
            enqueue(() -> clientStub.onLobbyUpdate(players, colorsByPlayer, expected));
        }

        /**
         * Notifies the remote client about an error.
         *
         * @param code        error code
         * @param description human-readable error description
         */
        @Override
        public void onError(String code, String description){
            enqueue(() -> clientStub.onError(code, description));
        }

        /**
         * Notifies the remote client that the connection has been closed.
         *
         * @param reason reason of the disconnection
         */
        @Override
        public void onDisconnection(String reason){
            enqueue(() -> clientStub.onDisconnection(reason));
            disconnect();
        }

        /**
         * Notifies the remote client that the game has started.
         *
         * @param snapshot initial game state snapshot
         */
        @Override
        public void onGameStarted(GameStateSnapshot snapshot){
            enqueue(() -> clientStub.onGameStarted(snapshot));
        }

        /**
         * Notifies the remote client that a totem has been placed.
         *
         * @param dto data describing the totem placement
         */
        @Override
        public void onTotemPlaced(TotemPlacedDTO dto){
            enqueue(() -> clientStub.onTotemPlaced(dto));

        }

        /**
         * Notifies the remote client that cards have been taken.
         *
         * @param dto data describing the taken cards and resulting state changes
         */
        @Override
        public void onCardsTaken(CardsTakenDTO dto){
            enqueue(() -> clientStub.onCardsTaken(dto));
        }

        /**
         * Notifies the remote client that an extra card has been taken.
         *
         * @param dto data describing the extra card action
         */
        @Override
        public void onExtraCardTaken(ExtraCardTakenDTO dto){
            enqueue(() -> clientStub.onExtraCardTaken(dto));
        }

        /**
         * Notifies the remote client that one or more events have been resolved.
         *
         * @param dto data describing the resolved events
         */
        @Override
        public void onEventResolved(EventResolvedDTO dto){
            enqueue(() -> clientStub.onEventResolved(dto));
        }

        /**
         * Notifies the remote client that the current round has ended.
         *
         * @param dto data describing the new round state
         */
        @Override
        public void onRoundEnded(RoundEndedDTO dto){
            enqueue(() -> clientStub.onRoundEnded(dto));
        }

        /**
         * Notifies the remote client that the game has ended.
         *
         * @param dto data describing the final game result
         */
        @Override
        public void onGameEnded(GameEndedDTO dto){
            enqueue(() -> clientStub.onGameEnded(dto));
        }

        /**
         * Forwards the leaderboard to the remote client asynchronously.
         *
         * @param ranking  ordered list of match results
         * @param position position of the client player
         */
        @Override
        public void onLeaderboard(List<MatchResult> ranking, int position){
            enqueue(() -> clientStub.onLeaderboard(ranking, position));
        }

        /**
         * Marks this client as disconnected and notifies the controller.
         */
        private void handleClientFailure(){
            if(!connected){
                return;
            }

            connected = false;

            if(nickname != null){
                connectionsByNickname.remove(nickname, this);
                controller.onDisconnect(nickname);
            }
        }
    }

    /**
     * Functional interface used to execute RMI callbacks with uniform error handling.
     */
    @FunctionalInterface
    private interface RemoteCallback{

        /**
         * Executes a remote callback.
         *
         * @throws RemoteException if the remote invocation fails
         */
        void call() throws RemoteException;
    }
}