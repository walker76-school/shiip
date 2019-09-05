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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import shiip.serialization.Deframer;
import shiip.serialization.FrameConstants;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.IllegalArgumentException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Tests Deframer
 *
 * @author Andrew Walker
 */
public class DeframerTest {

    /**
     * Tests the constructor of Deframer
     */
    @Nested
    @DisplayName("constructor")
    public class Constructor{

        /**
         * Tests that a NullPointerException is thrown when InputStream is null
         */
        @Test
        @DisplayName("Null InputStream")
        public void testConstructorNull(){
            assertThrows(NullPointerException.class, () -> new Deframer(null));
        }
    }

    /**
     * Tests the getFrame of Deframer
     */
    @Nested
    @DisplayName("getFrame")
    public class GetFrame{

        /**
         * Validates that a message without payload is valid
         */
        @Test
        @DisplayName("No payload")
        public void testGetFrameNoPayload(){
            byte[] message = new byte[]{0, 0, 0, 5, 5, 5, 5, 5, 5};
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertDoesNotThrow(() -> {
                byte[] res = deframer.getFrame();
                assertArrayEquals(new byte[] {5, 5, 5, 5, 5, 5}, res);
            });
        }

        /**
         * Tests a valid paylaod with length of 1
         */
        @Test
        @DisplayName("Valid payload")
        public void testGetFrameValidPayload() {
            byte[] message = new byte[]{0, 0, 1, 5, 5, 5, 5, 5, 5, 2};
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertDoesNotThrow(() -> {
                byte[] res = deframer.getFrame();
                assertArrayEquals(new byte[] {5, 5, 5, 5, 5, 5, 2}, res);
            });
        }

        /**
         * Tests a valid payload with the maximum payload
         */
        @Test
        @DisplayName("Maximum length payload")
        public void testGetFrameMaximumPayload() {
            byte[] message = new byte[FrameConstants.HEADER_BYTES
                                        + FrameConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES
                                        + FrameConstants.LENGTH_BYTES];
            message[1] = 0x40;
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertDoesNotThrow(() -> {
                byte[] solution = new byte[FrameConstants.HEADER_BYTES
                                            + FrameConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES];

                assertArrayEquals(solution, deframer.getFrame());
            });
        }

        /**
         * Tests a payload with malformed length
         */
        @ParameterizedTest(name = "length = {0}")
        @ValueSource(ints = {0, 1, 2})
        @DisplayName("Malformed length")
        public void testPutFrameNullPayload(int arrLength){
            byte[] message = new byte[arrLength];
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertThrows(EOFException.class, deframer::getFrame);
        }

        /**
         * Tests an oversized payload
         */
        @Test
        @DisplayName("Oversized payload")
        public void testPutFrameOversizedPayload(){
            byte[] message = new byte[FrameConstants.HEADER_BYTES
                                        + FrameConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES
                                        + FrameConstants.LENGTH_BYTES + 1];
            message[1] = 0x40;
            message[2] = 0x01;
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertThrows(IllegalArgumentException.class, deframer::getFrame);
        }

        /**
         * Test that IOException is thrown if extra data in message
         */
        @Test
        @DisplayName("Extra data payload")
        public void testPutFrameExtraDataPayload(){
            byte[] message = new byte[FrameConstants.HEADER_BYTES
                                        + FrameConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES
                                        + FrameConstants.LENGTH_BYTES + 1];
            message[1] = 0x40;
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertThrows(IOException.class, deframer::getFrame);
        }

        /**
         * Tests EOFException is thrown if missing headers
         */
        @ParameterizedTest(name = "header = {0}")
        @ValueSource(ints = {0, 1, 2, 3, 4, 5})
        @DisplayName("Missing headers")
        public void testPutFrameMissingHeaders(int headerLength){
            byte[] message = new byte[FrameConstants.LENGTH_BYTES + headerLength];
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertThrows(EOFException.class, deframer::getFrame);
        }

        /**
         * Tests EOFException is thrown if underfull message
         */
        @Test
        public void testMissmatchedLength(){
            byte[] message = new byte[]{0,0,1,5,5,5,5,5,5};
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertThrows(EOFException.class, deframer::getFrame);
        }
    }

}
