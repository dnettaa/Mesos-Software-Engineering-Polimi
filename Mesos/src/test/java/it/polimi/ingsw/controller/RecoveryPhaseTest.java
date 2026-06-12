package it.polimi.ingsw.controller;

import it.polimi.ingsw.leaderboard.MatchResult;
import it.polimi.ingsw.model.game.DTO.CardsTakenDTO;
import it.polimi.ingsw.model.game.DTO.EventResolvedDTO;
import it.polimi.ingsw.model.game.DTO.ExtraCardTakenDTO;
import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
import it.polimi.ingsw.model.game.DTO.PlayerData;
import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.model.game.DTO.TotemPlacedDTO;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.game.GameListener;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the controller recovery phase used after loading a saved game.
 * The suite verifies reconnection validation, progress updates, and the
 * transition back to active gameplay when all original players reconnect.
 *
 * @author Diana
 */
class RecoveryPhaseTest {

    private GameController controller;
    private FakeGame game;
    private RecoveryPhase recoveryPhase;

    @BeforeEach
    void setUp() {
        controller = new GameController();
        game = new FakeGame();
        recoveryPhase = new RecoveryPhase(controller, game);
    }

    @AfterEach
    void tearDown() throws Exception {
        shutdownRecoveryTimeout();
    }

    /**
     * Verifies that only players stored in the recovered game snapshot may reconnect.
     * Setup: a recovery phase for Diana and Luca.
     * Action: an unknown player attempts to reconnect.
     * Expected behavior: the view receives a recovery-mode error and is not registered.
     * Edge case covered: recovery cannot be hijacked by a nickname absent from the saved match.
     */
    @Test
    void acceptRecoveryShouldRejectUnknownPlayer() {
        FakeView unknownView = new FakeView();

        recoveryPhase.acceptRecovery(controller, "Unknown", TotemColor.RED, unknownView);

        assertEquals("RECOVERY_MODE", unknownView.lastErrorCode);
        assertEquals("Unknown player for recovery.", unknownView.lastErrorDescription);
        assertEquals(1, unknownView.errorCount);
        assertNull(controller.getViews().get("Unknown"));
    }

    /**
     * Verifies that a reconnecting player must use the original totem color from
     * the recovered snapshot.
     * Setup: Diana exists in the recovered game with red color.
     * Action: Diana attempts recovery with blue color.
     * Expected behavior: the view receives an invalid-color recovery error and is not registered.
     * Edge case covered: nickname alone is not sufficient to resume a saved player session.
     */
    @Test
    void acceptRecoveryShouldRejectPlayerWithWrongColor() {
        FakeView dianaView = new FakeView();

        recoveryPhase.acceptRecovery(controller, "Diana", TotemColor.BLUE, dianaView);

        assertEquals("RECOVERY_MODE", dianaView.lastErrorCode);
        assertEquals("Invalid recovery color.", dianaView.lastErrorDescription);
        assertEquals(1, dianaView.errorCount);
        assertNull(controller.getViews().get("Diana"));
    }

    /**
     * Verifies that a valid partial reconnection registers the player and reports
     * the recovery progress to already reconnected clients.
     * Setup: a recovery phase for two players.
     * Action: Diana reconnects with the correct color.
     * Expected behavior: Diana is registered and receives the list of reconnected and missing players.
     * Edge case covered: recovery remains pending until every original player reconnects.
     */
    @Test
    void acceptRecoveryShouldRegisterPlayerAndNotifyPartialProgress() {
        FakeView dianaView = new FakeView();

        recoveryPhase.acceptRecovery(controller, "Diana", TotemColor.RED, dianaView);

        assertEquals(dianaView, controller.getViews().get("Diana"));
        assertEquals(List.of("Diana"), dianaView.lastReconnectedPlayers);
        assertEquals(List.of("Luca"), dianaView.lastMissingPlayers);
        assertEquals(1, dianaView.recoveryUpdateCount);
        assertNull(controller.getCurrentPhase());
    }

    /**
     * Verifies that the same player cannot reconnect twice during recovery.
     * Setup: Diana has already reconnected successfully.
     * Action: another view attempts to recover the same nickname and color.
     * Expected behavior: the second view receives a player-already-reconnected error.
     * Regression covered: duplicate recovery attempts must not replace the registered connection.
     */
    @Test
    void acceptRecoveryShouldRejectDuplicateReconnection() {
        FakeView firstView = new FakeView();
        FakeView duplicateView = new FakeView();
        recoveryPhase.acceptRecovery(controller, "Diana", TotemColor.RED, firstView);

        recoveryPhase.acceptRecovery(controller, "Diana", TotemColor.RED, duplicateView);

        assertEquals("RECOVERY_MODE", duplicateView.lastErrorCode);
        assertEquals("Player already reconnected.", duplicateView.lastErrorDescription);
        assertEquals(1, duplicateView.errorCount);
        assertEquals(firstView, controller.getViews().get("Diana"));
    }

    /**
     * Verifies that recovery completes when all original players reconnect.
     * Setup: a saved two-player game and two matching views.
     * Action: Diana and Luca reconnect with their original colors.
     * Expected behavior: the controller enters {@link InGamePhase} and both views receive the resumed snapshot.
     * Edge case covered: the last reconnection must resume gameplay immediately without waiting for timeout.
     */
    @Test
    void acceptRecoveryShouldResumeGameWhenAllPlayersReconnect() {
        FakeView dianaView = new FakeView();
        FakeView lucaView = new FakeView();

        recoveryPhase.acceptRecovery(controller, "Diana", TotemColor.RED, dianaView);
        recoveryPhase.acceptRecovery(controller, "Luca", TotemColor.BLUE, lucaView);

        assertTrue(controller.getCurrentPhase() instanceof InGamePhase);
        assertEquals(2, controller.getViews().size());
        assertEquals(1, dianaView.gameStartedCount);
        assertEquals(1, lucaView.gameStartedCount);
        assertEquals(game.snapshot, dianaView.lastGameStartedSnapshot);
        assertEquals(game.snapshot, lucaView.lastGameStartedSnapshot);
    }

    /**
     * Verifies that a disconnect during recovery removes only the reconnected player.
     * Setup: Diana has reconnected while Luca is still missing.
     * Action: Diana disconnects during recovery.
     * Expected behavior: Diana is removed from the controller registry.
     * Edge case covered: recovery can continue tracking missing players after a partial reconnect disconnects.
     */
    @Test
    void onDisconnectShouldUnregisterReconnectedPlayer() {
        FakeView dianaView = new FakeView();
        recoveryPhase.acceptRecovery(controller, "Diana", TotemColor.RED, dianaView);

        recoveryPhase.onDisconnect("Diana");

        assertNull(controller.getViews().get("Diana"));
    }

    private void shutdownRecoveryTimeout() throws Exception {
        Field schedulerField = RecoveryPhase.class.getDeclaredField("timeoutScheduler");
        schedulerField.setAccessible(true);
        ScheduledExecutorService scheduler = (ScheduledExecutorService) schedulerField.get(recoveryPhase);
        scheduler.shutdownNow();
    }

    /**
     * Fake recovered game that exposes a stable two-player snapshot for recovery tests.
     */
    private static class FakeGame implements GameActions {

        private final GameStateSnapshot snapshot = new GameStateSnapshot(
                4,
                Era.Era2,
                "TotemPlacementPhase",
                "Diana",
                List.of("Diana", "Luca"),
                List.of(),
                List.of("Diana", "Luca"),
                20,
                List.of("CH01", "CH02"),
                List.of("CH03", "BU01"),
                List.of(),
                List.of(
                        new PlayerData("Diana", TotemColor.RED, 5, 10, List.of("CH01"), List.of()),
                        new PlayerData("Luca", TotemColor.BLUE, 4, 8, List.of("CH02"), List.of())
                )
        );

        @Override
        public void placeTotem(String nickname, char slotID) {
        }

        @Override
        public void takeCards(String nickname, List<String> chosenUpperIDs, List<String> chosenLowerIDs,
                              List<String> orderedIDs) {
        }

        @Override
        public void takeExtraCard(String nickname, String cardID) {
        }

        @Override
        public String getCurrentPlayerNickname() {
            return "Diana";
        }

        @Override
        public String getCurrentPhaseName() {
            return "TotemPlacementPhase";
        }

        @Override
        public boolean isGameEnded() {
            return false;
        }

        @Override
        public void startGame() {
        }

        @Override
        public void addListener(GameListener listener) {
        }

        @Override
        public void removeListener(GameListener listener) {
        }

        @Override
        public GameStateSnapshot buildSnapshot() {
            return snapshot;
        }
    }

    /**
     * Fake implementation of {@link VirtualView} used to observe recovery callbacks.
     */
    private static class FakeView implements VirtualView {

        private String nickname;
        private boolean connected = true;
        private String lastErrorCode;
        private String lastErrorDescription;
        private int errorCount;
        private List<String> lastReconnectedPlayers = List.of();
        private List<String> lastMissingPlayers = List.of();
        private int recoveryUpdateCount;
        private GameStateSnapshot lastGameStartedSnapshot;
        private int gameStartedCount;

        @Override
        public String getNickname() {
            return nickname;
        }

        @Override
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        @Override
        public void disconnect() {
            connected = false;
        }

        @Override
        public void onJoinSuccess(String nickname, TotemColor color) {
        }

        @Override
        public void onLobbyUpdate(List<String> players, Map<String, TotemColor> colorsByPlayer, int expected) {
        }

        @Override
        public void onError(String code, String description) {
            this.lastErrorCode = code;
            this.lastErrorDescription = description;
            errorCount++;
        }

        @Override
        public void onDisconnection(String reason) {
        }

        @Override
        public void onRecoveryCancelled(String reason) {
        }

        @Override
        public void onGameStarted(GameStateSnapshot snapshot) {
            this.lastGameStartedSnapshot = snapshot;
            gameStartedCount++;
        }

        @Override
        public void onTotemPlaced(TotemPlacedDTO dto) {
        }

        @Override
        public void onCardsTaken(CardsTakenDTO dto) {
        }

        @Override
        public void onExtraCardTaken(ExtraCardTakenDTO dto) {
        }

        @Override
        public void onEventResolved(EventResolvedDTO dto) {
        }

        @Override
        public void onRoundEnded(RoundEndedDTO dto) {
        }

        @Override
        public void onGameEnded(GameEndedDTO dto) {
        }

        @Override
        public void onRecoveryUpdate(List<String> reconnectedPlayers, List<String> missingPlayers) {
            this.lastReconnectedPlayers = List.copyOf(reconnectedPlayers);
            this.lastMissingPlayers = List.copyOf(missingPlayers);
            recoveryUpdateCount++;
        }

        @Override
        public void onLeaderboard(List<MatchResult> ranking, int position) {
        }
    }
}
