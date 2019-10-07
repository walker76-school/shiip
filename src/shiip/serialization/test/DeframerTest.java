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
import shiip.serialization.Constants;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
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
            byte[] message = new byte[Constants.HEADER_BYTES
                                    + Constants.MAXIMUM_PAYLOAD_LENGTH_BYTES
                                    + Constants.LENGTH_BYTES];
            message[1] = 0x40;
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);
            byte[] solution = new byte[Constants.HEADER_BYTES
                    + Constants.MAXIMUM_PAYLOAD_LENGTH_BYTES];

            assertDoesNotThrow(() -> {
                assertArrayEquals(solution, deframer.getFrame());
            });
        }

        /**
         * Tests a payload with malformed length
         * @param arrLength the length of the array
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
            byte[] message = new byte[Constants.HEADER_BYTES
                                    + Constants.MAXIMUM_PAYLOAD_LENGTH_BYTES
                                    + Constants.LENGTH_BYTES + 1];
            message[1] = 0x40;
            message[2] = 0x01;
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertThrows(IllegalArgumentException.class, deframer::getFrame);
        }

        /**
         * Tests EOFException is thrown if missing headers
         * @param headerLength the length of the header
         */
        @ParameterizedTest(name = "header = {0}")
        @ValueSource(ints = {0, 1, 2, 3, 4, 5})
        @DisplayName("Missing headers")
        public void testPutFrameMissingHeaders(int headerLength){
            byte[] message = new byte[Constants.LENGTH_BYTES + headerLength];
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertThrows(EOFException.class, deframer::getFrame);
        }

        /**
         * Tests EOFException is thrown if underfull message
         */
        @Test
        @DisplayName("Mismatched length")
        public void testMismatchedLength(){
            byte[] message = new byte[]{0,0,1,5,5,5,5,5,5};
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            Deframer deframer = new Deframer(in);

            assertThrows(EOFException.class, deframer::getFrame);
        }

        /**
         * Tests IOException is thrown if broken InputStream
         */
        @Test
        @DisplayName("Broken InputStream")
        public void testBrokenInputStream(){
            InputStream in = new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException();
                }
            };

            Deframer deframer = new Deframer(in);

            assertThrows(IOException.class, deframer::getFrame);
        }
    }

}
