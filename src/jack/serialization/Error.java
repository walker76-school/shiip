package jack.serialization;

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
     * Creates a Error message from a given byte array
     * @param msgBytes byte array
     * @throws IllegalArgumentException if any validation problem with errorMessage, including null, etc.
     */
    public Error(byte[] msgBytes) throws IllegalArgumentException {
        String message = new String(msgBytes, ENC);
        String[] tokens = message.split(" ");
        if(tokens.length != 2){
            throw new IllegalArgumentException("Invalid error message");
        }

        String errorMessage = tokens[1];
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
        if (errorMessage == null){
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
        return null;
    }

    @Override
    public String getOperation() {
        return "E";
    }
}
