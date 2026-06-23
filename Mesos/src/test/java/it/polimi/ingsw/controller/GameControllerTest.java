package it.polimi.ingsw.controller;

import it.polimi.ingsw.leaderboard.MatchResult;
import it.polimi.ingsw.model.game.DTO.CardsTakenDTO;
import it.polimi.ingsw.model.game.DTO.EventResolvedDTO;
import it.polimi.ingsw.model.game.DTO.ExtraCardTakenDTO;
import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.model.game.DTO.TotemPlacedDTO;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the controller-level responsibilities that do not require a real game model.
 * The suite verifies view registration, error dispatching, connection cleanup,
 * phase transitions, and the initial lobby-delegation behavior.
 *
 * @author Diana
 */
class GameControllerTest {

    private GameController controller;
    private FakeView view;

    @BeforeEach
    void setUp() {
        controller = new GameController();
        view = new FakeView();
    }

    /**
     * Verifies that registering a view stores it in the controller registry and assigns
     * the nickname to the virtual view.
     * Setup: a fresh controller and an unregistered fake view.
     * Action: register the view with nickname "Diana".
     * Expected behavior: the view is retrievable from the registry and exposes the assigned nickname.
     * Edge case covered: the controller must initialize the view identity before later callbacks use it.
     */
    @Test
    void registerViewShouldStoreViewAndAssignNickname() {
        controller.registerView("Diana", view);

        assertSame(view, controller.getViews().get("Diana"));
        assertEquals("Diana", view.nickname);
    }

    /**
     * Verifies that unregistering a view removes only its registry entry.
     * Setup: a controller with one registered view.
     * Action: unregister the nickname associated with that view.
     * Expected behavior: the registry no longer contains the nickname.
     * Edge case covered: removing a view must not require disconnecting the underlying client object.
     */
    @Test
    void unregisterViewShouldRemoveViewFromRegistry() {
        controller.registerView("Diana", view);

        controller.unregisterView("Diana");

        assertFalse(controller.getViews().containsKey("Diana"));
        assertTrue(view.connected);
    }

    /**
     * Verifies that {@link GameController#sendError(String, String, String)} uses the
     * current {@link VirtualView} callback API.
     * Setup: a connected view registered for the recipient nickname.
     * Action: send an error to that nickname.
     * Expected behavior: the view receives exactly the provided error code and description.
     * Regression covered: older tests expected socket message dispatch, while production now uses callbacks.
     */
    @Test
    void sendErrorShouldNotifyRegisteredConnectedView() {
        controller.registerView("Diana", view);

        controller.sendError("Diana", "INVALID_PHASE", "Cannot do this now");

        assertEquals("INVALID_PHASE", view.lastErrorCode);
        assertEquals("Cannot do this now", view.lastErrorDescription);
        assertEquals(1, view.errorCount);
    }

    /**
     * Verifies that errors are not delivered to disconnected views.
     * Setup: a registered view whose connection flag is false.
     * Action: send an error to the disconnected nickname.
     * Expected behavior: no error callback is invoked.
     * Edge case covered: stale client connections must not receive server notifications.
     */
    @Test
    void sendErrorShouldIgnoreDisconnectedView() {
        controller.registerView("Diana", view);
        view.connected = false;

        controller.sendError("Diana", "INVALID_PHASE", "Cannot do this now");

        assertNull(view.lastErrorCode);
        assertNull(view.lastErrorDescription);
        assertEquals(0, view.errorCount);
    }

    /**
     * Verifies that sending an error to an unknown nickname is a no-op.
     * Setup: a fresh controller with no registered views.
     * Action: send an error to a nickname that is absent from the registry.
     * Expected behavior: no exception is thrown and the registry remains empty.
     * Edge case covered: network messages may arrive after a player has already been unregistered.
     */
    @Test
    void sendErrorShouldIgnoreUnknownNickname() {
        controller.sendError("Unknown", "INVALID_PHASE", "Cannot do this now");

        assertTrue(controller.getViews().isEmpty());
    }

    /**
     * Verifies that transitioning to a controller phase stores the exact phase instance.
     * Setup: a fresh controller and a fake phase.
     * Action: transition the controller to the fake phase.
     * Expected behavior: {@link GameController#getCurrentPhase()} returns the same instance.
     * Edge case covered: phase replacement must not wrap or clone the provided state object.
     */
    @Test
    void transitionToShouldReplaceCurrentPhase() {
        FakePhase phase = new FakePhase();

        controller.transitionTo(phase);

        assertSame(phase, controller.getCurrentPhase());
    }

    /**
     * Verifies that creating the first lobby initializes the lobby phase through the
     * public controller API.
     * Setup: a controller without an active phase.
     * Action: create a lobby for one player.
     * Expected behavior: the current phase becomes {@link LobbyPhase} and the creator receives join success.
     * Edge case covered: the controller must lazily create the lobby phase when the first client starts a lobby.
     */
    @Test
    void createLobbyShouldInitializeLobbyPhaseAndRegisterCreator() {
        controller.createLobby("Diana", TotemColor.RED, 2, view);

        assertTrue(controller.getCurrentPhase() instanceof LobbyPhase);
        assertEquals("Diana", view.nickname);
        assertEquals("Diana", view.lastJoinSuccessNickname);
        assertEquals(TotemColor.RED, view.lastJoinSuccessColor);
        assertEquals(1, view.lobbyUpdateCount);
    }

    /**
     * Verifies that joining a lobby before any lobby exists reports an error directly
     * to the requesting view.
     * Setup: a controller without an active phase.
     * Action: request to join a lobby.
     * Expected behavior: the view receives a lobby-not-created error and no phase is created.
     * Edge case covered: clients cannot join before another client creates the lobby.
     */
    @Test
    void joinLobbyWithoutExistingLobbyShouldNotifyRequestingViewWithoutCreatingPhase() {
        controller.joinLobby("Diana", TotemColor.RED, view);

        assertEquals("LOBBY_NOT_CREATED", view.lastErrorCode);
        assertEquals("Lobby has not been created yet", view.lastErrorDescription);
        assertEquals(1, view.errorCount);
        assertNull(controller.getCurrentPhase());
    }

    /**
     * Fake implementation of {@link VirtualView} used to observe controller callbacks.
     */
    private static class FakeView implements VirtualView {

        private String nickname;
        private boolean connected = true;
        private String lastErrorCode;
        private String lastErrorDescription;
        private int errorCount;
        private String lastJoinSuccessNickname;
        private TotemColor lastJoinSuccessColor;
        private int lobbyUpdateCount;

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
            this.lastJoinSuccessNickname = nickname;
            this.lastJoinSuccessColor = color;
        }

        @Override
        public void onLobbyUpdate(List<String> players, Map<String, TotemColor> colorsByPlayer, int expected) {
            lobbyUpdateCount++;
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

    /**
     * Fake phase used only to verify phase replacement.
     */
    private static class FakePhase implements ControllerPhase {

        @Override
        public void createLobby(String nickname, TotemColor color, VirtualView view) {
        }

        @Override
        public void joinLobby(String nickname, TotemColor color, VirtualView view) {
        }

        @Override
        public void placeTotem(String nickname, char slotID) {
        }

        @Override
        public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs, List<String> orderedIDs) {
        }

        @Override
        public void takeExtraCard(String nickname, String cardID) {
        }

        @Override
        public void onDisconnect(String nickname) {
        }
    }
}
