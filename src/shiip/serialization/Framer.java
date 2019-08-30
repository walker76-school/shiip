package shiip.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class Framer {

    private OutputStream out;

    public Framer(OutputStream out){
        this.out = Objects.requireNonNull(out, "OutputStream may not be null");
    }

    public void	putFrame(byte[] message) throws IOException, NullPointerException {
        Objects.requireNonNull(message, "Payload cannot be null");

        // Retrieve the actual length of the payload without header
        int payloadLength = message.length - SerializationConstants.HEADER_BYTES;

        // Payload cannot be less than 0 or there is an issue with header
        if(payloadLength < 0){
            throw new IOException("Malformed header");
        }

        // Payload cannot be more than maximum length
        if(payloadLength > SerializationConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES){
            throw new IOException("Payload is too long");
        }

        // Encode the length of the payload in a 3-byte array
        byte[] encodedLength = ByteBuffer.allocate(4).putInt(payloadLength).array();
        encodedLength = Arrays.copyOfRange(encodedLength, 1,4);

        // Merge the length and message together
        byte[] encodedMessage = new byte[SerializationConstants.LENGTH_BYTES + message.length];
        System.arraycopy(encodedLength, 0, encodedMessage, 0, SerializationConstants.LENGTH_BYTES);
        System.arraycopy(message, 0, encodedMessage, SerializationConstants.LENGTH_BYTES, message.length);

        this.out.write(encodedMessage);
    }
}
