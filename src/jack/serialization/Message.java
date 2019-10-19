package jack.serialization;

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
        return null;
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
