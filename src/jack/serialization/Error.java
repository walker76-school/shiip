/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.serialization;

import static jack.serialization.Constants.ENC;
import static jack.serialization.Constants.ERROR_OP;

/**
 * Error message
 *
 * @version 1.0
 */
public class Error extends Message {

    private String errorMessage;

    /**
     * Create an Error message from given values
     * @param errorMessage error message
     * @throws IllegalArgumentException if any validation problem with errorMessage, including null, etc.
     */
    public Error(String errorMessage) throws IllegalArgumentException {
        setErrorMessage(errorMessage);
    }

    /**
     * Get error message
     * @return error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set the error message
     * @param errorMessage error message
     * @throws IllegalArgumentException if validation fails, including null
     */
    public final void setErrorMessage(String errorMessage) throws IllegalArgumentException {
        if (errorMessage == null || errorMessage.isEmpty()){
            throw new IllegalArgumentException("Error message cannot be null");
        }

        this.errorMessage = errorMessage;
    }

    /**
     * Returns string of the form
     * ERROR message
     *
     * For example
     * ERROR Bad stuff
     */
    public String toString() {
        return String.format("ERROR %s", errorMessage);
    }

    @Override
    public byte[] encode() {
        return String.format("%s %s", getOperation(), getErrorMessage()).getBytes(ENC);
    }

    @Override
    public String getOperation() {
        return ERROR_OP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Error error = (Error) o;

        return errorMessage.equals(error.errorMessage);
    }

    @Override
    public int hashCode() {
        return errorMessage.hashCode();
    }
}
