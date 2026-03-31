package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.card.building.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * Static factory responsible for loading card data from JSON files
 * and producing {@link TribeDeck} and {@link BuildingDeck} instances.
 *
 * <p>This class is never instantiated — all members are static.</p>
 *
 * @author Luca Grecchi
*/
public class CardFactory {
    private static final JsonArray charactersData;
    private static final JsonArray eventsData;
    private static final JsonArray buildingsData;

    //Map for character type
    private static final Map<String, Function<JsonObject, CharacterCard>> CHARACTER_PARSERS = Map.of(
            "HUNTER",   CardFactory::parseHunter,
            "SHAMAN",   CardFactory::parseShaman,
            "BUILDER",  CardFactory::parseBuilder,
            "INVENTOR", CardFactory::parseInventor,
            "ARTIST",   CardFactory::parseArtist,
            "GATHERER", CardFactory::parseGatherer
    );

    //Map for event type
    private static final Map<String, Function<JsonObject, EventCard>> EVENT_PARSERS = Map.of(
            "HUNT",   CardFactory::parseHunt,
            "SUSTENANCE",   CardFactory::parseSustenance,
            "SHAMANIC_RITUAL",  CardFactory::parseShamanicRitual,
            "CAVE_PAINTINGS", CardFactory::parseCavePaintings
    );

    //Map for building type
    private static final Map<String, Function<JsonObject, BuildingCard>> BUILDING_PARSERS = Map.ofEntries(
            Map.entry("TRIGGER_SET",   CardFactory::parseTriggerSet),
            Map.entry("SUSTENANCE_DISCOUNT",   CardFactory::parseSustenanceDiscount),
            Map.entry("SHAMAN_NO_PENALTY",  CardFactory::parseShamanNoPenalty),
            Map.entry("TURN_ORDER_BONUS", CardFactory::parseTurnOrderBonus),
            Map.entry("TRIGGER_INVENTOR",   CardFactory::parseTriggerInventor),
            Map.entry("SHAMAN_DOUBLE_REWARD", CardFactory::parseShamanDoubleReward),
            Map.entry("SHAMAN_BONUS_ICONS",   CardFactory::parseShamanBonusIcons),
            Map.entry("HUNT_BONUS",   CardFactory::parseHuntBonus),
            Map.entry("END_DOUBLE_BUILDER",  CardFactory::parseEndDoubleBuilder),
            Map.entry("PAINTINGS_FOOD", CardFactory::parsePaintingsFood),
            Map.entry("END_SIX_SET",   CardFactory::parseEndSixSet),
            Map.entry("END_PER_TYPE", CardFactory::parseEndPerType),
            Map.entry("EXTRA_PICK",   CardFactory::parseExtraPick),
            Map.entry("END_BONUS", CardFactory::parseEndBonus)
    );

    // Loads JSON data from resources into static arrays at class initialization
    static{
        //Characters
        InputStream is_c = CardFactory.class.getResourceAsStream("/JSON/characters.json");

        //Throw exception if file not found
        if (is_c == null) throw new IllegalStateException("File not found: characters.json");

        Reader reader_c = new InputStreamReader(is_c, StandardCharsets.UTF_8);
        charactersData = JsonParser.parseReader(reader_c).getAsJsonArray();

        //Events
        InputStream is_e = CardFactory.class.getResourceAsStream("/JSON/events.json");

        if (is_e == null) throw new IllegalStateException("File not found: events.json");

        Reader reader_e = new InputStreamReader(is_e, StandardCharsets.UTF_8);
        eventsData = JsonParser.parseReader(reader_e).getAsJsonArray();

        //Buildings
        InputStream is_b = CardFactory.class.getResourceAsStream("/JSON/buildings.json");

        if (is_b == null) throw new IllegalStateException("File not found: buildings.json");

        Reader reader_b = new InputStreamReader(is_b, StandardCharsets.UTF_8);
        buildingsData = JsonParser.parseReader(reader_b).getAsJsonArray();
    }

    /** Private constructor — this class is never instantiated. */
    private CardFactory(){}


    /**
     * Creates a TribeDeck for the given number of players.
     *
     * @param numPlayers number of players in the game
     * @return a fully assembled TribeDeck
     */
    public static TribeDeck createTribeDeck(int numPlayers){

        List<CharacterCard> characters = parseCharacters(numPlayers);
        List<EventCard> events = parseEvents();
        Deque<TribeCard> deck = new ArrayDeque<>();

        //Bucket per Era
        Map<Era, List<TribeCard>> buckets = Map.of(
                Era.Era1, new ArrayList<>(),
                Era.Era2, new ArrayList<>(),
                Era.Era3, new ArrayList<>()
        );

        //Add characters in the correct bucket
        for(CharacterCard character: characters){
            buckets.get(character.getEra()).add(character);
        }

        //Add events in the correct bucket, filtering final events
        for(EventCard event: events){
            if(!event.isFinal()){
                buckets.get(event.getEra()).add(event);
            }else{
                deck.addLast(event);
            }
        }

        //Shuffle buckets
        Collections.shuffle(buckets.get(Era.Era1));
        Collections.shuffle(buckets.get(Era.Era2));
        Collections.shuffle(buckets.get(Era.Era3));

        //Populating the deck
        for(TribeCard card: buckets.get(Era.Era3)){
            deck.addFirst(card);
        }
        for(TribeCard card: buckets.get(Era.Era2)){
            deck.addFirst(card);
        }
        for(TribeCard card: buckets.get(Era.Era1)){
            deck.addFirst(card);
        }

        return new TribeDeck(deck, Era.Era1);
    }

    /**
     * Creates a BuildingDeck for the given number of players.
     *
     * @param numPlayers number of players in the game
     * @return a fully assembled BuildingDeck
     */
    public static BuildingDeck createBuildingDeck(int numPlayers){

        List<BuildingCard> buildings = parseBuildings();

        int count_era1;
        int count_era2;
        int count_era3;

        //Bucket per Era
        Map<Era, List<BuildingCard>> buckets = Map.of(
                Era.Era1, new ArrayList<>(),
                Era.Era2, new ArrayList<>(),
                Era.Era3, new ArrayList<>()
        );

        //Add buildings in the correct bucket
        for(BuildingCard building: buildings){
            buckets.get(building.getEra()).add(building);
        }

        //Shuffle buckets
        Collections.shuffle(buckets.get(Era.Era1));
        Collections.shuffle(buckets.get(Era.Era2));
        Collections.shuffle(buckets.get(Era.Era3));

        switch(numPlayers){
            case 2 -> { count_era1 = 1; count_era2 = 2; count_era3 = 3; }
            case 3 -> { count_era1 = 2; count_era2 = 2; count_era3 = 4; }
            case 4 -> { count_era1 = 2; count_era2 = 3; count_era3 = 4; }
            case 5 -> { count_era1 = 2; count_era2 = 3; count_era3 = 5; }
            default -> throw new IllegalArgumentException("Invalid numPlayers: " + numPlayers);
        }

        List<BuildingCard> era1 = new ArrayList<>(buckets.get(Era.Era1).subList(0, count_era1));
        List<BuildingCard> era2 = new ArrayList<>(buckets.get(Era.Era2).subList(0, count_era2));
        List<BuildingCard> era3 = new ArrayList<>(buckets.get(Era.Era3).subList(0, count_era3));

        return new BuildingDeck(era1, era2, era3);
    }

    //Characters
    /**
     * Static method for creating instances of character cards
     *
     * @param numPlayers number of players in the game
     *
     * @return list of CharacterCard filtered by number of players
     * */
    private static List<CharacterCard> parseCharacters(int numPlayers){
        List<CharacterCard> characters = new ArrayList<>();

        // Skip cards that require more players than the current game
        for(JsonElement el : charactersData){
            JsonObject obj = el.getAsJsonObject();
            String type = obj.get("type").getAsString();
            int minPlayers = obj.get("minPlayers").getAsInt();

            if(minPlayers > numPlayers) continue;

            Function<JsonObject, CharacterCard> parser = CHARACTER_PARSERS.get(type);
            characters.add(parser.apply(obj));
        }

        return characters;
    }

    /**
     * Method for parsing an HunterCard
     *
     * @param obj JsonObject
     * @return new hunterCard
     */
    private static HunterCard parseHunter(JsonObject obj){
        return new HunterCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("hunterFoodBonus").getAsBoolean()
        );
    }

    /**
     * Method for parsing a ShamanCard
     *
     * @param obj JsonObject
     * @return new ShamanCard
     */
    private static ShamanCard parseShaman(JsonObject obj){
        return new ShamanCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("shamanSymbols").getAsInt()
        );
    }

    /**
     * Method for parsing a BuilderCard
     *
     * @param obj JsonObject
     * @return new BuilderCard
     */
    private static BuilderCard parseBuilder(JsonObject obj){
        return new BuilderCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("builderDiscount").getAsInt(),
                obj.get("builderPrestige").getAsInt()
        );
    }

    /**
     * Method for parsing an InventorCard
     *
     * @param obj JsonObject
     * @return new InventorCard
     */
    private static InventorCard parseInventor(JsonObject obj){
        return new InventorCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                InventionType.valueOf(obj.get("inventionType").getAsString())
        );
    }

    /**
     * Method for parsing a ArtistCard
     *
     * @param obj JsonObject
     * @return new ArtistCard
     */
    private static ArtistCard parseArtist(JsonObject obj){
        return new ArtistCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString()
        );
    }

    /**
     * Method for parsing a GathererCard
     *
     * @param obj JsonObject
     * @return new GathererCard
     */
    private static GathererCard parseGatherer(JsonObject obj){
        return new GathererCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString()
        );
    }



    //Events
    /**
     * Static method for creating instances of events cards
     *
     * @return list of EventCard
     * */
    private static List<EventCard> parseEvents(){
        List<EventCard> events = new ArrayList<>();

        for(JsonElement el : eventsData){
            JsonObject obj = el.getAsJsonObject();
            String type = obj.get("type").getAsString();

            Function<JsonObject, EventCard> parser = EVENT_PARSERS.get(type);
            events.add(parser.apply(obj));
        }

        return events;
    }

    /**
     * Method for parsing a SustenanceEventCard
     *
     * @param obj JsonObject
     * @return new SustenanceEventCard
     */
    private static SustenanceEventCard parseSustenance(JsonObject obj){
        return new SustenanceEventCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("isFinal").getAsBoolean(),
                obj.get("prestigePenalty").getAsInt()
        );
    }

    /**
     * Method for parsing a HuntEventCard
     *
     * @param obj JsonObject
     * @return new HuntEventCard
     */
    private static HuntEventCard parseHunt(JsonObject obj){
        return new HuntEventCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("isFinal").getAsBoolean(),
                obj.get("prestigeReward").getAsInt()
        );
    }

    /**
     * Method for parsing a ShamanicRitualEventCard
     *
     * @param obj JsonObject
     * @return new ShamanicRitualEventCard
     */
    private static ShamanicRitualEventCard parseShamanicRitual(JsonObject obj){
        return new ShamanicRitualEventCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("isFinal").getAsBoolean(),
                obj.get("majorityReward").getAsInt(),
                obj.get("minorityPenalty").getAsInt()
        );
    }

    /**
     * Method for parsing a CavePaintingsEventCard
     *
     * @param obj JsonObject
     * @return new CavePaintingsEventCard
     */
    private static CavePaintingsEventCard parseCavePaintings(JsonObject obj){
        return new CavePaintingsEventCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("isFinal").getAsBoolean(),
                obj.get("requiredArtists").getAsInt(),
                obj.get("prestigePenalty").getAsInt(),
                obj.get("rewardPerArtist").getAsInt()
        );
    }


    //Buildings
    /**
     * Static method for creating instances of building cards
     *
     * @return list of BuildingCard
     * */
    private static List<BuildingCard> parseBuildings(){
        List<BuildingCard> buildings = new ArrayList<>();

        for(JsonElement el : buildingsData){
            JsonObject obj = el.getAsJsonObject();
            String type = obj.get("type").getAsString();

            Function<JsonObject, BuildingCard> parser = BUILDING_PARSERS.get(type);
            buildings.add(parser.apply(obj));
        }

        return buildings;
    }

    /**
     * Method for parsing a TriggerSetCard
     *
     * @param obj JsonObject
     * @return new TriggerSetCard
     */
    private static TriggerSetCard parseTriggerSet(JsonObject obj){
        return new TriggerSetCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt(),
                obj.get("foodBonus").getAsInt()
        );
    }

    /**
     * Method for parsing a SustenanceDiscountCard
     *
     * @param obj JsonObject
     * @return new SustenanceDiscountCard
     */
    private static SustenanceDiscountCard parseSustenanceDiscount(JsonObject obj){
        return new SustenanceDiscountCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt(),
                CharacterType.valueOf(obj.get("targetCharacterType").getAsString())
        );
    }

    /**
     * Method for parsing a ShamanNoPenaltyCard
     *
     * @param obj JsonObject
     * @return new ShamanNoPenaltyCard
     */
    private static ShamanNoPenaltyCard parseShamanNoPenalty(JsonObject obj){
        return new ShamanNoPenaltyCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt()
        );
    }

    /**
     * Method for parsing a TurnOrderBonusCard
     *
     * @param obj JsonObject
     * @return new TurnOrderBonusCard
     */
    private static TurnOrderBonusCard parseTurnOrderBonus(JsonObject obj){
        return new TurnOrderBonusCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt(),
                obj.get("foodBonus").getAsInt()
        );
    }

    /**
     * Method for parsing a TriggerInventorCard
     *
     * @param obj JsonObject
     * @return new TriggerInventorCard
     */
    private static TriggerInventorCard parseTriggerInventor(JsonObject obj){
        return new TriggerInventorCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt(),
                obj.get("foodBonus").getAsInt()
        );
    }

    /**
     * Method for parsing a ShamanDoubleRewardCard
     *
     * @param obj JsonObject
     * @return new ShamanDoubleRewardCard
     */
    private static ShamanDoubleRewardCard parseShamanDoubleReward(JsonObject obj){
        return new ShamanDoubleRewardCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt()
        );
    }

    /**
     * Method for parsing a ShamanBonusIconsCard
     *
     * @param obj JsonObject
     * @return new ShamanBonusIconsCard
     */
    private static ShamanBonusIconsCard parseShamanBonusIcons(JsonObject obj){
        return new ShamanBonusIconsCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt(),
                obj.get("bonusIcons").getAsInt()
        );
    }

    /**
     * Method for parsing a HuntBonusCard
     *
     * @param obj JsonObject
     * @return new HuntBonusCard
     */
    private static HuntBonusCard parseHuntBonus(JsonObject obj){
        return new HuntBonusCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt(),
                obj.get("foodBonus").getAsInt(),
                obj.get("prestigeBonus").getAsInt()
        );
    }

    /**
     * Method for parsing a EndDoubleBuilderCard
     *
     * @param obj JsonObject
     * @return new EndDoubleBuilderCard
     */
    private static EndDoubleBuilderCard parseEndDoubleBuilder(JsonObject obj){
        return new EndDoubleBuilderCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt()
        );
    }

    /**
     * Method for parsing a PaintingsFoodCard
     *
     * @param obj JsonObject
     * @return new PaintingsFoodCard
     */
    private static PaintingsFoodCard parsePaintingsFood(JsonObject obj){
        return new PaintingsFoodCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt(),
                obj.get("foodPerArtist").getAsInt()
        );
    }

    /**
     * Method for parsing a EndSixSetCard
     *
     * @param obj JsonObject
     * @return new EndSixSetCard
     */
    private static EndSixSetCard parseEndSixSet(JsonObject obj){
        return new EndSixSetCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt()
        );
    }

    /**
     * Method for parsing a EndPerTypeCard
     *
     * @param obj JsonObject
     * @return new EndPerTypeCard
     */
    private static EndPerTypeCard parseEndPerType(JsonObject obj){
        return new EndPerTypeCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt(),
                CharacterType.valueOf(obj.get("targetType").getAsString()),
                obj.get("ppPerCard").getAsInt()
        );
    }

    /**
     * Method for parsing a ExtraPickCard
     *
     * @param obj JsonObject
     * @return new ExtraPickCard
     */
    private static ExtraPickCard parseExtraPick(JsonObject obj){
        return new ExtraPickCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt()
        );
    }

    /**
     * Method for parsing a EndBonusCard
     *
     * @param obj JsonObject
     * @return new EndBonusCard
     */
    private static EndBonusCard parseEndBonus(JsonObject obj){
        return new EndBonusCard(
                Era.valueOf(obj.get("era").getAsString()),
                obj.get("id").getAsString(),
                obj.get("cost").getAsInt(),
                obj.get("prestigePoints").getAsInt(),
                obj.get("prestigeBonus").getAsInt()
        );
    }
}
