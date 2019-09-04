/************************************************
 *
 * Author: Andrew Walker
 * Assignment: Prog0
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import shiip.serialization.Deframer;
import shiip.serialization.SerializationConstants;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeframerTest {

    @Nested
    @DisplayName("constructor")
    public class Constructor{

        @Test
        @DisplayName("Null InputStream")
        public void testConstructorNull(){
            assertThrows(NullPointerException.class, () -> new Deframer(null));
        }
    }

    @Nested
    @DisplayName("getFrame")
    public class GetFrame{

        @Test
        @DisplayName("No payload")
        public void testGetFrameNoPayload() throws IOException {
            byte[] message = new byte[]{0, 0, 0, 5, 5, 5, 5, 5, 5};
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            byte[] res = deframer.getFrame();
            assertArrayEquals(new byte[] {5, 5, 5, 5, 5, 5}, res);
        }

        @Test
        @DisplayName("Valid payload")
        public void testGetFrameValidPayload() throws IOException {
            byte[] message = new byte[]{0, 0, 1, 5, 5, 5, 5, 5, 5, 2};
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            byte[] res = deframer.getFrame();
            assertArrayEquals(new byte[] {5, 5, 5, 5, 5, 5, 2}, res);
        }

        @Test
        @DisplayName("Maximum length payload")
        public void testGetFrameMaximumPayload() throws IOException {
            byte[] message = new byte[SerializationConstants.HEADER_BYTES + SerializationConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES + 3];
            message[1] = 0x40;
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            byte[] solution = new byte[SerializationConstants.HEADER_BYTES + SerializationConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES];

            assertArrayEquals(solution, deframer.getFrame());
        }

        @Test
        @DisplayName("Empty payload")
        public void testPutFrameNullPayload(){
            byte[] message = new byte[]{};
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertThrows(EOFException.class, deframer::getFrame);
        }

        @Test
        @DisplayName("Oversized payload")
        public void testPutFrameOversizedPayload(){
            byte[] message = new byte[SerializationConstants.HEADER_BYTES + SerializationConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES + 4];
            message[1] = 0x40;
            message[2] = 0x01;
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertThrows(IOException.class, deframer::getFrame);
        }

        @Test
        @DisplayName("Extra data payload")
        public void testPutFrameExtraDataPayload(){
            byte[] message = new byte[SerializationConstants.HEADER_BYTES + SerializationConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES + 4];
            message[1] = 0x40;
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertThrows(IOException.class, deframer::getFrame);
        }

        @Test
        @DisplayName("Missing headers")
        public void testPutFrameMissingHeaders(){
            byte[] message = new byte[]{0, 0, 1};
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertThrows(EOFException.class, deframer::getFrame);
        }
    }

}
