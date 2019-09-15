package shiip.serialization;

import java.util.Arrays;
import java.util.Objects;

/**
 * Data message
 * @author Andrew Walker
 */
public class Data extends Message {

    private boolean isEnd;
    private byte[] data;

    /**
     * Creates Data message from given values
     * @param streamID stream ID
     * @param isEnd true if last data message
     * @param data bytes of application data
     * @throws BadAttributeException if attribute invalid (set protocol spec)
     */
    public Data(int streamID, boolean isEnd, byte[] data) throws BadAttributeException {
        this.code = (byte)0x0;
        setStreamID(streamID);
        this.isEnd = isEnd;
        setData(data);
    }

    /**
     * Return Data's data
     * @return data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Return end value
     * @return end value
     */
    public boolean isEnd() {
        return isEnd;
    }

    /**
     * Sets the stream ID in the frame. Stream ID validation depends on specific
     * message type
     * @param streamID new stream id value
     * @throws BadAttributeException if input stream id is invalid
     */
    @Override
    public final void setStreamID(int streamID) throws BadAttributeException {
        if(streamID == 0){
            throw new BadAttributeException("streamID cannot be 0", "streamID");
        }
        this.streamID = streamID;
    }
    /**
     * Set data
     * @param data data to set
     * @throws BadAttributeException if invalid
     */
    public final void setData(byte[] data) throws BadAttributeException {
        if(data == null){
            throw new BadAttributeException("Data cannot be null", "data");
        }
        this.data = data;
    }

    /**
     * Set end value
     * @param end end value
     */
    public void	setEnd(boolean end) {
        this.isEnd = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data1 = (Data) o;
        return streamID == data1.streamID &&
                isEnd == data1.isEnd &&
                Arrays.equals(data, data1.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(streamID, isEnd);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    /**
     * Returns string of the form
     * Data: StreamID=<streamid> isEnd=<end> data=<length>
     *
     * For example
     * Data: StreamID=5 isEnd=true data=5
     */
    @Override
    public String toString() {
        return String.format("Data: StreamID=%d isEnd=%b data=%d", streamID, isEnd, data.length);
    }
}
