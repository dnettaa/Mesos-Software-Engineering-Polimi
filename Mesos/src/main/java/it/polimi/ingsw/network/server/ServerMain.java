package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameController;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Entry point for the Mesos game server.
 * Starts both the Socket and RMI servers on their respective ports.
 * If either server fails to start, the process exits with an error code.
 *
 * @author Luca Grecchi
 */
public class ServerMain {

    private static final int PORT = 12345;
    private static final int RMI_PORT = 1099;

    /**
     * Starts the server by initializing the GameController and launching
     * both the Socket and RMI servers. If any error occurs during startup,
     * logs the error and terminates the process.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args){
        GameController controller = new GameController();

        try {
            SocketServer socketServer = new SocketServer(controller);
            socketServer.start(PORT);
            System.out.println("Socket server started on port " + PORT);

            RMIServerAdapter rmiAdapter = new RMIServerAdapter(controller);
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            registry.rebind("MesosServer", rmiAdapter);
            System.out.println("RMI server started on port " + RMI_PORT);
        } catch (RemoteException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            System.exit(1);
        }

        System.out.println("Server ready, waiting for connections...");
    }
}
