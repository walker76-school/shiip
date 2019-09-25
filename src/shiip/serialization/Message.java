/************************************************
 * Author: Andrew Walker
 * Assignment: Prog1
 * Class: CSI 4321
 ************************************************/

package shiip.serialization;

import com.twitter.hpack.Decoder;

import java.nio.ByteBuffer;
import java.util.Objects;

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
    public static Message decode(byte[] msgBytes, Decoder decoder)
                                                throws BadAttributeException {
        Objects.requireNonNull(msgBytes);

        // Check for a valid length header
        if(msgBytes.length < Constants.HEADER_BYTES){
            throw new BadAttributeException("Malformed Header", "msgBytes");
        }
        ByteBuffer buffer = ByteBuffer.wrap(msgBytes);

        // Get header and extract streamID
        byte type = buffer.get();
        byte flags = buffer.get();
        int rAndStreamID = buffer.getInt();
        int streamID = rAndStreamID & 0x7FFFFFFF;
        int payloadLength = msgBytes.length - Constants.HEADER_BYTES;
        // Retrieve the remaining data
        byte[] payload = new byte[payloadLength];
        buffer.get(payload);

        switch(type){
            case Constants.DATA_TYPE:
                return Data.decode(streamID, flags, payload);
            case Constants.HEADERS_TYPE:
                return Headers.decode(decoder, streamID, flags, payload);
            case Constants.SETTINGS_TYPE:
                return Settings.decode(streamID, flags, payload);
            case Constants.WINDOW_UPDATE_TYPE:
                return Window_Update.decode(streamID, flags, payload);
            default:
                throw new BadAttributeException("Invalid type", "code");
        }
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (streamID != message.streamID) return false;
        return code == message.code;
    }

    @Override
    public int hashCode() {
        int result = streamID;
        result = 31 * result + (int) code;
        return result;
    }
}
