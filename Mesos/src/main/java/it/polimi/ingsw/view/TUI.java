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

        // ── GAME OVER ──────────────────────────────────────────
        if ("EndGame".equals(clientModel.getCurrentPhaseName())) {
            System.out.println("\n" + YELLOW + BOLD + "╔" + "═".repeat(50) + "╗" + RESET);
            System.out.println(YELLOW + BOLD + "║" + " ".repeat(15) + "🏆  GAME OVER!  🏆" + " ".repeat(15) + "║" + RESET);
            System.out.println(YELLOW + BOLD + "╚" + "═".repeat(50) + "╝" + RESET);

            List<String> ranking = clientModel.getRanking();
            Map<String, Integer> finalPP = clientModel.getFinalPP();
            Map<String, Integer> bonus = clientModel.getEndGameBonus();

            System.out.println(BOLD + "\n  Pos  Giocatore       PP base   Bonus   TOTALE" + RESET);
            System.out.println("  " + "─".repeat(47));
            for (int i = 0; i < ranking.size(); i++) {
                String player = ranking.get(i);
                int total = finalPP.get(player);
                int endBonus = bonus.getOrDefault(player, 0);
                int base = total - endBonus;
                String medal = i == 0 ? BRIGHT_YELLOW + "🥇" : i == 1 ? WHITE + "🥈" : YELLOW + "🥉";
                String color = i == 0 ? BRIGHT_YELLOW : i == 1 ? WHITE : YELLOW;
                System.out.printf("  %s %d.  " + color + "%-14s" + RESET + "  %5d    %5d    " + BOLD + "%5d" + RESET + "%n",
                        medal, i + 1, player, base, endBonus, total);
            }
            return;
        }

        // ── HEADER ────────────────────────────────────────────
        System.out.println("\n" + CYAN + BOLD + "╔" + "═".repeat(58) + "╗" + RESET);
        System.out.printf(CYAN + BOLD + "║" + RESET + "  ROUND: " + BRIGHT_YELLOW + BOLD + "%-3d" + RESET +
                        "  ERA: " + BRIGHT_GREEN + BOLD + "%-5s" + RESET +
                        "  FASE: " + BRIGHT_CYAN + BOLD + "%-25s" + RESET + CYAN + BOLD + "║" + RESET + "%n",
                clientModel.getCurrentRound(),
                clientModel.getCurrentEra(),
                clientModel.getCurrentPhaseName());
        System.out.println(CYAN + BOLD + "╚" + "═".repeat(58) + "╝" + RESET);

        // ── TURN ORDER ────────────────────────────────────────
        System.out.print(BOLD + "  TURNO: " + RESET);
        List<String> turnOrder = clientModel.getTurnOrder();
        for (int i = 0; i < turnOrder.size(); i++) {
            String p = turnOrder.get(i);
            if (p.equals(nickname)) System.out.print(BRIGHT_GREEN + BOLD + "[" + p + "]" + RESET);
            else System.out.print(WHITE + p + RESET);
            if (i < turnOrder.size() - 1) System.out.print(CYAN + " → " + RESET);
        }
        System.out.println("   Carte tribù: " + YELLOW + clientModel.getTribeDeckRemaining() + RESET);

        // ── OFFER TRACK ───────────────────────────────────────
        System.out.println("\n" + MAGENTA + BOLD + "  ╔══ OFFER TRACK " + "═".repeat(42) + "╗" + RESET);
        System.out.println(MAGENTA + "  ║  Slot │  ↑ Upper │  ↓ Lower │  +Food  │ Occupante          ║" + RESET);
        System.out.println(MAGENTA + "  ╠───────┼──────────┼──────────┼─────────┼────────────────────╣" + RESET);
        for (OfferSlotData slot : clientModel.getOfferSlots()) {
            boolean occupied = slot.occupantNickname() != null && !slot.occupantNickname().isEmpty();
            String occ = occupied ? BRIGHT_GREEN + slot.occupantNickname() + RESET : RED + "FREE" + RESET;
            System.out.printf(MAGENTA + "  ║" + RESET + "   " + BOLD + "%c" + RESET + "   │    %d     │    %d     │    %d    │ %-30s" + MAGENTA + "║" + RESET + "%n",
                    slot.slotID(), slot.upSel(), slot.downSel(), slot.foodReward(), occ);
        }
        System.out.println(MAGENTA + "  ╚═══════╧══════════╧══════════╧═════════╧════════════════════╝" + RESET);

        // ── CARD ROWS ─────────────────────────────────────────
        System.out.println("\n" + BRIGHT_YELLOW + BOLD + "  ▶ TOP ROW:" + RESET);
        drawCardRow(clientModel.getUpperRowCardIDs(), BRIGHT_YELLOW);
        System.out.println(BLUE + BOLD + "  ▶ BOTTOM ROW:" + RESET);
        drawCardRow(clientModel.getLowerRowCardIDs(), BLUE);

        // ── PLAYERS ───────────────────────────────────────────
        System.out.println(WHITE + BOLD + "\n  ╔══ PLAYERS " + "═".repeat(47) + "╗" + RESET);
        for (PlayerData p : clientModel.getPlayers().values()) {
            boolean isYou = p.nickname().equals(nickname);
            boolean isCurrent = p.nickname().equals(clientModel.getCurrentPlayerNickname());
            String nameColor = isYou ? BRIGHT_GREEN : WHITE;
            String marker = isCurrent ? BRIGHT_YELLOW + " ◄ TURNO" + RESET : "";
            System.out.printf(WHITE + "  ║ " + RESET + nameColor + BOLD + "%-12s" + RESET +
                            " │ 🍖 " + YELLOW + "%-3d" + RESET +
                            " │ ⭐ " + BRIGHT_CYAN + "%-4d" + RESET +
                            " │ 👥 " + "%-2d" + " carte" +
                            " │ 🏛  " + "%-2d" + " edifici%s%n",
                    p.nickname(), p.food(), p.prestigePoints(),
                    p.tribeCardID().size(), p.buildingID().size(), marker);
            if (!p.tribeCardID().isEmpty())
                System.out.println(WHITE + "  ║   " + RESET + CYAN + "Tribù:    " + RESET + String.join(", ", p.tribeCardID()));
            if (!p.buildingID().isEmpty())
                System.out.println(WHITE + "  ║   " + RESET + MAGENTA + "Edifici:  " + RESET + String.join(", ", p.buildingID()));
        }
        System.out.println(WHITE + BOLD + "  ╚" + "═".repeat(58) + "╝" + RESET);

        // ── ACTION PROMPT ─────────────────────────────────────
        if (clientModel.getCurrentPlayerNickname() != null) {
            if (nickname.equals(clientModel.getCurrentPlayerNickname())) {
                System.out.println("\n" + BRIGHT_GREEN + BOLD + "  *** È IL TUO TURNO! ***" + RESET);
                handleTurnInput(clientModel.getCurrentPhaseName());
            } else {
                System.out.println("\n" + WHITE + "  In attesa di " + YELLOW + clientModel.getCurrentPlayerNickname() + RESET + "...");
            }
        }
    }

    private void drawCardRow(List<String> cardIDs, String color) {
        if (cardIDs == null || cardIDs.isEmpty()) {
            System.out.println("    " + RED + "[Empty]" + RESET);
            return;
        }
        StringBuilder top = new StringBuilder("  ");
        StringBuilder mid = new StringBuilder("  ");
        StringBuilder bot = new StringBuilder("  ");
        for (String id : cardIDs) {
            top.append(color).append("┌───────────┐ ").append(RESET);
            mid.append(color).append("│ ").append(RESET)
                    .append(String.format("%-9s", id))
                    .append(color).append(" │ ").append(RESET);
            bot.append(color).append("└───────────┘ ").append(RESET);
        }
        System.out.println(top);
        System.out.println(mid);
        System.out.println(bot);
    }

    /**
     * Main entry point for the TUI interaction.
     * Handles nickname entry, color selection, and lobby creation/joining.
     */
    public void run() {
        System.out.println("=========================================");
        System.out.println("          WELCOME TO MESOS               ");
        System.out.println("=========================================");

        // 1. Chiedi prima l'azione (Creare o Unirsi)
        System.out.println("\nLOBBY MENU:");
        System.out.println("1) Create a new Lobby");
        System.out.println("2) Join an existing Lobby");
        System.out.print("> ");
        int lobbyChoice = Integer.parseInt(scanner.nextLine().trim());

        // 2. Raccogli i dati del giocatore
        System.out.print("\nPlease enter your Nickname: ");
        this.nickname = scanner.nextLine().trim();

        System.out.println("Available colors: " + java.util.Arrays.toString(TotemColor.values()));
        TotemColor chosenColor = null;
        // Ciclo finché non otteniamo un colore valido
        while (chosenColor == null) {
            System.out.print("Choose your Totem Color: ");
            String colorInput = scanner.nextLine().trim().toUpperCase();

            try {
                chosenColor = TotemColor.valueOf(colorInput);
            } catch (IllegalArgumentException e) {
                System.out.println("[ERRORE] Colore non valido. Per favore, scegli uno dei colori nella lista.");
            }
        }

        if (lobbyChoice == 1) {
            System.out.print("Enter number of expected players: ");
            int players = Integer.parseInt(scanner.nextLine().trim());
            virtualServer.createLobby(nickname, chosenColor, players);
        } else {
            virtualServer.joinLobby(nickname, chosenColor);
        }

        System.out.println("\n[INFO] Request sent. Waiting for game to start...");
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