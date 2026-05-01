package it.polimi.ingsw.network.message;

import it.polimi.ingsw.view.View;

/**
 * Message sent by the server to notify a client about an invalid action or error.
 * @author Diana
 */

public class ErrorMessage extends ServerMessage{
    private final String errorCode;
    private final String description;

    /**
     * Creates a new error message.
     *
     * @param errorCode the machine-readable error code
     * @param description the human-readable error description
     */
    public ErrorMessage(String errorCode, String description) {
        this.errorCode = errorCode;
        this.description = description;
    }

    /**
     * Returns the error code.
     *
     * @return the machine-readable error code
     */
    public String getErrorCode(){
        return errorCode;
    }

    /**
     * Returns the error description.
     *
     * @return the human-readable error description
     */
    public String getDescription(){
        return description;
    }

    /**
     * Applies this message to the given view.
     *
     * @param view the view that must display the error
     */
    @Override
    public void apply(View view){
        view.showError(errorCode, description);
    }
}
