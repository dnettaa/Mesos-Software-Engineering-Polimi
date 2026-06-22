package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.CharacterType;
import it.polimi.ingsw.model.card.HunterCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the character-type based end-game bonus provided by {@link EndPerTypeCard}.
 */
class EndPerTypeCardTest {

    /**
     * Setup: a bonus building rewards each owned hunter and the player owns three hunters.
     * Action: calculate the building end-game bonus.
     * Expected behavior: the bonus equals matching card count multiplied by the configured value.
     * Edge case: only the configured character type contributes to the bonus.
     */
    @Test
    void calculateEndGameBonusShouldRewardConfiguredCharacterTypeCount() {
        EndPerTypeCard building = new EndPerTypeCard(Era.Era1, "B1", 0, 0, CharacterType.HUNTER, 2);
        Player player = new Player("P1", null, new Tribe(), 0, 0);
        new HunterCard(Era.Era1, "H1", false).applyTo(player);
        new HunterCard(Era.Era1, "H2", false).applyTo(player);
        new HunterCard(Era.Era1, "H3", false).applyTo(player);

        assertEquals(6, building.calculateEndGameBonus(player));
    }
}
