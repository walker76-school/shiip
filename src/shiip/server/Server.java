package shiip.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Server {

    public static void main(String[] args) {

        // Test for correct # of args
        if(args.length != 3){
            throw new IllegalArgumentException("Parameter(s): <Port> <ThreadCount> <DocumentRoot>");
        }

        ExecutorService pool = initThreadPool(args[1]);
        if(pool != null) {

            try (ServerSocket servSock = new ServerSocket(Integer.parseInt(args[0]))) {

                while (true) { // Run forever, accepting and servicing connections

                    Socket clntSock = servSock.accept();     // Get client connection
                    pool.execute(new ShiipServerProtocol(clntSock));
                    clntSock.close();  // Close the socket.  We are done with this client!
                }
                /* NOT REACHED */
            } catch (IOException e) {
                System.err.println(e.getMessage());
            } catch (NumberFormatException e) {
                System.err.println("Parameter(s): <Port> <ThreadCount> <DocumentRoot>");
            }
        } else {
            System.err.println("Help");
        }
    }

    private static ExecutorService initThreadPool(String threadCountS) {
        int threadCount;
        try {
            threadCount = Integer.parseInt(threadCountS);
        } catch (NumberFormatException e){
            System.err.println(e.getMessage());
            return null;
        }
        return Executors.newFixedThreadPool(threadCount);
    }
}
