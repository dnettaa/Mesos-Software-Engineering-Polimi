package it.polimi.ingsw.network.server;

import java.rmi.RemoteException;
import java.util.Scanner;
import it.polimi.ingsw.network.rmi.RMIClientAdapter;
import it.polimi.ingsw.network.socket.VirtualSocketServer;
import it.polimi.ingsw.view.GUI.GUI;
import it.polimi.ingsw.view.TUI;
import javafx.application.Application;

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
    public static void main(String[] args) throws RemoteException {

        // Bound RMI calls so a dead connection (e.g. unplugged network) fails fast
        // instead of blocking the heartbeat forever on a silent socket.
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
        System.setProperty("sun.rmi.transport.connectionTimeout", "5000");
        System.setProperty("sun.rmi.transport.proxy.connectTimeout", "5000");

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
        System.out.println(WHITE + "  " + "-".repeat(48) + RESET);
        System.out.println();
        System.out.println(BOLD + "  SELECT INTERFACE:" + RESET);
        System.out.println(CYAN + "  1)" + RESET + " TUI  --Text User Interface");
        System.out.println(CYAN + "  2)" + RESET + " GUI  --Graphic User Interface");
        int choiceInterface = readInt(scanner, "  > ", 1, 2, RED, BOLD, RESET);

        if (choiceInterface == 2) {
            Application.launch(GUI.class, args);
        } else {

            TUI tui = new TUI();

            System.out.println();
            System.out.println(BOLD + "  SELECT CONNECTION:" + RESET);
            System.out.println(CYAN + "  1)" + RESET + " Socket");
            System.out.println(CYAN + "  2)" + RESET + " RMI");
            int choiceConnection = readInt(scanner, "  > ", 1, 2, RED, BOLD, RESET);

            System.out.println();
            System.out.print(BOLD + "  Server IP [localhost]: " + RESET);
            String hostInput = scanner.nextLine().trim();
            String host = hostInput.isEmpty() ? HOST : hostInput;

            if (choiceConnection == 1) {
                VirtualSocketServer socketServer = new VirtualSocketServer(tui);
                tui.setVirtualServer(socketServer);
                socketServer.connect(host, PORT);
            } else {
                RMIClientAdapter rmiClient = new RMIClientAdapter(tui);
                tui.setVirtualServer(rmiClient);
                try {
                    rmiClient.connect(host, RMI_PORT);
                } catch (Exception e) {
                    System.out.println(RED + "  Connection failed: " + e.getMessage() + RESET);
                    return;
                }
            }

            tui.run();
        }
    }

    private static int readInt(Scanner scanner, String prompt, int min, int max,
                               String red, String bold, String reset) {
        while (true) {
            System.out.print(bold + prompt + reset);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {}
            System.out.println(red + "  Please enter a number between " + min + " and " + max + "." + reset);
        }
    }
}
