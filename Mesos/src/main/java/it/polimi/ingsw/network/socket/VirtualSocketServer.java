package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualServer;
import it.polimi.ingsw.network.socket.message.*;
import it.polimi.ingsw.view.View;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Client-side socket adapter that implements {@link VirtualServer}.
 * Connects to the game server via TCP socket, deserializes incoming
 * {@link ServerMessage} objects and forwards them to the {@link View}.
 * Outgoing {@link ClientMessage} objects are serialized and sent via
 * a synchronized write method to avoid concurrent writes.
 *
 * @author Luca Grecchi
 */
public class VirtualSocketServer implements VirtualServer, Runnable {
    private static final int RECONNECT_TIMEOUT_SECONDS = 20;
    /** A server that sends no message (not even a heartbeat) for this long is considered dead. */
    private static final int READ_TIMEOUT_MS = 6000;
    /** How often a ping is sent to the server to keep its read alive. */
    private static final int PING_INTERVAL_MS = 2000;

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final View view;
    private volatile boolean running;
    private String nickname;
    private String host;
    private int port;
    private TotemColor savedColor;
    private volatile boolean recovering;

    /**
     * Creates a new VirtualSocketServer for the given view.
     * The connection is not established until {@link #connect} is called.
     *
     * @param view the view to forward server messages to
     */
    public VirtualSocketServer(View view) {
        this.view = view;
        this.running = false;
    }

    /**
     * Connects to the server at the given host and port.
     * Initializes the object streams and starts the reader thread.
     *
     * @param host the server host address
     * @param port the server port
     * @throws RuntimeException if the connection cannot be established
     */
    public void connect(String host, int port){
        this.host = host;
        this.port = port;

        try{
            this.socket = new Socket(host, port);
            this.socket.setSoTimeout(READ_TIMEOUT_MS);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());

            this.running = true;
            Thread readerThread = new Thread(this, "VirtualSocketServer-Reader");
            readerThread.start();
            startHeartbeat();
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to server", e);
        }
    }

    /**
     * Starts a background thread that pings the server every {@link #PING_INTERVAL_MS}.
     * The pings keep the server's read alive and let this client's read timeout
     * detect a dead server within {@link #READ_TIMEOUT_MS}.
     */
    private void startHeartbeat(){
        Thread heartbeatThread = new Thread(() -> {
            while(running){
                try{
                    Thread.sleep(PING_INTERVAL_MS);
                    if(running) write(new PingMessage());
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "VirtualSocketServer-Heartbeat");
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    /**
     * Reader loop. Deserializes incoming {@link ServerMessage} objects
     * and applies them to the view. Runs until {@link #disconnect()} is called.
     */
    @Override
    public void run(){
        try {
            while (running) {
                ServerMessage msg = (ServerMessage) in.readObject();
                msg.apply(view);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (running) {
                handleServerCrash();
            }
        }
    }

    /**
     * Handles an unexpected server disconnection.
     * If the client was waiting in the lobby, returns directly to the welcome screen.
     * If the client was in a game, starts the automatic recovery loop instead.
     */
    private synchronized void handleServerCrash(){

        if (!running) {
            // Another thread (reader or heartbeat) already detected the crash.
            return;
        }

        running = false;
        recovering = true;

        if (!view.getClientModel().isInGame()) {
            recovering = false;
            view.goToWelcomeScreen("Server disconnected. Please reconnect.");
            return;
        }

        view.notifyDisconnection(
                "Server offline. Waiting for recovery..."
        );

        startReconnectLoop();
    }

    /**
     * Starts a background loop that periodically
     * attempts to reconnect to the server.
     */
    private void startReconnectLoop(){

        Thread reconnectThread = new Thread(() -> {

            long deadline = System.currentTimeMillis() + RECONNECT_TIMEOUT_SECONDS * 1000L;

            while (!running) {

                if (System.currentTimeMillis() >= deadline) {
                    view.showRecoveryCancelled("Recovery timeout expired. Server did not come back online.");
                    return;
                }

                try {
                    Thread.sleep(3000);
                    reconnect();
                } catch (Exception ignored) {
                }
            }

        }, "Reconnect-Loop");

        reconnectThread.start();
    }

    /**
     * Attempts to restore the socket connection
     * to the server and restart the reader thread.
     */
    private void reconnect(){

        try {

            this.socket = new Socket(host, port);
            this.socket.setSoTimeout(READ_TIMEOUT_MS);

            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();

            this.in = new ObjectInputStream(socket.getInputStream());

            this.running = true;

            Thread readerThread = new Thread(this, "VirtualSocketServer-Reader");
            readerThread.start();
            startHeartbeat();

            if (view.getClientModel().isInGame()) {
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
                    if (view.askRecoveryChoice()) {
                        reconnectToSavedGame();
                    } else {
                        declineRecovery();
                    }
                } finally {
                    choiceTimeout.shutdownNow();
                }
                recovering = false;
            } else {
                view.showRecoveryCancelled("Reconnected to server. Please rejoin the lobby.");
                recovering = false;
            }

        } catch (IOException ignored) {

        }
    }

    /**
     * Sends a request to the server to decline saved-game recovery.
     * Clears the local in-game flag so the client returns to the normal lobby flow.
     */
    private void declineRecovery() {
        view.getClientModel().reset();
        write(new DeclineRecoveryMessage());
    }

    /**
     * Sends an automatic reconnect request
     * using the previously saved player data.
     */
    private void reconnectToSavedGame(){

        write(new ReconnectMessage(nickname, savedColor));
    }

    // --- VirtualServer: client-to-server commands ---

    /**
     * Sends a request to the server to create a new lobby.
     * Wraps the parameters into a {@link CreateLobbyMessage} and writes it to the socket.
     *
     * @param nickname the player's chosen nickname
     * @param color the player's chosen totem color
     * @param expectedPlayers the number of players required to start the game
     */
    @Override
    public void createLobby(String nickname, TotemColor color, int expectedPlayers){
        this.nickname = nickname;
        this.savedColor = color;
        write(new CreateLobbyMessage(nickname, color, expectedPlayers));
    }

    /**
     * Sends a request to the server to join an existing lobby.
     * Wraps the parameters into a {@link JoinLobbyMessage} and writes it to the socket.
     *
     * @param nickname the player's chosen nickname
     * @param color the player's chosen totem color
     */
    @Override
    public void joinLobby(String nickname, TotemColor color){
        this.nickname = nickname;
        this.savedColor = color;
        write(new JoinLobbyMessage(nickname, color));
    }

    /**
     * Sends a command to the server to place a totem on a specific offer slot.
     * Wraps the action into a {@link PlaceTotemMessage} and writes it to the socket.
     *
     * @param nickname the nickname of the player making the move
     * @param slotID the ID of the offer slot (e.g., 'A', 'B', 'C')
     */
    @Override
    public void placeTotem(String nickname, char slotID) {
        write(new PlaceTotemMessage(nickname, slotID));
    }

    /**
     * Sends a command to the server to take selected cards from the board.
     * Wraps the action into a {@link TakeCardsMessage} and writes it to the socket.
     *
     * @param nickname the nickname of the player taking the cards
     * @param upperIDs the list of IDs for the selected cards in the upper row
     * @param lowerIDs the list of IDs for the selected cards in the lower row
     */
    @Override
    public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs, List<String> orderedIDs) {
        write(new TakeCardsMessage(nickname, upperIDs, lowerIDs, orderedIDs));
    }

    /**
     * Sends a command to the server to take an extra card (e.g., triggered by a specific effect).
     * Wraps the action into a {@link TakeExtraCardMessage} and writes it to the socket.
     *
     * @param nickname the nickname of the player taking the extra card
     * @param cardID the ID of the chosen extra card
     */
    @Override
    public void takeExtraCard(String nickname, String cardID) {
        write(new TakeExtraCardMessage(nickname, cardID));
    }

    /**
     * Disconnects from the server by closing the socket and notifying the view.
     */
    @Override
    public void disconnect(){

        running = false;
        view.getClientModel().reset();
        recovering = false;

        try{
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket: " + e.getMessage());
        }
        view.notifyDisconnection("Disconnected from server");
    }

    /**
     * Returns whether the socket connection is currently usable for gameplay commands.
     *
     * @return true if the client is running, not recovering, and the socket is open
     */
    @Override
    public boolean isConnected() {
        return running && !recovering && socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Serializes and sends a message to the server.
     * Synchronized to prevent concurrent writes on the output stream.
     *
     * @param msg the message to write
     */
    private synchronized void write(ClientMessage msg){
        try{
            out.writeObject(msg);
            out.flush();
            out.reset();
        }catch(IOException e){
            if(running){
                handleServerCrash();
            }
        }
    }

    /**
     * Sends a reconnect request to the server.
     *
     * @param nickname nickname of the reconnecting player
     * @param color    chosen totem color
     */
    @Override
    public void reconnect(String nickname, TotemColor color) {

        this.nickname = nickname;
        this.savedColor = color;

        write(new ReconnectMessage(nickname, color));
    }
}
