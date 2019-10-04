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
public final class Data extends Message {

    private static final byte ERROR_BIT = 0x8;
    private static final byte IS_END_BIT = 0x1;
    private static final byte FLAGS = 0x0;

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
     * Creates Data message from given byte array
     * @param msgBytes encoded Data
     * @throws BadAttributeException if attribute invalid (set protocol spec)
     */
    protected Data(byte[] msgBytes) throws BadAttributeException {
        ByteBuffer buffer = ByteBuffer.wrap(msgBytes);

        // Throw away type
        buffer.get();

        byte flags = buffer.get();
        int rAndStreamID = buffer.getInt();
        int streamID = rAndStreamID & Constants.STREAM_ID_MASK;
        setStreamID(streamID);

        // Retrieve the remaining data
        int payloadLength = buffer.remaining();
        byte[] payload = new byte[payloadLength];
        buffer.get(payload);
        setData(payload);

        // Check for errors
        if((flags & ERROR_BIT) != 0){
            throw new BadAttributeException("Error bit is set", "flags");
        }

        // Retrieve isEnd from the flags
        this.isEnd = (flags & IS_END_BIT) != 0;
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
            throw new BadAttributeException("streamID must be a positive integer", "streamID");
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

        if(data.length > Constants.MAX_DATA_SIZE){
            throw new BadAttributeException("Data is too large", "data");
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

        buffer.put(Constants.DATA_TYPE);
        buffer.put(isEnd ? (FLAGS | IS_END_BIT) : FLAGS);
        buffer.putInt(streamID & Constants.STREAM_ID_MASK);
        buffer.put(data);
        return buffer.array();
    }

    @Override
    public byte getCode() {
        return Constants.DATA_TYPE;
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
        result = Constants.HASH_PRIME * result + (isEnd ? 1 : 0);
        result =  Constants.HASH_PRIME * result + Arrays.hashCode(data);
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
