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
import java.net.SocketTimeoutException;
import java.util.*;

import static jack.serialization.Constants.*;

/**
 * A UDP client for Jack
 * @author Andrew Walker
 */
public class Client {

    // Minimum number of parameters allowed
    private static final int MIN_ARGS = 3;

    // Index of the op in the parameters
    private static final int OP_NDX = 2;

    // Index of the start of the payload in the parameters
    private static final int PAYLOAD_NDX = 3;

    // Max length of message
    private static final int MAX_LENGTH = 65507;

    // Maximum number of times the client can retransmit
    private static final int MAX_RETRANSMIT = 3;

    public static void main(String[] args) {
        if(args.length < MIN_ARGS){
            System.err.println("Usage: Client [host] [port] [op] [payload]");
            return;
        }

        try (DatagramSocket sock = connectSocket(args)){

            Message message = constructMessage(args);
            if(message != null) {
                sendMessage(message, sock);

                int retransmitCount = 0;
                boolean done = false;
                while (!done) {

                    // Get message
                    Message reply = getMessage(sock);
                    if (reply != null) {

                        // Handle the reply
                        done = handleReply(args[OP_NDX], message, reply);

                    } else { // If the socket experiences a timeout then retransmit
                        if(retransmitCount >= MAX_RETRANSMIT){
                            System.err.println("Max retransmit limit reached");
                            return;
                        } else {
                            sendMessage(message, sock);
                            retransmitCount++;
                        }
                    }
                }
            }

        } catch (NumberFormatException e){
            System.err.println("Bad parameters: Invalid port");
        } catch(IOException e){
            System.err.println("Communication problem: " + e.getMessage());
        }
    }

    /**
     * Connects the datagram socket to the given host and port
     * @param args the command line args
     * @return a datagram socket connected to th given host and port
     * @throws IOException if communication problem
     */
    private static DatagramSocket connectSocket(String[] args) throws IOException{
        DatagramSocket sock = new DatagramSocket();

        // Connect to the server
        InetAddress destAddr = InetAddress.getByName(args[HOST_NDX]); // Destination addr
        int destPort = Integer.parseInt(args[PORT_NDX]); // Destination port
        sock.connect(destAddr, destPort);

        sock.setSoTimeout(3000);

        return sock;
    }

    /**
     * Constructs the proper message from the command line args
     * @param args the command line args
     * @return the proper message
     */
    private static Message constructMessage(String[] args) {
        Message message = null;
        switch (args[OP_NDX]) {
            case QUERY_OP: message = buildQuery(args); break;
            case NEW_OP: message = buildNew(args); break;
            default: System.err.println("Bad parameters: Invalid op");
        }
        return message;
    }

    /**
     * Builds a Query from the command line args
     * @param args the command line args
     * @return Query
     */
    private static Query buildQuery(String[] args){
        if(args.length != 4){
            System.err.println("Bad parameters: Invalid payload");
            return null;
        }

        return new Query(args[PAYLOAD_NDX]);
    }

    /**
     * Builds a New from the command line args
     * @param args the command line args
     * @return New
     */
    private static New buildNew(String[] args){
        if(args.length != 4){
            System.err.println("Bad parameters: Invalid payload");
            return null;
        }

        String payload = args[PAYLOAD_NDX];
        String[] tokens = payload.split(SERVICE_REGEX);
        if(tokens.length != SERVICE_TOKEN_LEN){
            System.err.println("Bad parameters: Invalid payload");
            return null;
        }
        String host = tokens[HOST_NDX];
        String portString = tokens[PORT_NDX];

        int port;
        try{
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e){
            System.err.println("Bad parameters: Invalid port");
            return null;
        }

        return new New(host, port);
    }

    /**
     * Retrieves the next message from the server
     * @param socket the socket to receive from
     * @return the next message from the server
     */
    private static Message getMessage(DatagramSocket socket) throws IOException {
        try {
            // Receive response
            DatagramPacket packet = new DatagramPacket(new byte[MAX_LENGTH], MAX_LENGTH);
            socket.receive(packet);
            byte[] encodedMessage = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
            Message message = Message.decode(encodedMessage);

            // Check if from same host
            if(!packet.getAddress().equals(socket.getInetAddress()) || packet.getPort() != socket.getPort()){
                System.err.println("Unexpected message source: " + message);
                return null;
            }

            return message;
        } catch (IllegalArgumentException e){
            System.err.println("Invalid message: " + e.getMessage());
            return null;
        } catch (SocketTimeoutException e){
            return null;
        }
    }

    /**
     * Handles the reply from the server
     * @param op the op of the original message
     * @param message the original message
     * @param reply the reply from the server
     * @return if the client can terminate
     * @throws IOException if communication problem
     */
    private static boolean handleReply(String op, Message message, Message reply) throws IOException {
        switch (reply.getOperation()){
            case RESPONSE_OP:
                if(op.equals(QUERY_OP)){ // Q sent

                    System.out.println(reply); // Print answer
                    return true; // Terminate

                } else { // Q not sent
                    System.err.println("Unexpected Response");
                }

            case ACK_OP:
                if(op.equals(NEW_OP)){ // N sent

                    New n = (New) message; // N
                    ACK ackReply = (ACK) reply; // <name:port>

                    if(n.getHost().equals(ackReply.getHost()) && n.getPort() == ackReply.getPort()) { // <name>:<port> match N
                        System.out.println(ackReply); // Print ACK
                        return true; // Terminate

                    } else { // <name>:<port> does not match N
                        System.err.println("Unexpected ACK");
                    }

                } else { // N not sent
                    System.err.println("Unexpected ACK");
                }

            case ERROR_OP:
                Error error = (Error) reply;
                System.out.println(error.getErrorMessage()); // Print error message
                return true; // Terminate

            default: // Receive Q or N
                System.err.println("Unexpected message type");

            return false; // Reattempt message reception
        }
    }

    /**
     * Sends a message to a socket
     * @param message the Message to send
     * @param sock socket to send the message on
     * @throws IOException if communication problem
     */
    private static void sendMessage(Message message, DatagramSocket sock) throws IOException{
        byte[] encodedMessage = message.encode();
        DatagramPacket packet = new DatagramPacket(encodedMessage, encodedMessage.length);
        sock.send(packet);
    }
}
