package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.HunterCard;
import it.polimi.ingsw.model.card.CharacterType;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EndPerTypeCard bonus calculation.
 */
class EndPerTypeCardTest {

    @Test
    void testCalculateEndGameBonus() {
        // Bonus: 2 PP per HUNTER
        EndPerTypeCard building = new EndPerTypeCard(Era.Era1, "B1", 0, 0, CharacterType.HUNTER, 2);
        Player player = new Player("P1", null, new Tribe(), 0, 0);

        // Add 3 Hunters to the tribe
        new HunterCard(Era.Era1, "H1", false).applyTo(player);
        new HunterCard(Era.Era1, "H2", false).applyTo(player);
        new HunterCard(Era.Era1, "H3", false).applyTo(player);

        // 3 Hunters * 2 PP = 6 PP expected
        assertEquals(6, building.calculateEndGameBonus(player));
    }
}