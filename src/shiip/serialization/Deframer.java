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
     * @returns next frame NOT including the length (but DOES include the header)
     * @throws EOFException if premature EOF
     * @throws IOException if I/O error occurs
     */
    public byte[] getFrame() throws java.io.IOException {
        byte[] lengthBuffer = new byte[SerializationConstants.LENGTH_BYTES];
        int bytesRead = in.readNBytes(lengthBuffer, 0, SerializationConstants.LENGTH_BYTES);
        if(bytesRead != SerializationConstants.LENGTH_BYTES){
            throw new EOFException("EOF reached before payload length read");
        }

        int length = (lengthBuffer[0] & 0xFF) << 16 | (lengthBuffer[1] & 0xFF) << 8 | (lengthBuffer[2] & 0xFF);

        if(length > SerializationConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES){
            throw new IOException("Message too long");
        }

        int totalLength = SerializationConstants.HEADER_BYTES + length;

        byte[] messageBuffer = new byte[totalLength];
        bytesRead = in.readNBytes(messageBuffer, 0, totalLength);
        if(bytesRead != totalLength){
            throw new EOFException("EOF reached before payload read");
        }

        if(in.available() > 0){
            throw new IOException("Length incorrect");
        }

        return messageBuffer;
    }
}
