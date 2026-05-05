package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.exception.ErrorCode;
import it.polimi.ingsw.model.exception.GameException;
import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.game.GameListener;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.message.ErrorMessage;
import it.polimi.ingsw.network.message.ServerMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InGamePhase.
 * Verifies that in-game actions are delegated to the model
 * and that GameException is translated into ErrorMessage.
 * @author Diana
 */

class InGamePhaseTest{
    private GameController controller;
    private FakeGame game;
    private InGamePhase inGamePhase;
    private FakeView dianaView;

    @BeforeEach
    void setUp(){
        controller = new GameController();
        game = new FakeGame();
        inGamePhase = new InGamePhase(controller, game);

        dianaView = new FakeView();
        controller.registerView("Diana", dianaView);
    }

    /**
     * Fake model used to verify that InGamePhase delegates actions correctly.
     */
    private static class FakeGame implements GameActions{
        private int placeTotemCalls;
        private int takeCardsCalls;
        private int takeExtraCardCalls;
        private String lastTotemNickname;
        private char lastSlotID;
        private String lastTakeCardsNickname;
        private List<String> lastUpperIDs;
        private List<String> lastLowerIDs;
        private String lastExtraCardNickname;
        private String lastCardID;
        private GameException exceptionToThrow;

        @Override
        public void placeTotem(String nickname, char slotID){
            placeTotemCalls++;
            lastTotemNickname = nickname;
            lastSlotID = slotID;

            if (exceptionToThrow != null){
                throw exceptionToThrow;
            }
        }

        @Override
        public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs){
            takeCardsCalls++;
            lastTakeCardsNickname = nickname;
            lastUpperIDs = upperIDs;
            lastLowerIDs = lowerIDs;

            if (exceptionToThrow != null){
                throw exceptionToThrow;
            }
        }

        @Override
        public void takeExtraCard(String nickname, String cardID){
            takeExtraCardCalls++;
            lastExtraCardNickname = nickname;
            lastCardID = cardID;

            if (exceptionToThrow != null){
                throw exceptionToThrow;
            }
        }

        @Override
        public String getCurrentPlayerNickname(){
            return "";
        }

        @Override
        public String getCurrentPhaseName(){
            return "";
        }

        @Override
        public boolean isGameEnded(){
            return false;
        }

        @Override
        public void addListener(GameListener listener){
            //not needed for this test
        }

        @Override
        public void removeListener(GameListener listener){
            //do nothing

        }

        @Override
        public void startGame(){
            //do nothing
        }
    }

    /**
     * Fake implementation of VirtualView used to observe messages sent by the controller.
     */
    private static class FakeView implements VirtualView{
        private String nickname;
        private boolean connected = true;
        private final List<ServerMessage> sentMessages = new ArrayList<>();

        @Override
        public String getNickname(){
            return nickname;
        }

        @Override
        public void setNickname(String nickname){
            this.nickname = nickname;
        }

        @Override
        public boolean isConnected(){
            return connected;
        }

        @Override
        public void send(ServerMessage message){
            sentMessages.add(message);
        }

        @Override
        public void disconnect(){
            connected = false;
            sentMessages.clear();
        }
    }
}
