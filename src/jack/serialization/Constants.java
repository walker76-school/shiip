/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.serialization;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Constants {

    // Encoding of the bytes
    public static final Charset ENC = StandardCharsets.US_ASCII;

    // Absolute minimum length a message must be
    public static final int MIN_MSG_LEN = 2;

    // Regex to split a host:port
    public static final String SERVICE_REGEX = ":";

    // Valid host regex
    public static final String HOST_REGEX = "[a-zA-Z0-9.-]+";

    // Wildcard match on hosts
    public static final String WILDCARD = "*";

    // Index in message for the op code
    public static final int OP_NDX = 0;

    // Index in message for the space
    public static final int SP_NDX = 1;

    // Index for start of payload in message
    public static final int PAYLOAD_NDX = 2;

    // Proper length of tokens for a service after splitting
    public static final int SERVICE_TOKEN_LEN = 2;

    // Index for host after split
    public static final int HOST_NDX = 0;

    // Index for port after split
    public static final int PORT_NDX = 1;

    // Prime for hashing
    public static final int HASH_PRIME = 31;

    // Minimum allowed port
    public static final int PORT_LOW = 1;

    // Maximum allowed port
    public static final int PORT_HIGH = 65535;

    // Operation for ACK
    public static final String ACK_OP = "A";

    // Operation for Error
    public static final String ERROR_OP = "E";

    // Operation for Query
    public static final String QUERY_OP = "Q";

    // Operation for New
    public static final String NEW_OP = "N";

    // Operation for Response
    public static final String RESPONSE_OP = "R";

    // Maximum allowed payload length
    public static final int MAX_PAYLOAD_LENGTH = 65502;

    // Max length of message
    public static final int MAX_LENGTH = 65507;

}
