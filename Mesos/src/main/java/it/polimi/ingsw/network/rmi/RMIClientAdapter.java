package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.leaderboard.MatchResult;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualServer;
import it.polimi.ingsw.model.game.DTO.CardsTakenDTO;
import it.polimi.ingsw.model.game.DTO.EventResolvedDTO;
import it.polimi.ingsw.model.game.DTO.ExtraCardTakenDTO;
import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.model.game.DTO.TotemPlacedDTO;
import it.polimi.ingsw.view.View;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Client-side RMI adapter.
 * <p>
 * This class adapts the local {@link View} to the RMI protocol.
 * As a {@link VirtualServer}, it lets the view invoke server-side game actions
 * without knowing that RMI is used underneath.
 * As a {@link ClientRMI}, it exposes remote callback methods that the server
 * can invoke directly.
 * <p>
 * Unlike the Socket implementation, this adapter does not create or dispatch
 * network messages. Every client-to-server action and server-to-client
 * notification is represented by a specific remote method call.
 *
 * @author Diana
     */

public class RMIClientAdapter extends UnicastRemoteObject implements ClientRMI, VirtualServer{

    private static final String SERVER_NAME = "MesosServer";
    private static final int RECONNECT_TIMEOUT_SECONDS = 20;
    private final View view;
    private ServerRMI serverStub;
    private String nickname;
    private volatile boolean connected;
    private volatile boolean disconnectionNotified;
    private volatile boolean reconnectLoopRunning;
    private volatile boolean recovering;
    private String host;
    private int port;
    private TotemColor savedColor;
    private Thread heartbeatThread;
    private static final Set<String> LOGIN_ERROR_CODES = Set.of(
            "NICKNAME_TAKEN",
            "COLOR_TAKEN",
            "LOBBY_FULL",
            "GAME_ALREADY_STARTED",
            "LOBBY_NOT_CREATED"
    );
    private final ExecutorService renderExecutor = Executors.newSingleThreadExecutor();

    /**
     * Applies a DTO update to the local client model
     * and triggers a view re-render asynchronously.
     *
     * @param applyDTO action that applies the DTO update
     */
    private void applyAndRender(Runnable applyDTO) {
        renderExecutor.submit(() -> {
            applyDTO.run();
            view.render();
        });
    }

    /**
     * Creates a new RMI client adapter.
     *
     * @param view the local client-side view updated by remote callbacks
     * @throws RemoteException if this remote object cannot be exported
     */
    public RMIClientAdapter(View view) throws RemoteException{
        super();
        this.view = view;
        this.connected = false;
    }

    /**
     * Connects this adapter to the remote RMI server.
     * <p>
     * The method retrieves the server stub from the RMI registry. The current
     * object is already exported because this class extends
     * {@link UnicastRemoteObject}, so it can be passed directly to the server
     * in {@link #createLobby(String, TotemColor, int)} or
     * {@link #joinLobby(String, TotemColor)}.
     *
     * @param host the host where the RMI registry is running
     * @param port the port where the RMI registry is listening
     */
    public void connect(String host, int port) throws Exception{
        this.host = host;
        this.port = port;
        try{
            Registry registry = LocateRegistry.getRegistry(host, port);
            serverStub = (ServerRMI) registry.lookup(SERVER_NAME);
            connected = true;
            disconnectionNotified = false;
            startHeartbeat();
        } catch(Exception e){
            connected = false;
            throw new Exception("Unable to connect to RMI server: " + e.getMessage(), e);
        }
    }

    /**
     * Requests the creation of a new lobby through a direct remote method call.
     *
     * @param nickname        nickname of the player creating the lobby
     * @param color           chosen totem color
     * @param expectedPlayers number of players required to start the game
     */
    @Override
    public void createLobby(String nickname, TotemColor color, int expectedPlayers){
        this.nickname = nickname;
        this.savedColor = color;
        if(!isReady()){
            return;
        }

        try{
            serverStub.createLobby(nickname, color, expectedPlayers, this);
        } catch(RemoteException e){
            handleRemoteFailure("Connection with the RMI server lost while creating the lobby.");
        }
    }

    /**
     * Requests to join an existing lobby through a direct remote method call.
     *
     * @param nickname nickname of the joining player
     * @param color    chosen totem color
     */
    @Override
    public void joinLobby(String nickname, TotemColor color){
        this.nickname = nickname;
        this.savedColor = color;

        if(!isReady()){
            return;
        }

        try{
            serverStub.joinLobby(nickname, color, this);
        } catch(RemoteException e){
            handleRemoteFailure("Connection with the RMI server lost while joining the lobby.");
        }
    }

    /**
     * Requests to place a totem through a direct remote method call.
     *
     * @param nickname nickname of the player performing the action
     * @param slotID   identifier of the chosen offer slot
     */
    @Override
    public void placeTotem(String nickname, char slotID){
        if(!isReady()){
            return;
        }

        try{
            serverStub.placeTotem(nickname, slotID);
        } catch(RemoteException e){
            handleRemoteFailure("Connection with the RMI server lost while placing the totem.");
        }
    }

    /**
     * Requests to take cards through a direct remote method call.
     *
     * @param nickname nickname of the player performing the action
     * @param upperIDs identifiers of the selected upper-row cards
     * @param lowerIDs identifiers of the selected lower-row cards
     */
    @Override
    public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs, List<String> orderedIDs){
        if(!isReady()){
            return;
        }

        try{
            serverStub.takeCards(nickname, upperIDs, lowerIDs, orderedIDs);
        } catch(RemoteException e){
            handleRemoteFailure("Connection with the RMI server lost while taking cards.");
        }
    }

    /**
     * Requests to take an extra card through a direct remote method call.
     *
     * @param nickname nickname of the player performing the action
     * @param cardID   identifier of the selected card
     */
    @Override
    public void takeExtraCard(String nickname, String cardID){
        if(!isReady()){
            return;
        }

        try{
            serverStub.takeExtraCard(nickname, cardID);
        } catch(RemoteException e){
            handleRemoteFailure("Connection with the RMI server lost while taking the extra card.");
        }
    }

    /**
     * Disconnects this client from the remote server.
     */
    @Override
    public void disconnect(){
        renderExecutor.shutdown();
        if (heartbeatThread != null) {
            heartbeatThread.interrupt();
        }
        if(!connected){
            return;
        }

        connected = false;

        try{
            if (serverStub != null && nickname != null){
                serverStub.disconnect(nickname);
            }
        } catch(RemoteException ignored){
            // The client is already disconnecting, so there is no useful recovery action here.
        }

        try{
            UnicastRemoteObject.unexportObject(this, true);
        } catch(Exception ignored){
            // The object may already be unexported.
        }
    }

    /**
     * Notifies the local view that the join operation succeeded.
     *
     * @param nickname assigned nickname
     * @param color    assigned totem color
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void onJoinSuccess(String nickname, TotemColor color) throws RemoteException{
        this.nickname = nickname;
        view.showJoinSuccess(nickname, color);
    }

    /**
     * Notifies the local view that the lobby state changed.
     *
     * @param players        nicknames of the players currently in the lobby
     * @param colorsByPlayer selected color for each player
     * @param expected       number of players required to start the game
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void onLobbyUpdate(List<String> players, Map<String, TotemColor> colorsByPlayer, int expected)
            throws RemoteException{
        view.showLobbyUpdate(players, colorsByPlayer, expected);
    }

    /**
     * Notifies the local view about an error.
     *
     * @param code        error code
     * @param description human-readable error description
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void onError(String code, String description) throws RemoteException {
        String normalized = code == null ? "" : code.trim().toUpperCase();
        if (LOGIN_ERROR_CODES.contains(normalized)) {
            view.showLoginError(description);
        } else {
            view.showGameError(description);
        }
    }

    /**
     * Notifies the local view that the connection has been closed.
     *
     * @param reason reason of the disconnection
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void onDisconnection(String reason) throws RemoteException{
        connected = false;
        recovering = false;
        disconnectionNotified = true;
        if (view.getClientModel().isInGame()) {
            view.shutdown(reason);
        } else {
            view.goToWelcomeScreen(reason);
        }
    }

    /**
     * Notifies the local view that recovery was canceled without closing the RMI connection.
     *
     * @param reason reason shown to the user
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void onRecoveryCancelled(String reason) throws RemoteException {
        view.showRecoveryCancelled(reason);
    }

    @Override
    public void onRecoveryUpdate(List<String> reconnectedPlayers, List<String> missingPlayers)
            throws RemoteException {
        view.showRecoveryUpdate(reconnectedPlayers, missingPlayers);
    }

    /**
     * Applies the initial game snapshot to the local client model and renders the view.
     *
     * @param snapshot initial game state snapshot
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void onGameStarted(GameStateSnapshot snapshot) throws RemoteException{
        applyAndRender(() -> view.getClientModel().applyGameStarted(snapshot));
    }

    /**
     * Applies a totem placement update to the local client model and renders the view.
     *
     * @param dto data describing the totem placement
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void onTotemPlaced(TotemPlacedDTO dto) throws RemoteException{
        applyAndRender(() -> view.getClientModel().applyTotemPlaced(dto));

    }

    /**
     * Applies a card-taking update to the local client model and renders the view.
     *
     * @param dto data describing the taken cards and resulting state changes
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void onCardsTaken(CardsTakenDTO dto) throws RemoteException{
        applyAndRender(() -> view.getClientModel().applyCardsTaken(dto));
    }

    /**
     * Applies an extra-card update to the local client model and renders the view.
     *
     * @param dto data describing the extra card action
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void onExtraCardTaken(ExtraCardTakenDTO dto) throws RemoteException{
        applyAndRender(() -> view.getClientModel().applyExtraCardTaken(dto));
    }

    /**
     * Applies an event-resolution update to the local client model and renders the view.
     *
     * @param dto data describing the resolved events
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void onEventResolved(EventResolvedDTO dto) throws RemoteException {
        applyAndRender(() -> {
            view.getClientModel().applyEventResolved(dto);
            view.showEventResolved(dto.eventCardID(), dto.eventType(), dto.ppDeltaByPlayer(), dto.foodDeltaByPlayer());
        });
    }

    /**
     * Applies an end-round update to the local client model and renders the view.
     *
     * @param dto data describing the new round state
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void onRoundEnded(RoundEndedDTO dto) throws RemoteException{
        applyAndRender(() -> view.getClientModel().applyRoundEnded(dto));
    }

    /**
     * Applies an end-game update to the local client model and renders the view.
     *
     * @param dto data describing the final game result
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void onGameEnded(GameEndedDTO dto) throws RemoteException{
        applyAndRender(() -> view.getClientModel().applyGameEnded(dto));
    }

    /**
     * Checks whether this client callback object is reachable.
     *
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void ping() throws RemoteException{
        // Empty by design: a successful remote invocation is enough to prove reachability.
    }

    /**
     * Receives the leaderboard from the server and forwards it to the view.
     *
     * @param ranking  ordered list of match results
     * @param position position of the client player
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void onLeaderboard(List<MatchResult> ranking, int position) throws RemoteException {
        view.showLeaderboard(ranking, position);
    }

    /**
     * Checks whether the adapter can perform a remote call to the server.
     *
     * @return {@code true} if the server stub is available, {@code false} otherwise
     */
    private boolean isReady(){
        if(!connected || serverStub == null){
            notifyServerOffline();
            return false;
        }

        return true;
    }

    @Override
    public boolean isConnected() {
        return connected && !recovering && serverStub != null;
    }

    /**
     * Handles a failed remote call.
     * If the client was waiting in the lobby, returns directly to the welcome screen.
     * If the client was in a game, starts the automatic recovery loop instead.
     *
     * @param message message shown to the user
     */
    private synchronized void handleRemoteFailure(String message){
        if (!connected) {
            // Another thread (heartbeat or an action call) already detected the failure.
            return;
        }

        connected = false;
        recovering = true;

        if (!view.getClientModel().isInGame()) {
            recovering = false;
            view.goToWelcomeScreen("Server disconnected. Please reconnect.");
            return;
        }

        notifyServerOffline();
        startReconnectLoop();
    }

    private void notifyServerOffline() {
        if (!disconnectionNotified) {
            disconnectionNotified = true;
            view.notifyDisconnection("Server offline. Waiting for recovery...");
        }
    }

    /**
     * Returns the nickname associated with this client.
     *
     * @return the client nickname
     */
    public String getNickname(){
        return nickname;
    }

    /**
     * Requests to reconnect to an existing game session.
     *
     * @param nickname nickname of the reconnecting player
     * @param color    chosen totem color
     */
    @Override
    public void reconnect(String nickname, TotemColor color) {

        if(!isReady()){
            return;
        }

        this.nickname = nickname;
        this.savedColor = color;

        try{
            serverStub.reconnect(nickname, color, this);
        } catch(RemoteException e){
            handleRemoteFailure("Connection with the RMI server lost during recovery.");
        }
    }

    /**
     * Requests the server to discard the saved game during recovery.
     */
    private void declineRecovery() {
        if(!isReady()){
            return;
        }
        try{
            view.getClientModel().reset();
            serverStub.declineRecovery(this);
        } catch(RemoteException e){
            handleRemoteFailure("Connection with the RMI server lost while declining recovery.");
        }
    }

    /**
     * Starts a background thread that pings the server every 3 seconds.
     * If the ping fails, triggers the reconnect flow.
     */
    private void startHeartbeat() {
        heartbeatThread = new Thread(() -> {
            while (connected) {
                try {
                    Thread.sleep(3000);
                    if (connected) serverStub.ping();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (RemoteException e) {
                    if (connected) {
                        handleRemoteFailure("Connection with the RMI server lost.");
                    }
                    return;
                }
            }
        }, "RMI-Heartbeat");
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    /**
     * Starts a background loop that periodically
     * attempts to reconnect to the RMI server.
     */
    private void startReconnectLoop(){
        if (reconnectLoopRunning) {
            return;
        }
        reconnectLoopRunning = true;

        Thread reconnectThread = new Thread(() -> {

            try {
                long deadline = System.currentTimeMillis() + RECONNECT_TIMEOUT_SECONDS * 1000L;
                while (!connected) {
                    if (System.currentTimeMillis() >= deadline) {
                        view.showRecoveryCancelled("Recovery timeout expired. Server did not come back online.");
                        return;
                    }
                    try {
                        Thread.sleep(3000);
                        reconnectToServer();
                    } catch (Exception ignored) {}
                }
            } finally {
                reconnectLoopRunning = false;
            }
        }, "RMI-Reconnect-Loop");

        reconnectThread.start();
    }

    /**
     * Attempts to restore the connection to the RMI server.
     * If successful, the client automatically requests
     * recovery of the previous game session.
     */
    private void reconnectToServer(){

        try{

            Registry registry = LocateRegistry.getRegistry(host, port);

            serverStub = (ServerRMI) registry.lookup(SERVER_NAME);

            connected = true;
            disconnectionNotified = false;
            startHeartbeat();

            if(view.getClientModel().isInGame()){
                ScheduledExecutorService choiceTimeout = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "recovery-choice-timeout");
                    t.setDaemon(true);
                    return t;
                });
                choiceTimeout.schedule(
                    () -> view.shutdown("Recovery timeout. No response to recovery prompt."),
                    RECONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS
                );
                try {
                    if(view.askRecoveryChoice()){
                        reconnect(nickname, savedColor);
                    } else {
                        declineRecovery();
                    }
                    recovering = false;
                } finally {
                    choiceTimeout.shutdownNow();
                }
            } else {
                view.showRecoveryCancelled("Reconnected to server. Please rejoin the lobby.");
                recovering = false;
            }

        }catch(Exception ignored){}
    }
}
