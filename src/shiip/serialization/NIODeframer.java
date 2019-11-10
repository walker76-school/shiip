package shiip.serialization;

import java.util.Objects;

public class NIODeframer {

    /**
     * Get the next frame (if available)
     * @param buffer next bytes of frame
     * @return next frame NOT including the length (but DOES include the header)
     * @throws NullPointerException if buffer is null
     * @throws IllegalArgumentException if bad input value (e.g., bad length)
     */
    public byte[] getFrame(byte[] buffer) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(buffer, "Byte buffer cannot be null");
    }

}
