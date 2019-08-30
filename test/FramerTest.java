import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import shiip.serialization.Framer;
import shiip.serialization.SerializationConstants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class FramerTest {

    @Test
    public void testConstructor(){
        assertThrows(NullPointerException.class, () -> new Framer(null));
    }

    @ParameterizedTest(name = "Valid Payload of {0}")
    @ValueSource(ints = { 0, 1, 100, SerializationConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES })
    public void testPutFrameValid(int payloadLength){
        byte[] message = new byte[SerializationConstants.HEADER_BYTES + payloadLength];
        Framer framer = new Framer(System.out);
        assertDoesNotThrow(() -> framer.putFrame(message));
    }

    @Test
    public void testPutFrameNull(){
        Framer framer = new Framer(System.out);
        assertThrows(NullPointerException.class, () -> framer.putFrame(null));
    }

    @ParameterizedTest(name = "Invalid Payload of {0}")
    @ValueSource(ints = { -1, -5, -SerializationConstants.HEADER_BYTES, SerializationConstants.MAXIMUM_PAYLOAD_LENGTH_BYTES + 1 })
    public void testPutFrameInvalid(int payloadLength){
        byte[] message = new byte[SerializationConstants.HEADER_BYTES + payloadLength];
        Framer framer = new Framer(System.out);
        assertThrows(IOException.class, () -> framer.putFrame(message));
    }
}
