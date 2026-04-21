package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShamanBonusIconsCardTest {
    @Test
    void testBonusShamanIcons() {
        ShamanBonusIconsCard building = new ShamanBonusIconsCard(Era.Era1, "B1", 2, 2, 4);
        assertEquals(4, building.getBonusShamanIcons()); //
    }
}