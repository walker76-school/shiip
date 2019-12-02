/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 7
 * Class: Data Comm
 *******************************************************/

package jack.client;

import jack.serialization.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;

import static jack.serialization.Constants.MAX_LENGTH;

/**
 * Prints every message from a multicast socket
 */
public class Receiver implements Runnable {

    private MulticastSocket sock;

    /**
     * Constructs a receiver from a given socket
     * @param sock socket
     */
    public Receiver(MulticastSocket sock){
        this.sock = sock;
    }

    @Override
    public void run() {
        try{
            while(true){
                Message message = getMessage();
                System.out.println(message);
            }
        } catch (IOException e){
            return;
        }
    }

    /**
     * Retrieves the next message from the socket
     * @return the next message from the socket
     */
    private Message getMessage() throws IOException {
        try {
            // Receive response
            DatagramPacket packet = new DatagramPacket(new byte[MAX_LENGTH], MAX_LENGTH);
            sock.receive(packet);
            byte[] encodedMessage = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
            return Message.decode(encodedMessage);
        } catch (IllegalArgumentException e){
            System.err.println("Invalid message: " + e.getMessage());
            return null;
        }
    }
}
