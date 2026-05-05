package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualServer;
import it.polimi.ingsw.network.message.ClientMessage;
import it.polimi.ingsw.network.message.CreateLobbyMessage;
import it.polimi.ingsw.network.message.JoinLobbyMessage;
import it.polimi.ingsw.network.message.ServerMessage;
import it.polimi.ingsw.view.View;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Client-side RMI adapter.
 * <p>
 * It implements {@link VirtualServer}, so the View can use it as a normal server,
 * and {@link ClientRMI}, so the real server can call it back remotely.
 * @author Diana
 */

public class RMIClientAdapter implements VirtualServer, ClientRMI{
    private static final String SERVER_NAME = "MesosServer";
    private final View view;
    private ServerRMI serverStub;
    private ClientRMI clientStub;
    private String nickname;
    private boolean connected;

    /**
     * Creates a new RMI client adapter.
     *
     * @param view the local client-side view
     */
    public RMIClientAdapter(View view){
        this.view = view;
        this.connected = false;
    }

    /**
     * Connects this client to the RMI registry and registers it on the server.
     *
     * @param host the host where the RMI registry is running
     * @param port the port where the RMI registry is listening
     */
    public void connect(String host, int port){
        try{
            Registry registry = LocateRegistry.getRegistry(host, port);
            serverStub = (ServerRMI) registry.lookup(SERVER_NAME);

            clientStub = (ClientRMI) UnicastRemoteObject.exportObject(this, 0);
            serverStub.connect(clientStub);

            connected = true;
        } catch (Exception e){
            connected = false;
            view.notifyDisconnection("Unable to connect to RMI server: " + e.getMessage());
        }
    }

    /**
     * Sends a create-lobby request to the server.
     *
     * @param nickname the nickname chosen by the player
     * @param color the totem color chosen by the player
     * @param expectedPlayers the number of players required to start the game
     */
    @Override
    public void createLobby(String nickname, TotemColor color, int expectedPlayers){
        this.nickname = nickname;
        sendMessage(new CreateLobbyMessage(nickname, color, expectedPlayers));
    }

    /**
     * Sends a join-lobby request to the server.
     *
     * @param nickname the nickname chosen by the player
     * @param color the totem color chosen by the player
     */
    @Override
    public void joinLobby(String nickname, TotemColor color){
        this.nickname = nickname;
        sendMessage(new JoinLobbyMessage(nickname, color));
    }


    /**
     * Sends a generic client message to the remote server.
     *
     * @param message the message representing the client action
     */
    @Override
    public void sendMessage(ClientMessage message){
        if (!connected || serverStub == null || clientStub == null){
            view.notifyDisconnection("RMI client is not connected to the server.");
            return;
        }

        try{
            serverStub.sendMessage(message, clientStub);
        } catch (RemoteException e){
            connected = false;
            view.notifyDisconnection("Connection with the RMI server lost.");
        }
    }

    /**
     * Receives a server message through RMI and applies it to the local view.
     *
     * @param message the server message to be delivered to the view
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void receiveMessage(ServerMessage message) throws RemoteException{
        System.out.println("[RMI CLIENT] Ricevuto: " + message.getClass().getSimpleName());
        message.apply(view);
        System.out.println("[RMI CLIENT] Applicato: " + message.getClass().getSimpleName());
    }

    /**
     * Disconnects this client from the remote server and unexports the local RMI object.
     */
    @Override
    public void disconnect(){
        if (!connected){
            return;
        }

        connected = false;

        try{
            if (serverStub != null && clientStub != null){
                serverStub.disconnect(clientStub);
            }
        }catch (RemoteException ignored){}

        try{
            UnicastRemoteObject.unexportObject(this, true);
        } catch (Exception ignored){}
    }

    /**
     * Returns the nickname associated with this client.
     *
     * @return the client nickname
     */
    public String getNickname(){
        return nickname;
    }
}