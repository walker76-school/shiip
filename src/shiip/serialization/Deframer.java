/************************************************
 *
 * Author: Andrew Walker
 * Assignment: Prog0
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Deserialize frames from given input stream
 *
 * @author Andrew Walker
 */
public class Deframer {

    // InputStream to read the frame in from
    private InputStream in;

    /**
     * Construct framer with given input stream
     *
     * @param in byte source
     *
     * @throws NullPointerException if in is null
     */
    public Deframer(InputStream in){
        this.in = Objects.requireNonNull(in, "InputStream may not be null");
    }

    /**
     * Get the next frame
     *
     * @return next frame NOT including the length (but DOES include the header)
     * @throws EOFException if premature EOF
     * @throws IOException if I/O error occurs
     * @throws IllegalArgumentException if bad value in input stream
     *                                      (e.g., bad length)
     */
    public byte[] getFrame() throws IOException {

        // Read the length in from the InputStream
        byte[] lengthBuffer = new byte[Constants.LENGTH_BYTES];
        int bytesRead = in.readNBytes(lengthBuffer, 0, Constants.LENGTH_BYTES);

        if(bytesRead != Constants.LENGTH_BYTES){
            throw new EOFException("EOF reached before payload length read - " + bytesRead);
        }

        // Convert bytes into length
        int length = decodeInteger(lengthBuffer);

        // Check for valid length
        if(length > Constants.MAXIMUM_PAYLOAD_LENGTH_BYTES){
            throw new IllegalArgumentException("Message too long");
        }

        // Read the rest of the message (header and payload)
        int totalLength = Constants.HEADER_BYTES + length;
        byte[] messageBuffer = new byte[totalLength];
        bytesRead = in.readNBytes(messageBuffer, 0, totalLength);

        // Verify that the whole message was read (length was valid)
        if(bytesRead != totalLength){

            // Underfull payload
            throw new EOFException("EOF reached before payload read");
        }

        return messageBuffer;
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
