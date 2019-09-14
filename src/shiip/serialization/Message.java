package shiip.serialization;

/**
 * Represents a SHiiP message
 * @author Andrew Walker
 */
public class Message {

    protected int streamID;
    protected byte code;

    /**
     * Deserializes message from given bytes
     * @param msgBytes message bytes
     * @param decoder decoder for deserialization. This is ignored (so can be
     *                null) if not needed (in which case it can be null). Whether
     *                it is need is determined by and specified in specific
     *                message type.
     * @throws NullPointerException if msg is null or if decoder is null + needed.
     * @throws BadAttributeException if validation failure
     * @return specific Message resulting from deserialization
     */
    public static Message decode(byte[] msgBytes, com.twitter.hpack.Decoder decoder) throws BadAttributeException{
        return null;
    }

    /**
     * Serializes message
     * @param encoder encoder for serialization. Ignored (so can be null) if not
     *                needed (determined by and specified in specific
     *                message type)
     * @throws NullPointerException if encoder is null + needed
     * @return serialized message
     */
    public byte[] encode(com.twitter.hpack.Encoder encoder){
        return null;
    }

    /**
     * Returns type code for message
     * @return type code
     */
    public byte getCode(){
        return this.code;
    }

    /**
     * Returns the stream ID
     * @return stream ID
     */
    public int	getStreamID(){
        return this.streamID;
    }

    /**
     * Sets the stream ID in the frame. Stream ID validation depends on specific
     * message type
     * @param streamID new stream id value
     * @throws BadAttributeException if input stream id is invalid
     */
    public void setStreamID(int streamID) throws BadAttributeException {
        this.streamID = streamID;
    }
}
