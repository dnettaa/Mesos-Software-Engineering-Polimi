package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SustenanceEventCard}, verifying food payment, penalties, and
 * event ordering classification.
 *
 * @author Diana
 */
class SustenanceEventCardTest {

    /**
     * Verifies that unpaid sustenance food produces prestige penalties.
     * Setup: a player has three characters, one food, and ten prestige points.
     * Action: resolve a sustenance event with penalty three per unpaid food.
     * Expected behavior: the player spends one food, leaves two unpaid, and loses six prestige points.
     * Edge case covered: partial payment consumes all available food before applying penalties.
     */
    @Test
    void resolveEventShouldSpendFoodAndApplyPenaltyForUnpaidSustenance() {
        SustenanceEventCard event = new SustenanceEventCard(Era.Era1, "SU1", false, 3);
        Player player = new Player("P1", TotemColor.RED, new Tribe(), 1, 10);
        new HunterCard(Era.Era1, "H1", false).applyTo(player);
        new ShamanCard(Era.Era1, "S1", 1).applyTo(player);
        new ShamanCard(Era.Era1, "S2", 1).applyTo(player);

        event.resolveEvent(List.of(player));

        assertEquals(0, player.getFood());
        assertEquals(4, player.getPrestigePoints());
    }

    /**
     * Verifies that sustenance events classify themselves into the sustenance list.
     * Setup: empty normal and sustenance event lists.
     * Action: add a sustenance event to the event lists.
     * Expected behavior: the normal list stays empty and the sustenance list receives the event.
     * Edge case covered: event resolution processes sustenance after normal events.
     */
    @Test
    void addToListShouldAppendEventToSustenanceListOnly() {
        SustenanceEventCard event = new SustenanceEventCard(Era.Era1, "SU1", false, 3);
        List<EventCard> normalEvents = new ArrayList<>();
        List<EventCard> sustenanceEvents = new ArrayList<>();

        event.addToList(normalEvents, sustenanceEvents);

        assertTrue(normalEvents.isEmpty());
        assertEquals(List.of(event), sustenanceEvents);
    }
}
