package it.polimi.ingsw.controller;

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
 * Tests for GameController communication utilities and phase delegation.
 * @author Diana
 */

class GameControllerTest{
    private GameController controller;
    private FakeView view;

    @BeforeEach
    void setUp(){
        controller = new GameController();
        view = new FakeView();
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

    /**
     * Fake phase used only to verify phase transitions.
     */
    private static class FakePhase implements ControllerPhase{

        @Override
        public void createLobby(String nickname, TotemColor color, VirtualView view){
        }

        @Override
        public void joinLobby(String nickname, TotemColor color, VirtualView view){
        }

        @Override
        public void placeTotem(String nickname, char slotID){
        }

        @Override
        public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs){
        }

        @Override
        public void takeExtraCard(String nickname, String cardID){
        }

        @Override
        public void onDisconnect(String nickname){
        }
    }

}
