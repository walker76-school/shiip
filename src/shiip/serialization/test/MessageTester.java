/*******************************************************
 * Author: Ian Laird, Andrew Walker
 * Assignment: Prog 1
 * Class: Data Comm
 *******************************************************/

package shiip.serialization.test;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import shiip.serialization.*;
import static shiip.serialization.test.TestingConstants.*;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Performs testing for the {@link shiip.serialization.Message}.
 *
 * @version 1.0
 * @author Ian Laird, Andrew Walker
 */
public class MessageTester {

    private static byte [] TEST_HEADER_1 =
            {0x0,0x0,0x0,0x0,0x0,0x1,0x0,0x0,0x0,0x0};
    private static byte [] TEST_HEADER_BAD_TYPE  =
            {(byte)0xEE,0x0,0x0,0x0,0x0,0x1};

    /*
     * an example data frame that has a six byte payload
     * the type is data (0)
     * the flags are 0
     * the stream identifier is one
     * the contents are 0,1,2,3,4,5
     */
    private static byte [] GOOD_DATA_ONE =
        {0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
    private static Data CORRECT_DATA_ONE = null;
    private static byte [] CORRECT_DATA_ONE_ENCODED = null;

    /*
     * an example data frame that has a six byte payload
     * the type is data (0)
     * the bad flag is set BAD!!
     * the stream identifier is one
     * the contents are 0,1,2,3,4,5
     */
    private static byte [] BAD_DATA_ONE =
        {0x00, 0x08, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05};

    /*
     * an example data frame that has a six byte payload
     * the type is data (0)
     * no flags are set
     * the stream identifier is zero
     * the contents are 0,1,2,3,4,5
     */
    private static byte [] BAD_DATA_TWO =
        {0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05};

    /*
     * an example settings frame that has no payload
     * the type is settings (4)
     * the flags are 1
     * the stream identifier is zero
     */
    private static byte [] GOOD_SETTINGS_ONE =
            {0x04, 0x01, 0x00, 0x00, 0x00, 0x00};
    private static Settings CORRECT_SETTINGS_ONE = null;
    private static byte [] CORRECT_SETTINGS_ENCODED = null;

    /*
     * an example settings frame that has no payload
     * the type is settings (4)
     * the flags are 1
     * the stream identifier is zero
     * there is a two byte payload
     */
    private static byte [] GOOD_SETTINGS_TWO =
            {0x04, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    /*
     * an example data frame that has no payload
     * the type is settings (4)
     * the flags are 1
     * the stream identifier is one (error)
     */
    private static byte [] BAD_SETTINGS_ONE =
            {0x04, 0x01, 0x00, 0x00, 0x00, 0x01};

    /*
     * an example data frame that has no payload
     * the type is settings (4)
     * the flags are 0 (error!)
     * the stream identifier is zero
     */
    private static byte [] BAD_SETTINGS_TWO =
            {0x04, 0x00, 0x00, 0x00, 0x00, 0x00};

    /*
     * an example window update frame
     * the type is 8
     * the flags are 0
     * the stream identifier is one
     * the payload is 4 octets and contains 1
     */
    private static byte [] GOOD_WINDOW_UPDATE_ONE =
            {0x08, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01};
    private static Window_Update CORRECT_WINDOW_UPDATE_ONE = null;
    private static byte [] CORRECT_WINDOw_UPDATE_ENCODED = null;


    /*
     * an example window update frame
     * the type is 8
     * the flags are 0
     * the stream identifier is one
     * the payload is 4 octets and the R bit is set
     */
    private static byte [] GOOD_WINDOW_UPDATE_TWO =
            {0x08, 0x00, 0x00, 0x00, 0x00, 0x01, 0x08, 0x00, 0x00, 0x01};

    /*
     * an example window update frame
     * the type is 8
     * the flags are 0
     * the stream identifier is one
     * the payload is 3 octets BAD!!! and contains 1
     */
    private static byte [] BAD_WINDOW_UPDATE_ONE =
            {0x08, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x01};

    /**
     * init the static objects
     */
    @BeforeAll
    public static void initialize(){
        assertDoesNotThrow(() -> {
            CORRECT_DATA_ONE = new Data(1,false,
                                new byte[] {0x00, 0x01, 0x02, 0x03, 0x04, 0x05});
            CORRECT_SETTINGS_ONE = new Settings();
            CORRECT_WINDOW_UPDATE_ONE = new Window_Update(1,1);

            CORRECT_DATA_ONE_ENCODED =
                    CORRECT_DATA_ONE.encode(null);
            CORRECT_SETTINGS_ENCODED =
                    CORRECT_SETTINGS_ONE.encode(null);
            CORRECT_WINDOw_UPDATE_ENCODED =
                    CORRECT_WINDOW_UPDATE_ONE.encode(null);
        });
    }

    /**
     * Performs decoding a {@link shiip.serialization.Message}.
     *
     * @version 1.0
     * @author Ian Laird, Andrew Walker
     */
    @Nested
    @DisplayName("decode")
    public class Decode {

        /**
         * null msg
         */
        @DisplayName("Null")
        @Test
        void testNullMsgBytes() {
            assertThrows(NullPointerException.class, () -> {
                Message.decode(null, null);
            });
        }

        /**
         * invalid message type
         */
        @DisplayName("Invalid Type")
        @Test
        void testInvalidType() {
            assertThrows(BadAttributeException.class,() -> {
                Message.decode(TEST_HEADER_BAD_TYPE, null);
            });
        }

        /**
         *
         */
        @Nested
        @DisplayName("Data")
        public class DataType{

            /**
             * Data Type
             */
            @DisplayName("Valid Type")
            @Test
            void testDataFrameRecognized() {
                assertDoesNotThrow(() -> {
                    Message message = Message.decode(GOOD_DATA_ONE, null);
                    assertNotNull(message);
                    assertEquals(message.getCode(), DATA_TYPE);
                });
            }

            /**
             * Valid Data frame
             */
            @DisplayName("Valid Frame")
            @Test
            void testDataFrameReadIn() {
                assertDoesNotThrow(() -> {
                    Message message = Message.decode(GOOD_DATA_ONE, null);
                    Data data = (Data) message;
                    assertEquals(data, CORRECT_DATA_ONE);
                });
            }

            /**
             * Data with bad bit set
             */
            @DisplayName("Bad Bit Set")
            @Test
            void testDataFrameBadBit() {
                assertThrows(BadAttributeException.class, () -> {
                    Message.decode(BAD_DATA_ONE, null);
                });
            }

            /**
             * Data with streamID of 0
             */
            @DisplayName("Bad streamID")
            @Test
            void testDataFrameStreamIdentifierZero() {
                assertThrows(BadAttributeException.class, () -> {
                    Message.decode(BAD_DATA_TWO, null);
                });
            }
        }

        @Nested
        @DisplayName("Settings")
        public class SettingsType {
            /**
             * Settings Type
             */
            @DisplayName("Valid Type")
            @Test
            void testSettingsFrameRecognized() {
                assertDoesNotThrow(() -> {
                    Message message = Message.decode(GOOD_SETTINGS_ONE, null);
                    assertNotNull(message);
                    assertEquals(message.getCode(), SETTINGS_TYPE);
                });
            }

            /**
             * Valid Settings with payload
             */
            @DisplayName("Valid with Payload")
            @Test
            void testSettingsFramePayload() {
                assertDoesNotThrow(() -> {
                    Message.decode(GOOD_SETTINGS_TWO, null);
                });
            }

            /**
             * Invalid streamID
             */
            @DisplayName("Bad StreamID")
            @Test
            void testSettingsFrameBadStreamIdentifier() {
                assertThrows(BadAttributeException.class, () -> {
                    Message.decode(BAD_SETTINGS_ONE, null);
                });
            }

            /**
             * Invalid Flags
             */
            @DisplayName("Bad Flags")
            @Test
            void testSettingsFrameBadFlags() {
                assertDoesNotThrow( () -> {
                    Message.decode(BAD_SETTINGS_TWO, null);
                });
            }
        }

        /**
         *
         */
        @Nested
        @DisplayName("Window_Update")
        public class WindowUpdateType {

            /**
             * Window_Update Type
             */
            @DisplayName("Valid Type")
            @Test
            void testWindowUpdateFrameRecognized() {
                assertDoesNotThrow(() -> {
                    Message message =
                            Message.decode(GOOD_WINDOW_UPDATE_ONE, null);
                    assertEquals(message.getCode(), WINDOW_UPDATE_TYPE);
                });
            }

            /**
             * R bit
             */
            @DisplayName("R Bit")
            @Test
            void testWindowsUpdateRPaylaod() {
                assertDoesNotThrow(() -> {
                    Message.decode(GOOD_WINDOW_UPDATE_TWO, null);
                });
            }

            /**
             * Payload too short
             */
            @DisplayName("Missing Payload")
            @Test
            void testWindowsUpdateFrameShort() {
                assertThrows(BadAttributeException.class, () -> {
                    Message.decode(BAD_WINDOW_UPDATE_ONE, null);
                });
            }
        }
    }

    /**
     * Performs encoding a {@link shiip.serialization.Message}.
     *
     * @version 1.0
     * @author Ian Laird, Andrew Walker
     */
    @Nested
    @DisplayName("Encoding Tests")
    public class EncodingTests{

        /**
         * Performs flags tests
         *
         * @version 1.0
         * @author Ian Laird, Andrew Walker
         */
        @Nested
        public class FlagsTests{

            /**
             * data flags unset
             */
            @DisplayName("Data Flags Unset")
            @Test
            public void testDataFlagsUnset(){
                assertEquals(NO_FLAGS,
                        CORRECT_DATA_ONE_ENCODED[FLAG_POS_IN_HEADER]);
            }

            /**
             * settings flags unset
             */
            @DisplayName("Settings Flags Unset")
            @Test
            public void testSettingsFlagsUnset(){
                assertEquals(REQUIRED_SETTINGS_FLAGS_SERIALIZATION,
                        CORRECT_SETTINGS_ENCODED[FLAG_POS_IN_HEADER]);
            }

            /**
             * window update flags unset
             */
            @DisplayName("Window_Update Flags Unset")
            @Test
            public void testWindowUpdateFlagsUnset(){
                assertEquals(NO_FLAGS,
                        CORRECT_WINDOw_UPDATE_ENCODED[FLAG_POS_IN_HEADER]);
            }

        }

        /**
         * null for the encoder
         */
        @Test
        @DisplayName("Null Encoder")
        public void testNullEncoder(){
            assertDoesNotThrow(()->CORRECT_DATA_ONE.encode(null));
        }

        /**
         * testing the r bit
         */
        @DisplayName("R Bit Unset")
        @Test
        public void testRBit(){
            assertAll(
                    () -> assertEquals(
                            (byte)(CORRECT_DATA_ONE_ENCODED
                                    [BYTE_CONTAINING_R_BIT_LOCATION]
                                    & HIGHEST_BIT_IN_BYTE),
                            (byte)0),
                    () -> assertEquals(
                            (byte)(CORRECT_SETTINGS_ENCODED
                                    [BYTE_CONTAINING_R_BIT_LOCATION]
                                    & HIGHEST_BIT_IN_BYTE),
                            (byte)0),
                    () -> assertEquals((byte)(CORRECT_WINDOw_UPDATE_ENCODED
                                    [BYTE_CONTAINING_R_BIT_LOCATION]
                                    & HIGHEST_BIT_IN_BYTE),
                            (byte)0)
            );
        }

        /**
         * second r bit in the window update
         */
        @DisplayName("R Bit Unset - Window_Update")
        @Test
        public void testSecondRBitWindowUpdate(){
            assertEquals((byte)(CORRECT_WINDOw_UPDATE_ENCODED
                            [BYTE_CONTAINING_SECOND_R_BIT_WINDOW_UPDATE]
                            & HIGHEST_BIT_IN_BYTE),
                    (byte)0);
        }

        /**
         * data encoding
         */
        @DisplayName("Valid Data Encoding")
        @Test
        public void testDataEncoding(){
            assertArrayEquals(GOOD_DATA_ONE, CORRECT_DATA_ONE_ENCODED);
        }

        /**
         * settings encoding
         */
        @DisplayName("Valid Settings Encoding")
        @Test
        public void testSettingsEncoding(){
            assertArrayEquals(GOOD_SETTINGS_ONE, CORRECT_SETTINGS_ENCODED);
        }

        /**
         * window update encoding
         */
        @DisplayName("Valid Window_Update Encoding")
        @Test
        public void testWindowUpdateEncoding(){
            assertArrayEquals(GOOD_WINDOW_UPDATE_ONE,
                    CORRECT_WINDOw_UPDATE_ENCODED);
        }
    }
}

