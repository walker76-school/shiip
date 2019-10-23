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
     * @param buffer encoded Settings
     * @throws BadAttributeException if attribute invalid (not thrown in this case)
     */
    protected Settings(ByteBuffer buffer) throws BadAttributeException {
        setup(buffer, null);
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

    @Override
    public byte getCode() {
        return Constants.SETTINGS_TYPE;
    }

    @Override
    protected byte getEncodedFlags() {
        return FLAGS;
    }

    @Override
    protected byte[] getEncodedData(Encoder encoder) {
        return new byte[0];
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
