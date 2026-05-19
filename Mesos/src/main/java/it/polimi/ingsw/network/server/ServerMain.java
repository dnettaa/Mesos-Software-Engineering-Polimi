package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.RecoveryPhase;
import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.network.rmi.RMIServerAdapter;
import it.polimi.ingsw.network.socket.SocketServer;
import it.polimi.ingsw.persistence.PersistenceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {

    private static final int PORT = 12345;
    private static final int RMI_PORT = 1099;

    public static void main(String[] args){
        GameController controller = new GameController();

        System.out.println("Verifica salvataggi precedenti in corso...");
        GameActions savedGame = PersistenceManager.loadGame();

        if (savedGame != null) {
            controller.setGame(savedGame);

            System.out.println("=== MODALITÀ RECOVERY ===");
            System.out.println("In attesa riconnessione giocatori...");

            controller.transitionTo(new RecoveryPhase(controller, savedGame));

        } else {
            System.out.println("Nessun salvataggio trovato. Avvio server normale.");
        }

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