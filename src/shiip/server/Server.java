/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 3
 * Class: Data Comm
 *******************************************************/

package shiip.server;

import shiip.server.protocols.ShiipServerProtocol;
import tls.TLSFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * SHiiP server for serving files to a client
 *
 * @author Andrew Walker
 */
public class Server {

    // Maximum size of Data frames
    public static final int MAXDATASIZE = 500;

    // Maximum interval to send Data frames
    public static final int MINDATAINTERVAL = 500;

    // Index of the port in args
    private static final int PORT_NDX = 0;

    // Index of thread count in args
    private static final int THREAD_NDX = 1;

    // Index of document root in args
    private static final int ROOT_NDX = 2;

    // Number of args
    private static final int NUM_ARGS = 3;

    // Keystore name
    private static final String KEYSTORE = "mykeystore";

    // Password for keystore
    private static final String KEYSTORE_PWD = "secret";

    // File for log
    private static final String LOG_FILE = "./connections.log";

    public static void main(String[] args) {

        // Establish Logger
        Logger logger = Logger.getLogger("ShiipServer");
        try {
            FileHandler handler = new FileHandler(LOG_FILE);
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
        } catch (IOException e){
            logger.log(Level.WARNING, e.getMessage());
            return;
        }

        // Test for correct # of args
        if(args.length != NUM_ARGS){
            logger.log(Level.SEVERE, "Parameter(s): <Port> <ThreadCount> <DocumentRoot>");
            return;
        }

        ExecutorService pool = initThreadPool(args[THREAD_NDX], logger);
        if(pool != null) {

            try (ServerSocket servSock = TLSFactory.getServerListeningSocket(Integer.parseInt(args[PORT_NDX]), KEYSTORE, KEYSTORE_PWD)) {

                while (true) { // Run forever, accepting and servicing connections

                    try {
                        Socket clntSock = TLSFactory.getServerConnectedSocket(servSock);
                        pool.execute(new ShiipServerProtocol(clntSock, args[ROOT_NDX], logger));
                    } catch (IOException e){
                        logger.log(Level.WARNING, e.getMessage());
                    }

                }
                /* NOT REACHED */
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Parameter(s): <Port> <ThreadCount> <DocumentRoot>");
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage());
            }
        }
    }

    /**
     * Initializes the thread pool
     * @param threadCountS the number of threads in the pool
     * @param logger logger
     * @return initialized ExecutorService
     */
    private static ExecutorService initThreadPool(String threadCountS, Logger logger) {
        int threadCount;
        try {
            threadCount = Integer.parseInt(threadCountS);
        } catch (NumberFormatException e){
            logger.log(Level.WARNING, e.getMessage());
            return null;
        }
        return Executors.newFixedThreadPool(threadCount);
    }
}
