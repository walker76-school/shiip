/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.client;

import jack.serialization.*;
import jack.serialization.Error;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A UDP client for Jack
 * @author Andrew Walker
 */
public class Client {

    // Encoding for handshake message
    private static final Charset ENC = StandardCharsets.US_ASCII;

    // Minimum number of parameters allowed
    private static final int MIN_ARGS = 3;

    // Index of the host in the parameters
    private static final int HOST_NDX = 0;

    // Index of the port in the parameters
    private static final int PORT_NDX = 1;

    // Index of the op in the parameters
    private static final int OP_NDX = 2;

    // Index of the start of the payload in the parameters
    private static final int PAYLOAD_NDX = 3;

    // Max length of message
    private static final int MAX_LENGTH = 65507;

    public static void main(String[] args) {
        if(args.length < MIN_ARGS){
            System.err.println("Usage: Client [host] [port] [op] [payload]");
            return;
        }

        try (DatagramSocket sock = new DatagramSocket()){

            // Connect to the server
            InetAddress destAddr = InetAddress.getByName(args[HOST_NDX]); // Destination addr
            int destPort = Integer.parseInt(args[PORT_NDX]); // Destination port
            sock.connect(destAddr, destPort);

            String op = args[OP_NDX];

            switch (op) {
                case "Q": handleQuery(args, sock);
                case "N": handleNew(args, sock);
                default: System.err.println("Bad parameters: Invalid op");
            }

        } catch (NumberFormatException e){
            System.err.println("Bad parameters: Invalid port");
        } catch(IOException e){
            System.err.println("Communication problem: " + e.getMessage());
        }
    }

    /**
     * Retrieves the next message from the server
     * @return the next message from the server
     */
    private static Message getMessage(DatagramSocket socket) throws IOException {
        try {

            // Receive response
            DatagramPacket message = new DatagramPacket(new byte[MAX_LENGTH], MAX_LENGTH);
            socket.receive(message);
            byte[] encodedMessage = Arrays.copyOfRange(message.getData(), 0, message.getLength());

            // Decode
            return Message.decode(encodedMessage);
        } catch (IllegalArgumentException e){
            System.err.println("Invalid message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handler for a Query message
     * @param args op and payload
     * @param sock socket connected to server
     */
    private static void handleQuery(String[] args, DatagramSocket sock) throws IOException {
        if(args.length != 4){
            System.err.println("Bad parameters: Invalid payload");
        }

        String searchString = args[PAYLOAD_NDX];

        // Validate searchString

        Query query = new Query(searchString);
        byte[] encodedQuery = query.encode();
        DatagramPacket packet = new DatagramPacket(encodedQuery, encodedQuery.length);
        sock.send(packet);

        // Receive message
        Message m = getMessage(sock);
        if(m != null){

            // Check it's from correct source

            switch (m.getOperation()){
                case "R":
                    Response response = (Response) m;
                    System.out.println(response.toString());
                    break;
                case "A":
                    System.err.println("Unexpected ACK");
                    break;
                case "E":
                    Error error = (Error) m;
                    System.out.println(error.getErrorMessage());
                    break;
                default: System.err.println("Unexpected message type");
            }
        }
    }

    /**
     * Handler for a New message
     * @param args op and payload
     * @param sock socket connected to server
     */
    private static void handleNew(String[] args, DatagramSocket sock) throws IOException {
        if(args.length != 4){
            System.err.println("Bad parameters: Invalid payload");
            return;
        }

        String payload = args[PAYLOAD_NDX];
        String[] tokens = payload.split(":");
        if(tokens.length != 2){
            System.err.println("Bad parameters: Invalid payload");
            return;
        }
        String host = tokens[0];
        String portString = tokens[1];

        int port;
        try{
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e){
            System.err.println("Bad parameters: Invalid port");
            return;
        }

        New n = new New(host, port);
        byte[] encodedNew = n.encode();
        DatagramPacket packet = new DatagramPacket(encodedNew, encodedNew.length);
        sock.send(packet);

        // Receive message
        Message m = getMessage(sock);
        if(m != null){

            // Check it's from correct source

            switch (m.getOperation()){
                case "R":
                    System.err.println("Unexpected Response");
                    break;
                case "A":
                    ACK ack = (ACK)m;
                    String ackHost = ack.getHost();
                    int ackPort = ack.getPort();
                    if(!ackHost.equals(host) || ackPort != port) {
                        System.err.println("Unexpected ACK");
                    } else {
                        System.out.println(ack);
                    }
                    break;
                case "E":
                    Error error = (Error) m;
                    System.out.println(error.getErrorMessage());
                    break;
                default: System.err.println("Unexpected message type");
            }
        }
    }
}
