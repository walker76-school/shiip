/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.serialization;

import jack.utils.Utils;

import static jack.serialization.Constants.*;

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
        this.searchString = Utils.validateQuery(searchString);
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
        return String.format("%s %s", getOperation(), getSearchString()).getBytes(ENC);
    }

    @Override
    public String getOperation() {
        return QUERY_OP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Query query = (Query) o;

        return searchString.equals(query.searchString);
    }

    @Override
    public int hashCode() {
        return searchString.hashCode();
    }
}
