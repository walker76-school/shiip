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
import shiip.serialization.Framer;
import shiip.serialization.FrameConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Tests Framer
 *
 * @author Andrew Walker
 */
public class FramerTest {

    /**
     * Tests the constructor of Framer
     */
    @Nested
    @DisplayName("constructor")
    public class Constructor{

        /**
         * Tests that a NullPointerException is thrown when OutputStream is null
         */
        @Test
        @DisplayName("Null OutputStream")
        public void testConstructorNull(){
            assertThrows(NullPointerException.class, () -> new Framer(null));
        }
    }

    /**
     * Tests the putFrame of Framer
     */
    @Nested
    @DisplayName("putFrame")
    public class PutFrame{

        /**
         * Tests that a payload of length 0 and valid headers will result in an
         * encoded length of 0
         */
        @Test
        @DisplayName("No payload")
        public void testPutFrameNoPayload(){
            byte[] message = new byte[]{5, 5, 5, 5, 5, 5};
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Framer framer = new Framer(out);

            assertDoesNotThrow(() -> framer.putFrame(message));
            assertArrayEquals(new byte[] {0, 0, 0, 5, 5, 5, 5, 5, 5},
                                out.toByteArray());
        }

        /**
         * Tests that a valid payload and valid headers will result in a
         * correctly encoded length
         */
        @Test
        @DisplayName("Valid payload")
        public void testPutFrameValidPayload(){
            byte[] message = new byte[]{5, 5, 5, 5, 5, 5, 2};
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Framer framer = new Framer(out);

            assertDoesNotThrow(() -> framer.putFrame(message));
            assertArrayEquals(new byte[] {0, 0, 1, 5, 5, 5, 5, 5, 5, 2},
                                out.toByteArray());
        }

        /**
         * Tests the maximum possible length of payload
         */
        @Test
        @DisplayName("Maximum length payload")
        public void testPutFrameMaxPayload(){
            byte[] message = new byte[FrameConstants.HEADER_BYTES
                                    + FrameConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES];

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Framer framer = new Framer(out);

            byte[] solution = new byte[FrameConstants.LENGTH_BYTES
                                    + FrameConstants.HEADER_BYTES
                                    + FrameConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES];
            solution[1] = 0x40;

            assertDoesNotThrow(() -> framer.putFrame(message));
            assertArrayEquals(solution, out.toByteArray());
        }

        /**
         * Tests that a NullPointerException is thrown if payload is null
         */
        @Test
        @DisplayName("Null payload")
        public void testPutFrameNullPayload(){
            Framer framer = new Framer(System.out);
            assertThrows(NullPointerException.class, () -> framer.putFrame(null));
        }

        /**
         * Verify that an IOException is thrown if excess length payload
         */
        @Test
        @DisplayName("Oversized payload")
        public void testPutFrameOversizedPayload(){
            byte[] message = new byte[FrameConstants.HEADER_BYTES
                                    + FrameConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES
                                    + 1];

            Framer framer = new Framer(System.out);
            assertThrows(IOException.class, () -> framer.putFrame(message));
        }

        /**
         * Verify that an IOException is thrown if invalid headers
         */
        @ParameterizedTest(name="length = {0}")
        @ValueSource(ints = {0, 1, 2, 3, 4, 5})
        @DisplayName("Missing headers")
        public void testPutFrameMissingHeaders(int headerLength){
            byte[] message = new byte[headerLength];
            Framer framer = new Framer(System.out);
            assertThrows(IOException.class, () -> framer.putFrame(message));
        }

        /**
         * Tests IOException is thrown if broken OutputStream
         */
        @Test
        public void testBrokenOutputStream(){
            byte[] message = new byte[]{5, 5, 5, 5, 5, 5, 2};
            OutputStream out = new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    throw new IOException();
                }
            };

            Framer framer = new Framer(out);

            assertThrows(IOException.class, () -> framer.putFrame(message));
        }
    }
}
