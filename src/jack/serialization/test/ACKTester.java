/*******************************************************
 * Author: Ian Laird, Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.serialization.test;

import jack.serialization.ACK;
import jack.serialization.Message;
import org.junit.jupiter.api.Test;

import static jack.serialization.test.ResponseTester.VALID_PORT;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Ian Laird and Andrew Walker
 */
public class ACKTester extends HostPortTester {

    /**
     * gets an Ack object
     * @param host the host
     * @param port the port
     * @return an Ack
     */
    protected Message getNewObject(String host, int port){
        return new ACK(host, port);
    }

    /**
     * sets the host
     * @param s the string to set
     * @return the retrieved host
     */
    protected String testHostSetter(String s){
        ACK toTest = new ACK("toChange", VALID_PORT);
        toTest.setHost(s);
        return toTest.getHost();
    }

    /**
     * sets and gets the port
     * @param port the port to set
     * @return the port that was retrieved
     */
    protected int testPortSetter(int port){
        ACK toTest = new ACK("toChange", port + 1);
        toTest.setPort(port);
        return toTest.getPort();
    }

    /**
     * ACK
     * @return ACK
     */
    protected String getMessageType(){
        return "ACK";
    }

    @Test
    public void testOversizedHostConstructor(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 65504; i++) {
            builder.append("A");
        }
        String host = builder.toString();
        int port = 1;
        assertThrows(IllegalArgumentException.class, () -> {
            new ACK(host, port);
        });
    }

    @Test
    public void testOversizedHostSetter(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 65503; i++) {
            builder.append("A");
        }
        String host = builder.toString();
        int port = 1;
        ACK ack = new ACK(host, port);

        assertThrows(IllegalArgumentException.class, () -> {
            String newHost = host.concat("A");
            ack.setHost(newHost);
        });
    }

    @Test
    public void testOversizedPortConstructor(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 65503; i++) {
            builder.append("A");
        }
        String host = builder.toString();
        int port = 12;
        assertThrows(IllegalArgumentException.class, () -> {
            new ACK(host, port);
        });
    }

    @Test
    public void testOversizedPortSetter(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 65503; i++) {
            builder.append("A");
        }
        String host = builder.toString();
        int port = 1;
        ACK ack = new ACK(host, port);

        assertThrows(IllegalArgumentException.class, () -> {
            int newPort = 12;
            ack.setPort(newPort);
        });
    }
}
