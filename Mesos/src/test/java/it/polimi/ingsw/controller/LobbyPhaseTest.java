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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the controller lobby phase through the current {@link VirtualView}
 * callback contract. The suite verifies lobby creation, join validation,
 * invalid in-game actions during lobby, and disconnection updates.
 *
 * @author Diana
 */
class LobbyPhaseTest {

    private GameController controller;
    private LobbyPhase lobbyPhase;

    @BeforeEach
    void setUp() {
        controller = new GameController();
        lobbyPhase = new LobbyPhase(controller, 3);
    }

    /**
     * Verifies that creating an empty lobby registers the first player and notifies
     * the view through the lobby callbacks.
     * Setup: a new lobby phase with no players.
     * Action: create a lobby with nickname "Diana" and red color.
     * Expected behavior: the creator receives join success and one lobby update.
     * Edge case covered: the first player is both lobby creator and first registered participant.
     */
    @Test
    void createLobbyShouldRegisterFirstPlayerAndBroadcastUpdate() {
        FakeView dianaView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);

        assertEquals("Diana", dianaView.nickname);
        assertEquals("Diana", dianaView.lastJoinSuccessNickname);
        assertEquals(TotemColor.RED, dianaView.lastJoinSuccessColor);
        assertEquals(1, dianaView.joinSuccessCount);
        assertEquals(1, dianaView.lobbyUpdateCount);
        assertEquals(List.of("Diana"), dianaView.lastLobbyPlayers);
        assertEquals(TotemColor.RED, dianaView.lastLobbyColorsByPlayer.get("Diana"));
        assertEquals(3, dianaView.lastExpectedPlayers);
    }

    /**
     * Verifies that creating a second lobby while one already exists is rejected.
     * Setup: a lobby already created by one player.
     * Action: another player attempts to create a new lobby.
     * Expected behavior: the second view receives a game-already-started error and is not registered.
     * Regression covered: duplicate lobby creation must not leave the rejected view in the registry.
     */
    @Test
    void createLobbyWhenLobbyAlreadyExistsShouldSendErrorAndUnregisterRejectedView() {
        FakeView dianaView = new FakeView();
        FakeView lucaView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        lobbyPhase.createLobby("Luca", TotemColor.BLUE, lucaView);

        assertEquals("GAME_ALREADY_STARTED", lucaView.lastErrorCode);
        assertEquals("A lobby already exists. Please join the existing one.", lucaView.lastErrorDescription);
        assertEquals(1, lucaView.errorCount);
        assertFalse(controller.getViews().containsKey("Luca"));
    }

    /**
     * Verifies that a player cannot join before a lobby has been created.
     * Setup: a lobby phase whose player selection is still empty.
     * Action: a player attempts to join.
     * Expected behavior: the view receives a lobby-not-created error and is removed from the registry.
     * Edge case covered: join requests cannot implicitly create a lobby.
     */
    @Test
    void joinLobbyBeforeCreationShouldSendErrorAndUnregisterView() {
        FakeView lucaView = new FakeView();

        lobbyPhase.joinLobby("Luca", TotemColor.BLUE, lucaView);

        assertEquals("LOBBY_NOT_CREATED", lucaView.lastErrorCode);
        assertEquals("Lobby has not been created yet", lucaView.lastErrorDescription);
        assertEquals(1, lucaView.errorCount);
        assertFalse(controller.getViews().containsKey("Luca"));
    }

    /**
     * Verifies that joining an existing lobby registers the player and broadcasts
     * the updated lobby state to all connected participants.
     * Setup: a lobby created by Diana.
     * Action: Luca joins with an available color.
     * Expected behavior: Luca receives join success and both players receive the updated lobby list.
     * Edge case covered: lobby updates must preserve insertion order for connected players.
     */
    @Test
    void joinLobbyShouldRegisterPlayerAndBroadcastUpdateToAllPlayers() {
        FakeView dianaView = new FakeView();
        FakeView lucaView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        lobbyPhase.joinLobby("Luca", TotemColor.BLUE, lucaView);

        assertEquals("Luca", lucaView.nickname);
        assertEquals("Luca", lucaView.lastJoinSuccessNickname);
        assertEquals(TotemColor.BLUE, lucaView.lastJoinSuccessColor);
        assertEquals(1, lucaView.joinSuccessCount);
        assertEquals(1, lucaView.lobbyUpdateCount);
        assertEquals(List.of("Diana", "Luca"), lucaView.lastLobbyPlayers);

        assertEquals(2, dianaView.lobbyUpdateCount);
        assertEquals(List.of("Diana", "Luca"), dianaView.lastLobbyPlayers);
        assertEquals(TotemColor.BLUE, dianaView.lastLobbyColorsByPlayer.get("Luca"));
    }

    /**
     * Verifies that duplicate nicknames are rejected before registration.
     * Setup: a lobby already containing player "Diana".
     * Action: another view attempts to join using the same nickname.
     * Expected behavior: the duplicate view receives a nickname-taken error and no join success.
     * Edge case covered: nickname uniqueness is checked before the controller registers the new view.
     */
    @Test
    void joinLobbyWithDuplicateNicknameShouldSendErrorWithoutRegisteringDuplicateView() {
        FakeView dianaView = new FakeView();
        FakeView duplicateView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        lobbyPhase.joinLobby("Diana", TotemColor.BLUE, duplicateView);

        assertEquals("NICKNAME_TAKEN", duplicateView.lastErrorCode);
        assertEquals("Nickname already taken", duplicateView.lastErrorDescription);
        assertEquals(1, duplicateView.errorCount);
        assertEquals(0, duplicateView.joinSuccessCount);
        assertEquals(dianaView, controller.getViews().get("Diana"));
    }

    /**
     * Verifies that duplicate colors are rejected after nickname validation.
     * Setup: a lobby already containing a red player.
     * Action: another player attempts to join with the same red color.
     * Expected behavior: the second view receives a color-taken error and is unregistered.
     * Edge case covered: color uniqueness must leave the existing lobby state unchanged.
     */
    @Test
    void joinLobbyWithDuplicateColorShouldSendErrorAndUnregisterRejectedView() {
        FakeView dianaView = new FakeView();
        FakeView lucaView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        lobbyPhase.joinLobby("Luca", TotemColor.RED, lucaView);

        assertEquals("COLOR_TAKEN", lucaView.lastErrorCode);
        assertEquals("Color already taken", lucaView.lastErrorDescription);
        assertEquals(1, lucaView.errorCount);
        assertEquals(0, lucaView.joinSuccessCount);
        assertFalse(controller.getViews().containsKey("Luca"));
        assertEquals(List.of("Diana"), dianaView.lastLobbyPlayers);
    }

    /**
     * Verifies that a player cannot join a lobby whose expected capacity is already full.
     * Setup: a one-player lobby already occupied by its creator.
     * Action: another player attempts to join.
     * Expected behavior: the joining view receives a lobby-full error and is unregistered.
     * Edge case covered: capacity validation protects completed lobbies from extra participants.
     */
    @Test
    void joinLobbyWhenLobbyIsFullShouldSendErrorAndUnregisterRejectedView() {
        LobbyPhase onePlayerLobby = new LobbyPhase(controller, 1);
        FakeView dianaView = new FakeView();
        FakeView lucaView = new FakeView();

        onePlayerLobby.createLobby("Diana", TotemColor.RED, dianaView);
        onePlayerLobby.joinLobby("Luca", TotemColor.BLUE, lucaView);

        assertEquals("LOBBY_FULL", lucaView.lastErrorCode);
        assertEquals("Lobby is full", lucaView.lastErrorDescription);
        assertEquals(1, lucaView.errorCount);
        assertFalse(controller.getViews().containsKey("Luca"));
    }

    /**
     * Verifies that totem placement is rejected while players are still in the lobby.
     * Setup: a created lobby with one registered player.
     * Action: that player attempts to place a totem.
     * Expected behavior: the player receives an invalid-phase error.
     * Regression covered: game actions must not be accepted before the game phase starts.
     */
    @Test
    void placeTotemDuringLobbyShouldSendInvalidPhaseError() {
        FakeView dianaView = new FakeView();
        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        dianaView.clearErrors();

        lobbyPhase.placeTotem("Diana", 'C');

        assertEquals("INVALID_PHASE", dianaView.lastErrorCode);
        assertEquals("Cannot place totem during lobby phase", dianaView.lastErrorDescription);
        assertEquals(1, dianaView.errorCount);
    }

    /**
     * Verifies that card selection is rejected while players are still in the lobby.
     * Setup: a created lobby with one registered player.
     * Action: that player attempts to take cards using the current four-argument API.
     * Expected behavior: the player receives an invalid-phase error.
     * Regression covered: the test follows the current ordered-card signature of controller phases.
     */
    @Test
    void takeCardsDuringLobbyShouldSendInvalidPhaseError() {
        FakeView dianaView = new FakeView();
        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        dianaView.clearErrors();

        lobbyPhase.takeCards("Diana", List.of("U1"), List.of("L1"), List.of("U1", "L1"));

        assertEquals("INVALID_PHASE", dianaView.lastErrorCode);
        assertEquals("Cannot take cards during lobby phase", dianaView.lastErrorDescription);
        assertEquals(1, dianaView.errorCount);
    }

    /**
     * Verifies that extra-card selection is rejected while players are still in the lobby.
     * Setup: a created lobby with one registered player.
     * Action: that player attempts to take an extra card.
     * Expected behavior: the player receives an invalid-phase error.
     * Edge case covered: optional extra-card actions are also forbidden before gameplay starts.
     */
    @Test
    void takeExtraCardDuringLobbyShouldSendInvalidPhaseError() {
        FakeView dianaView = new FakeView();
        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        dianaView.clearErrors();

        lobbyPhase.takeExtraCard("Diana", "C1");

        assertEquals("INVALID_PHASE", dianaView.lastErrorCode);
        assertEquals("Cannot take extra card during lobby phase", dianaView.lastErrorDescription);
        assertEquals(1, dianaView.errorCount);
    }

    /**
     * Verifies that disconnecting a lobby participant removes that player and updates
     * the remaining connected views.
     * Setup: a lobby with Diana and Luca.
     * Action: Luca disconnects.
     * Expected behavior: Diana receives an updated lobby containing only herself and Luca is unregistered.
     * Edge case covered: lobby disconnection must keep the remaining lobby state consistent.
     */
    @Test
    void onDisconnectShouldRemovePlayerAndBroadcastUpdateToRemainingViews() {
        FakeView dianaView = new FakeView();
        FakeView lucaView = new FakeView();
        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        lobbyPhase.joinLobby("Luca", TotemColor.BLUE, lucaView);
        dianaView.clearLobbyUpdates();
        lucaView.clearLobbyUpdates();

        lobbyPhase.onDisconnect("Luca");

        assertEquals(1, dianaView.lobbyUpdateCount);
        assertEquals(List.of("Diana"), dianaView.lastLobbyPlayers);
        assertEquals(TotemColor.RED, dianaView.lastLobbyColorsByPlayer.get("Diana"));
        assertFalse(dianaView.lastLobbyColorsByPlayer.containsKey("Luca"));
        assertFalse(controller.getViews().containsKey("Luca"));
        assertEquals(0, lucaView.lobbyUpdateCount);
    }

    /**
     * Fake implementation of {@link VirtualView} used to observe lobby callbacks.
     */
    private static class FakeView implements VirtualView {

        private String nickname;
        private boolean connected = true;
        private String lastErrorCode;
        private String lastErrorDescription;
        private int errorCount;
        private String lastJoinSuccessNickname;
        private TotemColor lastJoinSuccessColor;
        private int joinSuccessCount;
        private List<String> lastLobbyPlayers = List.of();
        private Map<String, TotemColor> lastLobbyColorsByPlayer = Map.of();
        private int lastExpectedPlayers;
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
            joinSuccessCount++;
        }

        @Override
        public void onLobbyUpdate(List<String> players, Map<String, TotemColor> colorsByPlayer, int expected) {
            this.lastLobbyPlayers = List.copyOf(players);
            this.lastLobbyColorsByPlayer = Map.copyOf(colorsByPlayer);
            this.lastExpectedPlayers = expected;
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

        private void clearErrors() {
            lastErrorCode = null;
            lastErrorDescription = null;
            errorCount = 0;
        }

        private void clearLobbyUpdates() {
            lastLobbyPlayers = List.of();
            lastLobbyColorsByPlayer = Map.of();
            lastExpectedPlayers = 0;
            lobbyUpdateCount = 0;
        }
    }
}
