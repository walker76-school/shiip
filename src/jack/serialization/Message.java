package jack.serialization;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
     * Checks the validity of the port
     * @param port port
     * @throws IllegalArgumentException if invalid port
     * @return validated port
     */
    protected int validatePort(int port) throws IllegalArgumentException{
        if(port < PORT_LOW || port > PORT_HIGH){
            throw new IllegalArgumentException("Invalid port: " + port);
        }
        return port;
    }

    /**
     * Checks the validity of the port
     * @param portString string representation of port
     * @throws IllegalArgumentException if invalid port
     * @return validated port
     */
    protected int validatePort(String portString) throws IllegalArgumentException{
        try {
            int port = Integer.parseInt(portString);
            return validatePort(port);
        } catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid port: " + portString, e);
        }
    }

    /**
     * Checks the validity of the host
     * @param host host
     * @throws IllegalArgumentException if invalid host
     * @return validated host
     */
    protected String validateHost(String host) throws IllegalArgumentException{
        if(host == null || host.isEmpty() || !host.matches(HOST_REGEX)){
            throw new IllegalArgumentException("Invalid host: " + host);
        }
        return host;
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
