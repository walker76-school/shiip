package shiip.server;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import shiip.serialization.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShiipServerProtocol implements Runnable {

    // Table size for Encoder and Decoder
    private static final int MAX_TABLE_SIZE = 4096;

    private Socket clntSock;               // Socket connect to client
    private Logger logger;                 // Server logger
    private Decoder decoder;
    private Encoder encoder;

    public ShiipServerProtocol(Socket clntSock, Logger logger) {
        this.clntSock = clntSock;
        this.logger = logger;
        this.decoder = new Decoder(MAX_TABLE_SIZE, MAX_TABLE_SIZE);
        this.encoder = new Encoder(MAX_TABLE_SIZE);
    }

    public void run() {
        handleEchoClient(clntSock, logger);
    }

    private void handleEchoClient(Socket clntSock, Logger logger) {
        try (clntSock) {
            // Get the input and output I/O streams from socket
            InputStream in = clntSock.getInputStream();
            Deframer deframer = new Deframer(in);
            OutputStream out = clntSock.getOutputStream();
            Framer framer = new Framer(out);
            Map<Integer, FileInputStream> ongoingDownloads = new TreeMap<>();

            if(in.available() > 0) {
                Message m = getMessage(deframer, this.decoder);
                if (m != null) {
                    switch (m.getCode()) {
                        case Constants.DATA_TYPE:
                            handleData(m);
                            break;
                        case Constants.HEADERS_TYPE:
                            handleHeaders(m, ongoingDownloads);
                            break;
                        case Constants.SETTINGS_TYPE:
                            handleSettings(m);
                            break;
                        case Constants.WINDOW_UPDATE_TYPE:
                            handleWindowUpdate(m);
                            break;
                    }
                }
            } else {

            }

        } catch (IOException ex) {
            logger.log(Level.WARNING, "Exception in echo protocol", ex);
        }
    }

    /**
     * Retrieves the next message from the server
     * @return the next message from the server
     */
    private Message getMessage(Deframer deframer, Decoder decoder){
        try {
            byte[] framedBytes = deframer.getFrame();
            return Message.decode(framedBytes, decoder);
        } catch (IOException | IllegalArgumentException e){
            logger.log(Level.WARNING, "Unable to parse: " + e.getMessage());
            return null;
        } catch (BadAttributeException | NullPointerException e){
            logger.log(Level.WARNING, "Invalid Message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handler for a Headers message
     * @param m the Headers message
     * @param ongoingDownloads a map of ongoing file downloads
     */
    private void handleHeaders(Message m, Map<Integer, FileInputStream> ongoingDownloads) {
        Headers h = (Headers) m;

    }

    /**
     * Handler for a Data message
     * @param m the Data message
     */
    private void handleData(Message m){
        logger.log(Level.WARNING, "Invalid message: " + m);
    }

    /**
     * Handler for a Settings message
     * @param m the Settings message
     */
    private void handleSettings(Message m) {
        logger.log(Level.INFO, "Received message: " + m);
    }

    /**
     * Handler for a Window_Update message
     * @param m the Window_Update message
     */
    private void handleWindowUpdate(Message m) {
        logger.log(Level.INFO, "Received message: " + m);
    }
}