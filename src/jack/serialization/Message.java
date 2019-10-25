package jack.serialization;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Represents a Jack message
 *
 * @version 1.0
 */
public abstract class Message {

    protected static final Charset ENC = StandardCharsets.US_ASCII;

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
        if (fullMessage.length() < 2){
            throw new IllegalArgumentException("Invalid message");
        }
        char op = fullMessage.charAt(0);
        char sp = fullMessage.charAt(1);
        if(sp != ' '){
            throw new IllegalArgumentException("Missing space");
        }

        String payload = fullMessage.substring(2);

        switch(op) {
            case 'Q': return new Query(payload);
            case 'R': return new Response(payload);
            case 'N': return new New(payload);
            case 'A': return new ACK(payload);
            case 'E': return new Error(payload);
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
        if(port <= 0 || port > 65535){
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
        if(host == null || host.isEmpty() || !host.matches("[a-zA-Z0-9.-]+")){
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
