package shiip.server;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import shiip.serialization.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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

    // Index of thread count in args
    private static final int THREAD_NDX = 1;

    // Index of document root in args
    private static final int ROOT_NDX = 2;

    // Number of args
    private static final int NUM_ARGS = 2;

    private static final int BUFSIZE = 256;

    // File for log
    private static final String LOG_FILE = "./connections.log";

    public static void main(String[] args) {
        // Establish Logger
        Logger logger = Logger.getLogger("ServerAIO");
        try {
            logger.setUseParentHandlers(false);
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
            listenChannel.bind(new InetSocketAddress(Integer.parseInt(args[0])));

            // Create accept handler
            listenChannel.accept(null, new ConnectionHandler(listenChannel, logger));
            // Block until current thread dies
            Thread.currentThread().join();

        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Server Interrupted", e);
        } catch (IOException e){
            logger.log(Level.WARNING, "Connection problem: " + e.getMessage());
        }
    }

    static class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {

        private AsynchronousServerSocketChannel listenChannel;
        private Logger logger;

        public ConnectionHandler(AsynchronousServerSocketChannel listenChannel, Logger logger) {
            this.listenChannel = listenChannel;
            this.logger = logger;
        }

        @Override
        public void completed(AsynchronousSocketChannel clntChan, Void attachment) {
            listenChannel.accept(null, this);
            handleAccept(clntChan);
        }

        @Override
        public void failed(Throwable e, Void attachment) {
            logger.log(Level.WARNING, "Close Failed", e);
        }

        /**
         * Called after each accept completion
         *
         * @param clntChan channel of new client
         */
        public void handleAccept(final AsynchronousSocketChannel clntChan) {
            ByteBuffer buf = ByteBuffer.allocateDirect(BUFSIZE);

            // Create Connection Context
            ClientConnectionContext connectionContext = new ClientConnectionContext(clntChan);

            clntChan.read(buf, buf, new ReadHandler(connectionContext, logger));
        }
    }

    static class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

        private final ClientConnectionContext context;
        private final Logger logger;
        private final Decoder decoder;

        public ReadHandler(ClientConnectionContext connectionContext, Logger logger) {
            this.context = connectionContext;
            this.logger = logger;
            this.decoder = new Decoder(4096, 4096);
        }

        @Override
        public void completed(Integer bytesRead, ByteBuffer buf) {
            try {
                handleRead(buf, bytesRead);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Handle Read Failed", e);
            }
        }

        @Override
        public void failed(Throwable ex, ByteBuffer v) {
            try {
                context.getClntSock().close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Close Failed", e);
            }
        }

        private void handleRead(ByteBuffer buf, int bytesRead) throws IOException {
            if (bytesRead == -1) { // Did the other end close?
                failed(null, buf);
            } else if (bytesRead > 0) {

                // Try to get a frame
                byte[] encodedMessage = context.getDeframer().getFrame(buf.array());

                if(encodedMessage != null){
                    Message message = getMessage(encodedMessage, decoder);
                    // If m is not null then we read a valid frame
                    if (message != null) {

                        switch (message.getCode()) {
                            case Constants.DATA_TYPE:
                                handleData(message);
                                break;
                            case Constants.HEADERS_TYPE:
                                handleHeaders(message, context);
                                break;
                            case Constants.SETTINGS_TYPE:
                                handleSettings(message);
                                break;
                            case Constants.WINDOW_UPDATE_TYPE:
                                handleWindowUpdate(message);
                                break;
                        }
                    }

                } else {
                    context.getClntSock().read(buf, buf, this);
                }
            }
        }

        /**
         * Retrieves the next message from the server
         *
         * @return the next message from the server
         */
        private Message getMessage(byte[] framedBytes, Decoder decoder) {
            try {
                return Message.decode(framedBytes, decoder);
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, "Unable to parse: " + e.getMessage());
            } catch (BadAttributeException | NullPointerException e) {
                logger.log(Level.WARNING, "Invalid Message: " + e.getMessage());
            }

            return null;
        }

        /**
         * Handler for a Headers message
         *  @param m the Headers message
         * @param connectionContext the connection context
         */
        private void handleHeaders(Message m, ClientConnectionContext connectionContext) throws BadAttributeException, IOException{
            Headers h = (Headers) m;

            int streamID = h.getStreamID();

            // Duplicate stream ID
            if(connectionContext.containsStreamID(streamID)){
                logger.log(Level.WARNING, "Duplicate request: " + h);
            }

            // Check that the streamIDs are increasing
            int maxPreviousStreamID = 0;
            for(int previousStreamID : connectionContext.getStreamIDs()){
                if(previousStreamID > maxPreviousStreamID){
                    maxPreviousStreamID = previousStreamID;
                }
            }
            boolean increasing = streamID > maxPreviousStreamID;

            // If it's even then it's illegal
            if(streamID % 2 == 0 || !increasing){
                logger.log(Level.WARNING, "Illegal stream ID: " + h);
            }

            // No or bad path specified
            String path = h.getValue(PATH_KEY);
            if(path == null){
                logger.log(Level.WARNING, "No or bad path");

                // Send headers
                Headers headers = new Headers(streamID, false);
                headers.addValue(STATUS_KEY, "404 No or bad path");

                // WRITE HANDLER (FAILED?)
                framer.putFrame(headers.encode(encoder));

                return; // Terminate stream
            }

            String filePath = documentRoot.concat(path);
            File file = new File(filePath);

            // Directory
            if(file.isDirectory()){
                logger.log(Level.WARNING, "Cannot request directory");

                // Send headers
                Headers headers = new Headers(streamID, false);
                headers.addValue(STATUS_KEY, "404 Cannot request directory");
                framer.putFrame(headers.encode(encoder));

                return; // Terminate stream
            }

            // Non-existent/No permission file
            if(!file.exists() || !Files.isReadable(Paths.get(filePath))){
                logger.log(Level.WARNING, "File not found");

                // Send headers
                Headers headers = new Headers(streamID, false);
                headers.addValue(STATUS_KEY, "404 File not found");
                framer.putFrame(headers.encode(encoder));

                return; // Terminate stream

            }

            // Record the streamID
            connectionContext.addStream(streamID);

            // Send good headers
            Headers headers = new Headers(streamID, false);
            headers.addValue(STATUS_KEY, "200 OK");
            framer.putFrame(headers.encode(encoder));

            // WRITE HANDLER USING filePath
            pool.execute(new ShiipDataProtocol(framer, streamID, filePath, logger));
        }

        /**
         * Handler for a Data message
         *
         * @param m the Data message
         */
        private void handleData(Message m) {
            logger.log(Level.WARNING, "Unexpected message: " + m);
        }

        /**
         * Handler for a Settings message
         *
         * @param m the Settings message
         */
        private void handleSettings(Message m) {
            logger.log(Level.INFO, "Received message: " + m);
        }

        /**
         * Handler for a Window_Update message
         *
         * @param m the Window_Update message
         */
        private void handleWindowUpdate(Message m) {
            logger.log(Level.INFO, "Received message: " + m);
        }
    }

    public static class WriteHandler implements CompletionHandler<Integer, ByteBuffer> {

        private AsynchronousSocketChannel clntChan;
        private Logger logger;
        private Deframer deframer;

        public WriteHandler(AsynchronousSocketChannel clntChan, Logger logger) {
            this.clntChan = clntChan;
            this.logger = logger;
        }

        @Override
        public void completed(Integer bytesWritten, ByteBuffer buf) {
            try {
                handleWrite(clntChan, buf);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Handle Write Failed", e);
            }
        }

        @Override
        public void failed(Throwable ex, ByteBuffer buf) {
            try {
                clntChan.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Close Failed", e);
            }
        }

        private void handleWrite(final AsynchronousSocketChannel clntChan, ByteBuffer buf) throws IOException {
            if (buf.hasRemaining()) { // More to write
                clntChan.write(buf, buf, this);
            } else { // Back to reading
                buf.clear();
                clntChan.read(buf, buf, new ReadHandler(clntChan, logger));
            }
        }
    }
}
