package shiip.serialization;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class NIODeframer {

    // Maximum number of octets of the payload of the frame
    private static final int INITAL_SIZE = 16387;

    // Maximum number of octets of the payload of the frame
    private static final int MAXIMUM_PAYLOAD_LENGTH_BYTES = 16384;

    private ByteBuffer buffer;

    public NIODeframer(){
        buffer = ByteBuffer.allocate(INITAL_SIZE);
    }

    /**
     * Get the next frame (if available)
     * @param msgBytes next bytes of frame
     * @return next frame NOT including the length (but DOES include the header)
     * @throws NullPointerException if buffer is null
     * @throws IllegalArgumentException if bad input value (e.g., bad length)
     */
    public byte[] getFrame(byte[] msgBytes) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(buffer, "Byte buffer cannot be null");
        buffer.put(msgBytes, buffer.array().length, msgBytes.length);
        if(buffer.array().length > 3){
            byte[] bufferBytes = buffer.array();

            byte[] lengthArray = Arrays.copyOfRange(bufferBytes, 0, 3);
            int length = decodeInteger(lengthArray);

            if(length > Constants.MAXIMUM_PAYLOAD_LENGTH_BYTES){
                throw new IllegalArgumentException("Message too long");
            }

            // Read the rest of the message (header and payload)
            int totalLength = Constants.HEADER_BYTES + length;

            if(bufferBytes.length - 3 >= totalLength){
                buffer = ByteBuffer.allocate(INITAL_SIZE).put(Arrays.copyOfRange(bufferBytes, totalLength + 3, bufferBytes.length));
                return Arrays.copyOfRange(bufferBytes, 3, totalLength + 3);
            }
        }

        return null;
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
