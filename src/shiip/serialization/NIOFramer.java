package shiip.serialization;

import java.nio.ByteBuffer;
import java.util.Objects;

public class NIOFramer {
    // Number of cycles to encode integer
    private static final int ENCODE_CYCLES = 3;

    /**
     * Create a frame by adding the prefix length to the given message and
     * sending the entire frame (i.e., prefix length, headers, and payload)
     *
     * @param message next frame NOT including the prefix length
     *                      (but DOES include the header)
     *
     * @throws NullPointerException if message is null
     * @return the encoded Message
     */
    public byte[] putFrame(byte[] message) throws NullPointerException {
        Objects.requireNonNull(message, "Payload cannot be null");

        // Retrieve the actual length of the payload without header
        int payloadLength = message.length - Constants.HEADER_BYTES;

        // Payload cannot be less than 0 or there is an issue with header
        if(payloadLength < 0){
            throw new IllegalArgumentException("Malformed header");
        }

        // Payload cannot be more than maximum length
        if(payloadLength > Constants.MAXIMUM_PAYLOAD_LENGTH_BYTES){
            throw new IllegalArgumentException("Payload is too long");
        }

        ByteBuffer buffer = ByteBuffer.allocate(Constants.LENGTH_BYTES + message.length);

        // Write out the length prefix
        encodeInteger(payloadLength, buffer);

        // Write the message and flush
        for (byte b : message) {
            buffer.put(b);
        }

        return buffer.array();

    }

    /**
     * Encodes an integer into bytes
     * @param toEncode the integer to encode
     * @param buffer the buffer to encode to
     */
    private void encodeInteger(int toEncode, ByteBuffer buffer)  {
        for(int i = 0; i < ENCODE_CYCLES; i++){
            buffer.put((byte) (toEncode >> Constants.BYTESHIFT * (ENCODE_CYCLES - i - 1) & Constants.BYTEMASK));
        }
    }
}
