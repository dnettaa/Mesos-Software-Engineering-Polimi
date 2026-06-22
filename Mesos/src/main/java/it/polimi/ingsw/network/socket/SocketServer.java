package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.controller.GameController;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Listens for incoming client connections on a TCP socket and spawns
 * a dedicated {@link SocketClientHandler} for each connected client.
 * Runs the accept loop on a thread managed by an internal executor.
 *
 * @author Luca Grecchi
 */
public class SocketServer implements Runnable{
    private ServerSocket serverSocket;
    private GameController controller;
    private ExecutorService executor;
    private boolean running;

    /**
     * Creates a new SocketServer with the given game controller.
     * Initializes the server socket and the thread pool.
     *
     * @param controller the game controller to pass to each client handler
     * @throws RuntimeException if the server socket cannot be created
     */
    public SocketServer(GameController controller) {
        try {
            this.serverSocket = new ServerSocket();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create server socket", e);
        }
        this.controller = controller;
        this.executor = Executors.newCachedThreadPool();
        this.running = false;
    }

    /**
     * Binds the server socket to the given port and starts the accept loop
     * on a separate thread.
     *
     * @param port the port to listen on
     * @throws RuntimeException if the socket cannot be bound to the port
     */
    public void start(int port){
        try{
            serverSocket.bind(new InetSocketAddress(port));
            running = true;
            executor.submit(this);
        }catch(IOException e){
            throw new RuntimeException("Failed to bind server socket on port: " + port, e);
        }
    }

    /**
     * Accept loop. For each incoming connection, creates a new
     * {@link SocketClientHandler} and submits it to the thread pool.
     * Runs until {@link #stop()} is called.
     */
    @Override
    public void run(){
        while(running){
            try {
                Socket clientSocket = serverSocket.accept();
                SocketClientHandler handler = new SocketClientHandler(clientSocket, controller);
                executor.submit(handler);
            } catch (IOException e) {
                if (running) throw new RuntimeException("Failed to accept connection", e);
            }
        }
    }

    /**
     * Stops the server by closing the server socket and shutting down
     * the thread pool. Any active client handlers are allowed to finish.
     */
    public void stop(){
        running = false;
        try{
            serverSocket.close();
        }catch(IOException e){
            System.err.println("Failed to close server socket: " + e.getMessage());
        }
        executor.shutdown();
    }
}
