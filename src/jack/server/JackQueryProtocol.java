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

import static jack.server.Server.MAX_LENGTH;

/**
 * Protocol for handling a client connection
 *
 * @author Andrew Walker
 */
public class JackQueryProtocol implements Runnable {


    // The socket the client is connected on
    private final DatagramSocket clntSock;

    // Logger for info and errors
    private final Logger logger;

    private final Set<Service> services;

    /**
     * Constructor for new Server protocol
     * @param clntSock the socket the client is connected on
     * @param logger logger
     */
    public JackQueryProtocol(DatagramSocket clntSock, Logger logger) {
        this.clntSock = clntSock;
        this.logger = logger;
        services = new HashSet<>();
    }

    @Override
    public void run() {

        try (clntSock){ // So the socket is auto-closable

            // Attempt to retrieve the first frame
            Message message = getMessage(clntSock);

            // If message is not null then we read a valid frame
            if (message != null) {

                switch (message.getOperation()) {
                    case "N":
                        handleNew(message, clntSock);
                        break;
                    case "Q":
                        handleQuery(message, clntSock);
                        break;
                    default:
                        String errorMessage = "Unexpected message type: " + message;
                        handleError(errorMessage, clntSock);
                        break;
                }
            }

        } catch(Exception ex){
            logger.log(Level.WARNING, "Communication problem: " + ex.getMessage());
            // Socket should auto-close
            // Connection is killed
        }
    }

    /**
     * Retrieves the next message from the server
     * @return the next message from the server
     */
    private Message getMessage(DatagramSocket socket) throws IOException {
        try {

            // Receive response
            DatagramPacket message = new DatagramPacket(new byte[MAX_LENGTH], MAX_LENGTH);
            socket.receive(message);
            byte[] encodedMessage = Arrays.copyOfRange(message.getData(), 0, message.getLength());

            // Decode
            return Message.decode(encodedMessage);
        } catch (IllegalArgumentException e){
            String errorMessage = "Invalid message: " + e.getMessage();
            handleError(errorMessage, clntSock);
            return null;
        }
    }

    private void handleNew(Message message, DatagramSocket clntSock) throws IOException {
        New n = (New) message;
        System.out.println("Received message: " + n);

        // Add service to store
        services.add(new Service(n.getHost(), n.getPort()));

        // Construct ACK
        ACK ack = new ACK(n.getHost(), n.getPort());
        logger.log(Level.INFO, ack.toString());

        // Send ACK
        byte[] encodedAck = ack.encode();
        DatagramPacket packet = new DatagramPacket(encodedAck, encodedAck.length);
        clntSock.send(packet);
    }

    private void handleQuery(Message message, DatagramSocket clntSock) throws IOException {
        Query query = (Query) message;
        System.out.println("Received message: " + query);

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
        byte[] encodedResponse = response.encode();
        DatagramPacket packet = new DatagramPacket(encodedResponse, encodedResponse.length);
        clntSock.send(packet);
    }

    private void handleError(String errorMessage, DatagramSocket clntSock) throws IOException {
        System.err.println(errorMessage);

        // Construct Error
        Error error = new Error(errorMessage);

        // Send Error
        byte[] encodedError = error.encode();
        DatagramPacket packet = new DatagramPacket(encodedError, encodedError.length);
        clntSock.send(packet);
    }
}
