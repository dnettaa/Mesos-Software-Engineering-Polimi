package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualServer;
import it.polimi.ingsw.network.message.ClientMessage;
import it.polimi.ingsw.network.message.CreateLobbyMessage;
import it.polimi.ingsw.network.message.JoinLobbyMessage;
import it.polimi.ingsw.network.message.ServerMessage;
import it.polimi.ingsw.view.View;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
    private boolean running;

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
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            running = true;
            Thread readerThread = new Thread(this);
            readerThread.start();
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
        while(running){
            try{
                ServerMessage message = (ServerMessage) in.readObject();
                message.apply(view);
            }catch (IOException | ClassNotFoundException e){
                disconnect();
            }
        }
    }

    /**
     * Sends a message to the server by delegating to {@link #write}.
     *
     * @param msg the message to send
     */
    @Override
    public void sendMessage(ClientMessage msg){
        write(msg);
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
     * Sends a {@link CreateLobbyMessage} to the server.
     *
     * @param nickname the nickname of the player creating the lobby
     * @param color the chosen totem color
     * @param expectedPlayers the expected number of players
     */
    @Override
    public void createLobby(String nickname, TotemColor color, int expectedPlayers){
        sendMessage(new CreateLobbyMessage(nickname, color, expectedPlayers));
    }

    /**
     * Sends a {@link JoinLobbyMessage} to the server.
     *
     * @param nickname the nickname of the player joining the lobby
     * @param color the chosen totem color
     */
    @Override
    public void joinLobby(String nickname, TotemColor color){
        sendMessage(new JoinLobbyMessage(nickname, color));
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
        }catch(IOException e){
            disconnect();
        }
    }



}
