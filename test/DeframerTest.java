import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import shiip.serialization.Deframer;
import shiip.serialization.Framer;
import shiip.serialization.SerializationConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeframerTest {

    @Test
    public void testConstructor(){
        assertThrows(NullPointerException.class, () -> new Deframer(null));
    }

    @ParameterizedTest(name = "Valid Payload of {0}")
    @ValueSource(ints = { 0, 1, 100 })
    public void testGetFrameValid(int length){

        ByteArrayInputStream in = new ByteArrayInputStream(new byte[length]);
    }

    @ParameterizedTest(name = "Invalid Payload of {0}")
    @ValueSource(ints = { -1, -5, -SerializationConstants.HEADER_BYTES })
    public void testPutFrameInvalid(int payloadLength){

    }

}
