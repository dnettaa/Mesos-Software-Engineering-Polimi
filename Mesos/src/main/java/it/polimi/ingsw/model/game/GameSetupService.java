package it.polimi.ingsw.model.game;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.card.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * Class for initial game setup, producing a functioning {@link Game}
 *
 * @author Luca Grecchi
 */
public class GameSetupService {

    /**
     * Create a new Game
     *
     * @param playerSelection Map with nicknames and totem colors
     * @param gameID id of the game
     * @return a functioning Game
     */
    public Game createNewGame(Map<String, TotemColor> playerSelection, int gameID){

        int numPlayers = playerSelection.size();

        //Creating all players
        List<Player> players = setupPlayers(playerSelection);

        //Creating Board
        Board board = setupBoard(numPlayers, players);

        //Assign initial food to each player
        assignInitialFood(players);

        return new Game(gameID, players, board, 1, GamePhase.TotemPlacement, GameState.InProgress,
                new ArrayList<Player>(board.getTurnOrderTrack().getPlayersInOrder()), 0, new ArrayList<OfferSlot>());
    }

    /**
     * It instances all the players in the game
     *
     * @param playerSelection Map with nicknames and totem colors
     * @return list of players
     */
    private List<Player> setupPlayers(Map<String,TotemColor> playerSelection) {

        List<Player> players = new ArrayList<>();

        for (Map.Entry<String, TotemColor> entry : playerSelection.entrySet()) {
            String nickname = entry.getKey();
            TotemColor totemColor = entry.getValue();
            Tribe tribe = new Tribe();

            players.add(new Player(nickname, totemColor, tribe, 0, 0));
        }
        return players;
    }

    /**
     * It set up all the Board components calling other private methods
     *
     * @param numPlayers number of players
     * @return a functioning Board
     */
    private Board setupBoard(int numPlayers, List<Player> players) {

        //Creating decks
        TribeDeck tribeDeck = CardFactory.createTribeDeck(numPlayers);
        BuildingDeck buildingDeck = CardFactory.createBuildingDeck(numPlayers);

        //Creating OfferTrack
        OfferTrack offerTrack = setupOfferTrack(numPlayers);

        //Creating TurnOrderTrack
        TurnOrderTrack turnOrderTrack = setupTurnOrderTrack(numPlayers, players);

        //Creating Rows
        CardRow upperRow = new CardRow(new ArrayList<TribeCard>(), new ArrayList<BuildingCard>());
        CardRow lowerRow = setupLowerRow(tribeDeck, numPlayers, upperRow);
        setupUpperRow(tribeDeck, buildingDeck, numPlayers, upperRow);

        return new Board (offerTrack, turnOrderTrack, tribeDeck, buildingDeck, upperRow, lowerRow, Era.Era1);
    }

    /**
     * It creates the offerTrack picking offer slots from JSON
     *
     * @param numPlayers number of players
     * @return a functioning OfferTrack
     */
    private OfferTrack setupOfferTrack(int numPlayers){

        List<OfferSlot> offerSlots = new ArrayList<>();

        JsonArray slots;
        InputStream is = GameSetupService.class.getResourceAsStream("/JSON/offerSlots.json");
        if (is == null) throw new IllegalStateException("File not found: offerSlots.json");
        Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        slots = JsonParser.parseReader(reader).getAsJsonArray();

        for(JsonElement el : slots){
            JsonObject obj = el.getAsJsonObject();

            int minPlayers = obj.get("minPlayers").getAsInt();
            if (minPlayers > numPlayers) continue;

            char slotID = obj.get("slotID").getAsString().charAt(0);
            int upSel = obj.get("upSel").getAsInt();
            int downSel = obj.get("downSel").getAsInt();
            int foodReward = obj.get("foodReward").getAsInt();

            offerSlots.add(new OfferSlot (slotID, upSel, downSel, foodReward));
        }

        return new OfferTrack(offerSlots);
    }

    /**
     * It creates the TurnOrderTrack picking it from JSON
     *
     * @param numPlayers number of players
     * @return a functioning OfferTrack
     */
    private TurnOrderTrack setupTurnOrderTrack(int numPlayers, List<Player> players){

        JsonArray turnOrderTrack;
        InputStream is = GameSetupService.class.getResourceAsStream("/JSON/turnOrderTrack.json");
        if (is == null) throw new IllegalStateException("File not found: turnOrderTrack.json");
        Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        turnOrderTrack = JsonParser.parseReader(reader).getAsJsonArray();

        int[] foodBonus = null;

        for(JsonElement el : turnOrderTrack){
            JsonObject obj = el.getAsJsonObject();

            int playersNumber = obj.get("numPlayers").getAsInt();
            if (playersNumber != numPlayers) continue;

            JsonArray bonusArray = obj.get("foodBonus").getAsJsonArray();
            foodBonus = new int[bonusArray.size()];
            for (int i = 0; i < bonusArray.size(); i++) {
                foodBonus[i] = bonusArray.get(i).getAsInt();
            }
        }

        Collections.shuffle(players);

        return new TurnOrderTrack(players, foodBonus, numPlayers);
    }

    /**
     * Sets up the lower card row by drawing cards from the TribeDeck.
     * Event cards drawn are placed in the upper row instead.
     *
     * @param tribeDeck the deck to draw from
     * @param numPlayers number of players
     * @param upperRow the upper row where event cards are placed
     * @return the populated lower CardRow
     */
    private CardRow setupLowerRow(TribeDeck tribeDeck, int numPlayers, CardRow upperRow){

        List<TribeCard> tribeCards = new ArrayList<>();

        int count = 0;

        while(count != numPlayers + 1){

            TribeCard card = tribeDeck.draw();

            if(card instanceof EventCard){
                upperRow.addTribeCard(card);
            }else{
                tribeCards.add(card);
                count++;
            }
        }

        return new CardRow(tribeCards, new ArrayList<>());
    }

    /**
     * Sets up the upper card row by drawing cards from the TribeDeck and the BuildingDeck
     *
     * @param tribeDeck the deck to draw from
     * @param buildingDeck the deck to draw from
     * @param numPlayers number of players
     * @param upperRow the upper row already created
     */
    private void setupUpperRow(TribeDeck tribeDeck, BuildingDeck buildingDeck, int numPlayers, CardRow upperRow){

        //controllare che size sia 0 se vuota
        int count = upperRow.getTribeCards().size();

        while(count != numPlayers + 4){

             TribeCard card = tribeDeck.draw();

             upperRow.addTribeCard(card);
             count++;
        }

        for(BuildingCard buildingCard: buildingDeck.revealAll(Era.Era1)){
            upperRow.addBuildingCard(buildingCard);
        }
    }

    /**
     * Assigns initial food to each player based on turn order position.
     * First player gets 2 food, 2nd and 3rd get 3, 4th and 5th get 4.
     *
     * @param players list of players in turn order
     */
    private void assignInitialFood(List<Player> players) {

        for (int i = 0; i < players.size(); i++) {
            switch(i + 1) {
                case 1 -> players.get(i).addFood(2);
                case 2, 3 -> players.get(i).addFood(3);
                case 4, 5 -> players.get(i).addFood(4);
            }
        }
    }
}

