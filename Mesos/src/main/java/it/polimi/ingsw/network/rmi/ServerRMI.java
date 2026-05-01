package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.network.message.ClientMessage;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface exposed by the server.
 * <p>
 * This interface defines the methods that a client can invoke remotely
 * to interact with the server using RMI.
 * @author Diana
 */

public interface ServerRMI extends Remote{

    /**
     * Registers a client to the server.
     * <p>
     * The server stores the reference to the client to allow callbacks
     * (server-to-client communication).
     *
     * @param client the remote reference to the client
     * @throws RemoteException if a communication error occurs
     */
    void connect(ClientRMI client) throws RemoteException;

    /**
     * Sends a generic client message to the server.
     *
     * @param message the message representing a client action
     * @param sender to understand which client sent a message
     * @throws RemoteException if a communication error occurs
     */
    void sendMessage(ClientMessage message, ClientRMI sender) throws RemoteException;

    /**
     * Disconnects an RMI client from the server.
     *
     * @param client the remote client reference
     * @throws RemoteException if a remote communication error occurs
     */
    void disconnect(ClientRMI client) throws RemoteException;
}