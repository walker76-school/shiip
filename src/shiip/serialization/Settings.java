package shiip.serialization;

/**
 * Settings message
 * @author Andrew Walker
 */
public class Settings extends Message {

    /**
     * Creates Settings message
     * @throws BadAttributeException if attribute invalid (not thrown in this case)
     */
    public Settings() throws BadAttributeException {
        setStreamID(0);
        this.code = (byte) 0x4;
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
    public final void setStreamID(int streamID) throws BadAttributeException {
        if(streamID != 0){
            throw new BadAttributeException("StreamID for Settings must be 0", "streamID");
        }
        this.streamID = streamID;
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
