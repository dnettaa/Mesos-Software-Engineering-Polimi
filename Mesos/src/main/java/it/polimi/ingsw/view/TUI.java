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
 * Implements a "Dumb Client" architecture: relies entirely on the Server for validation.
 */
public class TUI implements View {

    private VirtualServer virtualServer;
    private ClientModel clientModel;
    private String nickname;
    private final Scanner scanner;

    // =========================================================
    // ANSI COLORS
    // =========================================================
    private static final String RESET   = "\u001B[0m";
    private static final String BOLD    = "\u001B[1m";
    private static final String RED     = "\u001B[31m";
    private static final String GREEN   = "\u001B[32m";
    private static final String YELLOW  = "\u001B[33m";
    private static final String BLUE    = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN    = "\u001B[36m";
    private static final String WHITE   = "\u001B[37m";
    private static final String BRIGHT_YELLOW = "\u001B[93m";
    private static final String BRIGHT_GREEN  = "\u001B[92m";
    private static final String BRIGHT_RED    = "\u001B[91m";
    private static final String BRIGHT_CYAN   = "\u001B[96m";

    public TUI() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setVirtualServer(VirtualServer vs) {
        this.virtualServer = vs;
    }

    @Override
    public ClientModel getClientModel() {
        return clientModel;
    }

    @Override
    public void setClientModel(ClientModel model) {
        this.clientModel = model;
    }

    @Override
    public void render() {
        if (clientModel == null) return;

        // ── GAME OVER ──────────────────────────────────────────────────────────
        if ("EndGame".equals(clientModel.getCurrentPhaseName())) {
            System.out.println("\n" + YELLOW + BOLD + "  " + "═".repeat(50) + RESET);
            System.out.println(YELLOW + BOLD + "          🏆  GAME OVER!  🏆" + RESET);
            System.out.println(YELLOW + BOLD + "  " + "═".repeat(50) + RESET);

            List<String> ranking = clientModel.getRanking();
            Map<String, Integer> finalPP = clientModel.getFinalPP();
            Map<String, Integer> bonus = clientModel.getEndGameBonus();

            System.out.println(BOLD + "\n  Pos  Player          Base PP   Bonus   TOTAL" + RESET);
            System.out.println(WHITE + "  " + "─".repeat(47) + RESET);
            for (int i = 0; i < ranking.size(); i++) {
                String player = ranking.get(i);
                int total = finalPP.get(player);
                int endBonus = bonus.getOrDefault(player, 0);
                int base = total - endBonus;
                String medal = i == 0 ? BRIGHT_YELLOW + "🥇" : i == 1 ? WHITE + "🥈" : YELLOW + "🥉";
                String col   = i == 0 ? BRIGHT_YELLOW : i == 1 ? WHITE : YELLOW;
                System.out.printf("  %s %d.  " + col + "%-14s" + RESET + "  %5d    %5d    " + BOLD + "%5d" + RESET + "%n",
                        medal, i + 1, player, base, endBonus, total);
            }
            System.out.println(WHITE + "  " + "─".repeat(47) + RESET);
            return;
        }

        // ── HEADER ─────────────────────────────────────────────────────────────
        System.out.println("\n" + CYAN + BOLD + "  " + "═".repeat(58) + RESET);
        System.out.println("  ROUND: " + BRIGHT_YELLOW + BOLD + clientModel.getCurrentRound() + RESET
                + "   ERA: " + BRIGHT_GREEN + BOLD + clientModel.getCurrentEra() + RESET
                + "   PHASE: " + BRIGHT_CYAN + BOLD + clientModel.getCurrentPhaseName() + RESET);
        System.out.println(CYAN + BOLD + "  " + "═".repeat(58) + RESET);

        // ── TURN ORDER ─────────────────────────────────────────────────────────
        System.out.print(BOLD + "\n  TURN ORDER: " + RESET);
        List<String> turnOrder = clientModel.getTurnOrder();
        for (int i = 0; i < turnOrder.size(); i++) {
            String p = turnOrder.get(i);
            if (p.equals(nickname)) System.out.print(BRIGHT_GREEN + BOLD + "[" + p + "]" + RESET);
            else System.out.print(WHITE + p + RESET);
            if (i < turnOrder.size() - 1) System.out.print(CYAN + " → " + RESET);
        }
        System.out.println("   " + WHITE + "Tribe deck: " + RESET + YELLOW + clientModel.getTribeDeckRemaining() + RESET);

        // ── TOP ROW ────────────────────────────────────────────────────────────
        System.out.println("\n" + BRIGHT_YELLOW + BOLD + "  ▶ TOP ROW" + RESET);
        System.out.println(BRIGHT_YELLOW + "  " + "─".repeat(58) + RESET);
        drawCardRow(clientModel.getUpperRowCardIDs(), BRIGHT_YELLOW);
        System.out.println(BRIGHT_YELLOW + "  " + "─".repeat(58) + RESET);

        // ── OFFER TRACK ────────────────────────────────────────────────────────
        System.out.println(MAGENTA + BOLD + "\n  OFFER TRACK" + RESET);
        System.out.println(MAGENTA + "  " + "─".repeat(48) + RESET);
        System.out.println(MAGENTA + "  Slot    ↑ Upper    ↓ Lower    +Food    Occupant" + RESET);
        System.out.println(MAGENTA + "  " + "─".repeat(48) + RESET);
        for (OfferSlotData slot : clientModel.getOfferSlots()) {
            boolean occupied = slot.occupantNickname() != null && !slot.occupantNickname().isEmpty();
            String occColor = occupied ? BRIGHT_GREEN : RED;
            String occText = occupied ? slot.occupantNickname() : "FREE";
            System.out.println("  " + BOLD + CYAN + slot.slotID() + RESET
                    + "        " + slot.upSel()
                    + "          " + slot.downSel()
                    + "          " + slot.foodReward()
                    + "        " + occColor + occText + RESET);
        }
        System.out.println(MAGENTA + "  " + "─".repeat(48) + RESET);

        // ── BOTTOM ROW ─────────────────────────────────────────────────────────
        System.out.println("\n" + BLUE + BOLD + "  ▶ BOTTOM ROW" + RESET);
        System.out.println(BLUE + "  " + "─".repeat(58) + RESET);
        drawCardRow(clientModel.getLowerRowCardIDs(), BLUE);
        System.out.println(BLUE + "  " + "─".repeat(58) + RESET);

        // ── PLAYERS ────────────────────────────────────────────────────────────
        System.out.println(WHITE + BOLD + "\n  PLAYERS" + RESET);
        System.out.println(WHITE + "  " + "─".repeat(58) + RESET);
        for (PlayerData p : clientModel.getPlayers().values()) {
            boolean isYou     = p.nickname().equals(nickname);
            boolean isCurrent = p.nickname().equals(clientModel.getCurrentPlayerNickname());
            String nameColor  = isYou ? BRIGHT_GREEN : WHITE;
            String marker     = isCurrent ? BRIGHT_YELLOW + " ◄ TURN" + RESET : "";
            System.out.println("  " + nameColor + BOLD + String.format("%-12s", p.nickname()) + RESET
                    + " │ 🍖 " + YELLOW + p.food() + RESET
                    + "  │ ⭐ " + BRIGHT_CYAN + p.prestigePoints() + RESET
                    + "  │ 👥 " + p.tribeCardID().size() + " cards"
                    + "  │ 🏛  " + p.buildingID().size() + " buildings" + marker);
            if (!p.tribeCardID().isEmpty())
                System.out.println("    " + CYAN + "Tribe:     " + RESET + String.join(", ", p.tribeCardID()));
            if (!p.buildingID().isEmpty())
                System.out.println("    " + MAGENTA + "Buildings: " + RESET + String.join(", ", p.buildingID()));
        }
        System.out.println(WHITE + "  " + "─".repeat(58) + RESET);

        // ── ACTION PROMPT ──────────────────────────────────────────────────────
        if (clientModel.getCurrentPlayerNickname() != null) {
            if (nickname.equals(clientModel.getCurrentPlayerNickname())) {
                System.out.println("\n" + BRIGHT_GREEN + BOLD + "  *** IT IS YOUR TURN! ***" + RESET);
                handleTurnInput(clientModel.getCurrentPhaseName());
            } else {
                System.out.println("\n" + WHITE + "  Waiting for " + YELLOW + clientModel.getCurrentPlayerNickname() + RESET + "...");
            }
        }
    }

    private void drawCardRow(List<String> cardIDs, String color) {
        if (cardIDs == null || cardIDs.isEmpty()) {
            System.out.println("    " + RED + "[Empty]" + RESET);
            return;
        }
        for (String id : cardIDs) {
            String desc = CardCatalog.getDescription(id);
            System.out.print(color + BOLD + "  " + String.format("%-6s", id) + RESET);
            System.out.print(WHITE + String.format("%-17s", desc) + RESET);
        }
        System.out.println();
    }

    /**
     * Main entry point for the TUI interaction.
     * Handles nickname entry, color selection, and lobby creation/joining.
     */
    public void run() {
        System.out.println(CYAN + BOLD + "\n  ── LOBBY ──────────────────────────────────────" + RESET);
        System.out.println();

        System.out.println(BOLD + "  LOBBY MENU:" + RESET);
        System.out.println(CYAN + "  1)" + RESET + " Create a new Lobby");
        System.out.println(CYAN + "  2)" + RESET + " Join an existing Lobby");
        System.out.print(BOLD + "  > " + RESET);
        int lobbyChoice = Integer.parseInt(scanner.nextLine().trim());

        System.out.print(BOLD + "\n  Nickname: " + RESET);
        this.nickname = scanner.nextLine().trim();

        System.out.println("\n  Available colors: " + BRIGHT_YELLOW + Arrays.toString(TotemColor.values()) + RESET);
        TotemColor chosenColor = null;
        while (chosenColor == null) {
            System.out.print(BOLD + "  Totem Color: " + RESET);
            String colorInput = scanner.nextLine().trim().toUpperCase();
            try {
                chosenColor = TotemColor.valueOf(colorInput);
            } catch (IllegalArgumentException e) {
                System.out.println(RED + "  Invalid color. Please choose from the list." + RESET);
            }
        }

        if (lobbyChoice == 1) {
            System.out.print(BOLD + "  Number of players (2-5): " + RESET);
            int players = Integer.parseInt(scanner.nextLine().trim());
            virtualServer.createLobby(nickname, chosenColor, players);
        } else {
            virtualServer.joinLobby(nickname, chosenColor);
        }

        System.out.println("\n" + CYAN + "  Request sent. Waiting for game to start..." + RESET);
    }


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

    private void handleTurnInput(String phase) {
        switch (phase) {
            case "TotemPlacementPhase": {
                System.out.print("Select Offer Slot (Enter a letter): ");
                String input = scanner.nextLine().trim().toUpperCase();
                if (!input.isEmpty()) {
                    virtualServer.sendMessage(new PlaceTotemMessage(nickname, input.charAt(0)));
                }
                break;
            }

            case "OfferResolutionPhase": {
                System.out.print("Enter IDs to take from UPPER Row (space separated, or enter to skip): ");
                List<String> up = new ArrayList<>(Arrays.asList(scanner.nextLine().trim().toUpperCase().split("\\s+")));
                up.removeIf(String::isEmpty);

                System.out.print("Enter IDs to take from LOWER Row (space separated, or enter to skip): ");
                List<String> down = new ArrayList<>(Arrays.asList(scanner.nextLine().trim().toUpperCase().split("\\s+")));
                down.removeIf(String::isEmpty);

                virtualServer.sendMessage(new TakeCardsMessage(nickname, up, down));
                break;
            }

            case "ExtraCardPhase": {
                System.out.print("Select Extra Card ID: ");
                String extra = scanner.nextLine().trim().toUpperCase();
                virtualServer.sendMessage(new TakeExtraCardMessage(nickname, extra));
                break;
            }

            default: {
                System.out.println("This phase is automatic. Waiting for server...");
                break;
            }
        }
    }


    @Override
    public void notifyDisconnection(String reason) {
        System.out.println("\n[DISCONNECTED] " + reason);
        System.exit(0);
    }

    @Override
    public void showLoginError(String description) {
        System.out.println("\n\u001B[31m[SETUP ERROR] " + description + "\u001B[0m");
        System.out.println("⚠️ Please re-enter your details.\n");

        run();
    }

    @Override
    public void showGameError(String description) {
        System.out.println("\u001B[31m[GAME ERROR] " + description + "\u001B[0m");
        // Ri-mostra il prompt se è ancora il tuo turno
        if (nickname.equals(clientModel.getCurrentPlayerNickname())) {
            handleTurnInput(clientModel.getCurrentPhaseName());
        }
    }

    @Override
    public void showEventResolved(String eventCardID, String eventType,
                                  Map<String, Integer> ppDelta, Map<String, Integer> foodDelta) {
        System.out.println("\n>>> EVENT RESOLUTION: " + eventCardID + " (" + eventType + ") <<<");
        for (String player : ppDelta.keySet()) {
            int pp = ppDelta.getOrDefault(player, 0);
            int food = foodDelta.getOrDefault(player, 0);
            String ppStr = pp >= 0 ? "+" + pp : String.valueOf(pp);
            String foodStr = food >= 0 ? "+" + food : String.valueOf(food);
            System.out.printf("  %-12s → PP: %s  Food: %s%n", player, ppStr, foodStr);
        }
    }


}