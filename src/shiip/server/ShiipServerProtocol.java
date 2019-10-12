package shiip.server;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import shiip.serialization.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShiipServerProtocol implements Runnable {

    // Table size for Encoder and Decoder
    private static final int MAX_TABLE_SIZE = 4096;

    // Max number of threads a connection is allowed to spawn
    private static final int POOL_SIZE = 10;

    // Key for finding the path
    private static final String PATH_KEY = ":path";

    // Key for finding the status
    private static final String STATUS_KEY = ":status";

    // Timeout for sockets on IO - 20 seconds
    private static final int TIMEOUT = 20000;

    // Encoding for handshake message
    private static final Charset ENC = StandardCharsets.US_ASCII;

    // Initial HTTP handshake message
    private static final String HANDSHAKE_MESSAGE = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";

    private final Socket clntSock;
    private final String documentRoot;
    private final Logger logger;
    private final ExecutorService pool;

    public ShiipServerProtocol(Socket clntSock, String documentRoot, Logger logger) {
        this.clntSock = clntSock;
        this.documentRoot = documentRoot;
        this.logger = logger;
        pool = Executors.newFixedThreadPool(POOL_SIZE);
    }

    @Override
    public void run() {
        try (
            clntSock; // So the socket is auto-closable
            InputStream in = clntSock.getInputStream();
            OutputStream out = clntSock.getOutputStream();
        ) {

            // Some initial util objects
            Deframer deframer = new Deframer(in);
            Framer framer = new Framer(out);
            Decoder decoder = new Decoder(MAX_TABLE_SIZE, MAX_TABLE_SIZE);
            Encoder encoder = new Encoder(MAX_TABLE_SIZE);

            // To keep track of seen streamIDs
            List<Integer> streamIDs = new ArrayList<>();

            // Set the initial timeout of the socket
            clntSock.setSoTimeout(TIMEOUT);

            // Read the handshake message
            int handshakeLength = HANDSHAKE_MESSAGE.getBytes(ENC).length;
            byte[] handshake = new byte[handshakeLength];
            in.readNBytes(handshake, 0, handshakeLength);
            String handshakeMessage = b2s(handshake);
            if(!handshakeMessage.equals(HANDSHAKE_MESSAGE)){
                logger.log(Level.WARNING, "Invalid handshake message - " + handshakeMessage);
                return; // Kill connection
            }

            // Reset timeout
            clntSock.setSoTimeout(TIMEOUT);

            // Loop until IOException breaks out
            while (true) {

                // Attempt to retrieve the first frame
                Message m = getMessage(deframer, decoder);

                // If we have read in data then reset our timeout
                clntSock.setSoTimeout(TIMEOUT);

                // If m is not null then we read a valid frame
                if (m != null) {
                    switch (m.getCode()) {
                        case Constants.DATA_TYPE:
                            handleData(m);
                            break;
                        case Constants.HEADERS_TYPE:
                            handleHeaders(m, framer, encoder, streamIDs);
                            break;
                        case Constants.SETTINGS_TYPE:
                            handleSettings(m);
                            break;
                        case Constants.WINDOW_UPDATE_TYPE:
                            handleWindowUpdate(m);
                            break;
                    }
                }
            }

        } catch (IOException ex) {
            // This block should only trigger if an IOException is thrown meaning
            // the client has disconnected or a timeout has occurred
        } catch (BadAttributeException ex){
            logger.log(Level.SEVERE, ex.getMessage());
            // Socket should auto-close
            // Connection is killed
        }
    }

    /**
     * Retrieves the next message from the server
     *
     * @return the next message from the server
     */
    private Message getMessage(Deframer deframer, Decoder decoder) throws IOException {
        try {
            byte[] framedBytes = deframer.getFrame();
            return Message.decode(framedBytes, decoder);
        } catch (IllegalArgumentException | EOFException e) {
            logger.log(Level.WARNING, "Unable to parse: " + e.getMessage());
        } catch (BadAttributeException | NullPointerException e) {
            logger.log(Level.WARNING, "Invalid Message: " + e.getMessage());
        }

        return null;
    }

    /**
     * Handler for a Headers message
     *  @param m the Headers message
     * @param encoder
     * @param streamIDs a list of seen streamIDs
     */
    private void handleHeaders(Message m, Framer framer, Encoder encoder, List<Integer> streamIDs) throws BadAttributeException, IOException{
        Headers h = (Headers) m;

        int streamID = h.getStreamID();
        if(streamIDs.contains(streamID)){
            logger.log(Level.WARNING, "Duplicate request: " + h);
        }

        // If it's even then it's illegal
        if(streamID % 2 == 0){
            logger.log(Level.WARNING, "Illegal stream ID: " + h);
        }

        String path = h.getValue(PATH_KEY);
        if(path == null){
            logger.log(Level.WARNING, "No path specified");

            // Send headers
            Headers headers = new Headers(streamID, false);
            headers.addValue(STATUS_KEY, "404 File not found");
            framer.putFrame(headers.encode(encoder));

            return; // terminate stream
        }

        String filePath = documentRoot.concat(path);
        File file = new File(filePath);

        if(!file.exists() || !Files.isReadable(Paths.get(filePath))){
            logger.log(Level.WARNING, "Unable to open file: " + file);

            // Send headers
            Headers headers = new Headers(streamID, false);
            headers.addValue(STATUS_KEY, "404 File not found");
            framer.putFrame(headers.encode(encoder));

            return; // Terminate stream

        }

        if(file.isDirectory()){
            logger.log(Level.WARNING, "Cannot request directory: " + file);

            // Send headers
            Headers headers = new Headers(streamID, false);
            headers.addValue(STATUS_KEY, "404 Cannot request directory");
            framer.putFrame(headers.encode(encoder));

            return; // Terminate stream
        }

        // Record the streamID
        streamIDs.add(streamID);

        Headers headers = new Headers(streamID, false);
        headers.addValue(STATUS_KEY, "200 OK");
        framer.putFrame(headers.encode(encoder));

        pool.execute(new ShiipDataProtocol(framer, streamID, filePath, logger));


    }

    /**
     * Handler for a Data message
     *
     * @param m the Data message
     */
    private void handleData(Message m) {
        logger.log(Level.WARNING, "Invalid message: " + m);
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

    private static String b2s(byte[] b) {
        return new String(b, ENC);
    }
}
