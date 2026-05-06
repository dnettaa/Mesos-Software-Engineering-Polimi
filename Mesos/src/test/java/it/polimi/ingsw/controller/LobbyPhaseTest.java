package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.socket.message.ErrorMessage;
import it.polimi.ingsw.network.socket.message.JoinSuccessMessage;
import it.polimi.ingsw.network.socket.message.LobbyUpdateMessage;
import it.polimi.ingsw.network.socket.message.ServerMessage;
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

    @Test
    void testCreateLobbyRegistersFirstPlayerAndBroadcastsUpdate(){
        FakeView dianaView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);

        assertEquals("Diana", dianaView.nickname);
        assertEquals(2, dianaView.sentMessages.size());
        assertTrue(dianaView.sentMessages.get(0) instanceof JoinSuccessMessage);
        assertTrue(dianaView.sentMessages.get(1) instanceof LobbyUpdateMessage);
    }

    @Test
    void testCreateLobbyWhenLobbyAlreadyExistsSendsError(){
        FakeView dianaView = new FakeView();
        FakeView lucaView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        lobbyPhase.createLobby("Luca", TotemColor.BLUE, lucaView);

        assertEquals(1, lucaView.sentMessages.size());
        assertTrue(lucaView.sentMessages.getFirst() instanceof ErrorMessage);
    }

    @Test
    void testJoinLobbyBeforeCreationSendsError(){
        FakeView lucaView = new FakeView();

        lobbyPhase.joinLobby("Luca", TotemColor.BLUE, lucaView);

        assertEquals(1, lucaView.sentMessages.size());
        assertTrue(lucaView.sentMessages.getFirst() instanceof ErrorMessage);
    }

    @Test
    void testJoinLobbyRegistersPlayerAndBroadcastsUpdate(){
        FakeView dianaView = new FakeView();
        FakeView lucaView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        lobbyPhase.joinLobby("Luca", TotemColor.BLUE, lucaView);

        assertEquals("Luca", lucaView.nickname);
        assertEquals(2, lucaView.sentMessages.size());
        assertTrue(lucaView.sentMessages.get(0) instanceof JoinSuccessMessage);
        assertTrue(lucaView.sentMessages.get(1) instanceof LobbyUpdateMessage);

        assertEquals(3, dianaView.sentMessages.size());
        assertTrue(dianaView.sentMessages.get(2) instanceof LobbyUpdateMessage);
    }

    @Test
    void testJoinLobbyWithDuplicateNicknameSendsError(){
        FakeView dianaView = new FakeView();
        FakeView duplicateView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        lobbyPhase.joinLobby("Diana", TotemColor.BLUE, duplicateView);

        assertEquals(1, duplicateView.sentMessages.size());
        assertTrue(duplicateView.sentMessages.getFirst() instanceof ErrorMessage);
    }

    @Test
    void testJoinLobbyWithDuplicateColorSendsError(){
        FakeView dianaView = new FakeView();
        FakeView lucaView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        lobbyPhase.joinLobby("Luca", TotemColor.RED, lucaView);

        assertEquals(1, lucaView.sentMessages.size());
        assertTrue(lucaView.sentMessages.getFirst() instanceof ErrorMessage);
    }

    @Test
    void testJoinLobbyWhenLobbyIsFullSendsError(){
        LobbyPhase onePlayerLobby = new LobbyPhase(controller, 1);
        FakeView dianaView = new FakeView();
        FakeView lucaView = new FakeView();

        onePlayerLobby.createLobby("Diana", TotemColor.RED, dianaView);
        onePlayerLobby.joinLobby("Luca", TotemColor.BLUE, lucaView);

        assertEquals(1, lucaView.sentMessages.size());
        assertTrue(lucaView.sentMessages.getFirst() instanceof ErrorMessage);
    }

    @Test
    void testPlaceTotemDuringLobbySendsInvalidPhaseError(){
        FakeView dianaView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        dianaView.sentMessages.clear();

        lobbyPhase.placeTotem("Diana", 'C');

        assertEquals(1, dianaView.sentMessages.size());
        assertTrue(dianaView.sentMessages.getFirst() instanceof ErrorMessage);
    }

    @Test
    void testTakeCardsDuringLobbySendsInvalidPhaseError(){
        FakeView dianaView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        dianaView.sentMessages.clear();

        lobbyPhase.takeCards("Diana", List.of("U1"), List.of("L1"));

        assertEquals(1, dianaView.sentMessages.size());
        assertTrue(dianaView.sentMessages.getFirst() instanceof ErrorMessage);
    }

    @Test
    void testTakeExtraCardDuringLobbySendsInvalidPhaseError(){
        FakeView dianaView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        dianaView.sentMessages.clear();

        lobbyPhase.takeExtraCard("Diana", "C1");

        assertEquals(1, dianaView.sentMessages.size());
        assertTrue(dianaView.sentMessages.getFirst() instanceof ErrorMessage);
    }

    @Test
    void testOnDisconnectRemovesPlayerAndBroadcastsUpdate(){
        FakeView dianaView = new FakeView();
        FakeView lucaView = new FakeView();

        lobbyPhase.createLobby("Diana", TotemColor.RED, dianaView);
        lobbyPhase.joinLobby("Luca", TotemColor.BLUE, lucaView);

        dianaView.sentMessages.clear();
        lucaView.sentMessages.clear();

        lobbyPhase.onDisconnect("Luca");

        assertEquals(1, dianaView.sentMessages.size());
        assertTrue(dianaView.sentMessages.getFirst() instanceof LobbyUpdateMessage);

        controller.sendTo("Luca", new ErrorMessage("TEST", "Luca should be unregistered"));
        assertTrue(lucaView.sentMessages.isEmpty());
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
