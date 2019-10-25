/*******************************************************
 * Author: Ian Laird, Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.serialization.test;

import jack.serialization.ACK;
import jack.serialization.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static jack.serialization.test.ResponseTester.VALID_PORT;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ACKTester extends HostPortTester{

    // the charset that is being used
    private static final Charset ENC = StandardCharsets.US_ASCII;

    protected Message getNewObject(String host, int port){
        return new ACK(host, port);
    }

    protected String testHostSetter(String s){
        ACK toTest = new ACK("toChange", VALID_PORT);
        toTest.setHost(s);
        return toTest.getHost();
    }

    protected int testPortSetter(int port){
        ACK toTest = new ACK("toChange", port + 1);
        toTest.setPort(port);
        return toTest.getPort();
    }

    protected String getMessageType(){
        return "ACK";
    }

    @Test
    @DisplayName("Bad service")
    public void testBadService(){
        assertThrows(IllegalArgumentException.class, () -> {
            String badEncoded = "A hostport";
            Message.decode(badEncoded.getBytes(ENC));
        });
    }

    @Test
    @DisplayName("Bad port")
    public void testBadPort(){
        assertThrows(IllegalArgumentException.class, () -> {
            String badEncoded = "A host:port";
            Message.decode(badEncoded.getBytes(ENC));
        });
    }
}
