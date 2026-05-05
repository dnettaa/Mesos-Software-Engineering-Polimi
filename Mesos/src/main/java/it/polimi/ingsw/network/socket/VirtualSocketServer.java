package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualServer;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.view.View;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

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
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final View view;
    private Thread readerThread;
    private boolean running;
    private String nickname;

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
        try{
            this.socket = new Socket(host, port);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());

            this.running = true;
            this.readerThread = new Thread(this, "VirtualSocketServer-Reader");
            this.readerThread.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to server", e);
        }
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
                view.notifyDisconnection("Connection lost: " + e.getMessage());
                disconnect();
            }
        }
    }

    // --- VirtualServer: client-to-server commands ---

    @Override
    public void createLobby(String nickname, TotemColor color, int expectedPlayers){
        this.nickname = nickname;
        write(new CreateLobbyMessage(nickname, color, expectedPlayers));
    }

    @Override
    public void joinLobby(String nickname, TotemColor color){
        this.nickname = nickname;
        write(new JoinLobbyMessage(nickname, color));
    }

    @Override
    public void placeTotem(String nickname, char slotID) {
        write(new PlaceTotemMessage(nickname, slotID));
    }

    @Override
    public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs) {
        write(new TakeCardsMessage(nickname, upperIDs, lowerIDs));
    }

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
        try{
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket: " + e.getMessage());
        }
        view.notifyDisconnection("Disconnected from server");
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
            view.notifyDisconnection("Send failed: " + e.getMessage());
            disconnect();
        }
    }
}
