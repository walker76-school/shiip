package shiip.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import shiip.serialization.BadAttributeException;
import shiip.serialization.Settings;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests the Settings object
 *
 * @author
 */
public class SettingsTest {

    /**
     * Tests the constructor of Settings
     */
    @Nested
    @DisplayName("constructor")
    public class Constructor{

        /**
         * Tests the default values of Settings
         */
        @Test
        @DisplayName("Valid")
        public void testValidConstructor() {
            assertDoesNotThrow(() -> {
                Settings settings = new Settings();
                assertAll(() -> assertEquals(0, settings.getStreamID()),
                          () -> assertEquals((byte) 0x4, settings.getCode()));

            });
        }
    }

    /**
     * Tests the streamID
     */
    @Nested
    @DisplayName("getStreamID")
    public class GetStreamID{

        /**
         * Tests streamID is 0 even after setting it
         */
        @Test
        @DisplayName("Valid")
        public void testConstructorStreamID() {
            assertDoesNotThrow(() -> {
                Settings settings = new Settings();
                assertEquals(0, settings.getStreamID());
            });
        }
    }

    /**
     * Tests the streamID
     */
    @Nested
    @DisplayName("setStreamID")
    public class SetStreamID{

        /**
         * Tests streamID is 0 even after setting it
         */
        @Test
        @DisplayName("Valid")
        public void testConstructorStreamID() {
            assertDoesNotThrow(() -> {
                Settings settings = new Settings();
                settings.setStreamID(0);
                assertEquals(0, settings.getStreamID());
            });
        }

        /**
         * Tests valid toString after setting streamID
         */
        @ParameterizedTest(name = "streamID = {0}")
        @ValueSource(ints = {-1, 1, 100, 200, 500, 1000})
        @DisplayName("Invalid")
        public void testToStringSetter(int streamID) {

            BadAttributeException ex = assertThrows(BadAttributeException.class, () -> {
                Settings settings = new Settings();
                settings.setStreamID(streamID);
            });
            assertEquals(ex.getAttribute(), "streamID");
        }
    }

    @Nested
    @DisplayName("toString")
    public class ToString {

        /**
         * Tests valid toString
         */
        @Test
        @DisplayName("No setter")
        public void testToStringValid() {
            assertDoesNotThrow(() -> {
                Settings settings = new Settings();
                assertEquals("Settings: StreamID=0", settings.toString());
            });
        }
    }

    @Nested
    @DisplayName("equals")
    public class Equals{

        @Test
        @DisplayName("Matched")
        public void testEqualsMatched() throws BadAttributeException {
            Settings settings = new Settings();
            assertEquals(new Settings(), settings);
        }
    }

    @Nested
    @DisplayName("hashcode")
    public class Hashcode{

        @Test
        @DisplayName("Matched")
        public void testEqualsMatched() throws BadAttributeException {
            Settings settings = new Settings();
            assertEquals(new Settings().hashCode(), settings.hashCode());
        }
    }

}
