package it.polimi.ingsw;

import it.polimi.ingsw.view.Tui;

import java.util.Scanner;

/**
 * Main entry point for the client application.
 * Allows the user to choose between TUI and GUI at startup.
 */
public class ClientMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Select the interface (1 for TUI, 2 for GUI):");
        System.out.print("> ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            Tui tui = new Tui();
            // tui.setVirtualServer(myVirtualServer);
            tui.run();
        } else {
            System.out.println("GUI is not ready yet. Defaulting to TUI.");
            Tui tui = new Tui();
            tui.run();
        }

        System.out.println("Client application terminated.");
    }
}