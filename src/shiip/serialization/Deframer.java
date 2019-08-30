package shiip.serialization;

import java.io.EOFException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

public class Deframer {

    private InputStream in;

    public Deframer(InputStream in){
        this.in = Objects.requireNonNull(in, "InputStream may not be null");
    }

    public byte[] getFrame() throws java.io.IOException {
        byte[] lengthBuffer = new byte[SerializationConstants.LENGTH_BYTES];
        int bytesRead = in.readNBytes(lengthBuffer, 0, SerializationConstants.LENGTH_BYTES);
        if(bytesRead != SerializationConstants.LENGTH_BYTES){
            throw new EOFException("EOF reached before payload length read");
        }

        ByteBuffer lengthByteBuffer = ByteBuffer.allocate(4).put(new byte[]{(byte) 0x00}).put(lengthBuffer);
        int length = lengthByteBuffer.getInt();

        int offset = 0;
        int totalBytesRead = 0;
        byte[] messageBuffer = new byte[SerializationConstants.HEADER_BYTES + length];

        return null;
    }
}
