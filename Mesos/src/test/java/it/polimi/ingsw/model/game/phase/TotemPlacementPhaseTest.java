package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.exception.GameException;
import it.polimi.ingsw.model.game.DTO.CardsTakenDTO;
import it.polimi.ingsw.model.game.DTO.EventResolvedDTO;
import it.polimi.ingsw.model.game.DTO.ExtraCardTakenDTO;
import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.model.game.DTO.TotemPlacedDTO;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameListener;
import it.polimi.ingsw.model.game.GameSetupService;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests legal actions, invalid actions, transitions, and notifications in {@link TotemPlacementPhase}.
 */
class TotemPlacementPhaseTest {

    private final GameSetupService setup = new GameSetupService();

    private Game createGame(int playerCount) {
        Map<String, TotemColor> players = new LinkedHashMap<>();
        TotemColor[] colors = TotemColor.values();

        for (int i = 0; i < playerCount; i++) {
            players.put("P" + i, colors[i]);
        }

        return setup.createNewGame(players, 1);
    }

    private char getFreeSlot(Game game) {
        return game.getBoard()
                .buildOfferSlotsData()
                .stream()
                .filter(slot -> slot.occupantNickname() == null)
                .findFirst()
                .orElseThrow()
                .slotID();
    }

    /**
     * Captures totem-placement notifications emitted by the game.
     */
    static class TestListener implements GameListener {
        boolean called = false;
        TotemPlacedDTO dto;

        @Override
        public void onTotemPlaced(TotemPlacedDTO dto) {
            this.called = true;
            this.dto = dto;
        }

        @Override public void onGameStarted(GameStateSnapshot snapshot) {}
        @Override public void onCardsTaken(CardsTakenDTO dto) {}
        @Override public void onExtraCardTaken(ExtraCardTakenDTO dto) {}
        @Override public void onEventResolved(EventResolvedDTO dto) {}
        @Override public void onRoundEnded(RoundEndedDTO dto) {}
        @Override public void onGameEnded(GameEndedDTO dto) {}
    }

    /**
     * Setup: a game is in the initial totem-placement phase.
     * Action: the current player places a totem in a free slot.
     * Expected behavior: the action completes without throwing.
     * Edge case: the slot is selected dynamically to avoid depending on board fixture ordering.
     */
    @Test
    void placeTotemShouldAcceptCurrentPlayer() {
        Game game = createGame(3);
        String current = game.getCurrentPlayerNickname();
        char slotID = getFreeSlot(game);

        assertDoesNotThrow(() -> game.placeTotem(current, slotID));
    }

    /**
     * Setup: a non-active player attempts to act during totem placement.
     * Action: that player tries to place a totem in a free slot.
     * Expected behavior: the phase rejects the action with a game exception.
     * Edge case: a valid free slot must not allow the wrong player to act.
     */
    @Test
    void placeTotemShouldRejectNonActivePlayer() {
        Game game = createGame(3);
        List<Player> players = game.getPlayers();
        String wrong = players.get(1).getNickname();
        char slotID = getFreeSlot(game);

        assertThrows(GameException.class, () -> game.placeTotem(wrong, slotID));
    }

    /**
     * Setup: every player in a three-player game has an available placement turn.
     * Action: each current player places one totem.
     * Expected behavior: after all placements, the game moves to offer resolution.
     * Edge case: the transition occurs only after the last required placement.
     */
    @Test
    void placeTotemsForAllPlayersShouldTransitionToOfferResolution() {
        Game game = createGame(3);

        for (int i = 0; i < game.getPlayers().size(); i++) {
            game.placeTotem(game.getCurrentPlayerNickname(), getFreeSlot(game));
        }

        assertEquals("OfferResolutionPhase", game.getCurrentPhaseName());
    }

    /**
     * Setup: a listener is registered before a valid placement.
     * Action: the current player places a totem.
     * Expected behavior: a DTO is emitted with player, slot, next player, and next phase data.
     * Edge case: the DTO must contain enough information for both TUI and GUI clients to update.
     */
    @Test
    void placeTotemShouldEmitCompleteTotemPlacedDto() {
        Game game = createGame(3);
        TestListener listener = new TestListener();
        game.addListener(listener);
        String current = game.getCurrentPlayerNickname();
        char slotID = getFreeSlot(game);

        game.placeTotem(current, slotID);

        assertTrue(listener.called);
        assertNotNull(listener.dto);
        assertEquals(current, listener.dto.placerNickname());
        assertEquals(slotID, listener.dto.slotID());
        assertNotNull(listener.dto.nextPlayerNickname());
        assertNotNull(listener.dto.nextPhaseName());
    }
}
