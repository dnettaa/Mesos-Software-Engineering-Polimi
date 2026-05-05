package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.exception.ErrorCode;
import it.polimi.ingsw.model.game.DTO.*;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.controller.GameController;

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

    @Override
    public void onJoinSuccess(String nickname, TotemColor color){
        enqueue(new JoinSuccessMessage(nickname, color));
    }

    @Override
    public void onLobbyUpdate(List<String> players, Map<String,TotemColor> colorsByPlayer, int expected){
        enqueue(new LobbyUpdateMessage(players, colorsByPlayer, expected));
    }

    @Override
    public void onError(String code, String desc){
        enqueue(new ErrorMessage(code, desc));
    }

    @Override
    public void onDisconnection(String reason){
        enqueue(new DisconnectionMessage(reason));
    }

    @Override
    public void onGameStarted(GameStateSnapshot snapshot){
        enqueue(new GameStartedMessage(snapshot));
    }

    @Override
    public void onTotemPlaced(TotemPlacedDTO dto){
        enqueue(new TotemPlacedMessage(dto));
    }

    @Override
    public void onCardsTaken(CardsTakenDTO dto){
        enqueue(new CardsTakenMessage(dto));
    }

    @Override
    public void onExtraCardTaken(ExtraCardTakenDTO dto){
        enqueue(new ExtraCardTakenMessage(dto));
    }

    @Override
    public void onEventResolved(EventResolvedDTO dto){
        enqueue(new EventResolvedMessage(dto));
    }

    @Override
    public void onRoundEnded(RoundEndedDTO dto){
        enqueue(new RoundEndedMessage(dto));
    }

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
        controller.onDisconnect(nickname);
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
            } catch (InterruptedException | IOException e) {
                if (connected) disconnect();
            }
        }
    }

    private void enqueue(ServerMessage msg) {
        outbox.add(msg);
    }
}
