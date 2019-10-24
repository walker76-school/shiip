package jack.serialization;

/**
 * Query message
 *
 * @version 1.0
 */
public class Query extends Message {

    private String searchString;

    /**
     * Creates a Query message from given values
     * @param searchString search string for query
     * @throws IllegalArgumentException if any validation problem with searchString, including null, etc.
     */
    public Query(String searchString) throws IllegalArgumentException {
        setSearchString(searchString);
    }

    /**
     * Creates a Query message from a given byte array
     * @param msgBytes byte array
     * @throws IllegalArgumentException if any validation problem with searchString, including null, etc.
     */
    public Query(byte[] msgBytes) throws IllegalArgumentException {
        String message = new String(msgBytes, ENC);
        String[] tokens = message.split(" ");
        if(tokens.length != 2){
            throw new IllegalArgumentException("Invalid search string");
        }

        String searchString = tokens[1];
        setSearchString(searchString);
    }

    /**
     * Get the search string
     * @return search string
     */
    public String getSearchString() {
        return searchString;
    }

    /**
     * Set the search string
     * @param searchString string to search for
     * @throws IllegalArgumentException if search string fails validation, including null
     */
    public final void setSearchString(String searchString) throws IllegalArgumentException {
        if (searchString == null){
            throw new IllegalArgumentException("Search string cannot be null");
        }

        this.searchString = searchString;
    }

    /**
     * Returns string of the form
     * QUERY query
     *
     * For example
     * QUERY win
     */
    @Override
    public String toString() {
        return String.format("QUERY %s", searchString);
    }

    @Override
    public byte[] encode() {
        return null;
    }

    @Override
    public String getOperation() {
        return "Q";
    }
}
