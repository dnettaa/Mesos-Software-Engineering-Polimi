package it.polimi.ingsw.view;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import it.polimi.ingsw.model.player.TotemColor;

/**
 * Text User Interface (TUI) implementation of the View.
 * Handles the main input loop and displays game state using the console.
 */
public class Tui implements View {

    private /* VirtualServer */ Object virtualServer;
    private /* GameStateMessage */ Object lastState;
    private String nickname;
    private final Scanner scanner;

    /**
     * Initializes the TUI and its input scanner.
     */
    public Tui() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setVirtualServer(Object vs) {
        this.virtualServer = vs;
    }

    /**
     * The core loop for the pre-game lobby phase.
     */
    public void run() {
        System.out.println("--- WELCOME TO MESOS ---");
        System.out.print("Please enter your Nickname: ");
        this.nickname = scanner.nextLine();

        boolean running = true;
        while (running) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. Join a Lobby");
            System.out.println("2. Exit");
            System.out.print("> ");

            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                System.out.println("Forwarding request to the VirtualServer...");
                // virtualServer.joinLobby(nickname, color);
                // Once connected, this while loop could be paused or broken,
                // as the GameStateMessage will take over the flow.
            } else if (choice.equals("2")) {
                running = false;
            } else {
                showError("INPUT_ERR", "Invalid command. Please try again.");
            }
        }
        System.out.println("Closing TUI. Goodbye!");
    }

    // --- VIEW INTERFACE METHODS ---

    @Override
    public void showJoinSuccess(String nickname, TotemColor color) {
        System.out.println("\n[SUCCESS] Welcome " + nickname + "! Your assigned color is " + color);
    }

    @Override
    public void showLobbyUpdate(List<String> players, Map<String, TotemColor> colorsByPlayer, int expected) {
        System.out.println("\n----- LOBBY UPDATE -----");
        System.out.println("Connected players: " + players.size() + "/" + expected);
        for (String p : players) {
            System.out.println("- " + p + " (" + colorsByPlayer.get(p) + ")");
        }
    }

    /**
     * Updates the board visualization and handles the phase-based input logic.
     */
    @Override
    public void showGameState(Object state) {
        this.lastState = state;

        // 1. Update board visualization for all players
        System.out.println("\n====== GAME BOARD STATE ======");
        System.out.println("Drawing the board using ASCII art...");
        System.out.println("=================================");

        // --- MOCK VARIABLES ---
        // Replace these with actual getters of the GameStateMessage
        // String currentPlayer = ((GameStateMessage) state).getCurrentPlayerNickname();
        // String currentPhase = ((GameStateMessage) state).getCurrentPhaseName();
        String currentPlayer = this.nickname; // Simulating our turn
        String currentPhase = "TotemPlacementPhase"; // Simulating the first phase

        // 2. Check if it's our turn
        if (currentPlayer.equals(this.nickname)) {
            System.out.println("\n*** It is YOUR turn! ***");
            // 3. Request correct input based on the phase
            handlePhaseInput(currentPhase);
        } else {
            System.out.println("\nIt is " + currentPlayer + "'s turn. Please wait...");
        }
    }

    /**
     * Uses a switch statement to determine which input to ask the user,
     * based on the current game phase.
     * * @param phaseName the name of the current phase
     */
    private void handlePhaseInput(String phaseName) {
        switch (phaseName) {
            case "TotemPlacementPhase":
                System.out.print("Enter the slot letter to place your Totem (e.g., A, B, C): ");
                String slot = scanner.nextLine();
                System.out.println("Sending PlaceTotemMessage for slot '" + slot + "'...");
                // virtualServer.sendMessage(new PlaceTotemMessage(this.nickname, slot.charAt(0)));
                break;

            case "OfferResolutionPhase":
                System.out.print("Enter Upper Row card IDs to take (comma separated, or leave blank): ");
                String upper = scanner.nextLine();
                System.out.print("Enter Lower Row card IDs to take (comma separated, or leave blank): ");
                String lower = scanner.nextLine();
                System.out.println("Sending TakeCardsMessage...");
                // Note: you will need to parse the strings into List<String> before sending
                // virtualServer.sendMessage(new TakeCardsMessage(this.nickname, upperList, lowerList));
                break;

            case "ExtraCardPhase":
                System.out.print("Enter the ID of the extra card to take: ");
                String extra = scanner.nextLine();
                System.out.println("Sending TakeExtraCardMessage for card '" + extra + "'...");
                // virtualServer.sendMessage(new TakeExtraCardMessage(this.nickname, extra));
                break;

            // These phases require no input
            case "EventResolutionPhase":
            case "EndRoundPhase":
            case "EndGamePhase":
                System.out.println("[INFO] Phase '" + phaseName + "' is handled automatically by the server.");
                System.out.println("Waiting for the next state update...");
                break;

            default:
                showError("PHASE_ERR", "Unknown game phase received from server: " + phaseName);
                break;
        }
    }

    @Override
    public void showError(String code, String description) {
        System.err.println("\n[ERROR - " + code + "] " + description);
    }

    @Override
    public void notifyDisconnection(String reason) {
        System.out.println("\n[DISCONNECTED] " + reason);
        System.exit(0);
    }
}