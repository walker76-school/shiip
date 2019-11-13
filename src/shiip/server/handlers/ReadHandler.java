package shiip.server.handlers;

import com.twitter.hpack.Decoder;
import shiip.serialization.BadAttributeException;
import shiip.serialization.Constants;
import shiip.serialization.Headers;
import shiip.serialization.Message;
import shiip.server.models.ClientConnectionContext;
import shiip.server.models.FileContext;
import shiip.server.models.WriteState;
import shiip.server.protocols.ShiipDataProtocol;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

    // Key for finding the path
    private static final String PATH_KEY = ":path";

    // Key for finding the status
    private static final String STATUS_KEY = ":status";

    private final ClientConnectionContext context;
    private final Logger logger;

    public ReadHandler(ClientConnectionContext connectionContext, Logger logger) {
        logger.log(Level.INFO, "new ReadHandler");
        this.context = connectionContext;
        this.logger = logger;
    }

    @Override
    public void completed(Integer bytesRead, ByteBuffer buf) {
        try {
            handleRead(buf, bytesRead);
        } catch (BadAttributeException e) {
            logger.log(Level.WARNING, "Handle Read Failed", e);
        }
    }

    @Override
    public void failed(Throwable ex, ByteBuffer v) {
        logger.log(Level.INFO, "Client closed connection");
        try {
            context.getClntSock().close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Close Failed", e);
        }
    }

    private void handleRead(ByteBuffer buf, int bytesRead) throws BadAttributeException {
        if (bytesRead == -1) { // Did the other end close?
            failed(null, buf);
        } else if (bytesRead > 0) {

            boolean moreFrames = true;

            // Load the bytes we did read
            byte[] msgBytes = Arrays.copyOfRange(buf.array(), 0, bytesRead);
            context.getDeframer().feed(msgBytes);

            while(moreFrames) {
                byte[] encodedMessage = context.getDeframer().getFrame();

                if (encodedMessage != null) {
                    Message message = getMessage(encodedMessage, context.getDecoder());
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
                    moreFrames = false;
                }
            }

            buf.clear();
            context.getClntSock().read(buf, buf, new ReadHandler(context, logger));

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
    private void handleHeaders(Message m, ClientConnectionContext connectionContext) throws BadAttributeException {
        logger.log(Level.INFO, "Received message: " + m);

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
            sendHeaders(headers, WriteState.BAD_HEADERS);
            return; // Terminate stream
        }

        String filePath = connectionContext.getDocumentRoot().concat(path);
        File file = new File(filePath);

        // Directory
        if(file.isDirectory()){
            logger.log(Level.WARNING, "Cannot request directory");

            // Send headers
            Headers headers = new Headers(streamID, false);
            headers.addValue(STATUS_KEY, "404 Cannot request directory");
            sendHeaders(headers, WriteState.BAD_HEADERS);
            return; // Terminate stream
        }

        // Non-existent/No permission file
        if(!file.exists() || !Files.isReadable(Paths.get(filePath))){
            logger.log(Level.WARNING, "File not found");

            // Send headers
            Headers headers = new Headers(streamID, false);
            headers.addValue(STATUS_KEY, "404 File not found");
            sendHeaders(headers, WriteState.BAD_HEADERS);
            return; // Terminate stream
        }

        // Record the streamID
        connectionContext.addStream(streamID);

        FileContext fileContext = buildFileContext(streamID, filePath);

        // Send good headers
        Headers headers = new Headers(streamID, false);
        headers.addValue(STATUS_KEY, "200 OK");
        sendHeaders(headers, fileContext, WriteState.HEADERS);
    }

    private FileContext buildFileContext(int streamID, String filePath) {
        try{
            Path path = Paths.get(filePath);
            AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            return new FileContext(streamID, channel, path);
        } catch (Exception e){
            // Do nothing
        }
        return null;
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

    private void sendHeaders(Headers headers, WriteState state){
        sendHeaders(headers, null, state);
    }

    private void sendHeaders(Headers headers, FileContext fileContext, WriteState state){
        ByteBuffer buffer = ByteBuffer.wrap(context.getFramer().putFrame(headers.encode(context.getEncoder())));
        context.getClntSock().write(buffer, buffer, new WriteHandler(context, state, fileContext, logger));
    }
}
