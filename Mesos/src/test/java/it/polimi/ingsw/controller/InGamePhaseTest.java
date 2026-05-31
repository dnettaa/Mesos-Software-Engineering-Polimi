package it.polimi.ingsw.controller;

import it.polimi.ingsw.leaderboard.MatchResult;
import it.polimi.ingsw.model.exception.ErrorCode;
import it.polimi.ingsw.model.exception.GameException;
import it.polimi.ingsw.model.game.DTO.CardsTakenDTO;
import it.polimi.ingsw.model.game.DTO.EventResolvedDTO;
import it.polimi.ingsw.model.game.DTO.ExtraCardTakenDTO;
import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.model.game.DTO.TotemPlacedDTO;
import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.game.GameListener;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests active-game controller behavior. The suite verifies that legal actions
 * are delegated to the game model, model rule violations are converted into
 * view errors, lobby operations are rejected, and disconnection ends the session.
 *
 * @author Diana
 */
class InGamePhaseTest {

    private GameController controller;
    private FakeGame game;
    private InGamePhase inGamePhase;
    private FakeView dianaView;

    @BeforeEach
    void setUp() {
        controller = new GameController();
        game = new FakeGame();
        inGamePhase = new InGamePhase(controller, game);

        dianaView = new FakeView();
        controller.registerView("Diana", dianaView);
    }

    /**
     * Verifies that a valid totem-placement request is delegated to the model.
     * Setup: an in-game phase backed by a fake game that does not throw errors.
     * Action: Diana places a totem on slot C.
     * Expected behavior: the fake game receives the nickname and slot exactly once.
     * Edge case covered: successful delegation must not generate a view error.
     */
    @Test
    void placeTotemShouldDelegateToGameWithoutSendingError() {
        inGamePhase.placeTotem("Diana", 'C');

        assertEquals("Diana", game.lastTotemNickname);
        assertEquals('C', game.lastSlotID);
        assertEquals(1, game.placeTotemCalls);
        assertNull(dianaView.lastErrorCode);
        assertNull(dianaView.lastErrorDescription);
    }

    /**
     * Verifies that a model exception during totem placement is translated into
     * an error callback for the requesting player.
     * Setup: the fake game throws {@link ErrorCode#NOT_YOUR_TURN}.
     * Action: Diana attempts to place a totem.
     * Expected behavior: Diana receives the same error code and message from the exception.
     * Regression covered: controller phases must not leak domain exceptions to the network layer.
     */
    @Test
    void placeTotemShouldSendErrorWhenGameThrowsException() {
        game.exceptionToThrow = new GameException(ErrorCode.NOT_YOUR_TURN, "Not your turn");

        inGamePhase.placeTotem("Diana", 'C');

        assertEquals("NOT_YOUR_TURN", dianaView.lastErrorCode);
        assertEquals("Not your turn", dianaView.lastErrorDescription);
        assertEquals(1, dianaView.errorCount);
    }

    /**
     * Verifies that a valid card-selection request is delegated to the model with
     * upper, lower, and ordered card identifiers preserved.
     * Setup: an in-game phase backed by a fake game that accepts card selections.
     * Action: Diana takes two upper cards and one lower card in a specific order.
     * Expected behavior: the fake game receives all three lists unchanged.
     * Edge case covered: ordered card identifiers are part of the current API and must be forwarded.
     */
    @Test
    void takeCardsShouldDelegateAllCardListsToGameWithoutSendingError() {
        List<String> upper = List.of("U1", "U2");
        List<String> lower = List.of("L1");
        List<String> ordered = List.of("U1", "L1", "U2");

        inGamePhase.takeCards("Diana", upper, lower, ordered);

        assertEquals("Diana", game.lastTakeCardsNickname);
        assertEquals(upper, game.lastUpperIDs);
        assertEquals(lower, game.lastLowerIDs);
        assertEquals(ordered, game.lastOrderedIDs);
        assertEquals(1, game.takeCardsCalls);
        assertNull(dianaView.lastErrorCode);
        assertNull(dianaView.lastErrorDescription);
    }

    /**
     * Verifies that a model exception during card selection is translated into an
     * error callback for the requesting player.
     * Setup: the fake game throws {@link ErrorCode#INVALID_SELECTION}.
     * Action: Diana attempts to take invalid cards.
     * Expected behavior: Diana receives the invalid-selection code and message.
     * Regression covered: invalid model actions must be reported through {@link VirtualView#onError(String, String)}.
     */
    @Test
    void takeCardsShouldSendErrorWhenGameThrowsException() {
        game.exceptionToThrow = new GameException(ErrorCode.INVALID_SELECTION, "Invalid selection");

        inGamePhase.takeCards("Diana", List.of("U1"), List.of("L1"), List.of("U1", "L1"));

        assertEquals("INVALID_SELECTION", dianaView.lastErrorCode);
        assertEquals("Invalid selection", dianaView.lastErrorDescription);
        assertEquals(1, dianaView.errorCount);
    }

    /**
     * Verifies that a valid extra-card request is delegated to the model.
     * Setup: an in-game phase backed by a fake game that accepts extra-card selections.
     * Action: Diana takes card C1.
     * Expected behavior: the fake game receives the nickname and card identifier exactly once.
     * Edge case covered: optional extra-card flow uses a distinct delegation method.
     */
    @Test
    void takeExtraCardShouldDelegateToGameWithoutSendingError() {
        inGamePhase.takeExtraCard("Diana", "C1");

        assertEquals("Diana", game.lastExtraCardNickname);
        assertEquals("C1", game.lastCardID);
        assertEquals(1, game.takeExtraCardCalls);
        assertNull(dianaView.lastErrorCode);
        assertNull(dianaView.lastErrorDescription);
    }

    /**
     * Verifies that a model exception during extra-card selection is translated into
     * an error callback for the requesting player.
     * Setup: the fake game throws {@link ErrorCode#UNKNOWN_CARD}.
     * Action: Diana attempts to take an unknown extra card.
     * Expected behavior: Diana receives the unknown-card code and message.
     * Edge case covered: errors from optional phases follow the same reporting path as normal actions.
     */
    @Test
    void takeExtraCardShouldSendErrorWhenGameThrowsException() {
        game.exceptionToThrow = new GameException(ErrorCode.UNKNOWN_CARD, "Unknown card");

        inGamePhase.takeExtraCard("Diana", "C1");

        assertEquals("UNKNOWN_CARD", dianaView.lastErrorCode);
        assertEquals("Unknown card", dianaView.lastErrorDescription);
        assertEquals(1, dianaView.errorCount);
    }

    /**
     * Verifies that lobby creation is rejected once the game is in progress.
     * Setup: an in-game phase with Luca registered as a connected view.
     * Action: Luca attempts to create a lobby.
     * Expected behavior: Luca receives a game-already-started error.
     * Regression covered: lobby operations must not be accepted after game start.
     */
    @Test
    void createLobbyDuringGameShouldSendGameAlreadyStartedError() {
        FakeView lucaView = new FakeView();
        controller.registerView("Luca", lucaView);

        inGamePhase.createLobby("Luca", TotemColor.BLUE, lucaView);

        assertEquals("GAME_ALREADY_STARTED", lucaView.lastErrorCode);
        assertEquals("Game already started", lucaView.lastErrorDescription);
        assertEquals(1, lucaView.errorCount);
    }

    /**
     * Verifies that joining a lobby is rejected once the game is in progress.
     * Setup: an in-game phase with Luca registered as a connected view.
     * Action: Luca attempts to join a lobby.
     * Expected behavior: Luca receives a game-already-started error.
     * Edge case covered: both create and join lobby commands share the same in-game rejection policy.
     */
    @Test
    void joinLobbyDuringGameShouldSendGameAlreadyStartedError() {
        FakeView lucaView = new FakeView();
        controller.registerView("Luca", lucaView);

        inGamePhase.joinLobby("Luca", TotemColor.BLUE, lucaView);

        assertEquals("GAME_ALREADY_STARTED", lucaView.lastErrorCode);
        assertEquals("Game already started", lucaView.lastErrorDescription);
        assertEquals(1, lucaView.errorCount);
    }

    /**
     * Verifies that disconnecting a player during an active match notifies all
     * connected views and resets the controller session.
     * Setup: an in-game phase with two registered connected views.
     * Action: Diana disconnects.
     * Expected behavior: both views receive the disconnection reason and the controller registry is cleared.
     * Regression covered: production resets the controller instead of calling {@link VirtualView#disconnect()} on each view.
     */
    @Test
    void onDisconnectDuringGameShouldNotifyViewsAndResetController() {
        FakeView lucaView = new FakeView();
        controller.registerView("Luca", lucaView);

        inGamePhase.onDisconnect("Diana");

        assertEquals("Diana disconnected. The game has ended.", dianaView.lastDisconnectionReason);
        assertEquals("Diana disconnected. The game has ended.", lucaView.lastDisconnectionReason);
        assertEquals(1, dianaView.disconnectionCount);
        assertEquals(1, lucaView.disconnectionCount);
        assertTrue(controller.getViews().isEmpty());
        assertNull(controller.getGame());
        assertNull(controller.getCurrentPhase());
    }

    /**
     * Fake model used to verify that {@link InGamePhase} delegates game actions correctly.
     */
    private static class FakeGame implements GameActions {

        private int placeTotemCalls;
        private int takeCardsCalls;
        private int takeExtraCardCalls;
        private String lastTotemNickname;
        private char lastSlotID;
        private String lastTakeCardsNickname;
        private List<String> lastUpperIDs;
        private List<String> lastLowerIDs;
        private List<String> lastOrderedIDs;
        private String lastExtraCardNickname;
        private String lastCardID;
        private GameException exceptionToThrow;

        @Override
        public void placeTotem(String nickname, char slotID) {
            placeTotemCalls++;
            lastTotemNickname = nickname;
            lastSlotID = slotID;

            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
        }

        @Override
        public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs, List<String> orderedIDs) {
            takeCardsCalls++;
            lastTakeCardsNickname = nickname;
            lastUpperIDs = upperIDs;
            lastLowerIDs = lowerIDs;
            lastOrderedIDs = orderedIDs;

            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
        }

        @Override
        public void takeExtraCard(String nickname, String cardID) {
            takeExtraCardCalls++;
            lastExtraCardNickname = nickname;
            lastCardID = cardID;

            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
        }

        @Override
        public String getCurrentPlayerNickname() {
            return "";
        }

        @Override
        public String getCurrentPhaseName() {
            return "";
        }

        @Override
        public boolean isGameEnded() {
            return false;
        }

        @Override
        public void addListener(GameListener listener) {
        }

        @Override
        public void removeListener(GameListener listener) {
        }

        @Override
        public GameStateSnapshot buildSnapshot() {
            return null;
        }

        @Override
        public void startGame() {
        }
    }

    /**
     * Fake implementation of {@link VirtualView} used to observe in-game callbacks.
     */
    private static class FakeView implements VirtualView {

        private String nickname;
        private boolean connected = true;
        private String lastErrorCode;
        private String lastErrorDescription;
        private int errorCount;
        private String lastDisconnectionReason;
        private int disconnectionCount;

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
            this.lastDisconnectionReason = reason;
            disconnectionCount++;
        }

        @Override
        public void onRecoveryCancelled(String reason) {
        }

        @Override
        public void onGameStarted(GameStateSnapshot snapshot) {
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
        }

        @Override
        public void onLeaderboard(List<MatchResult> ranking, int position) {
        }
    }
}
