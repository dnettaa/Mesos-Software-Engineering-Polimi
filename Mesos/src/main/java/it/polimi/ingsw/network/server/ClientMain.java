package it.polimi.ingsw.network.server;

import java.util.Scanner;

/**
 * Entry point for the Mesos game client.
 * Asks the user to choose between TUI and GUI, and between Socket and RMI.
 * Creates the appropriate view and network adapter, connects to the server,
 * and starts the client.
 *
 * @author Luca Grecchi
 */
public class ClientMain {

    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    private static final int RMI_PORT = 1099;

    /**
     * Starts the client by prompting the user to select the interface type
     * and connection technology, then connects to the server and launches the view.
     *
     * @param args command-line arguments (not used)
     * @throws IllegalArgumentException if the user enters an invalid choice
     */
    public static void main(String args[]){

        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose interface: 1) TUI  2) GUI");
        int choiceInterface = Integer.parseInt(scanner.nextLine().trim());

        if(choiceInterface == 2){
            GUI gui = new GUI();
            gui.run();
        }else if(choiceInterface == 1){

            TUI tui = new TUI();

            System.out.println("Choose connection type: 1) Socket  2) RMI");
            int choiceConnection = Integer.parseInt(scanner.nextLine().trim());

            if(choiceConnection == 1){
                VirtualSocketServer socketServer = new VirtualSocketServer(tui);
                tui.setVirtualServer(socketServer);
                socketServer.connect(HOST, PORT);
            }else if(choiceConnection == 2){
                RMIClientAdapter rmiClient = new RMIClientAdapter(tui);
                tui.setVirtualServer(rmiClient);
                rmiClient.connect(HOST, RMI_PORT);
            }else{
                throw new IllegalArgumentException("Chose 1 or 2!");
            }

            tui.run();

        }else{
            throw new IllegalArgumentException("Chose 1 or 2!");
        }
    }
}
