/************************************************
 * Author: Andrew Walker
 * Assignment: Prog1
 * Class: CSI 4321
 ************************************************/

package shiip.serialization;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;

import java.nio.ByteBuffer;

/**
 * Window_Update frame
 * @author Andrew Walker
 */
public final class Window_Update extends Message {

    // Required length of payload in bytes
    private static final int PAYLOAD_LENGTH = 4;

    // Flags for Window_Update frames
    private static final byte FLAGS = 0x0;

    // Minimum streamID
    private static final int MIN_STREAMID = 0;

    // Amount to increment window
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

    /**
     * Creates Window_Update message from byte array
     * @param buffer encoded Window_Update
     * @throws BadAttributeException if attribute invalid (set protocol spec)
     */
    protected Window_Update(ByteBuffer buffer) throws BadAttributeException {
        setup(buffer, null);
    }

    @Override
    protected void handlePayload(byte[] payload, Decoder decoder) throws BadAttributeException {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        int payloadLength = buffer.remaining();

        // Check for valid length frame
        if(payloadLength != PAYLOAD_LENGTH){
            throw new BadAttributeException("Payload should be length 4", "payload");
        }

        // Get the payload and extract increment
        int rAndIncrement = buffer.getInt();
        int increment = rAndIncrement & Constants.STREAM_ID_MASK;
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
        if(streamID < MIN_STREAMID){
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

    @Override
    public byte getCode() {
        return Constants.WINDOW_UPDATE_TYPE;
    }

    @Override
    protected byte getEncodedFlags() {
        return FLAGS;
    }

    @Override
    protected byte[] getEncodedData(Encoder encoder) {
        int encodedIncrement = this.increment & Constants.STREAM_ID_MASK;
        return new byte[] {
                (byte) (encodedIncrement >>> 24),
                (byte) (encodedIncrement >>> 16),
                (byte) (encodedIncrement >>> 8),
                (byte) encodedIncrement};
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
        result =  Constants.HASH_PRIME * result + increment;
        return result;
    }

    /**
     * Returns string of the form
     * Window_Update: StreamID=streamid increment=inc
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
