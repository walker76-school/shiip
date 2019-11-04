/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 5
 * Class: Data Comm
 *******************************************************/

package jack.server;

import jack.serialization.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SHiiP server for serving files to a client
 *
 * @author Andrew Walker
 */
public class BlackHoleServer {

    // Max length of message
    private static final int MAX_LENGTH = 65507;

    // Index of the port in args
    private static final int PORT_NDX = 0;

    // Number of args
    private static final int NUM_ARGS = 1;

    public static void main(String[] args) {

        // Establish Logger
        Logger logger = Logger.getLogger("JackServer");

        // Test for correct # of args
        if(args.length != NUM_ARGS){
            logger.log(Level.SEVERE, "Parameter(s): <Port>");
            return;
        }

        try (DatagramSocket sock = new DatagramSocket(Integer.parseInt(args[PORT_NDX]))) {
            byte[] inBuffer = new byte[MAX_LENGTH];

            while (true) { // Run forever, accepting and servicing connections

                try {
                    DatagramPacket packet = new DatagramPacket(inBuffer, inBuffer.length);
                    sock.receive(packet);
                    handleDatagram(packet);
                } catch (IOException e){
                    logger.log(Level.WARNING, "Communication problem: " + e.getMessage());
                }

            }
            /* NOT REACHED */
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Parameter(s): <Port>");
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        }

    }

    /**
     * Handles a received datagram
     * @param packet the received datagram
     */
    private static void handleDatagram(DatagramPacket packet) throws IOException{
        Message message = getMessage(packet);
        if(message != null) {
            System.out.println("Received Message: " + message);
        }
    }

    /**
     * Converts a Datagram into a Message
     * @param packet the datagram
     * @return a Message from the datagram
     * @throws IOException If communication issue
     */
    private static Message getMessage(DatagramPacket packet) throws IOException {
        try{
            byte[] encodedMessage = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
            return Message.decode(encodedMessage);
        } catch (IllegalArgumentException e){
            return null;
        }
    }
}
