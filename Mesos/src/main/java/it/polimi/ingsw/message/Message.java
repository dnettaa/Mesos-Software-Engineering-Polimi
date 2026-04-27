package it.polimi.ingsw.message;

import java.io.Serializable;

/**
 * Temporary mock of the base Message class to allow the client to compile.
 * This will be overwritten when the network team pushes the real implementation.
 */
public abstract class Message implements Serializable {

    // As requested by the UML
    protected static final long serialVersionUID = 1L;

}