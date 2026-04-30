package it.polimi.ingsw.view;

import it.polimi.ingsw.model.game.DTO.OfferSlotData;
import it.polimi.ingsw.model.game.DTO.PlayerData;
import it.polimi.ingsw.network.VirtualServer;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.model.player.TotemColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Text User Interface (TUI) implementation of the View.
 * Final version integrated with the actual Network and Message classes.
 */
public class TUI implements View {

    private VirtualServer virtualServer;
    private GameStateMessage lastState;
    private String nickname;
    private final Scanner scanner;

    public TUI() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setVirtualServer(VirtualServer vs) {
        this.virtualServer = vs;
    }

    /**
     * Main entry point for the TUI interaction.
     * Handles nickname entry, color selection, and lobby creation/joining.
     */
    public void run() {
        System.out.println("=========================================");
        System.out.println("          WELCOME TO MESOS               ");
        System.out.println("=========================================");

        System.out.print("Please enter your Nickname: ");
        this.nickname = scanner.nextLine().trim();

        // 1. Pick a Color
        System.out.println("\nAvailable colors: " + Arrays.toString(TotemColor.values()));
        System.out.print("Choose your Totem Color: ");
        String colorInput = scanner.nextLine().trim().toUpperCase();
        TotemColor chosenColor = TotemColor.valueOf(colorInput);

        // 2. Lobby Menu
        System.out.println("\nLOBBY MENU:");
        System.out.println("1) Create a new Lobby");
        System.out.println("2) Join an existing Lobby");
        System.out.print("> ");
        int lobbyChoice = Integer.parseInt(scanner.nextLine().trim());

        if (lobbyChoice == 1) {
            System.out.print("Enter number of expected players: ");
            int players = Integer.parseInt(scanner.nextLine().trim());
            virtualServer.createLobby(nickname, chosenColor, players); //
        } else {
            virtualServer.joinLobby(nickname, chosenColor); //
        }

        System.out.println("\n[INFO] Request sent. Waiting for game to start...");
        // From now on, showGameState will be called by the network thread when updates arrive.
    }

    // --- VIEW INTERFACE METHODS ---

    @Override
    public void showJoinSuccess(String nickname, TotemColor color) {
        System.out.println("\n[SUCCESS] Welcome " + nickname + "! Your color is: " + color);
    }

    @Override
    public void showLobbyUpdate(List<String> players, Map<String, TotemColor> colorsByPlayer, int expected) {
        System.out.println("\n--- LOBBY STATUS (" + players.size() + "/" + expected + ") ---");
        for (String p : players) {
            System.out.println("- " + p + " (" + colorsByPlayer.get(p) + ")");
        }
    }

    @Override
    public void showGameState(GameStateMessage state) {
        this.lastState = state;

        // Render Board Header
        System.out.println("\n" + "=".repeat(40));
        System.out.printf(" ROUND: %d | ERA: %s | PHASE: %s %n",
                state.getCurrentRound(), state.getCurrentEra(), state.getCurrentPhaseName());
        System.out.println("=".repeat(40));

        // Turn Order
        System.out.println("TURN ORDER: " + String.join(" -> ", state.getTurnOrder()));

        // Offer Track
        System.out.println("\n>>> OFFER TRACK <<<");
        for (OfferSlotData slot : state.getOfferSlots()) {
            String occ = slot.getOccupantNickname() == null ? "FREE" : slot.getOccupantNickname();
            System.out.printf("[%c] Up:%d Down:%d Food:%d -> %s%n",
                    slot.getSlotID(), slot.getUpSel(), slot.getDownSel(), slot.getFoodReward(), occ);
        }

        // ASCII Card Rows
        System.out.println("\nTOP ROW:");
        drawCardRow(state.getUpperRowCardIDs());
        System.out.println("BOTTOM ROW:");
        drawCardRow(state.getLowerRowCardIDs());

        // Player Stats
        System.out.println(">>> PLAYERS <<<");
        for (PlayerData p : state.getPlayers()) {
            System.out.printf("%-10s | Food:%d | PP:%d | Tribe:%d cards%n",
                    p.getNickname(), p.getFood(), p.getPrestigePoints(), p.getTribeCardID().size());
        }

        // Action Logic
        if (nickname.equals(state.getCurrentPlayerNickname())) {
            System.out.println("\n*** IT IS YOUR TURN! ***");
            handleTurnInput(state.getCurrentPhaseName());
        } else {
            System.out.println("\nWaiting for " + state.getCurrentPlayerNickname() + "...");
        }
    }

    private void drawCardRow(List<String> cardIDs) {
        if (cardIDs.isEmpty()) { System.out.println("  [Empty]\n"); return; }
        StringBuilder top = new StringBuilder(), mid = new StringBuilder(), bot = new StringBuilder();
        for (String id : cardIDs) {
            top.append("┌───────────┐ ");
            mid.append(String.format("│ %-9s │ ", id));
            bot.append("└───────────┘ ");
        }
        System.out.println(top + "\n" + mid + "\n" + bot);
    }

    private void handleTurnInput(String phase) {
        switch (phase) {
            case "TotemPlacementPhase":
                System.out.print("Select Offer Slot (A-G): ");
                String slot = scanner.nextLine().trim().toUpperCase();
                virtualServer.sendMessage(new PlaceTotemMessage(nickname, slot.charAt(0)));
                break;

            case "OfferResolutionPhase":
                System.out.print("Enter IDs to take from Upper Row (space separated, or enter to skip): ");
                List<String> up = new ArrayList<>(Arrays.asList(scanner.nextLine().trim().split("\\s+")));
                up.removeIf(String::isEmpty);

                System.out.print("Enter IDs to take from Lower Row (space separated, or enter to skip): ");
                List<String> down = new ArrayList<>(Arrays.asList(scanner.nextLine().trim().split("\\s+")));
                down.removeIf(String::isEmpty);

                virtualServer.sendMessage(new TakeCardsMessage(nickname, up, down));
                break;

            case "ExtraCardPhase":
                System.out.print("Select Extra Card ID: ");
                String extra = scanner.nextLine().trim();
                virtualServer.sendMessage(new TakeExtraCardMessage(nickname, extra));
                break;

            default:
                System.out.println("This phase is automatic. Waiting for server...");
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