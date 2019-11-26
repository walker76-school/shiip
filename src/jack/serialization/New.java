/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.serialization;

import static jack.serialization.Constants.*;

/**
 * New message
 *
 * @version 1.0
 */
public class New extends HostPortMessage {

    // Constant for hashing
    private static final int HASH_CONSTANT = 14;

    /**
     * Creates a New message from given values
     * @param host host ID
     * @param port port
     * @throws IllegalArgumentException if any validation problem with host and/or port, including null, etc.
     */
    public New(String host, int port) throws IllegalArgumentException {
        super(host, port);
    }

    /**
     * Creates a New message from a given byte array
     * @param payload payload
     * @throws IllegalArgumentException if any validation problem with host and/or port, including null, etc.
     */
    protected New(String payload) throws IllegalArgumentException {
        super(payload);
    }

    @Override
    public String getOperation() {
        return NEW_OP;
    }

    @Override
    protected String getName() {
        return "NEW";
    }

    @Override
    public int hashCode() {
        return HASH_CONSTANT * super.hashCode();
    }
}
