/************************************************
 * Author: Andrew Walker
 * Assignment: Prog1
 * Class: CSI 4321
 ************************************************/

package shiip.serialization;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Represents a SHiiP message
 * @author Andrew Walker
 */
public abstract class Message {

    protected int streamID;

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
    public static Message decode(byte[] msgBytes, Decoder decoder)
                                                throws BadAttributeException {
        Objects.requireNonNull(msgBytes, "msgBytes cannot be null");

        // Check for a valid length header
        if(msgBytes.length < Constants.HEADER_BYTES){
            throw new BadAttributeException("Malformed Header", "msgBytes");
        }
        ByteBuffer buffer = ByteBuffer.wrap(msgBytes);

        // Get type and pass of decoding
        byte type = buffer.get();
        switch(type){
            case Constants.DATA_TYPE: return new Data(msgBytes);
            case Constants.HEADERS_TYPE:  return new Headers(msgBytes, decoder);
            case Constants.SETTINGS_TYPE: return new Settings(msgBytes);
            case Constants.WINDOW_UPDATE_TYPE: return new Window_Update(msgBytes);
            default: throw new BadAttributeException("Invalid type - " + type, "code");
        }
    }

    /**
     * Serializes message
     * @param encoder encoder for jack.serialization. Ignored (so can be null) if not
     *                needed (determined by and specified in specific
     *                message type)
     * @throws NullPointerException if encoder is null + needed
     * @return serialized message
     */
    public abstract byte[] encode(Encoder encoder);

    /**
     * Returns type code for message
     * @return type code
     */
    public abstract byte getCode();

    /**
     * Returns the stream ID
     * @return stream ID
     */
    public int getStreamID(){
        return this.streamID;
    }

    /**
     * Sets the stream ID in the frame. Stream ID validation depends on specific
     * message type
     * @param streamID new stream id value
     * @throws BadAttributeException if input stream id is invalid
     */
    public abstract void setStreamID(int streamID) throws BadAttributeException;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return streamID == message.streamID;
    }

    @Override
    public int hashCode() {
        return streamID;
    }
}
