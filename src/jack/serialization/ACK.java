/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.serialization;

import jack.utils.Service;
import jack.utils.Utils;

import static jack.serialization.Constants.*;

/**
 * ACK message
 *
 * @version 1.0
 */
public class ACK extends HostPortMessage {

    /**
     * Create an ACK message from given values
     * @param host host ID
     * @param port port
     * @throws IllegalArgumentException if any validation problem with host and/or port, including null, etc.
     */
    public ACK(String host, int port) throws IllegalArgumentException {
        super(host, port);
    }

    /**
     * Creates a ACK message from a given byte array
     * @param payload payload
     * @throws IllegalArgumentException if any validation problem with host and/or port, including null, etc.
     */
    protected ACK(String payload) throws IllegalArgumentException {
        super(payload);
    }

    @Override
    public String getOperation() {
        return ACK_OP;
    }

    @Override
    protected String getName() {
        return "ACK";
    }
}
