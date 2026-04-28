package it.polimi.ingsw.view;

import it.polimi.ingsw.network.VirtualServer;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.model.player.TotemColor;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Text User Interface (TUI) implementation of the View.
 * Handles game state visualization by extracting data from GameStateMessage.
 */
public class Tui implements View {

    private VirtualServer virtualServer;
    private GameStateMessage lastState;
    private String nickname;
    private final Scanner scanner;

    public Tui() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setVirtualServer(VirtualServer vs) {
        this.virtualServer = vs;
    }

    /**
     * Entry point for the TUI input loop.
     */
    public void run() {
        System.out.println("=========================================");
        System.out.println("          WELCOME TO MESOS               ");
        System.out.println("=========================================");
        System.out.print("Please enter your Nickname: ");
        this.nickname = scanner.nextLine();

        System.out.println("Waiting for server messages...");
        // The flow is now managed by showGameState when messages arrive.
    }

    // --- VIEW INTERFACE METHODS ---

    @Override
    public void showJoinSuccess(String nickname, TotemColor color) {
        System.out.println("\n[SUCCESS] Welcome " + nickname + "! Your color is: " + color);
    }

    @Override
    public void showLobbyUpdate(List<String> players, Map<String, TotemColor> colorsByPlayer, int expected) {
        System.out.println("\n--- LOBBY (" + players.size() + "/" + expected + ") ---");
        for (String p : players) {
            System.out.println("- " + p + " (" + colorsByPlayer.get(p) + ")");
        }
    }

    /**
     * Extracts data from GameStateMessage to render the board.
     */
    @Override
    public void showGameState(GameStateMessage state) {
        this.lastState = state;

        System.out.println("\n=================================");
        System.out.println("ROUND: " + state.getCurrentRound()
                + " | ERA: " + state.getCurrentEra()
                + " | PHASE: " + state.getCurrentPhaseName());
        System.out.println("=================================");

        System.out.println("TURN ORDER: " + String.join(" -> ", state.getTurnOrder()));

        System.out.println("\nOFFER TRACK:");
        for (OfferSlotData slot : state.getOfferSlots()) {
            String occ = slot.getOccupantNickname() == null ? "FREE" : slot.getOccupantNickname();
            System.out.printf("[%c] Up:%d Down:%d Food:%d -> %s%n",
                    slot.getSlotID(), slot.getUpSel(), slot.getDownSel(), slot.getFoodReward(), occ);
        }

        System.out.println("\nTOP ROW:");
        printRow(state.getUpperRowCardIDs());

        System.out.println("BOTTOM ROW:");
        printRow(state.getLowerRowCardIDs());

        System.out.println("\nPLAYERS:");
        for (PlayerData p : state.getPlayers()) {
            System.out.printf("%s | Food:%d | PP:%d | Cards:%d%n",
                    p.getNickname(), p.getFood(), p.getPrestigePoints(), p.getTribeCardID().size());
        }

        if (nickname.equals(state.getCurrentPlayerNickname())) {
            System.out.println("\n*** YOUR TURN ***");
            handleInput(state.getCurrentPhaseName());
        } else {
            System.out.println("\nWaiting for " + state.getCurrentPlayerNickname());
        }
    }

    private void printRow(List<String> ids) {
        if (ids.isEmpty()) {
            System.out.println("[Empty]");
            return;
        }
        for (String id : ids) {
            System.out.print("[" + id + "] ");
        }
        System.out.println();
    }

    private void handleInput(String phase) {
        switch (phase) {

            case "TotemPlacementPhase":
                System.out.print("Choose slot: ");
                String slot = scanner.nextLine();
                if (!slot.isEmpty()) {
                    virtualServer.sendMessage(
                            new PlaceTotemMessage(nickname, slot.toUpperCase().charAt(0))
                    );
                }
                break;

            case "OfferResolutionPhase":
                System.out.print("Upper IDs: ");
                List<String> up = List.of(scanner.nextLine().split(" "));
                System.out.print("Lower IDs: ");
                List<String> down = List.of(scanner.nextLine().split(" "));

                virtualServer.sendMessage(
                        new TakeCardsMessage(nickname, up, down)
                );
                break;

            case "ExtraCardPhase":
                System.out.print("Extra card ID: ");
                String extra = scanner.nextLine();

                virtualServer.sendMessage(
                        new TakeExtraCardMessage(nickname, extra)
                );
                break;

            default:
                System.out.println("Waiting...");
        }
    }

    /**
     * Helper to draw IDs inside ASCII boxes for visual consistency.
     */
    private void drawCardRow(List<String> cardIDs) {
        if (cardIDs.isEmpty()) {
            System.out.println("  [Empty]\n");
            return;
        }
        StringBuilder top = new StringBuilder();
        StringBuilder mid = new StringBuilder();
        StringBuilder bot = new StringBuilder();

        for (String id : cardIDs) {
            top.append("┌───────────┐ ");
            mid.append(String.format("│ %-9s │ ", id));
            bot.append("└───────────┘ ");
        }
        System.out.println(top + "\n" + mid + "\n" + bot + "\n");
    }

    /**
     * Phase-based input handling using switch.
     */
    private void handlePhaseInput(String phaseName) {
        switch (phaseName) {
            case "TotemPlacementPhase":
                System.out.print("Select Offer Slot (A-G): ");
                String slot = scanner.nextLine();
                // Send PlaceTotemMessage(nickname, slot.charAt(0)) via virtualServer
                break;

            case "OfferResolutionPhase":
                System.out.print("Enter IDs to take (Upper Row): ");
                String up = scanner.nextLine();
                System.out.print("Enter IDs to take (Lower Row): ");
                String down = scanner.nextLine();
                // Send TakeCardsMessage via virtualServer
                break;

            case "ExtraCardPhase":
                System.out.print("Select Extra Card ID: ");
                String extra = scanner.nextLine();
                // Send TakeExtraCardMessage via virtualServer
                break;

            default:
                System.out.println("Automatic phase. Waiting for server...");
                break;
        }
    }

    @Override
    public void showError(String code, String description) {
        System.err.println("\n[ERROR " + code + "] " + description);
    }

    @Override
    public void notifyDisconnection(String reason) {
        System.out.println("\n[DISCONNECTED] " + reason);
        System.exit(0);
    }
}