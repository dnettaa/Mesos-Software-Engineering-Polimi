package it.polimi.ingsw.network.rmi;

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
    private final View view;
    private ServerRMI serverStub;
    private String nickname;
    private boolean connected;
    private static final Set<String> LOGIN_ERROR_CODES = Set.of(
            "NICKNAME_TAKEN",
            "COLOR_TAKEN",
            "LOBBY_FULL",
            "GAME_ALREADY_STARTED",
            "LOBBY_NOT_CREATED"
    );
    private final ExecutorService renderExecutor = Executors.newSingleThreadExecutor();

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
    public void connect(String host, int port){
        try{
            Registry registry = LocateRegistry.getRegistry(host, port);
            serverStub = (ServerRMI) registry.lookup(SERVER_NAME);
            connected = true;
        } catch(Exception e){
            connected = false;
            view.notifyDisconnection("Unable to connect to RMI server: " + e.getMessage());
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
        if(!isReady()){
            return;
        }

        this.nickname = nickname;

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
        if(!isReady()){
            return;
        }

        this.nickname = nickname;

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
    public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs){
        if(!isReady()){
            return;
        }

        try{
            serverStub.takeCards(nickname, upperIDs, lowerIDs);
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
        if (LOGIN_ERROR_CODES.contains(code)) {
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
        view.notifyDisconnection(reason);
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
    public void onEventResolved(EventResolvedDTO dto) throws RemoteException{
        applyAndRender(() -> view.getClientModel().applyEventResolved(dto));
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
     * Checks whether the adapter can perform a remote call to the server.
     *
     * @return {@code true} if the server stub is available, {@code false} otherwise
     */
    private boolean isReady(){
        if(!connected || serverStub == null){
            view.notifyDisconnection("RMI client is not connected to the server.");
            return false;
        }

        return true;
    }

    /**
     * Handles a failed remote call by marking the connection as closed and notifying the view.
     *
     * @param message message shown to the user
     */
    private void handleRemoteFailure(String message){
        connected = false;
        view.notifyDisconnection(message);
    }

    /**
     * Returns the nickname associated with this client.
     *
     * @return the client nickname
     */
    public String getNickname(){
        return nickname;
    }
}