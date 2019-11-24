/*******************************************************
 * Author: Andrew Walker
 * Assignment: Prog 6
 * Class: Data Comm
 *******************************************************/

package shiip.server;

import shiip.server.handlers.ConnectionHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static shiip.server.utils.ServerUtils.configureLogger;

public class ServerAIO {
    // Maximum size of Data frames
    public static final int MAXDATASIZE = 500;

    // Maximum interval to send Data frames
    public static final int MINDATAINTERVAL = 500;

    // Index of the port in args
    private static final int PORT_NDX = 0;

    // Index of document root in args
    private static final int ROOT_NDX = 1;

    // Number of args
    private static final int NUM_ARGS = 2;


    public static void main(String[] args) {
        // Establish Logger
        Logger logger = Logger.getLogger("ServerAIO");
        if (!configureLogger(logger)) {
            return;
        }

        // Test for correct # of args
        if(args.length != NUM_ARGS){
            logger.log(Level.SEVERE, "Parameter(s): <Port> <DocumentRoot>");
            return;
        }

        try (AsynchronousServerSocketChannel listenChannel = AsynchronousServerSocketChannel.open()) {

            // Bind local port
            listenChannel.bind(new InetSocketAddress(Integer.parseInt(args[PORT_NDX])));

            // Create accept handler
            listenChannel.accept(null, new ConnectionHandler(listenChannel, args[ROOT_NDX], logger));

            // Block until current thread dies
            Thread.currentThread().join();

        } catch (InterruptedException | IOException e){
            logger.log(Level.WARNING, "Connection problem: " + e.getMessage());
        }
    }
}
