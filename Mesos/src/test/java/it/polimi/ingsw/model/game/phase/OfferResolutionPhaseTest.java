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
import it.polimi.ingsw.model.card.HunterCard;
import it.polimi.ingsw.model.card.EventCard;
import it.polimi.ingsw.model.card.TribeCard;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.card.building.EndBonusCard;
import it.polimi.ingsw.model.card.building.ExtraPickCard;
import it.polimi.ingsw.model.exception.ErrorCode;
import it.polimi.ingsw.model.exception.GameException;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
     * Verifies that a building cannot be selected before a same-turn builder
     * when the building is affordable only after the builder discount.
     * Setup: Diana has 1 food, selects one builder with discount 2 and one cost-3 building.
     * Action: cards are submitted in building-then-builder order.
     * Expected behavior: the selection is rejected because the building is not affordable before the builder applies.
     * Regression covered: same-turn builder discounts must not make earlier buildings valid.
     */
    @Test
    void takeCardsShouldRejectBuildingBeforeBuilderWhenFullPriceIsUnaffordable() {
        Player diana = createPlayer("Diana", TotemColor.RED, 1);
        Player luca = createPlayer("Luca", TotemColor.BLUE, 0);
        BuilderCard builder = new BuilderCard(Era.Era1, "CH_BUILDER", 2, 0);
        EndBonusCard building = new EndBonusCard(Era.Era1, "BU_UNAFFORDABLE_FIRST", 3, 0, 0);
        Game game = createOfferResolutionGame(
                List.of(diana, luca),
                List.of(builder),
                List.of(building),
                List.of(new OfferSlot('A', 1, 1, 0), new OfferSlot('B', 0, 0, 0)),
                1,
                0
        );

        GameException exception = assertThrows(GameException.class, () ->
                game.takeCards("Diana", List.of("CH_BUILDER"), List.of("BU_UNAFFORDABLE_FIRST"),
                        List.of("BU_UNAFFORDABLE_FIRST", "CH_BUILDER")));

        assertEquals(ErrorCode.INSUFFICIENT_FOOD, exception.getCode());
        assertEquals(1, diana.getFood());
        assertTrue(diana.getTribe().getBuildings().isEmpty());
        assertEquals(0, diana.getTribe().getBuildingDiscount());
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

    /**
     * Verifies that buildings are optional: with upSel 2 over one character and one
     * affordable building, the player may take just the character and skip the building.
     * Setup: Diana has 5 food, upper row holds one character and one cost-2 building, upSel 2.
     * Action: Diana takes only the character.
     * Expected behavior: no exception, the building stays in the row, no food is spent.
     * Rule covered: a pickable character is mandatory but an affordable building can be skipped.
     */
    @Test
    void takeCardsShouldAllowSkippingOptionalBuildingWhenCharacterIsTaken() {
        Player diana = createPlayer("Diana", TotemColor.RED, 5);
        Player luca = createPlayer("Luca", TotemColor.BLUE, 0);
        HunterCard character = new HunterCard(Era.Era1, "CH_PLAIN", false);
        EndBonusCard building = new EndBonusCard(Era.Era1, "BU_OPTIONAL", 2, 0, 0);
        Game game = createOfferResolutionGame(
                List.of(diana, luca),
                List.of(character, building),
                List.of(),
                List.of(new OfferSlot('A', 2, 0, 0), new OfferSlot('B', 0, 0, 0)),
                1,
                0
        );

        assertDoesNotThrow(() -> game.takeCards("Diana", List.of("CH_PLAIN"), List.of(),
                List.of("CH_PLAIN")));

        assertEquals(5, diana.getFood());
        assertFalse(game.getBoard().getUpperRowCards().contains(character));
        assertTrue(game.getBoard().getUpperRowCards().contains(building));
        assertEquals(1, game.getCurrentPlayerIndex());
    }

    /**
     * Verifies that a character cannot be left behind while the action is not fully used.
     * Setup: Diana has 5 food, upper row holds one character and one cost-2 building, upSel 2.
     * Action: Diana takes only the building, leaving the character unselected.
     * Expected behavior: the selection is rejected with {@link ErrorCode#INVALID_SELECTION}.
     * Rule covered: stopping below the slot action is allowed only when no character is left.
     */
    @Test
    void takeCardsShouldRejectTakingBuildingWhileLeavingAvailableCharacter() {
        Player diana = createPlayer("Diana", TotemColor.RED, 5);
        Player luca = createPlayer("Luca", TotemColor.BLUE, 0);
        HunterCard character = new HunterCard(Era.Era1, "CH_PLAIN", false);
        EndBonusCard building = new EndBonusCard(Era.Era1, "BU_AFFORDABLE", 2, 0, 0);
        Game game = createOfferResolutionGame(
                List.of(diana, luca),
                List.of(character, building),
                List.of(),
                List.of(new OfferSlot('A', 2, 0, 0), new OfferSlot('B', 0, 0, 0)),
                1,
                0
        );

        GameException exception = assertThrows(GameException.class, () ->
                game.takeCards("Diana", List.of("BU_AFFORDABLE"), List.of(),
                        List.of("BU_AFFORDABLE")));

        assertEquals(ErrorCode.INVALID_SELECTION, exception.getCode());
        assertEquals(5, diana.getFood());
        assertEquals(0, game.getCurrentPlayerIndex());
        assertEquals("OfferResolutionPhase", game.getCurrentPhaseName());
    }

    /**
     * Verifies that the slot action may be filled entirely with buildings, skipping a character.
     * Setup: Diana has 5 food, upper row holds one character and two cost-2 buildings, upSel 2.
     * Action: Diana takes both buildings and leaves the character.
     * Expected behavior: no exception, both buildings are paid (food 5 to 1), the character stays in the row.
     * Rule covered: a character may be skipped as long as every action slot is filled.
     */
    @Test
    void takeCardsShouldAllowFillingActionWithBuildingsSkippingCharacter() {
        Player diana = createPlayer("Diana", TotemColor.RED, 5);
        Player luca = createPlayer("Luca", TotemColor.BLUE, 0);
        HunterCard character = new HunterCard(Era.Era1, "CH_PLAIN", false);
        EndBonusCard firstBuilding = new EndBonusCard(Era.Era1, "BU_ONE", 2, 0, 0);
        EndBonusCard secondBuilding = new EndBonusCard(Era.Era1, "BU_TWO", 2, 0, 0);
        Game game = createOfferResolutionGame(
                List.of(diana, luca),
                List.of(character, firstBuilding, secondBuilding),
                List.of(),
                List.of(new OfferSlot('A', 2, 0, 0), new OfferSlot('B', 0, 0, 0)),
                1,
                0
        );

        assertDoesNotThrow(() -> game.takeCards("Diana", List.of("BU_ONE", "BU_TWO"), List.of(),
                List.of("BU_ONE", "BU_TWO")));

        assertEquals(1, diana.getFood());
        assertTrue(game.getBoard().getUpperRowCards().contains(character));
        assertEquals(1, game.getCurrentPlayerIndex());
    }

    /**
     * Verifies that taking fewer cards than the action is rejected when a character is left unselected.
     * Setup: Diana has 0 food, upper row holds two characters, upSel 2.
     * Action: Diana takes only one of the two characters.
     * Expected behavior: the selection is rejected with {@link ErrorCode#INVALID_SELECTION}.
     * Rule covered: when no buildings are wanted, the action must be filled with characters.
     */
    @Test
    void takeCardsShouldRejectFewerCardsWhenCharacterIsLeftUnselected() {
        Player diana = createPlayer("Diana", TotemColor.RED, 0);
        Player luca = createPlayer("Luca", TotemColor.BLUE, 0);
        HunterCard firstCharacter = new HunterCard(Era.Era1, "CH_ONE", false);
        HunterCard secondCharacter = new HunterCard(Era.Era1, "CH_TWO", false);
        Game game = createOfferResolutionGame(
                List.of(diana, luca),
                List.of(firstCharacter, secondCharacter),
                List.of(),
                List.of(new OfferSlot('A', 2, 0, 0), new OfferSlot('B', 0, 0, 0)),
                1,
                0
        );

        GameException exception = assertThrows(GameException.class, () ->
                game.takeCards("Diana", List.of("CH_ONE"), List.of(), List.of("CH_ONE")));

        assertEquals(ErrorCode.INVALID_SELECTION, exception.getCode());
        assertEquals(0, game.getCurrentPlayerIndex());
        assertEquals("OfferResolutionPhase", game.getCurrentPhaseName());
    }

    /**
     * Verifies that selecting more cards than the slot action allows is rejected.
     * Setup: Diana has 0 food, upper row holds two characters, upSel 1.
     * Action: Diana tries to take both characters.
     * Expected behavior: the selection is rejected with {@link ErrorCode#INVALID_SELECTION}.
     * Rule covered: a row selection may never exceed the slot action.
     */
    @Test
    void takeCardsShouldRejectMoreCardsThanSlotAction() {
        Player diana = createPlayer("Diana", TotemColor.RED, 0);
        Player luca = createPlayer("Luca", TotemColor.BLUE, 0);
        HunterCard firstCharacter = new HunterCard(Era.Era1, "CH_ONE", false);
        HunterCard secondCharacter = new HunterCard(Era.Era1, "CH_TWO", false);
        Game game = createOfferResolutionGame(
                List.of(diana, luca),
                List.of(firstCharacter, secondCharacter),
                List.of(),
                List.of(new OfferSlot('A', 1, 0, 0), new OfferSlot('B', 0, 0, 0)),
                1,
                0
        );

        GameException exception = assertThrows(GameException.class, () ->
                game.takeCards("Diana", List.of("CH_ONE", "CH_TWO"), List.of(),
                        List.of("CH_ONE", "CH_TWO")));

        assertEquals(ErrorCode.INVALID_SELECTION, exception.getCode());
        assertEquals(0, game.getCurrentPlayerIndex());
        assertEquals("OfferResolutionPhase", game.getCurrentPhaseName());
    }

    /**
     * Verifies that hunter food bonuses are applied in pick order against the growing tribe.
     * Setup: Diana has 0 food and no hunters; the upper row holds a hunter without food bonus
     * and a hunter with food bonus, upSel 2.
     * Action: Diana takes them in order — first the one without bonus, then the one with bonus.
     * Expected behavior: the no-bonus hunter grants nothing, then the bonus hunter grants 1 food
     * per hunter now in the tribe (2), so Diana ends with 2 food.
     * Regression covered: ordered card application makes each hunter count the hunters already added this turn.
     */
    @Test
    void takeCardsShouldApplyHunterFoodBonusInPickOrder() {
        Player diana = createPlayer("Diana", TotemColor.RED, 0);
        Player luca = createPlayer("Luca", TotemColor.BLUE, 0);
        HunterCard hunterNoBonus = new HunterCard(Era.Era1, "CH_HUNT_PLAIN", false);
        HunterCard hunterWithBonus = new HunterCard(Era.Era1, "CH_HUNT_BONUS", true);
        Game game = createOfferResolutionGame(
                List.of(diana, luca),
                List.of(hunterNoBonus, hunterWithBonus),
                List.of(),
                List.of(new OfferSlot('A', 2, 0, 0), new OfferSlot('B', 0, 0, 0)),
                1,
                0
        );

        game.takeCards("Diana", List.of("CH_HUNT_PLAIN", "CH_HUNT_BONUS"), List.of(),
                List.of("CH_HUNT_PLAIN", "CH_HUNT_BONUS"));

        assertEquals(2, diana.getFood());
        assertEquals(1, game.getCurrentPlayerIndex());
        assertEquals("OfferResolutionPhase", game.getCurrentPhaseName());
    }

    /**
     * Verifies that the hunter pick order changes the food gained.
     * Setup: the same two hunters as the in-order scenario, upSel 2, Diana with 0 food.
     * Action: Diana takes the bonus hunter first, then the plain one.
     * Expected behavior: the bonus hunter grants 1 food (a single hunter in the tribe) and the plain
     * hunter grants nothing, so Diana ends with 1 food instead of 2.
     * Regression covered: the food bonus depends on tribe size at application time, which the order controls.
     */
    @Test
    void takeCardsShouldGrantLessHunterFoodWhenBonusHunterIsTakenFirst() {
        Player diana = createPlayer("Diana", TotemColor.RED, 0);
        Player luca = createPlayer("Luca", TotemColor.BLUE, 0);
        HunterCard hunterNoBonus = new HunterCard(Era.Era1, "CH_HUNT_PLAIN", false);
        HunterCard hunterWithBonus = new HunterCard(Era.Era1, "CH_HUNT_BONUS", true);
        Game game = createOfferResolutionGame(
                List.of(diana, luca),
                List.of(hunterNoBonus, hunterWithBonus),
                List.of(),
                List.of(new OfferSlot('A', 2, 0, 0), new OfferSlot('B', 0, 0, 0)),
                1,
                0
        );

        game.takeCards("Diana", List.of("CH_HUNT_PLAIN", "CH_HUNT_BONUS"), List.of(),
                List.of("CH_HUNT_BONUS", "CH_HUNT_PLAIN"));

        assertEquals(1, diana.getFood());
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
