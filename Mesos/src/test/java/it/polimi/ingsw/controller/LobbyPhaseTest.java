package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.message.ErrorMessage;
import it.polimi.ingsw.network.message.JoinSuccessMessage;
import it.polimi.ingsw.network.message.LobbyUpdateMessage;
import it.polimi.ingsw.network.message.ServerMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LobbyPhase.
 * Verifies lobby creation, player joins, validation errors,
 * invalid in-game actions during lobby, and disconnection handling.
 * @author Diana
 */

class LobbyPhaseTest{
    private GameController controller;
    private LobbyPhase lobbyPhase;

    @BeforeEach
    void setUp(){
        controller = new GameController();
        lobbyPhase = new LobbyPhase(controller, 3);
    }

    /**
     * Fake implementation of VirtualView used to observe messages sent during lobby tests.
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
