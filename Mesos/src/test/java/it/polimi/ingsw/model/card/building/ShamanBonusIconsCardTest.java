package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the virtual shaman-icon bonus exposed by {@link ShamanBonusIconsCard}.
 */
class ShamanBonusIconsCardTest {

    /**
     * Setup: a shaman-icon building is created with a fixed icon bonus.
     * Action: query the bonus icon accessor.
     * Expected behavior: the configured number of virtual icons is returned.
     * Edge case: virtual icons are separate from shaman cards stored in the tribe.
     */
    @Test
    void bonusShamanIconsShouldReturnConfiguredValue() {
        ShamanBonusIconsCard building = new ShamanBonusIconsCard(Era.Era1, "B1", 2, 2, 4);

        assertEquals(4, building.getBonusShamanIcons());
    }
}
