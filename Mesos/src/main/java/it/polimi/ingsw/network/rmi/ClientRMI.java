package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.network.message.ServerMessage;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface exposed by an RMI client.
 * <p>
 * This interface defines the callback method that the server can invoke
 * to deliver messages to the client.
 * @author Diana
 */

public interface ClientRMI extends Remote{
    /**
     * Receives a message sent by the server.
     * <p>
     * The concrete client-side adapter will apply the message to the local view.
     *
     * @param message the server message to be delivered to the client
     * @throws RemoteException if a communication error occurs
     */
    void receiveMessage(ServerMessage message) throws RemoteException;
}