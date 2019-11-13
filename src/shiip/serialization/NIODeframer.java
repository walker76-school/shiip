package shiip.serialization;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class NIODeframer {

    // Maximum number of octets of the payload of the frame
    private static final int INITIAL_SIZE = 16387;

    // Maximum number of octets of the payload of the frame
    private static final int MAXIMUM_PAYLOAD_LENGTH_BYTES = 16384;

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
        Objects.requireNonNull(buffer, "Byte buffer cannot be null");

        ByteBuffer msgBuffer = ByteBuffer.wrap(msgBytes);
        feed(msgBuffer);

        if(buffer.position() > Constants.LENGTH_BYTES){
            byte[] bufferBytes = buffer.array();

            byte[] lengthArray = Arrays.copyOfRange(bufferBytes, 0, Constants.LENGTH_BYTES);
            int length = decodeInteger(lengthArray);

            if(length > Constants.MAXIMUM_PAYLOAD_LENGTH_BYTES){
                throw new IllegalArgumentException("Message too long");
            }

            // Read the rest of the message (header and payload)
            int messageLength = Constants.HEADER_BYTES + length;
            int totalLength = Constants.LENGTH_BYTES + messageLength;

            if(bufferBytes.length >= totalLength){
                int oldPosition = buffer.position();
                buffer.position(3);
                byte[] returnBytes = new byte[messageLength];
                for(int i = 0; i < messageLength; i++){
                    returnBytes[i] = buffer.get();
                }
                adjust(oldPosition);
                feed(msgBuffer);
                return returnBytes;
            }
        }

        return null;
    }

    public byte[] getFrame() throws NullPointerException, IllegalArgumentException {

        if(buffer.position() > Constants.LENGTH_BYTES){
            byte[] bufferBytes = buffer.array();

            byte[] lengthArray = Arrays.copyOfRange(bufferBytes, 0, Constants.LENGTH_BYTES);
            int length = decodeInteger(lengthArray);

            if(length > Constants.MAXIMUM_PAYLOAD_LENGTH_BYTES){
                throw new IllegalArgumentException("Message too long");
            }

            // Read the rest of the message (header and payload)
            int messageLength = Constants.HEADER_BYTES + length;
            int totalLength = Constants.LENGTH_BYTES + messageLength;

            if(bufferBytes.length >= totalLength){
                int oldPosition = buffer.position();
                buffer.position(3);
                byte[] returnBytes = new byte[messageLength];
                for(int i = 0; i < messageLength; i++){
                    returnBytes[i] = buffer.get();
                }
                adjust(oldPosition);
                return returnBytes;
            }
        }

        return null;
    }

    public void feed(byte[] msgBytes){
        ByteBuffer msgBuffer = ByteBuffer.wrap(msgBytes);
        feed(msgBuffer);
    }

    private void feed(ByteBuffer msgBuffer){
        while(buffer.position() != buffer.limit() && msgBuffer.position() != msgBuffer.limit()){
            buffer.put(msgBuffer.get());
        }
    }

    private void adjust(int oldPosition){
        ByteBuffer newBuffer = ByteBuffer.allocate(INITIAL_SIZE);
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
