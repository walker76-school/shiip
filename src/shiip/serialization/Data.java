/************************************************
 * Author: Andrew Walker
 * Assignment: Prog1
 * Class: CSI 4321
 ************************************************/

package shiip.serialization;

import com.twitter.hpack.Encoder;

import java.nio.ByteBuffer;
import java.util.Arrays;

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
    public Data(int streamID, boolean isEnd, byte[] data)
                                                throws BadAttributeException {
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
        if(streamID <= 0){
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

    public static Message decode(ByteBuffer buffer) throws BadAttributeException {

        byte flags = buffer.get();
        int rAndStreamID = buffer.getInt();
        int streamID = rAndStreamID & 0x7FFFFFFF;
        int payloadLength = buffer.remaining();
        // Retrieve the remaining data
        byte[] payload = new byte[payloadLength];
        buffer.get(payload);

        // Check for errors
        if((flags & (byte)0x8) == 8){
            throw new BadAttributeException("Error bit is set", "flags");
        }

        // Retrieve isEnd from the flags
        boolean isEnd = (flags & (byte)0x1) == 1;

        return new Data(streamID, isEnd, payload);
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
        ByteBuffer buffer = ByteBuffer.allocate(Constants.HEADER_BYTES
                                                    + this.data.length);
        byte type = (byte)0x0;
        byte flags = (byte)0x0;
        if(this.isEnd){
            flags = (byte) (flags | 0x1);
        }
        buffer.put(type);
        buffer.put(flags);
        int rAndStreamID = this.streamID & 0x7FFFFFFF;
        buffer.putInt(rAndStreamID);
        buffer.put(this.data);
        return buffer.array();
    }

    @Override
    public byte getCode() {
        return 0x0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Data data1 = (Data) o;

        if (isEnd != data1.isEnd) return false;
        return Arrays.equals(data, data1.data);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isEnd ? 1 : 0);
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
        return String.format("Data: StreamID=%d isEnd=%b data=%d",
                                streamID, isEnd, data.length);
    }
}
