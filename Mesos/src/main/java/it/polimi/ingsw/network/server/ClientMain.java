package it.polimi.ingsw.network.server;

import java.util.Scanner;
import it.polimi.ingsw.network.rmi.RMIClientAdapter;
import it.polimi.ingsw.network.socket.VirtualSocketServer;
import it.polimi.ingsw.view.TUI;

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

        String RESET  = "\u001B[0m";
        String BOLD   = "\u001B[1m";
        String CYAN   = "\u001B[36m";
        String YELLOW = "\u001B[93m";
        String WHITE  = "\u001B[37m";
        String RED    = "\u001B[31m";

        System.out.println(YELLOW + BOLD);
        System.out.println("  ███╗   ███╗███████╗███████╗ ██████╗ ███████╗");
        System.out.println("  ████╗ ████║██╔════╝██╔════╝██╔═══██╗██╔════╝");
        System.out.println("  ██╔████╔██║█████╗  ███████╗██║   ██║███████╗");
        System.out.println("  ██║╚██╔╝██║██╔══╝  ╚════██║██║   ██║╚════██║");
        System.out.println("  ██║ ╚═╝ ██║███████╗███████║╚██████╔╝███████║");
        System.out.println("  ╚═╝     ╚═╝╚══════╝╚══════╝ ╚═════╝ ╚══════╝");
        System.out.println(RESET);
        System.out.println(CYAN + "          A prehistoric civilization game" + RESET);
        System.out.println(WHITE + "  " + "─".repeat(48) + RESET);
        System.out.println();

        System.out.println(BOLD + "  SELECT INTERFACE:" + RESET);
        System.out.println(CYAN + "  1)" + RESET + " TUI  —  Text User Interface");
        System.out.println(CYAN + "  2)" + RESET + " GUI  —  Graphic User Interface");
        System.out.print(BOLD + "  > " + RESET);
        int choiceInterface = Integer.parseInt(scanner.nextLine().trim());

        if (choiceInterface == 2) {
            // GUI
        } else if (choiceInterface == 1) {

            TUI tui = new TUI();

            System.out.println();
            System.out.println(BOLD + "  SELECT CONNECTION:" + RESET);
            System.out.println(CYAN + "  1)" + RESET + " Socket");
            System.out.println(CYAN + "  2)" + RESET + " RMI");
            System.out.print(BOLD + "  > " + RESET);
            int choiceConnection = Integer.parseInt(scanner.nextLine().trim());

            if (choiceConnection == 1) {
                VirtualSocketServer socketServer = new VirtualSocketServer(tui);
                tui.setVirtualServer(socketServer);
                socketServer.connect(HOST, PORT);
            } else if (choiceConnection == 2) {
                RMIClientAdapter rmiClient = new RMIClientAdapter(tui);
                tui.setVirtualServer(rmiClient);
                rmiClient.connect(HOST, RMI_PORT);
            } else {
                System.out.println(RED + "  Invalid choice." + RESET);
                return;
            }

            tui.run();

        } else {
            System.out.println(RED + "  Invalid choice." + RESET);
        }
    }
}
