/*******************************************************
 * Author: Ian Laird, Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.serialization.test;

import jack.serialization.ACK;
import jack.serialization.Message;
import jack.serialization.New;
import org.junit.jupiter.api.Test;

import static jack.serialization.test.ResponseTester.VALID_PORT;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Ian Laird and Andrew Walker
 */
public class NewTester extends HostPortTester {

    /**
     * gets a New for the vals
     * @param host the host
     * @param port the port
     * @return a New
     */
    protected Message getNewObject(String host, int port){
        return new New(host, port);
    }

    /**
     * tests setting host
     * @param s the string to set
     * @return the get host
     */
    protected String testHostSetter(String s){
        New toTest = new New("toChange", VALID_PORT);
        toTest.setHost(s);
        return toTest.getHost();
    }

    /**
     * tests setting port
     * @param port the port to set
     * @return the get port
     */
    protected int testPortSetter(int port){
        New toTest = new New("toChange", port + 1);
        toTest.setPort(port);
        return toTest.getPort();
    }

    /**
     * NEW
     * @return NEW
     */
    protected String getMessageType(){
        return "NEW";
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
            new New(host, port);
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
        New n = new New(host, port);

        assertThrows(IllegalArgumentException.class, () -> {
            String newHost = host.concat("A");
            n.setHost(newHost);
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
            new New(host, port);
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
        New n = new New(host, port);

        assertThrows(IllegalArgumentException.class, () -> {
            int newPort = 12;
            n.setPort(newPort);
        });
    }
}
