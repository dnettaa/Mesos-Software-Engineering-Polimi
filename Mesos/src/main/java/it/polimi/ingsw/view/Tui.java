package it.polimi.ingsw.view;

import it.polimi.ingsw.network.message.GameStateMessage;
import it.polimi.ingsw.network.message.OfferSlotData;
import it.polimi.ingsw.network.message.PlayerData;
import it.polimi.ingsw.model.player.TotemColor;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Text User Interface (TUI) implementation of the View.
 * Handles game state visualization by extracting data from GameStateMessage.
 */
public class Tui implements View {

    private /* VirtualServer */ Object virtualServer;
    private GameStateMessage lastState;
    private String nickname;
    private final Scanner scanner;

    public Tui() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setVirtualServer(Object vs) {
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
    public void showGameState(Object state) {
        if (!(state instanceof GameStateMessage)) return;

        this.lastState = (GameStateMessage) state;

        // 1. Render Header
        System.out.println("\n" + "=".repeat(70));
        System.out.printf(" ROUND: %d | ERA: %s | PHASE: %s %n",
                lastState.getCurrentRound(), lastState.getCurrentEra(), lastState.getCurrentPhaseName());
        System.out.println("=".repeat(70));

        // 2. Render Turn Order
        System.out.print("TURN ORDER: ");
        System.out.println(String.join(" -> ", lastState.getTurnOrder()));

        // 3. Render Offer Track
        System.out.println("\n>>> OFFER TRACK <<<");
        for (OfferSlotData slot : lastState.getOfferSlots()) {
            String occupant = slot.getOccupantNickname() == null ? "FREE" : slot.getOccupantNickname();
            System.out.printf(" [%c] (Up:%d Down:%d Food:%d) : %s %n",
                    slot.getSlotID(), slot.getUpSel(), slot.getDownSel(), slot.getFoodReward(), occupant);
        }

        // 4. Render Card Rows
        System.out.println("\n>>> TOP ROW <<<");
        drawCardRow(lastState.getUpperRowCardIDs());

        System.out.println(">>> BOTTOM ROW <<<");
        drawCardRow(lastState.getLowerRowCardIDs());

        // 5. Render Players Status
        System.out.println(">>> PLAYERS STATS <<<");
        for (PlayerData p : lastState.getPlayers()) {
            System.out.printf(" * %-10s | Food: %-2d | PP: %-3d | Tribe: %d cards %n",
                    p.getNickname(), p.getFood(), p.getPrestigePoints(), p.getTribeCardID().size());
        }
        System.out.println("=".repeat(70));

        // 6. Handle turn-based logic as requested by client flow
        if (lastState.getCurrentPlayerNickname().equals(this.nickname)) {
            System.out.println("\n*** IT IS YOUR TURN! ***");
            handlePhaseInput(lastState.getCurrentPhaseName());
        } else {
            System.out.println("\nWaiting for " + lastState.getCurrentPlayerNickname() + " to play...");
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