/************************************************
 * Author: Andrew Walker
 * Assignment: Prog1
 * Class: CSI 4321
 ************************************************/

package shiip.serialization;

import com.twitter.hpack.Encoder;

import java.nio.ByteBuffer;

/**
 * Settings message
 * @author Andrew Walker
 */
public final class Settings extends Message {

    private static final byte FLAGS = 0x1;

    /**
     * Creates Settings message
     * @throws BadAttributeException if attribute invalid (not thrown in this case)
     */
    public Settings() throws BadAttributeException {
        setStreamID(0);
    }

    /**
     * Creates Settings message from byte array
     * @param msgBytes encoded Settings
     * @throws BadAttributeException if attribute invalid (not thrown in this case)
     */
    protected Settings(byte[] msgBytes) throws BadAttributeException {
        ByteBuffer buffer = ByteBuffer.wrap(msgBytes);
        // Throw away type and flags
        buffer.getShort();
        int rAndStreamID = buffer.getInt();
        int streamID = rAndStreamID & Constants.STREAM_ID_MASK;
        int payloadLength = buffer.remaining();
        // Retrieve the remaining data
        byte[] payload = new byte[payloadLength];
        buffer.get(payload);

        setStreamID(streamID);
    }

    /**
     * Returns the stream ID
     * @return stream ID
     */
    @Override
    public int getStreamID() {
        return 0;
    }

    /**
     * Sets the stream ID in the frame. Stream ID validation depends on specific
     * message type
     * @param streamID new stream id value
     * @throws BadAttributeException if input stream id is invalid
     */
    @Override
    public void setStreamID(int streamID) throws BadAttributeException {
        if(streamID != 0){
            throw new BadAttributeException("StreamID for Settings must be 0",
                                            "streamID");
        }
        this.streamID = streamID;
    }

    /**
     * Serializes message
     * @param encoder encoder for serialization. Ignored (so can be null) if not
     *                needed (determined by and specified in specific
     *                message type)
     * @throws NullPointerException if encoder is null + needed
     * @return serialized message
     */
    @Override
    public byte[] encode(Encoder encoder) {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.HEADER_BYTES);
        buffer.put(Constants.SETTINGS_TYPE);
        buffer.put(FLAGS);
        buffer.putInt(this.streamID & Constants.STREAM_ID_MASK);
        return buffer.array();
    }

    @Override
    public byte getCode() {
        return Constants.SETTINGS_TYPE;
    }

    /**
     * Returns string of the form
     * Settings: StreamID=0
     *
     * For example
     * Settings: StreamID=0
     */
    @Override
    public String toString() {
        return "Settings: StreamID=0";
    }

}
