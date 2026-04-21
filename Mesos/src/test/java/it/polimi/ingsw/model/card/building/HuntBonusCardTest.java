package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HuntBonusCardTest {
    @Test
    void testHuntBonuses() {
        // Food bonus: 1, PP bonus: 2
        HuntBonusCard building = new HuntBonusCard(Era.Era1, "B1", 2, 2, 1, 2);
        assertEquals(1, building.getHunterBonusFood());
        assertEquals(2, building.getHuntBonusPP());
    }
}