package it.polimi.ingsw.model.player;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.game.Era;

class TribeTest {

    private Tribe tribe;

    @BeforeEach
    void setUp(){
        tribe = new Tribe();

        tribe.addCharacter(new HunterCard(Era.Era1, "H1", false));
        tribe.addCharacter(new HunterCard(Era.Era1, "H2", true));
        tribe.addCharacter(new ShamanCard(Era.Era1, "S1", 2));
        tribe.addCharacter(new ShamanCard(Era.Era2, "S2", 3));
        tribe.addCharacter(new BuilderCard(Era.Era1, "B1", 1, 3));
        tribe.addCharacter(new InventorCard(Era.Era1, "I1", InventionType.TYPE_1));
        tribe.addCharacter(new InventorCard(Era.Era2, "I2", InventionType.TYPE_3));
        tribe.addCharacter(new ArtistCard(Era.Era1, "A1"));
        tribe.addCharacter(new ArtistCard(Era.Era2, "A2"));
        tribe.addCharacter(new GathererCard(Era.Era1, "G1"));
    }

    @Test
    void testCountByType(){
        assertEquals(1, tribe.countByType(CharacterType.BUILDER));
        assertEquals(2, tribe.countByType(CharacterType.SHAMAN));
        assertEquals(2, tribe.countByType(CharacterType.ARTIST));
        assertEquals(1, tribe.countByType(CharacterType.GATHERER));
        assertEquals(2, tribe.countByType(CharacterType.HUNTER));
        assertEquals(2, tribe.countByType(CharacterType.INVENTOR));
    }

    @Test
    void testGetByType(){
        List<CharacterCard> hunters = tribe.getByType(CharacterType.HUNTER);
        assertEquals(2, hunters.size());
        assertEquals("H1", hunters.get(0).getId());
        assertEquals("H2", hunters.get(1).getId());
    }

    @Test
    void testCountDistinctInventionIcon(){
        assertEquals(2, tribe.countDistinctInventionIcons());
    }

    @Test
    void testGetBuildingDiscount(){
        assertEquals(1, tribe.getBuildingDiscount());
    }

    @Test
    void testGetCollectorDiscount(){
        assertEquals(3, tribe.getCollectorDiscount());
    }

    @Test
    void testCountDistinctType(){
        assertEquals(6, tribe.countDistinctTypes());
    }

    @Test
    void testGetFullSetsCount(){
        assertEquals(1, tribe.getFullSetsCount());
    }

    @Test
    void testCountShamanIcons(){
        assertEquals(5, tribe.countShamanIcons());
    }
}
