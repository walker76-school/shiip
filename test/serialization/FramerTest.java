/************************************************
 *
 * Author: Andrew Walker
 * Assignment: Prog0
 * Class: CSI 4321
 *
 ************************************************/

package serialization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shiip.serialization.Framer;
import shiip.serialization.SerializationConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FramerTest {

    @Test
    @DisplayName("constructor - Null outputstream")
    public void testConstructorNull(){
        assertThrows(NullPointerException.class, () -> new Framer(null));
    }

    @Test
    @DisplayName("putFrame - No payload")
    public void testPutFrameNoPayload(){
        byte[] message = new byte[]{5, 5, 5, 5, 5, 5};
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Framer framer = new Framer(out);

        assertDoesNotThrow(() -> framer.putFrame(message));
        assertArrayEquals(new byte[] {0, 0, 0, 5, 5, 5, 5, 5, 5}, out.toByteArray());
    }

    @Test
    @DisplayName("putFrame - Valid payload")
    public void testPutFrameValidPayload(){
        byte[] message = new byte[]{5, 5, 5, 5, 5, 5, 2};
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Framer framer = new Framer(out);

        assertDoesNotThrow(() -> framer.putFrame(message));
        assertArrayEquals(new byte[] {0, 0, 1, 5, 5, 5, 5, 5, 5, 2}, out.toByteArray());
    }

    @Test
    @DisplayName("putFrame - Maximum length payload")
    public void testPutFrameMaxPayload(){
        byte[] message = new byte[SerializationConstants.HEADER_BYTES + SerializationConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Framer framer = new Framer(out);

        byte[] solution = new byte[SerializationConstants.HEADER_BYTES + SerializationConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES + 3];
        solution[1] = 0x40;

        assertDoesNotThrow(() -> framer.putFrame(message));
        assertArrayEquals(solution, out.toByteArray());
    }

    @Test
    @DisplayName("putFrame - Null payload")
    public void testPutFrameNullPayload(){
        Framer framer = new Framer(System.out);
        assertThrows(NullPointerException.class, () -> framer.putFrame(null));
    }

    @Test
    @DisplayName("putFrame - Oversized payload")
    public void testPutFrameOversizedPayload(){
        byte[] message = new byte[SerializationConstants.HEADER_BYTES + SerializationConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES + 1];
        Framer framer = new Framer(System.out);
        assertThrows(IOException.class, () -> framer.putFrame(message));
    }

    @Test
    @DisplayName("putFrame - Missing headers")
    public void testPutFrameMissingHeaders(){
        byte[] message = new byte[SerializationConstants.HEADER_BYTES - 5];
        Framer framer = new Framer(System.out);
        assertThrows(IOException.class, () -> framer.putFrame(message));
    }
}
