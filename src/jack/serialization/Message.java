package jack.serialization;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
        if(msgBytes == null){
            throw new IllegalArgumentException("Message cannot be null");
        }

        ByteBuffer buffer = ByteBuffer.wrap(msgBytes);
        char op = buffer.getChar();
        switch(op) {
            case 'Q': return new Query(msgBytes);
            case 'R': return new Response(msgBytes);
            case 'N': return new New(msgBytes);
            case 'A': return new ACK(msgBytes);
            case 'E': return new Error(msgBytes);
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
