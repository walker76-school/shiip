/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.serialization;

import static jack.serialization.Constants.*;

/**
 * Represents a Jack message
 *
 * @version 1.0
 */
public abstract class Message {

    /**
     * Deserialize message from given bytes
     * @param msgBytes message bytes
     * @return specific Message resulting from deserialization
     * @throws IllegalArgumentException if validation fails, including null msgBytes
     */
    public static Message decode(byte[] msgBytes) throws IllegalArgumentException {
        if(msgBytes == null || msgBytes.length == 0){
            throw new IllegalArgumentException("Invalid msgBytes");
        }

        String fullMessage = new String(msgBytes, ENC);
        if (fullMessage.length() < MIN_MSG_LEN){
            throw new IllegalArgumentException("Invalid message");
        }
        String op = String.valueOf(fullMessage.charAt(OP_NDX));
        char sp = fullMessage.charAt(SP_NDX);
        if(sp != ' '){
            throw new IllegalArgumentException("Missing space");
        }

        String payload = fullMessage.substring(PAYLOAD_NDX);

        switch(op) {
            case QUERY_OP: return new Query(payload);
            case RESPONSE_OP: return new Response(payload);
            case NEW_OP: return new New(payload);
            case ACK_OP: return new ACK(payload);
            case ERROR_OP: return new Error(payload);
            default: throw new IllegalArgumentException("Invalid op");
        }
    }

    /**
     * Serialize the message
     * @return serialized message
     */
    public abstract byte[] encode();

    /**
     * Get the operation
     * @return operation
     */
    public abstract String getOperation();

}
