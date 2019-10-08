package shiip.server;

import com.twitter.hpack.Decoder;
import shiip.serialization.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class ShiipServerProtocol extends ShiipProtocol {

    // Table size for Encoder and Decoder
    private static final int MAX_TABLE_SIZE = 4096;

    private static final int POOL_SIZE = 10;

    private final Socket clntSock;               // Socket connect to client
    private final Decoder decoder;
    private final ExecutorService pool;

    public ShiipServerProtocol(Socket clntSock) {
        this.clntSock = clntSock;
        decoder = new Decoder(MAX_TABLE_SIZE, MAX_TABLE_SIZE);
        pool = Executors.newFixedThreadPool(POOL_SIZE);
    }

    @Override
    public void run() {
        try (
            clntSock; // So the socket is auto-closable
            InputStream in = clntSock.getInputStream();
            OutputStream out = clntSock.getOutputStream();
        ) {

            Deframer deframer = new Deframer(in);
            Framer framer = new Framer(out);

            while (true) {
                Message m = getMessage(deframer, decoder);
                if (m != null) {
                    switch (m.getCode()) {
                        case Constants.DATA_TYPE:
                            handleData(m);
                            break;
                        case Constants.HEADERS_TYPE:
                            handleHeaders(m, framer, pool);
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
            // the client has disconnected

            getLogger().log(Level.WARNING, ex.getMessage());
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
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.WARNING, "Unable to parse: " + e.getMessage());
        } catch (BadAttributeException | NullPointerException e) {
            getLogger().log(Level.WARNING, "Invalid Message: " + e.getMessage());
        }

        return null;
    }

    /**
     * Handler for a Headers message
     *
     * @param m the Headers message
     */
    private void handleHeaders(Message m, Framer framer, ExecutorService pool) {
        Headers h = (Headers) m;
        pool.execute(new ShiipDataProtocol(framer, h.getStreamID(), h.getValue(":path")));
    }

    /**
     * Handler for a Data message
     *
     * @param m the Data message
     */
    private void handleData(Message m) {
        getLogger().log(Level.WARNING, "Invalid message: " + m);
    }

    /**
     * Handler for a Settings message
     *
     * @param m the Settings message
     */
    private void handleSettings(Message m) {
        getLogger().log(Level.INFO, "Received message: " + m);
    }

    /**
     * Handler for a Window_Update message
     *
     * @param m the Window_Update message
     */
    private void handleWindowUpdate(Message m) {
        getLogger().log(Level.INFO, "Received message: " + m);
    }
}
