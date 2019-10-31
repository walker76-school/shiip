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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A UDP client for Jack
 * @author Andrew Walker
 */
public class Client {

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

                        // Handle the message
                        done = handleMessage(args[OP_NDX], message, reply);

                    } else { // If the socket experiences a timeout then retransmit
                        if(retransmitCount >= 3){
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

    private static DatagramSocket connectSocket(String[] args) throws IOException{
        DatagramSocket sock = new DatagramSocket();

        // Connect to the server
        InetAddress destAddr = InetAddress.getByName(args[HOST_NDX]); // Destination addr
        int destPort = Integer.parseInt(args[PORT_NDX]); // Destination port
        sock.connect(destAddr, destPort);

        sock.setSoTimeout(3000);

        return sock;
    }

    private static Message constructMessage(String[] args) {
        Message message = null;
        switch (args[OP_NDX]) {
            case "Q": message = buildQuery(args); break;
            case "N": message = buildNew(args); break;
            default: System.err.println("Bad parameters: Invalid op");
        }
        return message;
    }

    private static Query buildQuery(String[] args){
        if(args.length != 4){
            System.err.println("Bad parameters: Invalid payload");
            return null;
        }

        return new Query(args[PAYLOAD_NDX]);
    }

    private static New buildNew(String[] args){
        if(args.length != 4){
            System.err.println("Bad parameters: Invalid payload");
            return null;
        }

        String payload = args[PAYLOAD_NDX];
        String[] tokens = payload.split(":");
        if(tokens.length != 2){
            System.err.println("Bad parameters: Invalid payload");
            return null;
        }
        String host = tokens[0];
        String portString = tokens[1];

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
        } catch (SocketTimeoutException e){
            return null;
        }
    }

    private static boolean handleMessage(String op, Message message, Message reply) throws IOException {
        switch (reply.getOperation()){
            case "R":
                if(op.equals("Q")){ // Q sent

                    System.out.println(reply); // Print answer
                    return true; // Terminate

                } else if (op.equals("N")){ // Q not sent
                    System.err.println("Unexpected Response");
                }

            case "A":
                if(op.equals("N")){ // N sent

                    ACK ack = (ACK) message; // N
                    ACK ackReply = (ACK) reply; // <name:port>

                    if(ack.equals(ackReply)) { // <name>:<port> match N
                        System.out.println(ack); // Print ACK
                        return true; // Terminate

                    } else { // <name>:<port> does not match N
                        System.err.println("Unexpected ACK");
                    }

                } else if (op.equals("Q")){ // N not sent
                    System.err.println("Unexpected ACK");
                }

            case "E":
                Error error = (Error) reply;
                System.out.println(error.getErrorMessage()); // Print error message
                return true; // Terminate

            default: // Receive Q or N
                System.err.println("Unexpected message type");

            return false; // Reattempt message reception
        }
    }

    private static void sendMessage(Message message, DatagramSocket sock) throws IOException{
        byte[] encodedMessage = message.encode();
        DatagramPacket packet = new DatagramPacket(encodedMessage, encodedMessage.length);
        sock.send(packet);
    }
}
