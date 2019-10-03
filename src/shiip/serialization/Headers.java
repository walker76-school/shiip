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
        this.headerValues = new TreeMap<>();
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
        int streamID = rAndStreamID & 0x7FFFFFFF;
        int payloadLength = buffer.remaining();

        // Retrieve the remaining data
        byte[] payload = new byte[payloadLength];
        buffer.get(payload);

        setStreamID(streamID);

        // Check for errors
        if((flags & ERROR_FLAG_A) != 0x0 || (flags & ERROR_FLAG_B) != 0x0){
            throw new BadAttributeException("Error bit is set", "flags");
        }

        if((flags & SET_BIT) == 0x0){
            throw new BadAttributeException("Bit is not set in flags", "flags");
        }

        // Retrieve isEnd from the flags
        this.isEnd = (flags & IS_END_BIT) != 0;

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
        for(Map.Entry<String, String> entry : this.headerValues.entrySet()) {
            try {
                encoder.encodeHeader(out, entry.getKey().getBytes(), entry.getValue().getBytes(), false);
            } catch (IOException e) {
                System.err.println(e.getMessage());;
            }
        }
        byte[] compressedHeaders = out.toByteArray();
        ByteBuffer buffer = ByteBuffer.allocate(Constants.HEADER_BYTES + compressedHeaders.length);
        byte type = (byte)0x1;
        byte flags = (byte)0x4;
        if(this.isEnd){
            flags = (byte) (flags | 0x1);
        }
        buffer.put(type);
        buffer.put(flags);
        int rAndStreamID = this.streamID & 0x7FFFFFFF;
        buffer.putInt(rAndStreamID);
        buffer.put(compressedHeaders);
        return buffer.array();

    }

    /**
     * Return end value
     * @return end value
     */
    public boolean isEnd(){
        return this.isEnd;
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
        return 0x1;
    }

    @Override
    public void setStreamID(int streamID) throws BadAttributeException {
        if(streamID <= 0){
            throw new BadAttributeException("StreamID cannot be 0", "streamID");
        }
        this.streamID = streamID;
    }

    /**
     * Returns string of the form
     * Headers: StreamID=<streamid> isEnd=<end> ([<name> = <value>]...[<name> = <value>])
     * For example
     *
     * Headers: StreamID=5 isEnd=false ([method=GET][color=blue])
     */
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Headers: StreamID=%d isEnd=%b (", this.streamID, this.isEnd));

        for(Map.Entry<String, String> entry : this.headerValues.entrySet()){
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
        return this.headerValues.getOrDefault(name, null);
    }

    /**
     * Get set of names in Headers
     * @return set of names
     */
    public SortedSet<String> getNames(){
        return new TreeSet<>(this.headerValues.keySet());
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

        this.headerValues.put(name, value);
    }

    private boolean isValidName(String name){

        if(name == null){
            return false;
        }

        if(name.length() < 1){
            return false;
        }

        List<Character> delims = Arrays.asList('(', ')', ',', '/', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '{', '}');
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
        result = 31 * result + (isEnd ? 1 : 0);
        result = 31 * result + headerValues.hashCode();
        return result;
    }
}
