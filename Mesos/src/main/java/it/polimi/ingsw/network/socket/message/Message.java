package it.polimi.ingsw.network.socket.message;

import java.io.Serializable;

/**
 * Base class for all messages exchanged between client and server.
 * <p>
 * Messages are serializable because they must be transferable through both
 * Socket object streams and RMI method calls.
 *
 * @author Diana
 */

public abstract class Message implements Serializable{
    private static final long serialVersionUID = 1L;


}
