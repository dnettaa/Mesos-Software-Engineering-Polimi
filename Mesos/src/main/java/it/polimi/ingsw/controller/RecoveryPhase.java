package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;

import java.util.HashSet;
import java.util.Set;

/**
 * Special controller phase to handle post-crash recovery.
 * It waits for the original players to reconnect before resuming the game.
 */
public class RecoveryPhase implements ControllerPhase {

    private final GameController controller;
    private final GameActions game;
    private final Set<String> reconnectedPlayers;
    private final int expectedTotal;

    /**
     * Constructs a new RecoveryPhase.
     * Extracts the expected number of players from the loaded game state snapshot
     * to determine when the game can safely resume.
     *
     * @param controller the main game controller managing the network and state
     * @param game       the game state loaded from the persistence file
     */
    public RecoveryPhase(GameController controller, GameActions game) {
        this.controller = controller;
        this.game = game;
        this.reconnectedPlayers = new HashSet<>();
        GameStateSnapshot snap = game.buildSnapshot();
        this.expectedTotal = snap.players().size();
    }

    /**
     * Handles a player's request to join the lobby during the recovery phase.
     * Only players who were part of the original game session are allowed to reconnect.
     * Once all expected players have rejoined, the game transitions back to the in-game phase
     * and broadcasts the restored state to everyone.
     *
     * @param nickname the nickname of the reconnecting player
     * @param color    the chosen totem color
     * @param view     the virtual view associated with the client's network connection
     */
    @Override
    public synchronized void joinLobby(String nickname, TotemColor color, VirtualView view) {
        GameStateSnapshot snap = game.buildSnapshot();

        boolean isOriginalPlayer = snap.players().stream()
                .anyMatch(p -> p.nickname().equals(nickname));

        // Reject new players trying to sneak into the restored session
        if (!isOriginalPlayer) {
            view.onError("RECOVERY_MODE", "The server is restoring a previous session. " +
                    "Only the original players can reconnect.");
            return;
        }

        // Re-link the view to the controller
        controller.registerView(nickname, view);
        reconnectedPlayers.add(nickname);

        System.out.println("[RECOVERY] Player reconnected: " + nickname +
                " (" + reconnectedPlayers.size() + "/" + expectedTotal + ")");

        view.onJoinSuccess(nickname, color);

        // If everyone is back, we can resume playing
        if (reconnectedPlayers.size() == expectedTotal) {
            System.out.println("[RECOVERY] All players have returned. Resuming the game!");
            controller.transitionTo(new InGamePhase(controller, game));

            controller.onGameStarted(snap);
        }
    }

    /**
     * Rejects any attempt to create a new lobby, as the server is currently locked in recovery mode.
     *
     * @param nickname the nickname of the player attempting to create the lobby
     * @param color    the chosen totem color
     * @param view     the virtual view associated with the player
     */
    @Override
    public void createLobby(String nickname, TotemColor color, VirtualView view) {
        view.onError("RECOVERY_MODE", "Cannot create a new lobby: the server is currently restoring a previous session.");
    }

    /**
     * Handles the sudden disconnection of a player who had already reconnected
     * during this waiting phase.
     *
     * @param nickname the nickname of the disconnected player
     */
    @Override
    public void onDisconnect(String nickname) {
        reconnectedPlayers.remove(nickname);
        controller.unregisterView(nickname);
    }

    /** Disabled during the recovery phase. */
    @Override public void placeTotem(String nickname, char slotID) {}

    /** Disabled during the recovery phase. */
    @Override public void takeCards(String nickname, java.util.List<String> up, java.util.List<String> down) {}

    /** Disabled during the recovery phase. */
    @Override public void takeExtraCard(String nickname, String cardID) {}
}