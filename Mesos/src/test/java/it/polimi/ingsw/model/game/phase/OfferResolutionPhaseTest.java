package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.BuildingDeck;
import it.polimi.ingsw.model.board.CardRow;
import it.polimi.ingsw.model.board.OfferSlot;
import it.polimi.ingsw.model.board.OfferTrack;
import it.polimi.ingsw.model.board.TribeDeck;
import it.polimi.ingsw.model.board.TurnOrderTrack;
import it.polimi.ingsw.model.card.BuilderCard;
import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.card.CavePaintingsEventCard;
import it.polimi.ingsw.model.card.EventCard;
import it.polimi.ingsw.model.card.TribeCard;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.card.building.EndBonusCard;
import it.polimi.ingsw.model.card.building.ExtraPickCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests offer-resolution behavior around ordered card application, optional
 * selections, extra-card phase selection, and final-round turn-order handling.
 *
 * @author Diana
 */
class OfferResolutionPhaseTest {

    /**
     * Verifies that a builder selected before a building in the same offer resolution
     * applies its discount before the building cost is paid.
     * Setup: Diana has 3 food, selects one builder from the upper row and one cost-3 building from the lower row.
     * Action: cards are resolved in builder-then-building order.
     * Expected behavior: the building costs 1 food after the builder discount, leaving Diana with 2 food.
     * Regression covered: ordered card resolution must affect same-turn building discounts.
     */
    @Test
    void takeCardsShouldApplyBuilderDiscountWhenBuilderIsResolvedBeforeBuilding() {
        Player diana = createPlayer("Diana", TotemColor.RED, 3);
        Player luca = createPlayer("Luca", TotemColor.BLUE, 0);
        BuilderCard builder = new BuilderCard(Era.Era1, "CH_BUILDER", 2, 0);
        EndBonusCard building = new EndBonusCard(Era.Era1, "BU_DISCOUNTED", 3, 0, 0);
        Game game = createOfferResolutionGame(
                List.of(diana, luca),
                List.of(builder),
                List.of(building),
                List.of(new OfferSlot('A', 1, 1, 0), new OfferSlot('B', 0, 0, 0)),
                1,
                0
        );

        game.takeCards("Diana", List.of("CH_BUILDER"), List.of("BU_DISCOUNTED"),
                List.of("CH_BUILDER", "BU_DISCOUNTED"));

        assertEquals(2, diana.getFood());
        assertTrue(diana.getTribe().getBuildings().contains(building));
        assertEquals(2, diana.getTribe().getBuildingDiscount());
        assertEquals("OfferResolutionPhase", game.getCurrentPhaseName());
    }

    /**
     * Verifies that a builder selected after a building in the same offer resolution
     * does not discount the already resolved building.
     * Setup: Diana has 3 food, selects one cost-3 building and one builder in the same turn.
     * Action: cards are resolved in building-then-builder order.
     * Expected behavior: the building costs the full 3 food, leaving Diana with 0 food.
     * Regression covered: same-turn discounts must respect the ordered card list.
     */
    @Test
    void takeCardsShouldNotApplyBuilderDiscountWhenBuildingIsResolvedBeforeBuilder() {
        Player diana = createPlayer("Diana", TotemColor.RED, 3);
        Player luca = createPlayer("Luca", TotemColor.BLUE, 0);
        BuilderCard builder = new BuilderCard(Era.Era1, "CH_BUILDER", 2, 0);
        EndBonusCard building = new EndBonusCard(Era.Era1, "BU_FULL_PRICE", 3, 0, 0);
        Game game = createOfferResolutionGame(
                List.of(diana, luca),
                List.of(builder),
                List.of(building),
                List.of(new OfferSlot('A', 1, 1, 0), new OfferSlot('B', 0, 0, 0)),
                1,
                0
        );

        game.takeCards("Diana", List.of("CH_BUILDER"), List.of("BU_FULL_PRICE"),
                List.of("BU_FULL_PRICE", "CH_BUILDER"));

        assertEquals(0, diana.getFood());
        assertTrue(diana.getTribe().getBuildings().contains(building));
        assertEquals(2, diana.getTribe().getBuildingDiscount());
        assertEquals("OfferResolutionPhase", game.getCurrentPhaseName());
    }

    /**
     * Verifies that a lower-row selection is not forced when all lower-row cards are events.
     * Setup: Diana occupies a slot with one lower-row selection, but the lower row contains only a non-pickable event.
     * Action: Diana resolves the offer without selecting lower cards.
     * Expected behavior: no exception is thrown and resolution advances to the next player.
     * Edge case covered: event cards must not make a mandatory lower selection impossible.
     */
    @Test
    void takeCardsShouldAllowNoLowerSelectionWhenLowerRowContainsOnlyEvents() {
        Player diana = createPlayer("Diana", TotemColor.RED, 0);
        Player luca = createPlayer("Luca", TotemColor.BLUE, 0);
        EventCard event = new CavePaintingsEventCard(Era.Era1, "EV_ONLY", false, 1, 1, 1);
        Game game = createOfferResolutionGame(
                List.of(diana, luca),
                List.of(),
                List.of(event),
                List.of(new OfferSlot('A', 0, 1, 0), new OfferSlot('B', 0, 0, 0)),
                1,
                0
        );

        assertDoesNotThrow(() -> game.takeCards("Diana", List.of(), List.of(), List.of()));

        assertEquals(1, game.getCurrentPlayerIndex());
        assertEquals("OfferResolutionPhase", game.getCurrentPhaseName());
    }

    /**
     * Verifies that a lower-row selection is not forced when every lower-row building
     * is unaffordable for the resolving player.
     * Setup: Diana occupies a slot with one lower-row selection and has no food while the only lower building costs 5.
     * Action: Diana resolves the offer without selecting lower cards.
     * Expected behavior: no exception is thrown and resolution advances to the next player.
     * Edge case covered: unaffordable buildings must reduce the effective required lower selections.
     */
    @Test
    void takeCardsShouldAllowNoLowerSelectionWhenLowerRowContainsOnlyUnaffordableBuildings() {
        Player diana = createPlayer("Diana", TotemColor.RED, 0);
        Player luca = createPlayer("Luca", TotemColor.BLUE, 0);
        EndBonusCard expensiveBuilding = new EndBonusCard(Era.Era1, "BU_EXPENSIVE", 5, 0, 0);
        Game game = createOfferResolutionGame(
                List.of(diana, luca),
                List.of(),
                List.of(expensiveBuilding),
                List.of(new OfferSlot('A', 0, 1, 0), new OfferSlot('B', 0, 0, 0)),
                1,
                0
        );

        assertDoesNotThrow(() -> game.takeCards("Diana", List.of(), List.of(), List.of()));

        assertEquals(1, game.getCurrentPlayerIndex());
        assertEquals("OfferResolutionPhase", game.getCurrentPhaseName());
    }

    /**
     * Verifies that the extra-card phase is skipped when a player owns an ExtraPick
     * building but cannot afford any pickable upper-row card.
     * Setup: Diana owns ExtraPick, has no food, and the only upper-row card is an unaffordable building.
     * Action: Diana completes the last pending offer resolution without taking cards.
     * Expected behavior: the game does not enter {@code ExtraCardPhase}.
     * Edge case covered: ExtraPick requires both the building effect and at least one affordable upper card.
     */
    @Test
    void takeCardsShouldSkipExtraCardPhaseWhenExtraPickOwnerHasNoAffordableUpperCard() {
        Player diana = createPlayer("Diana", TotemColor.RED, 0);
        diana.getTribe().getBuildings().add(new ExtraPickCard(Era.Era1, "BU_EXTRA_PICK", 0, 0));
        EndBonusCard expensiveUpperBuilding = new EndBonusCard(Era.Era1, "BU_TOO_EXPENSIVE", 5, 0, 0);
        Game game = createOfferResolutionGame(
                List.of(diana),
                List.of(expensiveUpperBuilding),
                List.of(),
                List.of(new OfferSlot('A', 0, 0, 0)),
                1,
                0
        );

        game.takeCards("Diana", List.of(), List.of(), List.of());

        assertNotEquals("ExtraCardPhase", game.getCurrentPhaseName());
        assertEquals("TotemPlacementPhase", game.getCurrentPhaseName());
    }

    /**
     * Verifies that totems are not returned to the turn-order track during round 10
     * offer resolution.
     * Setup: Diana resolves an offer during round 10 with an initially empty turn-order track.
     * Action: Diana completes the offer resolution.
     * Expected behavior: the turn-order track remains empty after the action.
     * Regression covered: final-round offer resolution must not rebuild next-round turn order.
     */
    @Test
    void takeCardsShouldNotReturnTotemToTurnOrderTrackDuringRoundTen() {
        Player diana = createPlayer("Diana", TotemColor.RED, 0);
        Player luca = createPlayer("Luca", TotemColor.BLUE, 0);
        Game game = createOfferResolutionGame(
                List.of(diana, luca),
                List.of(),
                List.of(),
                List.of(new OfferSlot('A', 0, 0, 0), new OfferSlot('B', 0, 0, 0)),
                10,
                0
        );

        game.takeCards("Diana", List.of(), List.of(), List.of());

        assertTrue(game.getBoard().getTurnOrderTrack().getPlayersInOrder().isEmpty());
        assertEquals(1, game.getCurrentPlayerIndex());
        assertEquals("OfferResolutionPhase", game.getCurrentPhaseName());
    }

    private Player createPlayer(String nickname, TotemColor color, int food) {
        return new Player(nickname, color, new Tribe(), food, 0);
    }

    private Game createOfferResolutionGame(List<Player> players, List<Card> upperCards, List<Card> lowerCards,
                                           List<OfferSlot> slots, int round, int currentPlayerIndex) {
        for (int i = 0; i < players.size(); i++) {
            slots.get(i).place(players.get(i));
        }

        Board board = new Board(
                new OfferTrack(slots),
                new TurnOrderTrack(List.of(), new int[]{0, 0, 0, 0, 0}, players.size()),
                new TribeDeck(new ArrayDeque<>(), Era.Era1),
                new BuildingDeck(List.of(), List.of(), List.of()),
                createCardRow(upperCards),
                createCardRow(lowerCards),
                Era.Era1
        );

        return new Game(
                1,
                players,
                board,
                round,
                new OfferResolutionPhase(),
                GameState.InProgress,
                new ArrayList<>(players),
                currentPlayerIndex,
                board.getOfferResolutionOrder()
        );
    }

    private CardRow createCardRow(List<Card> cards) {
        List<TribeCard> tribeCards = new ArrayList<>();
        List<BuildingCard> buildingCards = new ArrayList<>();

        for (Card card : cards) {
            if (card instanceof BuildingCard buildingCard) {
                buildingCards.add(buildingCard);
            } else {
                tribeCards.add((TribeCard) card);
            }
        }

        return new CardRow(tribeCards, buildingCards);
    }
}
