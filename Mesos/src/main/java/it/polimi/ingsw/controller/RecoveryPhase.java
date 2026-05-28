package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
import it.polimi.ingsw.model.game.DTO.PlayerData;
import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.persistence.PersistenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Special controller phase used after a server crash.
 * <p>
 * During this phase the server waits for all original
 * players to reconnect before resuming the game.
 * <p>
 * Only players belonging to the saved match are allowed
 * to reconnect, and they must use the same nickname
 * and totem color originally associated with them.
 *
 * @author Andrea Markvukaj
 */
public class RecoveryPhase implements ControllerPhase {

    private static final int TIMEOUT_SECONDS = 20;

    private final GameController controller;
    private final GameActions game;

    private final Set<String> reconnectedPlayers;
    private final int expectedTotal;
    private boolean finished = false;
    private final ScheduledExecutorService timeoutScheduler;

    /**
     * Creates a new recovery phase.
     *
     * @param controller the main game controller
     * @param game the restored game instance
     */
    public RecoveryPhase(GameController controller, GameActions game) {

        this.controller = controller;
        this.game = game;
        this.reconnectedPlayers = new HashSet<>();

        GameStateSnapshot snapshot = game.buildSnapshot();
        this.expectedTotal = snapshot.players().size();

        timeoutScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "recovery-timeout");
            t.setDaemon(true);
            return t;
        });
        timeoutScheduler.schedule(this::onTimeout, TIMEOUT_SECONDS, TimeUnit.SECONDS);
        System.out.println("[RECOVERY] Waiting for players to reconnect. Timeout: " + TIMEOUT_SECONDS + "s.");
    }

    private synchronized void onTimeout() {
        if (!finished) {
            System.out.println("[RECOVERY] Timeout expired. Discarding saved game.");
            declineRecovery(controller, null);
        }
    }

    @Override
    public synchronized void reconnect(GameController controller, String nickname, TotemColor color, VirtualView view) {
        acceptRecovery(controller, nickname, color, view);
    }

    @Override
    public synchronized void acceptRecovery(GameController controller, String nickname, TotemColor color, VirtualView view) {
        GameStateSnapshot snapshot = game.buildSnapshot();

        PlayerData playerData = snapshot.players()
                .stream()
                .filter(p -> p.nickname().equals(nickname))
                .findFirst()
                .orElse(null);

        if (playerData == null) {
            view.onError("RECOVERY_MODE", "Unknown player for recovery.");
            return;
        }

        if (playerData.totemColor() != color) {
            view.onError("RECOVERY_MODE", "Invalid recovery color.");
            return;
        }

        if (reconnectedPlayers.contains(nickname)) {
            view.onError("RECOVERY_MODE", "Player already reconnected.");
            return;
        }

        controller.registerView(nickname, view);
        reconnectedPlayers.add(nickname);

        System.out.println("[RECOVERY] Player reconnected: " + nickname + " (" + reconnectedPlayers.size()
                + "/" + expectedTotal + ")");

        if(reconnectedPlayers.size() < expectedTotal) {
            notifyRecoveryUpdate();
        }

        if (reconnectedPlayers.size() == expectedTotal) {
            finished = true;
            timeoutScheduler.shutdownNow();
            System.out.println("[RECOVERY] All players reconnected. Resuming game.");

            controller.setPlayerCount(expectedTotal);
            controller.transitionTo(new InGamePhase(controller, game));

            GameStateSnapshot resumedSnapshot = game.buildSnapshot();
            for(VirtualView targetView : controller.getViews().values()) {
                if(targetView.isConnected()) {
                    targetView.onGameStarted(resumedSnapshot);
                }
            }
        }
    }

    private void notifyRecoveryUpdate() {
        GameStateSnapshot snapshot = game.buildSnapshot();

        List<String> allPlayers = snapshot.players().stream()
                .map(PlayerData::nickname)
                .toList();

        List<String> reconnected = allPlayers.stream()
                .filter(reconnectedPlayers::contains)
                .toList();

        List<String> missing = allPlayers.stream()
                .filter(p -> !reconnectedPlayers.contains(p))
                .toList();

        for (String reconnectedNickname : reconnectedPlayers) {
            VirtualView targetView = controller.getViews().get(reconnectedNickname);

            if (targetView != null && targetView.isConnected()) {
                targetView.onRecoveryUpdate(reconnected, missing);
            }
        }
    }

    @Override
    public synchronized void declineRecovery(GameController controller, VirtualView view) {
        if (finished) return;
        finished = true;
        timeoutScheduler.shutdownNow();
        List<VirtualView> viewsToNotify = new ArrayList<>(controller.getViews().values());

        if(view != null && !viewsToNotify.contains(view)) {
            viewsToNotify.add(view);
        }

        PersistenceManager.deleteSave();
        controller.reset();

        for(VirtualView targetView : viewsToNotify) {
            if(targetView != null && targetView.isConnected()) {
                targetView.onRecoveryCancelled("Recovery cancelled. You can create or join a new lobby.");
            }
        }

        System.out.println("[RECOVERY] Recovery declined. Saved game discarded.");
    }


    /**
     * Rejects any attempt to create a new lobby
     * while the server is restoring a match.
     */
    @Override
    public void createLobby(String nickname, TotemColor color, VirtualView view) {
        view.onError("RECOVERY_MODE", "Cannot create a new lobby while "
                + "a saved match is being restored.");
    }

    /**
     * Rejects normal lobby joins during recovery.
     */
    @Override
    public void joinLobby(String nickname, TotemColor color, VirtualView view) {
        view.onError("RECOVERY_MODE", "Server is currently restoring " + "a saved game.");
    }

    /**
     * Handles disconnects occurring during recovery.
     */
    @Override
    public void onDisconnect(String nickname) {

        reconnectedPlayers.remove(nickname);

        controller.unregisterView(nickname);
    }

    /**
     * Disabled during recovery.
     */
    @Override
    public void placeTotem(String nickname, char slotID) {}

    /**
     * Disabled during recovery.
     */
    @Override
    public void takeCards(String nickname, java.util.List<String> upperIDs, java.util.List<String> lowerIDs, java.util.List<String> orderedIDs) {}

    /**
     * Disabled during recovery.
     */
    @Override
    public void takeExtraCard(String nickname, String cardID) {}
}
