package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.card.building.*;
import it.polimi.ingsw.model.card.CharacterType;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class BuildingDeckTest {

    private BuildingDeck buildingDeck;

    @BeforeEach
    void setUp(){
        List<BuildingCard> era1 = new ArrayList<>(List.of(
                new TriggerSetCard(Era.Era1, "BU01", 4, 3, 5),
                new SustenanceDiscountCard(Era.Era1, "BU02", 4, 4, CharacterType.GATHERER)
        ));

        List<BuildingCard> era2 = new ArrayList<>(List.of(
                new ShamanDoubleRewardCard(Era.Era2, "BU07", 7, 0),
                new ShamanBonusIconsCard(Era.Era2, "BU08", 6, 4, 3)
        ));

        List<BuildingCard> era3 = new ArrayList<>(List.of(
                new EndPerTypeCard(Era.Era3, "BU14", 8, 8, CharacterType.HUNTER, 3),
                new ExtraPickCard(Era.Era3, "BU20", 9, 3),
                new EndBonusCard(Era.Era3, "BU21", 10, 0, 25)
        ));

        buildingDeck = new BuildingDeck(era1, era2, era3);
    }

    @Test
    void testRevealAllEra1(){
        List<BuildingCard> era1Cards = buildingDeck.revealAll(Era.Era1);
        assertEquals(2, era1Cards.size());
        assertEquals("BU01", era1Cards.get(0).getId());
        assertEquals("BU02", era1Cards.get(1).getId());
    }

    @Test
    void testRevealAllEra2(){
        List<BuildingCard> era2Cards = buildingDeck.revealAll(Era.Era2);
        assertEquals(2, era2Cards.size());
        assertEquals("BU07", era2Cards.get(0).getId());
        assertEquals("BU08", era2Cards.get(1).getId());
    }

    @Test
    void testRevealAllEra3(){
        List<BuildingCard> era3Cards = buildingDeck.revealAll(Era.Era3);
        assertEquals(3, era3Cards.size());
        assertEquals("BU14", era3Cards.get(0).getId());
        assertEquals("BU20", era3Cards.get(1).getId());
        assertEquals("BU21", era3Cards.get(2).getId());
    }
}