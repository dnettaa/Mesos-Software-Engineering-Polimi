package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.controller.GameController;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class SocketServer implements Runnable{
    private ServerSocket serverSocket;
    private GameController controller;
    private ExecutorService executor;
    private boolean running;

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
}
