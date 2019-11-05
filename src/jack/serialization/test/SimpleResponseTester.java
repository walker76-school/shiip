package jack.serialization.test;

import jack.serialization.Message;
import jack.serialization.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@DisplayName("Simple ResponseTest")
public class SimpleResponseTester {
    private static final Charset ENC = StandardCharsets.US_ASCII;

    @DisplayName("encode")
    @Test
    void testEncode() {
        var m = new Response();
        m.addService("b", 3);
        m.addService("a", 5);
        m.addService("b", 3);
        byte[] expEnc = "R a:5 b:3 ".getBytes(ENC);
        assertArrayEquals(expEnc, m.encode());
    }

    @DisplayName("decode")
    @Test
    void testDecode() throws IOException {
        byte[] enc = "R b:3 a:5 b:3 ".getBytes(ENC);
        Response m = (Response) Message.decode(enc);
        List expSvc = Arrays.asList("a:5", "b:3");
        assertIterableEquals(expSvc, m.getServiceList());
    }
}