package shiip.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import shiip.serialization.Constants;
import shiip.serialization.Deframer;
import shiip.serialization.Framer;
import shiip.serialization.NIODeframer;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NIODeframerTest {

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
            NIODeframer deframer = new NIODeframer();

            assertDoesNotThrow(() -> {
                byte[] res = deframer.getFrame(message);
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
            NIODeframer deframer = new NIODeframer();

            assertDoesNotThrow(() -> {
                byte[] res = deframer.getFrame(message);
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
            NIODeframer deframer = new NIODeframer();
            byte[] solution = new byte[Constants.HEADER_BYTES
                    + Constants.MAXIMUM_PAYLOAD_LENGTH_BYTES];

            assertDoesNotThrow(() -> {
                assertArrayEquals(solution, deframer.getFrame(message));
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
            NIODeframer deframer = new NIODeframer();

            assertDoesNotThrow( () -> {
                deframer.getFrame(message);
            });
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
            NIODeframer deframer = new NIODeframer();

            assertThrows(IllegalArgumentException.class, () -> {
                deframer.getFrame(message);
            });
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
            NIODeframer deframer = new NIODeframer();

            assertDoesNotThrow(() -> {
                deframer.getFrame(message);
            });
        }

        /**
         * Tests EOFException is thrown if underfull message
         */
        @Test
        @DisplayName("Mismatched length")
        public void testMismatchedLength(){
            byte[] message = new byte[]{0,0,1,5,5,5,5,5,5,1,1};
            NIODeframer deframer = new NIODeframer();

            assertDoesNotThrow(() -> {
                deframer.getFrame(message);
            });
        }
    }
}
