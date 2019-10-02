/************************************************
 * Author: Andrew Walker
 * Assignment: Prog1
 * Class: CSI 4321
 ************************************************/

package shiip.serialization;

import com.twitter.hpack.Encoder;

import java.nio.ByteBuffer;

/**
 * Window_Update frame
 * @author Andrew Walker
 */
public final class Window_Update extends Message {

    private static final int PAYLOAD_LENGTH = 4;

    private int increment;

    /**
     * Creates Window_Update message from given values
     * @param streamID stream ID
     * @param increment the increment
     * @throws BadAttributeException if attribute invalid (set protocol spec)
     */
    public Window_Update(int streamID, int increment)
                                                throws BadAttributeException {
        setStreamID(streamID);
        setIncrement(increment);
    }

    protected Window_Update(byte[] msgBytes) throws BadAttributeException {
        ByteBuffer buffer = ByteBuffer.wrap(msgBytes);

        // Throw away type and flags
        buffer.getShort();

        int rAndStreamID = buffer.getInt();
        int streamID = rAndStreamID & 0x7FFFFFFF;
        setStreamID(streamID);

        int payloadLength = buffer.remaining();

        // Check for valid length frame
        if(payloadLength < PAYLOAD_LENGTH){
            throw new BadAttributeException("Payload should be length 4",
                    "payload");
        }

        // Get the payload and extract increment
        int rAndIncrement = buffer.getInt();
        int increment = rAndIncrement & 0x7FFFFFFF;
        setIncrement(increment);
    }

    /**
     * Sets the stream ID in the frame. Stream ID validation depends on specific
     * message type
     * @param streamID new stream id value
     * @throws BadAttributeException if input stream id is invalid
     */
    @Override
    public void setStreamID(int streamID) throws BadAttributeException {
        if(streamID < 0){
            throw new BadAttributeException("streamID cannot be 0", "streamID");
        }
        this.streamID = streamID;
    }

    /**
     * Get increment value
     * @return increment value
     */
    public int getIncrement(){
        return this.increment;
    }

    /**
     * Set increment value
     * @param increment increment value
     * @throws BadAttributeException if invalid
     */
    public void setIncrement(int increment) throws BadAttributeException {
        if(increment <= 0){
            throw new BadAttributeException("Increment cannot be negative",
                                            "increment");
        }
        this.increment = increment;
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
        ByteBuffer buffer = ByteBuffer.allocate(Constants.HEADER_BYTES + 4);

        byte type = (byte)0x8;
        buffer.put(type);

        byte flags = (byte)0x0;
        buffer.put(flags);

        int rAndStreamID = this.streamID & 0x7FFFFFFF;
        buffer.putInt(rAndStreamID);

        int rAndIncrement = this.increment & 0x7FFFFFFF;
        buffer.putInt(rAndIncrement);

        return buffer.array();
    }

    @Override
    public byte getCode() {
        return 0x8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Window_Update that = (Window_Update) o;

        return increment == that.increment;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + increment;
        return result;
    }

    /**
     * Returns string of the form
     * Window_Update: StreamID=<streamid> increment=<inc>
     *
     * For example
     * Window_Update: StreamID=5 increment=1024
     */
    @Override
    public String toString() {
        return String.format("Window_Update: StreamID=%d increment=%d",
                                streamID, increment);
    }

}
