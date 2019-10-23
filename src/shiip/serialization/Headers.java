/************************************************
 *
 * Author: Andrew Walker
 * Assignment: Prog2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Headers message
 * The decode() and encode() methods need a Decoder/Encoder, respectively.
 *
 * @version 1.1
 */
public final class Headers extends Message {

    private static final Charset CHARENC = StandardCharsets.US_ASCII;
    private static final byte ERROR_FLAG_A = 0x8;
    private static final byte ERROR_FLAG_B = 0x20;
    private static final byte SET_BIT = 0x4;
    private static final byte FLAGS = 0x4;
    private static final byte IS_END_BIT = 0x1;

    private boolean isEnd;
    private Map<String, String> headerValues;

    /**
     * Creates Headers message from given values
     * @param streamID stream ID
     * @param isEnd true if last header
     * @throws BadAttributeException if attribute invalid (see protocol spec)
     */
    public Headers(int streamID, boolean isEnd) throws BadAttributeException {
        setStreamID(streamID);
        this.isEnd = isEnd;
        headerValues = new TreeMap<>();
    }

    /**
     * Creates Headers message from a byte array
     * @param msgBytes the encoded Headers
     * @param decoder the deocder for the Headers headers
     * @throws BadAttributeException  if attribute invalid (see protocol spec)
     */
    protected Headers(byte[] msgBytes, Decoder decoder) throws BadAttributeException {
        Objects.requireNonNull(decoder, "Decoder cannot be null");

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

        // Check for errors
        if((flags & ERROR_FLAG_A) != 0x0 || (flags & ERROR_FLAG_B) != 0x0){
            throw new BadAttributeException("Error bit is set", "flags");
        }

        if((flags & SET_BIT) == 0x0){
            throw new BadAttributeException("Bit is not set in flags", "flags");
        }

        // Retrieve isEnd from the flags
        isEnd = (flags & IS_END_BIT) != 0;

        Map<String, String> headerValues = new TreeMap<>();
        ByteArrayInputStream in = new ByteArrayInputStream(payload);
        try {
            decoder.decode(in, (name, value, sensitive) -> {
                headerValues.put(b2s(name), b2s(value));
            });
        } catch (IOException e) {
            throw new BadAttributeException("Invalid headers", "headers", e);
        }
        decoder.endHeaderBlock();

        this.headerValues = new TreeMap<>();
        for(Map.Entry<String, String> entry : headerValues.entrySet()){
            addValue(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public byte[] encode(Encoder encoder) {
        Objects.requireNonNull(encoder);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(Map.Entry<String, String> entry : headerValues.entrySet()) {
            try {
                encoder.encodeHeader(out, entry.getKey().getBytes(),
                                            entry.getValue().getBytes(),
                                        false);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        byte[] compressedHeaders = out.toByteArray();

        ByteBuffer buffer = ByteBuffer.allocate(Constants.HEADER_BYTES + compressedHeaders.length);
        buffer.put(Constants.HEADERS_TYPE);
        buffer.put(isEnd ? (FLAGS | IS_END_BIT) : FLAGS);
        buffer.putInt(streamID & Constants.STREAM_ID_MASK);
        buffer.put(compressedHeaders);
        return buffer.array();

    }

    /**
     * Return end value
     * @return end value
     */
    public boolean isEnd(){
        return isEnd;
    }

    /**
     * Set end value
     * @param end end value
     */
    public void setEnd(boolean end){
        this.isEnd = end;
    }

    @Override
    public byte getCode() {
        return Constants.HEADERS_TYPE;
    }

    @Override
    public void setStreamID(int streamID) throws BadAttributeException {
        if(streamID <= 0){
            throw new BadAttributeException("StreamID must be a positive integer", "streamID");
        }
        this.streamID = streamID;
    }

    /**
     * Returns string of the form
     * Headers: StreamID=streamid isEnd=end ([name=value]...[name=value])
     * For example
     *
     * Headers: StreamID=5 isEnd=false ([method=GET][color=blue])
     */
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Headers: StreamID=%d isEnd=%b (",
                                                    streamID, isEnd));

        for(Map.Entry<String, String> entry : headerValues.entrySet()){
            builder.append(String.format("[%s=%s]", entry.getKey(), entry.getValue()));
        }
        builder.append(")");
        return builder.toString();
    }

    /**
     * Gets the header value for the given name
     * @param name name
     * @return the header value for the name
     */
    public String getValue(String name){
        return headerValues.getOrDefault(name, null);
    }

    /**
     * Get set of names in Headers
     * @return set of names
     */
    public SortedSet<String> getNames(){
        return new TreeSet<>(headerValues.keySet());
    }

    /**
     * Add name/value pair to header. If the name is already contained in the
     * header, the corresponding value is replaced by the new value.
     * @param name name to add
     * @param value value to add/replace
     * @throws BadAttributeException if invalid name or value
     */
    public void addValue(String name, String value) throws BadAttributeException {

        if(!isValidName(name)){
            throw new BadAttributeException("Invalid header name", "name");
        }

        if(!isValidValue(value)){
            throw new BadAttributeException("Invalid header value", "value");
        }

        headerValues.put(name, value);
    }

    /**
     * Tests if a string is a valid HTTP name
     * @param name the name to test
     * @return if a string is a valid HTTP name
     */
    private boolean isValidName(String name){

        if(name == null){
            return false;
        }

        if(name.length() < 1){
            return false;
        }

        List<Character> delims = Arrays.asList('(', ')', ',', '/', ';', '<',
                                    '=', '>', '?', '@', '[', '\\', ']', '{', '}');
        for(char c : name.toCharArray()){
            if(delims.contains(c)){
                return false;
            }

            if (!(c >= 0x20 && c <= 0x7E) ){
                return false;
            }
        }

        return true;
    }

    /**
     * Tests if a string is a valid HTTP value
     * @param value the value to test
     * @return if a string is a valid HTTP value
     */
    private boolean isValidValue(String value){

        if(value == null){
            return false;
        }

        if(value.length() < 1){
            return false;
        }

        for(char c : value.toCharArray()){
            if (c != 0x9 && !(c >= 0x20 && c <= 0x7E) ){
                return false;
            }
        }

        return true;
    }

    /**
     * Turns a byte array to a string
     * @param b the byte array to convert
     * @return a string from a byte array
     */
    private static String b2s(byte[] b) {
        return new String(b, CHARENC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Headers headers = (Headers) o;

        if (isEnd != headers.isEnd) return false;
        return headerValues.equals(headers.headerValues);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = Constants.HASH_PRIME * result + (isEnd ? 1 : 0);
        result = Constants.HASH_PRIME * result + headerValues.hashCode();
        return result;
    }
}
