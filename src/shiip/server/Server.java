package shiip.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    public static final int MAXDATASIZE = 16384;
    public static final int MINDATAINTERVAL = 2000;

    public static void main(String[] args) {

        // Establish Logger
        Logger logger = Logger.getLogger("ShiipServer");
        try {
            logger.addHandler(new FileHandler("/output.log"));
        } catch (IOException e){
            logger.log(Level.WARNING, e.getMessage());
            return;
        }

        // Test for correct # of args
        if(args.length != 3){
            logger.log(Level.SEVERE, "Parameter(s): <Port> <ThreadCount> <DocumentRoot>");
            return;
        }

        ExecutorService pool = initThreadPool(args[1], logger);
        if(pool != null) {

            try (ServerSocket servSock = new ServerSocket(Integer.parseInt(args[0]))) {

                while (true) { // Run forever, accepting and servicing connections

                    try(Socket clntSock = servSock.accept()){ // Auto-closing
                        pool.execute(new ShiipServerProtocol(clntSock, args[2], logger));
                    }
                }
                /* NOT REACHED */
            } catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage());
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Parameter(s): <Port> <ThreadCount> <DocumentRoot>");
            }
        }
    }

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
