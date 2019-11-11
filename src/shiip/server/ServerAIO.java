package shiip.server;

import com.twitter.hpack.Decoder;
import shiip.serialization.*;
import shiip.server.handlers.ConnectionHandler;
import shiip.server.models.ClientConnectionContext;
import shiip.server.protocols.ShiipDataProtocol;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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

    // File for log
    private static final String LOG_FILE = "./connections.log";

    public static void main(String[] args) {
        // Establish Logger
        Logger logger = Logger.getLogger("ServerAIO");
        try {
            //logger.setUseParentHandlers(false);
            FileHandler handler = new FileHandler(LOG_FILE);
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
        } catch (IOException e){
            logger.log(Level.WARNING, e.getMessage());
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

        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Server Interrupted", e);
        } catch (IOException e){
            logger.log(Level.WARNING, "Connection problem: " + e.getMessage());
        }
    }
}
