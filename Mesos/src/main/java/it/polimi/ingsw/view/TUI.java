package it.polimi.ingsw.view;

import it.polimi.ingsw.model.game.DTO.OfferSlotData;
import it.polimi.ingsw.model.game.DTO.PlayerData;
import it.polimi.ingsw.network.VirtualServer;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.socket.message.PlaceTotemMessage;
import it.polimi.ingsw.network.socket.message.TakeCardsMessage;
import it.polimi.ingsw.network.socket.message.TakeExtraCardMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Text User Interface (TUI) implementation of the View for the Mesos game.
 * <p>
 * This class provides a rich console-based interface utilizing ANSI escape codes
 * for colored and formatted output. It strictly adheres to a "Dumb Client"
 * architecture: it relies entirely on the server for rule validation and game logic,
 * maintaining only a local replica of the state ({@link ClientModel}) updated via
 * delta messages.
 * </p>
 */
public class TUI implements View {

    /** Reference to the network layer to send messages to the server. */
    private VirtualServer virtualServer;

    /** Local replica of the game state, updated by the server's delta messages. */
    private ClientModel clientModel;

    /** The chosen nickname of the player using this client. */
    private String nickname;

    /** Scanner used to read standard input from the user. */
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

    /**
     * Constructs a new TUI instance and initializes the input scanner.
     */
    public TUI() {
        this.clientModel = new ClientModel();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Links the view to the network communication layer.
     *
     * @param vs The {@link VirtualServer} instance managing network output.
     */
    @Override
    public void setVirtualServer(VirtualServer vs) {
        this.virtualServer = vs;
    }

    /**
     * Retrieves the local replica of the game state.
     *
     * @return The current {@link ClientModel} associated with this view.
     */
    @Override
    public ClientModel getClientModel() {
        return clientModel;
    }

    /**
     * Sets the local replica of the game state.
     *
     * @param model The {@link ClientModel} to be stored and rendered.
     */
    @Override
    public void setClientModel(ClientModel model) {
        this.clientModel = model;
    }

    /**
     * Core rendering method. Reads the current state from the {@link ClientModel}
     * and draws the entire graphical board on the console using ASCII art and ANSI colors.
     * <p>
     * It handles the display of the End-Game leaderboard as well as the standard
     * in-game board (Turn Order, Offer Track, Card Rows, and Player Stats).
     * Automatically triggers the input prompt if it is the local player's turn.
     * </p>
     */
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

    /**
     * Helper method to render a horizontal row of cards.
     * Integrates with the CardCatalog to fetch and display card descriptions.
     *
     * @param cardIDs The list of string IDs representing the cards in the row.
     * @param color   The ANSI color code used to format the card IDs.
     */
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
     * Main entry point for the TUI execution.
     * Guides the user through a setup wizard to input their nickname,
     * select a Totem color, and either create or join a game lobby.
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

    /**
     * Displays a success message confirming the user has joined the lobby.
     *
     * @param nickname The accepted nickname of the user.
     * @param color    The accepted Totem color of the user.
     */
    @Override
    public void showJoinSuccess(String nickname, TotemColor color) {
        System.out.println("\n[SUCCESS] Welcome " + nickname + "! Your color is: " + color);
    }

    /**
     * Displays the current status of the lobby as players connect.
     *
     * @param players        The list of currently connected player nicknames.
     * @param colorsByPlayer A mapping linking each player to their chosen color.
     * @param expected       The total number of players required to start the game.
     */
    @Override
    public void showLobbyUpdate(List<String> players, Map<String, TotemColor> colorsByPlayer, int expected) {
        System.out.println("\n--- LOBBY STATUS (" + players.size() + "/" + expected + ") ---");
        for (String p : players) {
            System.out.println("- " + p + " (" + colorsByPlayer.get(p) + ")");
        }
    }

    /**
     * Gathers user input based on the current active phase of the game and
     * sends the corresponding network messages to the server.
     *
     * @param phase The string identifier of the current game phase
     *              (e.g., "TotemPlacementPhase", "OfferResolutionPhase").
     */
    private void handleTurnInput(String phase) {
        switch (phase) {
            case "TotemPlacementPhase": {
                System.out.print("Select Offer Slot (Enter a letter): ");
                String input = scanner.nextLine().trim().toUpperCase();
                if (!input.isEmpty()) {
                    virtualServer.placeTotem(nickname, input.charAt(0));
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

                virtualServer.takeCards(nickname, up, down);
                break;
            }

            case "ExtraCardPhase": {
                System.out.print("Select Extra Card ID: ");
                String extra = scanner.nextLine().trim().toUpperCase();
                virtualServer.takeExtraCard(nickname, extra);
                break;
            }

            default: {
                System.out.println("This phase is automatic. Waiting for server...");
                break;
            }
        }
    }

    /**
     * Notifies the user that the connection to the server has been lost.
     *
     * @param reason A string detailing why the disconnection occurred.
     */
    @Override
    public void notifyDisconnection(String reason) {
        System.out.println("\n[DISCONNECTED] " + reason);
    }

    /**
     * Displays setup or networking errors that occur before the game starts
     * (e.g., Lobby full, Name already taken) and restarts the setup wizard.
     *
     * @param description The human-readable error description.
     */
    @Override
    public void showLoginError(String description) {
        System.out.println("\n\u001B[31m[SETUP ERROR] " + description + "\u001B[0m");
        System.out.println("⚠️ Please re-enter your details.\n");

        run();
    }

    /**
     * Displays errors regarding rule violations during gameplay.
     * If it is still the user's turn, it prompts them to input a valid move again.
     *
     * @param description The human-readable error description from the server's GameController.
     */
    @Override
    public void showGameError(String description) {
        System.out.println("\u001B[31m[GAME ERROR] " + description + "\u001B[0m");
        // Re-open input prompt if it is still the local player's turn
        if (nickname.equals(clientModel.getCurrentPlayerNickname())) {
            handleTurnInput(clientModel.getCurrentPhaseName());
        }
    }

    /**
     * Displays a summary of the effects resolved from playing an Event Card,
     * detailing how it impacted the resources of the players.
     *
     * @param eventCardID The identifier of the resolved event card.
     * @param eventType   The semantic type or name of the event.
     * @param ppDelta     A map linking player nicknames to their Prestige Point variation.
     * @param foodDelta   A map linking player nicknames to their Food variation.
     */
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