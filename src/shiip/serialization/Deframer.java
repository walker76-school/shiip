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
     * @throws IllegalArgumentException if bad value in input stream (e.g., bad length)
     */
    public byte[] getFrame() throws IOException, IllegalArgumentException {

        // Read the length in from the InputStream
        byte[] lengthBuffer = new byte[SerializationConstants.LENGTH_BYTES];
        int bytesRead = in.read(lengthBuffer);
        if(bytesRead != SerializationConstants.LENGTH_BYTES){
            throw new EOFException("EOF reached before payload length read");
        }

        // Convert bytes into length
        int length = (lengthBuffer[0] & SerializationConstants.BYTEMASK) << SerializationConstants.BYTESHIFT * 2
                        | (lengthBuffer[1] & SerializationConstants.BYTEMASK) << SerializationConstants.BYTESHIFT
                        | (lengthBuffer[2] & SerializationConstants.BYTEMASK);

        // Check for valid length
        if(length > SerializationConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES){
            throw new IllegalArgumentException("Message too long");
        }

        // Read the rest of the message (header and payload)
        int totalLength = SerializationConstants.HEADER_BYTES + length;
        byte[] messageBuffer = new byte[totalLength];
        bytesRead = in.read(messageBuffer);

        // Verify that the whole message was read (length was valid)
        if(bytesRead != totalLength){

            // Underfull payload
            throw new EOFException("EOF reached before payload read");
        }

        // If there is extra payload then error
        if(in.available() > 0){

            // Overfull payload
            throw new IOException("Length incorrect");
        }

        return messageBuffer;
    }
}
