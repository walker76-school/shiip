/*******************************************************
 * Author: Ian Laird, Andrew walker
 * Assignment: Prog 2
 * Class: Data Comm
 *******************************************************/

package shiip.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import shiip.serialization.BadAttributeException;
import shiip.serialization.Headers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static shiip.serialization.test.TestingConstants.HEADERS_TYPE;

/**
 * Performs testing for the {@link Headers}.
 *
 * @version 1.0
 * @author Ian Laird, Andrew Walker
 */
public class HeadersTester {
    /**
     * Tests the constructor of Headers
     */
    @Nested
    @DisplayName("constructor")
    public class Constructor{

        /**
         * Tests constructor with invalid streamID
         */
        @ParameterizedTest(name = "streamID = {0}")
        @ValueSource(ints = {-10, -1, 0})
        @DisplayName("Invalid streamID")
        public void testConstructorInvalidStreamID(int streamID) {
            BadAttributeException ex = assertThrows(BadAttributeException.class, () -> {
                new Headers(streamID, false);
            });
            assertEquals(ex.getAttribute(), "streamID");
        }

        /**
         * Tests the constructor with valid streamID's
         * @param streamID valid streamID
         */
        @ParameterizedTest(name = "streamID = {0}")
        @ValueSource(ints = {1, 10, 20})
        @DisplayName("Valid streamID")
        public void testConstructorStreamID(int streamID){
            assertDoesNotThrow(() -> {
                Headers headers = new Headers(streamID, false);
                assertEquals(streamID, headers.getStreamID());
            });
        }

        /**
         * Tests the constructor with all possible isEnd values
         * @param isEnd valid isEnd
         */
        @ParameterizedTest(name = "isEnd = {0}")
        @CsvSource({"true", "false"})
        @DisplayName("Valid isEnd")
        public void testConstructorIsEnd(boolean isEnd){
            assertDoesNotThrow(() -> {
                Headers headers = new Headers(1, isEnd);
                assertEquals(isEnd, headers.isEnd());
            });
        }

        /**
         * Tests code is the correct one for Headers
         */
        @Test
        @DisplayName("code")
        public void testConstructorCode() {
            assertDoesNotThrow(() -> {
                Headers headers = new Headers(1, false);
                assertEquals(HEADERS_TYPE, headers.getCode());
            });
        }
    }

    /**
     * Test the setEnd method of Data
     */
    @Nested
    @DisplayName("setEnd")
    public class SetEnd{

        /**
         * Tests will all possible isEnd values
         * @param isEnd valid isEnd
         */
        @ParameterizedTest(name = "isEnd = {0}")
        @CsvSource({"true", "false"})
        @DisplayName("Valid")
        public void testSetEndIsEnd(boolean isEnd){
            assertDoesNotThrow(() -> {
                Headers headers = new Headers(1, !isEnd);
                headers.setEnd(isEnd);
                assertEquals(isEnd, headers.isEnd());
            });
        }
    }

    /**
     * Tests the setStreamID method of Data
     */
    @Nested
    @DisplayName("setStreamID")
    public class SetStreamID{

        /**
         * Tests that BadAttributeException is thrown on invalid streamID
         */
        @ParameterizedTest(name = "streamID = {0}")
        @ValueSource(ints = {-10, -1, 0})
        @DisplayName("Invalid")
        public void testInvalidSetStreamID(int streamID) {
            BadAttributeException ex = assertThrows(BadAttributeException.class, () -> {
                Headers headers = new Headers(1, false);
                headers.setStreamID(streamID);
            });
            assertEquals(ex.getAttribute(), "streamID");
        }

        /**
         * Tests valid streamID's
         * @param streamID valid streamID
         */
        @ParameterizedTest(name = "streamID = {0}")
        @ValueSource(ints = { 1, 10, 20})
        @DisplayName("Valid")
        public void testValidSetStreamID(int streamID){
            assertDoesNotThrow(() -> {
                Headers headers = new Headers(1, false);
                headers.setStreamID(streamID);
                assertEquals(streamID, headers.getStreamID());
            });
        }
    }

    /**
     * Tests the equals method of Data
     */
    @Nested
    @DisplayName("equals")
    public class Equals{

        /**
         * Tests that Headers with different streamID are unequal
         * @param streamID valid streamID
         * @throws BadAttributeException if invalid parameter
         */
        @ParameterizedTest(name = "streamID = {0}")
        @ValueSource(ints = { 10, 20})
        @DisplayName("Mismatched streamID")
        public void testEqualsInvalidStreamID(int streamID) throws BadAttributeException {
            Headers headers = new Headers(1, false);
            assertNotEquals(new Headers(streamID, false), headers);
        }

        /**
         * Tests that Data with different isEnd are unequal
         * @param isEnd valid isEnd
         * @throws BadAttributeException if invalid parameter
         */
        @ParameterizedTest(name = "isEnd = {0}")
        @CsvSource({"true", "false"})
        @DisplayName("Mismatched isEnd")
        public void testEqualsInvalidIsEnd(boolean isEnd) throws BadAttributeException {
            Headers headers = new Headers(1, !isEnd);
            assertNotEquals(new Headers(1, isEnd), headers);
        }

        /**
         * Tests Data with same valid streamID's are equal
         * @param streamID valid streamID
         * @throws BadAttributeException if invalid parameter
         */
        @ParameterizedTest(name = "streamID = {0}")
        @ValueSource(ints = { 1, 10, 20})
        @DisplayName("Matched streamID")
        public void testEqualsValidStreamID(int streamID) throws BadAttributeException {
            Headers headers = new Headers(streamID, false);
            assertEquals(new Headers(streamID, false), headers);
        }

        /**
         * Tests Data with same valid isEnd are equal
         * @param isEnd valid isEnd
         * @throws BadAttributeException if invalid parameter
         */
        @ParameterizedTest(name = "isEnd = {0}")
        @CsvSource({"true", "false"})
        @DisplayName("Matched isEnd")
        public void testEqualsValidIsEnd(boolean isEnd) throws BadAttributeException {
            Headers headers = new Headers(1, isEnd);
            assertEquals(new Headers(1, isEnd), headers);
        }
    }

    /**
     * Tests the hashcode method of Headers
     */
    @Nested
    @DisplayName("hashcode")
    public class Hashcode{

        /**
         * Tests Headers with same streamID have same hashcode
         * @param streamID valid streamID
         * @throws BadAttributeException if invalid parameter
         */
        @ParameterizedTest(name = "streamID = {0}")
        @ValueSource(ints = { 1, 10, 20})
        @DisplayName("Matched streamID")
        public void testHashcodeValidStreamID(int streamID) throws BadAttributeException {
            Headers headers = new Headers(streamID, false);
            assertEquals(new Headers(streamID, false).hashCode(), headers.hashCode());
        }

        /**
         * Tests Data with same isEnd have same hashcode
         * @param isEnd valid isEnd
         * @throws BadAttributeException if invalid parameter
         */
        @ParameterizedTest(name = "isEnd = {0}")
        @CsvSource({"true", "false"})
        @DisplayName("Matched isEnd")
        public void testHashcodeValidIsEnd(boolean isEnd) throws BadAttributeException {
            Headers headers = new Headers(1, isEnd);
            assertEquals(new Headers(1, isEnd).hashCode(), headers.hashCode());
        }
    }

    @Nested
    @DisplayName("getValue")
    class GetValue {

    }

    @Nested
    @DisplayName("getNames")
    class GetNames {

    }

    @Nested
    @DisplayName("addValue")
    class AddValue {

    }

    @Nested
    @DisplayName("toString")
    class ToString {
        /**
         * Tests valid toString with no options
         */
        @ParameterizedTest(name = "streamID = {0}, isEnd = {1}")
        @ArgumentsSource(ToStringNoOptionsProvider.class)
        @DisplayName("No Options")
        void testToStringValidNoOptions(int streamID, boolean isEnd, String expected) {
            assertDoesNotThrow(() -> {
                Headers headers = new Headers(streamID, isEnd);
                assertEquals(expected, headers.toString());
            });
        }

        /**
         * Tests valid toString with options
         */
        @ParameterizedTest(name = "streamID = {0}, isEnd = {1}")
        @ArgumentsSource(ToStringOptionsProvider.class)
        @DisplayName("Options")
        void testToStringValidOptions(int streamID, boolean isEnd, Map<String, String> options, String expected) {
            assertDoesNotThrow(() -> {
                Headers headers = new Headers(streamID, isEnd);
                for(Map.Entry<String, String> entry : options.entrySet()){
                    headers.addValue(entry.getKey(), entry.getValue());
                }
                assertEquals(expected, headers.toString());
            });
        }
    }

    static class ToStringOptionsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            List<Integer> validStreamIDs = Arrays.asList( 1, 20, 50);
            List<Boolean> validIsEnd = Arrays.asList(true, false);
            List<Map<String, String>> validOptions = Arrays.asList(
                    new TreeMap<>(),
                    Map.of("name1", "value1"),
                    Map.of("name1", "value1", "name2", "value2"),
                    Map.of("name1", "value1", "name2", "value2", "name3", "value3")
            );

            return validStreamIDs
                    .stream()
                    .flatMap(streamID ->
                            validIsEnd
                                    .stream()
                                    .flatMap(isEnd ->
                                            validOptions
                                                    .stream()
                                                    .map(options -> Arguments.of(streamID, isEnd, options, encode(streamID, isEnd, new TreeMap<>(options))))
                                    )
                    );
        }

        private String encode(int streamID, boolean isEnd, Map<String, String> options){
            StringBuilder builder = new StringBuilder();
            builder.append(String.format("Headers: StreamID=%d isEnd=%b",streamID, isEnd));
            builder.append(" (");
            for(Map.Entry<String, String> entry : options.entrySet()){
                builder.append(String.format("[%s=%s]", entry.getKey(), entry.getValue()));
            }
            builder.append(")");
            return builder.toString();
        }
    }

    static class ToStringNoOptionsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            List<Integer> validStreamIDs = Arrays.asList( 1, 20, 50);
            List<Boolean> validIsEnd = Arrays.asList(true, false);

            return validStreamIDs
                    .stream()
                    .flatMap(streamID ->
                            validIsEnd
                                    .stream()
                                    .map(isEnd ->
                                            Arguments.of(streamID, isEnd, encode(streamID, isEnd))
                                    )
                    );
        }

        private String encode(int streamID, boolean isEnd){
            return String.format("Headers: StreamID=%d isEnd=%b ()",streamID, isEnd);
        }
    }
}
