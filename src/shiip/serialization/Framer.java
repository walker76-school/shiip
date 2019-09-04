/************************************************
 *
 * Author: Andrew Walker
 * Assignment: Prog0
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Serialize framed messages to given output stream
 *
 * @author Andrew Walker
 */
public class Framer {

    // OutputStream to print the frame to
    private OutputStream out;

    /**
     * Construct framer with given output stream
     *
     * @param out byte sink
     *
     * @throws NullPointerException if out is null
     */
    public Framer(OutputStream out){
        this.out = Objects.requireNonNull(out, "OutputStream may not be null");
    }

    /**
     * Create a frame by adding the prefix length to the given message and sending the entire frame
     * (i.e., prefix length, headers, and payload)
     *
     * @param message next frame NOT including the prefix length (but DOES include the header)
     *
     * @throws IOException if I/O problem or frame payload too long
     * @throws NullPointerException if message is null
     */
    public void	putFrame(byte[] message) throws IOException, NullPointerException {
        Objects.requireNonNull(message, "Payload cannot be null");

        // Retrieve the actual length of the payload without header
        int payloadLength = message.length - FrameConstants.HEADER_BYTES;

        // Payload cannot be less than 0 or there is an issue with header
        if(payloadLength < 0){
            throw new IOException("Malformed header");
        }

        // Payload cannot be more than maximum length
        if(payloadLength > FrameConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES){
            throw new IOException("Payload is too long");
        }

        // Write out the length prefix
        this.out.write((payloadLength >> FrameConstants.BYTESHIFT * 2) & FrameConstants.BYTEMASK);
        this.out.write((payloadLength >> FrameConstants.BYTESHIFT) & FrameConstants.BYTEMASK);
        this.out.write(payloadLength & FrameConstants.BYTEMASK);

        // Write the message and flush
        this.out.write(message);
        this.out.flush();
    }
}
