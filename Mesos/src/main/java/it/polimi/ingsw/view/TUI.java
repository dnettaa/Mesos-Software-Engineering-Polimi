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

        // Caso fine partita
        if ("EndGame".equals(clientModel.getCurrentPhaseName())) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("          GAME OVER!");
            System.out.println("=".repeat(40));
            System.out.println("Classifica finale:");
            // I punteggi li vedi dai players nel clientModel
            clientModel.getPlayers().values().stream()
                    .sorted((a, b) -> b.prestigePoints() - a.prestigePoints())
                    .forEach(p -> System.out.printf("  %-12s → %d PP%n", p.nickname(), p.prestigePoints()));
            return;
        }

        // Render Board Header
        System.out.println("\n" + "=".repeat(40));
        System.out.printf(" ROUND: %d | ERA: %s | PHASE: %s %n",
                clientModel.getCurrentRound(), clientModel.getCurrentEra(), clientModel.getCurrentPhaseName());
        System.out.println("=".repeat(40));

        // Turn Order
        System.out.println("TURN ORDER: " + String.join(" -> ", clientModel.getTurnOrder()));

        // Offer Track
        System.out.println("\n>>> OFFER TRACK <<<");
        for (OfferSlotData slot : clientModel.getOfferSlots()) {
            String occ = slot.occupantNickname() == null ? "FREE" : slot.occupantNickname();
            System.out.printf("[%c] Up:%d Down:%d Food:%d -> %s%n",
                    slot.slotID(), slot.upSel(), slot.downSel(), slot.foodReward(), occ);
        }

        // ASCII Card Rows
        System.out.println("\nTOP ROW:");
        drawCardRow(clientModel.getUpperRowCardIDs());
        System.out.println("BOTTOM ROW:");
        drawCardRow(clientModel.getLowerRowCardIDs());

        // Player Stats
        System.out.println(">>> PLAYERS <<<");
        for (PlayerData p : clientModel.getPlayers().values()) {
            System.out.printf("%-10s | Food:%d | PP:%d | Tribe:%d cards | Buildings:%d%n",
                    p.nickname(), p.food(), p.prestigePoints(),
                    p.tribeCardID().size(), p.buildingID().size());
        }

        // Action Logic
        if (nickname.equals(clientModel.getCurrentPlayerNickname())) {
            System.out.println("\n*** IT IS YOUR TURN! ***");
            handleTurnInput(clientModel.getCurrentPhaseName());
        } else {
            System.out.println("\nWaiting for " + clientModel.getCurrentPlayerNickname() + "...");
        }
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
        System.out.println("⚠️ Riprova l'inserimento dei dati.\n");

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


}