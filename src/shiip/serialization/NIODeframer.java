package shiip.serialization;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class NIODeframer {

    // Maximum number of octets of the payload of the frame
    private static final int INITIAL_SIZE = 16387;

    private ByteBuffer buffer;

    public NIODeframer(){
        buffer = ByteBuffer.allocate(INITIAL_SIZE);
    }

    /**
     * Get the next frame (if available)
     * @param msgBytes next bytes of frame
     * @return next frame NOT including the length (but DOES include the header)
     * @throws NullPointerException if buffer is null
     * @throws IllegalArgumentException if bad input value (e.g., bad length)
     */
    public byte[] getFrame(byte[] msgBytes) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(msgBytes, "Bytes cannot be null");

        feed(msgBytes);

        // If we have enough for a length
        if(buffer.position() > Constants.LENGTH_BYTES){

            // Decode length
            byte[] bufferBytes = buffer.array();
            byte[] lengthArray = Arrays.copyOfRange(bufferBytes, 0, Constants.LENGTH_BYTES);
            int length = decodeInteger(lengthArray);

            // Calculate other lengths (header and payload)
            int messageLength = Constants.HEADER_BYTES + length;
            int totalLength = Constants.LENGTH_BYTES + messageLength;

            boolean badLength = length > Constants.MAXIMUM_PAYLOAD_LENGTH_BYTES;

            // If at least the full message is in the buffer
            if(bufferBytes.length >= totalLength){

                // If it was an invalid length
                if(length > Constants.MAXIMUM_PAYLOAD_LENGTH_BYTES){

                    // Store the bound of the buffer
                    int oldPosition = buffer.position();

                    // Ignore totalLength bytes
                    buffer.position(totalLength);

                    // Copy the rest of the buffer to the start
                    adjust(oldPosition);

                    // Illegal argument
                    throw new IllegalArgumentException("Message too long");
                } else {
                    // Store the bound of the buffer
                    int oldPosition = buffer.position();

                    // Ignore the length
                    buffer.position(3);

                    // Allocate for the encodedMessage
                    byte[] returnBytes = new byte[messageLength];

                    // Copy into the return buffer
                    for(int i = 0; i < messageLength; i++){
                        returnBytes[i] = buffer.get();
                    }

                    // Copy the rest of the buffer to the start
                    adjust(oldPosition);

                    // Return encoded Message
                    return returnBytes;
                }
            }

            if(badLength){
                throw new IllegalArgumentException("Message too long");
            }
        }

        return null;
    }

    public final void feed(byte[] msgBytes){
        int bufferSize = buffer.position() + msgBytes.length;
        byte[] oldArray = Arrays.copyOfRange(buffer.array(), 0, buffer.position());
        buffer = ByteBuffer.allocate(bufferSize).put(oldArray).put(msgBytes);
    }

    private void adjust(int oldPosition){
        int size = oldPosition - buffer.position();
        ByteBuffer newBuffer = ByteBuffer.allocate(size);
        while(buffer.position() != oldPosition){
            newBuffer.put(buffer.get());
        }
        buffer = newBuffer;
    }

    /**
     * Decodes a byte array into an integer
     * @param bytes the byte array to decode
     * @return the integer from the bytes
     */
    private int decodeInteger(byte[] bytes){
        int ret = 0;
        for(int i = 0; i < bytes.length; i++){
            ret = ret | (bytes[i] & Constants.BYTEMASK)
                    << (Constants.BYTESHIFT * (bytes.length - i - 1));
        }
        return ret;
    }

}
