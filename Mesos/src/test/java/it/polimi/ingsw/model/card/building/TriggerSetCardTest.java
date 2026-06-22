package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.ArtistCard;
import it.polimi.ingsw.model.card.BuilderCard;
import it.polimi.ingsw.model.card.GathererCard;
import it.polimi.ingsw.model.card.HunterCard;
import it.polimi.ingsw.model.card.InventorCard;
import it.polimi.ingsw.model.card.ShamanCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the complete-character-set trigger provided by {@link TriggerSetCard}.
 */
class TriggerSetCardTest {

    /**
     * Setup: a player owns the trigger building and starts with no complete set.
     * Action: add one card for five character types, then add the sixth type.
     * Expected behavior: food is granted only when the complete six-type set is formed.
     * Edge case: an incomplete five-type collection must not trigger the bonus.
     */
    @Test
    void triggerShouldGrantFoodWhenNewCompleteCharacterSetIsFormed() {
        Player player = new Player("P1", null, new Tribe(), 10, 0);
        TriggerSetCard building = new TriggerSetCard(Era.Era1, "B1", 0, 0, 5);

        building.applyTo(player);
        new HunterCard(Era.Era1, "C1", false).applyTo(player);
        new BuilderCard(Era.Era1, "C2", 0, 0).applyTo(player);
        new ShamanCard(Era.Era1, "C3", 1).applyTo(player);
        new InventorCard(Era.Era1, "C4", null).applyTo(player);
        new ArtistCard(Era.Era1, "C5").applyTo(player);

        assertEquals(10, player.getFood());

        new GathererCard(Era.Era1, "C6").applyTo(player);

        assertEquals(15, player.getFood());
    }
}
