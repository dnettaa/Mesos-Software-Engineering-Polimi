package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link ShamanCard}, verifying shaman symbols and acquisition behavior.
 *
 * @author Diana
 */
class ShamanCardTest {

    /**
     * Verifies that a shaman exposes its configured number of symbols.
     * Setup: a shaman card is created with three symbols.
     * Action: read the symbol count.
     * Expected behavior: the getter returns three.
     * Edge case covered: ritual scoring depends on card symbols, not only card count.
     */
    @Test
    void shamanShouldExposeConfiguredSymbolCount() {
        ShamanCard card = new ShamanCard(Era.Era1, "S1", 3);

        assertEquals(3, card.getShamanSymbols());
    }

    /**
     * Verifies that acquiring a shaman adds it to the player's tribe.
     * Setup: a player starts with no shamans.
     * Action: apply a shaman card to the player.
     * Expected behavior: the tribe contains one shaman and three shaman symbols.
     * Edge case covered: shaman acquisition updates both type count and ritual symbol total.
     */
    @Test
    void applyToShouldAddShamanToPlayerTribe() {
        ShamanCard card = new ShamanCard(Era.Era1, "S1", 3);
        Player player = new Player("P1", TotemColor.RED, new Tribe(), 0, 0);

        card.applyTo(player);

        assertEquals(1, player.getTribe().countByType(CharacterType.SHAMAN));
        assertEquals(3, player.getTribe().countShamanIcons());
    }
}
