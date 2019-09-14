package shiip.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import shiip.serialization.BadAttributeException;
import shiip.serialization.Data;

import static org.junit.jupiter.api.Assertions.*;

public class DataTest {

    /**
     * Tests the constructor of Data
     */
    @Nested
    @DisplayName("constructor")
    public class Constructor{

        /**
         *
         */
        @Test
        @DisplayName("Invalid streamID")
        public void testConstructorInvalidStreamID() {
            BadAttributeException ex = assertThrows(BadAttributeException.class, () -> {
                new Data(0, false, new byte[]{});
            });
            assertEquals(ex.getAttribute(), "streamID");
        }

        @Test
        @DisplayName("Invalid data")
        public void testConstructorInvalidData() {
            BadAttributeException ex = assertThrows(BadAttributeException.class, () -> {
                new Data(1, false, null);
            });
            assertEquals(ex.getAttribute(), "data");
        }

        @ParameterizedTest(name = "streamID = {0}")
        @ValueSource(ints = {-1, 1, 10, 20})
        @DisplayName("Valid streamID")
        public void testConstructorStreamID(int streamID){
            assertDoesNotThrow(() -> {
                Data data = new Data(streamID, false, new byte[]{});
                assertEquals(streamID, data.getStreamID());
            });
        }

        @ParameterizedTest(name = "isEnd = {0}")
        @CsvSource({"true", "false"})
        @DisplayName("Valid isEnd")
        public void testConstructorIsEnd(boolean isEnd){
            assertDoesNotThrow(() -> {
                Data data = new Data(1, isEnd, new byte[]{});
                assertEquals(isEnd, data.isEnd());
            });
        }

        @Test
        @DisplayName("Valid data full")
        public void testConstructorValidDataEmpty() {
            byte[] byteData = new byte[]{0,1,2,3,4,5,6,7};

            assertDoesNotThrow(() -> {
                Data data = new Data(0, false, byteData);
                assertArrayEquals(byteData, data.getData());
            });
        }

        @Test
        @DisplayName("Valid data full")
        public void testConstructorValidDataFull() {
            byte[] byteData = new byte[]{};

            assertDoesNotThrow(() -> {
                Data data = new Data(0, false, byteData);
                assertArrayEquals(byteData, data.getData());
            });
        }

        /**
         * Tests code
         */
        @Test
        @DisplayName("code")
        public void testConstructorCode() {
            assertDoesNotThrow(() -> {
                Data data = new Data(1, false, new byte[]{});
                assertEquals((byte) 0x0, data.getCode());
            });
        }
    }

    @Nested
    @DisplayName("setData")
    public class SetData{

        @Test
        @DisplayName("null")
        public void testSetDataInvalidData() throws BadAttributeException{
            Data data = new Data(1, false, new byte[]{});
            BadAttributeException ex = assertThrows(BadAttributeException.class, () -> {
                data.setData(null);
            });
            assertEquals(ex.getAttribute(), "data");
        }

        @Test
        @DisplayName("Valid data")
        public void testSetDataValidData() {
            byte[] byteData = new byte[]{0,1,2,3,4,5,6,7};

            assertDoesNotThrow(() -> {
                Data data = new Data(0, false, new byte[]{});
                data.setData(byteData);
                assertArrayEquals(byteData, data.getData());
            });
        }
    }

    @Nested
    @DisplayName("setIsEnd")
    public class SetIsEnd{

        @ParameterizedTest(name = "isEnd = {0}")
        @CsvSource({"true", "false"})
        @DisplayName("Valid")
        public void testSetEndIsEnd(boolean isEnd){
            assertDoesNotThrow(() -> {
                Data data = new Data(1, false, new byte[]{});
                data.setEnd(isEnd);
                assertEquals(isEnd, data.isEnd());
            });
        }
    }

    @Nested
    @DisplayName("setStreamID")
    public class SetStreamID{

        @Test
        @DisplayName("Invalid")
        public void testConstructorInvalidData() throws BadAttributeException{
            Data data = new Data(1, false, new byte[]{});
            BadAttributeException ex = assertThrows(BadAttributeException.class, () -> {
                data.setStreamID(0);
            });
            assertEquals(ex.getAttribute(), "streamID");
        }

        @ParameterizedTest(name = "streamID = {0}")
        @ValueSource(ints = {-1, 1, 10, 20})
        @DisplayName("Valid")
        public void testConstructorStreamID(int streamID){
            assertDoesNotThrow(() -> {
                Data data = new Data(1, false, new byte[]{});
                data.setStreamID(streamID);
                assertEquals(streamID, data.getStreamID());
            });
        }
    }

    @Nested
    @DisplayName("equals")
    public class Equals{

        @ParameterizedTest(name = "streamID = {0}")
        @ValueSource(ints = {-1, 10, 20})
        @DisplayName("Mismatched streamID")
        public void testEqualsInvalidStreamID(int streamID) throws BadAttributeException {
            Data data = new Data(1, false, new byte[]{});
            assertNotEquals(new Data(streamID, false, new byte[]{}), data);
        }

        @ParameterizedTest(name = "isEnd = {0}")
        @CsvSource({"true", "false"})
        @DisplayName("Mismatched isEnd")
        public void testEqualsInvalidIsEnd(boolean isEnd) throws BadAttributeException {
            Data data = new Data(1, !isEnd, new byte[]{});
            assertNotEquals(new Data(1, isEnd, new byte[]{}), data);
        }

        @Test
        @DisplayName("Mismatched data full")
        public void testEqualsInvalidDataFull() throws BadAttributeException {
            Data data = new Data(1, false, new byte[]{1,2,3});
            assertNotEquals(new Data(1, false, new byte[]{3,2,1}), data);
        }

        @Test
        @DisplayName("Mismatched data empty")
        public void testEqualsInvalidDataEmpty() throws BadAttributeException {
            Data data = new Data(1, false, new byte[]{});
            assertNotEquals(new Data(1, false, new byte[]{1}), data);
        }

        @ParameterizedTest(name = "streamID = {0}")
        @ValueSource(ints = {-1, 1, 10, 20})
        @DisplayName("Matched streamID")
        public void testEqualsValidStreamID(int streamID) throws BadAttributeException {
            Data data = new Data(streamID, false, new byte[]{});
            assertEquals(new Data(streamID, false, new byte[]{}), data);
        }

        @ParameterizedTest(name = "isEnd = {0}")
        @CsvSource({"true", "false"})
        @DisplayName("Matched isEnd")
        public void testEqualsValidIsEnd(boolean isEnd) throws BadAttributeException {
            Data data = new Data(1, isEnd, new byte[]{});
            assertEquals(new Data(1, isEnd, new byte[]{}), data);
        }

        @Test
        @DisplayName("Matched data full")
        public void testEqualsValidDataFull() throws BadAttributeException {
            Data data = new Data(1, false, new byte[]{1,2,3});
            assertEquals(new Data(1, false, new byte[]{1,2,3}), data);
        }

        @Test
        @DisplayName("Matched data empty")
        public void testEqualsValidDataEmpty() throws BadAttributeException {
            Data data = new Data(1, false, new byte[]{});
            assertEquals(new Data(1, false, new byte[]{}), data);
        }
    }

    @Nested
    @DisplayName("hashcode")
    public class Hashcode{

        @ParameterizedTest(name = "streamID = {0}")
        @ValueSource(ints = {-1, 1, 10, 20})
        @DisplayName("Matched streamID")
        public void testHashcodeValidStreamID(int streamID) throws BadAttributeException {
            Data data = new Data(streamID, false, new byte[]{});
            assertEquals(new Data(streamID, false, new byte[]{}).hashCode(), data.hashCode());
        }

        @ParameterizedTest(name = "isEnd = {0}")
        @CsvSource({"true", "false"})
        @DisplayName("Matched isEnd")
        public void testHashcodeValidIsEnd(boolean isEnd) throws BadAttributeException {
            Data data = new Data(1, isEnd, new byte[]{});
            assertEquals(new Data(1, isEnd, new byte[]{}).hashCode(), data.hashCode());
        }

        @Test
        @DisplayName("Matched data full")
        public void testHashcodeValidDataFull() throws BadAttributeException {
            Data data = new Data(1, false, new byte[]{1,2,3});
            assertEquals(new Data(1, false, new byte[]{1,2,3}).hashCode(), data.hashCode());
        }

        @Test
        @DisplayName("Matched data empty")
        public void testHashcodeValidDataEmpty() throws BadAttributeException {
            Data data = new Data(1, false, new byte[]{});
            assertEquals(new Data(1, false, new byte[]{}).hashCode(), data.hashCode());
        }
    }
}
