/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 5
 * Class: Data Comm
 *******************************************************/

package jack.server;

import jack.serialization.*;
import jack.serialization.Error;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * SHiiP server for serving files to a client
 *
 * @author Andrew Walker
 */
public class Server {

    // Max length of message
    private static final int MAX_LENGTH = 65507;

    // Index of the port in args
    private static final int PORT_NDX = 0;

    // Number of args
    private static final int NUM_ARGS = 1;

    private static Set<Service> services;

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
            services = new HashSet<>();

            while (true) { // Run forever, accepting and servicing connections

                try {
                    DatagramPacket packet = new DatagramPacket(inBuffer, inBuffer.length);
                    sock.receive(packet);
                    handleDatagram(packet, sock, logger);
                } catch (IOException e){
                    String errorMessage = "Communication problem: " + e.getMessage();
                    handleError(errorMessage, sock, logger);
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
     * @param sock the socket received on
     * @param logger logger
     */
    private static void handleDatagram(DatagramPacket packet, DatagramSocket sock, Logger logger) {
        try{

            Message message = getMessage(packet, sock, logger);

            if(message != null) {
                switch (message.getOperation()) {
                    case "N":
                        handleNew(message, sock, logger);
                        break;
                    case "Q":
                        handleQuery(message, sock, logger);
                        break;
                    default:
                        String errorMessage = "Unexpected message type: " + message;
                        handleError(errorMessage, sock, logger);
                        break;
                }
            }

        } catch(Exception ex){
            logger.log(Level.WARNING, "Communication problem: " + ex.getMessage());
        }
    }

    /**
     * Converts a Datagram into a Message
     * @param packet the datagram
     * @param sock the socket received on
     * @param logger logger
     * @return a Message from the datagram
     * @throws IOException If communication issue
     */
    private static Message getMessage(DatagramPacket packet, DatagramSocket sock, Logger logger) throws IOException {
        try{
            byte[] encodedMessage = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
            return Message.decode(encodedMessage);
        } catch (IllegalArgumentException e){
            String errorMessage = "Invalid message: " + e.getMessage();
            handleError(errorMessage, sock, logger);
            return null;
        }
    }

    /**
     * Handler for a New message
     * @param message the New message
     * @param clntSock the socket received on
     * @param logger logger
     * @throws IOException if communication issue
     */
    private static void handleNew(Message message, DatagramSocket clntSock, Logger logger) throws IOException {
        New n = (New) message;
        logger.log(Level.INFO, "Received message: " + n);

        // Add service to store
        services.add(new Service(n.getHost(), n.getPort()));

        // Construct ACK
        ACK ack = new ACK(n.getHost(), n.getPort());
        logger.log(Level.INFO, ack.toString());

        // Send ACK
        sendMessage(ack, clntSock);
    }

    /**
     * Handler for a Query message
     * @param message the Query message
     * @param clntSock the socket received on
     * @param logger logger
     * @throws IOException if communication issue
     */
    private static void handleQuery(Message message, DatagramSocket clntSock, Logger logger) throws IOException {
        Query query = (Query) message;
        logger.log(Level.INFO, "Received message: " + query);

        String searchString = query.getSearchString();

        // IS THIS CASE INSENSITIVE?
        List<Service> matches = services.stream()
                .filter(x -> x.getHost().contains(searchString) || searchString.equals("*"))
                .sorted(Comparator.comparing(Service::getHost))
                .collect(Collectors.toList());

        // Construct Response
        Response response = new Response();
        matches.forEach(x -> response.addService(x.getHost(), x.getPort()));
        logger.log(Level.INFO, response.toString());

        // Send Response
       sendMessage(response, clntSock);
    }

    /**
     * Handler for an error
     * @param errorMessage the error message
     * @param clntSock the socket received on
     * @param logger logger
     * @throws IOException if communication issue
     */
    private static void handleError(String errorMessage, DatagramSocket clntSock, Logger logger) throws IOException {
        logger.log(Level.WARNING, errorMessage);

        // Construct Error
        Error error = new Error(errorMessage);

        // Send Error
        sendMessage(error, clntSock);

    }

    /**
     * Sends a message in a DatagramPacket
     * @param message the message to send
     * @param clntSock the socket to send on
     * @throws IOException if communication issue
     */
    private static void sendMessage(Message message, DatagramSocket clntSock) throws IOException{
        byte[] encodedError = message.encode();
        DatagramPacket packet = new DatagramPacket(encodedError, encodedError.length);
        clntSock.send(packet);
    }
}
