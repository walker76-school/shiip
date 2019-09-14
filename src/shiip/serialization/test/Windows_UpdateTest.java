package shiip.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import shiip.serialization.BadAttributeException;
import shiip.serialization.Data;
import shiip.serialization.Windows_Update;

import static org.junit.jupiter.api.Assertions.*;

public class Windows_UpdateTest {

    /**
     * Tests the constructor of Windows_Update
     */
    @Nested
    @DisplayName("constructor")
    public class Constructor{

        /**
         * Tests
         */
        @ParameterizedTest(name = "increment = {0}")
        @ValueSource(ints = {-1, 0})
        @DisplayName("Invalid Increment")
        public void testConstructorInvalidIncrement(int increment) {
            assertThrows(BadAttributeException.class, () -> {
                new Windows_Update(0, increment);
            });
        }

        @ParameterizedTest(name = "increment = {0}")
        @ValueSource(ints = {1, 10, 20})
        @DisplayName("increment")
        public void testConstructorIncrement(int increment){
            assertDoesNotThrow(() -> {
                Windows_Update windowsUpdate = new Windows_Update(0, increment);
                assertEquals(increment, windowsUpdate.getIncrement());
            });
        }

        @ParameterizedTest(name = "isEnd = {0}")
        @CsvSource({"true", "false"})
        @DisplayName("isEnd")
        public void testConstructorIsEnd(boolean isEnd){
            assertDoesNotThrow(() -> {
                Data data = new Data(1, isEnd, null);
                assertEquals(isEnd, data.isEnd());
            });
        }
    }
}
