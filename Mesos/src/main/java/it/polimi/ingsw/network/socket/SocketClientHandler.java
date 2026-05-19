package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.leaderboard.MatchResult;
import it.polimi.ingsw.model.game.DTO.*;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.network.socket.message.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Handles the server-side communication with a single client connected via Socket.
 * Reads incoming {@link ClientMessage} objects from the socket and dispatches them
 * to the {@link GameController}. Outgoing {@link ServerMessage} objects are queued
 * in an outbox and sent by a dedicated writer thread to avoid blocking.
 *
 * @author Luca Grecchi
 */
public class SocketClientHandler implements VirtualView, Runnable {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final GameController controller;
    private String nickname;
    private boolean connected;
    private final BlockingQueue<ServerMessage> outbox;

    /**
     * Creates a new handler for the given client socket.
     * Initializes the object streams and the outbox queue.
     * Note: ObjectOutputStream must be opened before ObjectInputStream
     * to avoid deadlock.
     *
     * @param socket the client socket
     * @param controller the game controller to dispatch messages to
     * @throws RuntimeException if the streams cannot be initialized
     */
    public SocketClientHandler(Socket socket, GameController controller) {
        try {
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize streams", e);
        }
        this.controller = controller;
        this.nickname = null;
        this.connected = true;
        this.outbox = new LinkedBlockingQueue<>();
    }

    /**
     * Starts the writer thread and enters the reader loop.
     * Deserializes incoming messages and dispatches them to the controller.
     * On error, calls {@link #disconnect()}.
     */
    @Override
    public void run(){
        Thread writerThread = new Thread(this::writerLoop);
        writerThread.setDaemon(true);
        writerThread.start();

        while(connected){
            try{
                ClientMessage message = (ClientMessage) in.readObject();
                message.execute(controller, this);
            }catch (IOException | ClassNotFoundException e){
                if(connected) disconnect();
            }
        }
    }

    /**
     * Notifies the client of a successful lobby join.
     * Wraps the data into a {@link JoinSuccessMessage} and enqueues it.
     *
     * @param nickname the assigned nickname
     * @param color the assigned totem color
     */
    @Override
    public void onJoinSuccess(String nickname, TotemColor color){
        enqueue(new JoinSuccessMessage(nickname, color));
    }

    /**
     * Notifies the client of an update to the lobby state.
     * Wraps the data into a {@link LobbyUpdateMessage} and enqueues it.
     *
     * @param players the list of players currently in the lobby
     * @param colorsByPlayer a map of player nicknames to their respective totem colors
     * @param expected the total number of players expected to start the game
     */
    @Override
    public void onLobbyUpdate(List<String> players, Map<String,TotemColor> colorsByPlayer, int expected){
        enqueue(new LobbyUpdateMessage(players, colorsByPlayer, expected));
    }

    /**
     * Notifies the client of an error.
     * Wraps the error details into an {@link ErrorMessage} and enqueues it.
     *
     * @param code the error code
     * @param desc the error description
     */
    @Override
    public void onError(String code, String desc){
        enqueue(new ErrorMessage(code, desc));
    }

    /**
     * Notifies the client of a disconnection event.
     * Wraps the reason into a {@link DisconnectionMessage} and enqueues it.
     *
     * @param reason the reason for disconnection
     */
    @Override
    public void onDisconnection(String reason){
        enqueue(new DisconnectionMessage(reason));
    }

    /**
     * Notifies the client that recovery was canceled without
     * closing the socket
     *
     * @param reason the reason shown to the user
     */
    @Override
    public void onRecoveryCancelled(String reason) {
        enqueue(new RecoveryCancelledMessage(reason));
    }


    @Override
    public void onRecoveryUpdate(List<String> reconnectedPlayers, List<String> missingPlayers) {
        enqueue(new RecoveryUpdateMessage(reconnectedPlayers, missingPlayers));
    }

    /**
     * Notifies the client that the game has started.
     * Wraps the initial game state snapshot into a {@link GameStartedMessage} and enqueues it.
     *
     * @param snapshot the complete snapshot of the initial game state
     */
    @Override
    public void onGameStarted(GameStateSnapshot snapshot){
        enqueue(new GameStartedMessage(snapshot));
    }

    /**
     * Notifies the client that a totem has been placed on the offer track.
     * Wraps the event data into a {@link TotemPlacedMessage} and enqueues it.
     *
     * @param dto the data transfer object containing details of the placement
     */
    @Override
    public void onTotemPlaced(TotemPlacedDTO dto){
        enqueue(new TotemPlacedMessage(dto));
    }

    /**
     * Notifies the client that cards have been taken from the board.
     * Wraps the event data into a {@link CardsTakenMessage} and enqueues it.
     *
     * @param dto the data transfer object containing details of the taken cards
     */
    @Override
    public void onCardsTaken(CardsTakenDTO dto){
        enqueue(new CardsTakenMessage(dto));
    }

    /**
     * Notifies the client that an extra card has been taken during the Extra Card Phase.
     * Wraps the event data into an {@link ExtraCardTakenMessage} and enqueues it.
     *
     * @param dto the data transfer object containing details of the extra card action
     */
    @Override
    public void onExtraCardTaken(ExtraCardTakenDTO dto){
        enqueue(new ExtraCardTakenMessage(dto));
    }

    /**
     * Notifies the client that an event card has been resolved.
     * Wraps the event data into an {@link EventResolvedMessage} and enqueues it.
     *
     * @param dto the data transfer object containing details of the resolved event
     */
    @Override
    public void onEventResolved(EventResolvedDTO dto){
        enqueue(new EventResolvedMessage(dto));
    }

    /**
     * Notifies the client that the current round has ended.
     * Wraps the round end data into a {@link RoundEndedMessage} and enqueues it.
     *
     * @param dto the data transfer object containing details of the board refresh and next round
     */
    @Override
    public void onRoundEnded(RoundEndedDTO dto){
        enqueue(new RoundEndedMessage(dto));
    }

    /**
     * Notifies the client that the game has ended.
     * Wraps the final scoring data into a {@link GameEndedMessage} and enqueues it.
     *
     * @param dto the data transfer object containing final scores and the winner ranking
     */
    @Override
    public void onGameEnded(GameEndedDTO dto){
        enqueue(new GameEndedMessage(dto));
    }

    /**
     * Returns the nickname of the player associated with this handler.
     *
     * @return the player nickname, or null if not yet set
     */
    @Override
    public String getNickname(){
        return nickname;
    }

    /**
     * Sets the nickname of the player associated with this handler.
     *
     * @param nickname the player nickname
     */
    @Override
    public void setNickname(String nickname){
        this.nickname = nickname;
    }


    /**
     * Disconnects the client by closing the socket and notifying the controller.
     */
    @Override
    public void disconnect(){
        connected = false;
        try{
            socket.close();
        }catch(IOException e){
            System.err.println("Failed to close socket: " + e.getMessage());
        }
        if(nickname != null){
            controller.onDisconnect(nickname);
        }
    }

    /**
     * Returns whether the client is currently connected.
     *
     * @return true if connected, false otherwise
     */
    @Override
    public boolean isConnected(){
        return connected;
    }

    /**
     * Writer loop that runs on a dedicated thread.
     * Takes messages from the outbox and serializes them to the socket output stream.
     * On error, calls {@link #disconnect()}.
     */
    private void writerLoop(){
        while(connected) {
            try {
                ServerMessage message = outbox.take();
                out.writeObject(message);
                out.flush();
                out.reset();
            } catch (InterruptedException | IOException e) {
                if (connected) disconnect();
            }
        }
    }

    /**
     * Adds a {@link ServerMessage} to the outbox queue to be sent to the client.
     *
     * @param msg the message to be queued
     */
    private void enqueue(ServerMessage msg) {
        outbox.add(msg);
    }

    /**
     * Notifies the client with the current leaderboard.
     * Wraps the ranking data into a {@link LeaderboardMessage} and enqueues it.
     *
     * @param ranking the list of match results sorted by score
     * @param position the position of the player in the ranking
     */
    @Override
    public void onLeaderboard(List<MatchResult> ranking, int position){
        enqueue(new LeaderboardMessage(ranking, position));
    }
}
