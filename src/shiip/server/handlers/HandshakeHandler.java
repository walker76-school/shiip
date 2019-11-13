package shiip.server.handlers;

import shiip.serialization.BadAttributeException;
import shiip.serialization.Settings;
import shiip.serialization.Window_Update;
import shiip.server.models.ClientConnectionContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HandshakeHandler implements CompletionHandler<Integer, ByteBuffer> {

    // Initial HTTP handshake message
    private static final String HANDSHAKE_MESSAGE = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";

    // StreamID for the connection
    private static final int CONNECTION_STREAMID = 0;

    // Maximum sized payload for a frame
    private static final int MAX_INCREMENT = 16384;

    // Encoding for handshake message
    private static final Charset ENC = StandardCharsets.US_ASCII;

    private final ClientConnectionContext context;
    private final Logger logger;
    private final ByteBuffer localBuffer;

    public HandshakeHandler(ClientConnectionContext connectionContext, Logger logger) {
        logger.log(Level.INFO, "new HandshakeHandler");
        this.context = connectionContext;
        this.logger = logger;
        this.localBuffer = ByteBuffer.allocate(HANDSHAKE_MESSAGE.getBytes(ENC).length);
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
        try {
            context.getClntSock().close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Close Failed", e);
        }
    }

    private void handleRead(ByteBuffer buffer, int bytesRead) throws BadAttributeException {
        logger.log(Level.INFO, "HandshakeHandler - handleRead");
        localBuffer.put(buffer.array());

        if(localBuffer.position() == HANDSHAKE_MESSAGE.getBytes(ENC).length){
            // Read the handshake message
            byte[] handshake = localBuffer.array();

            // Check the handshake message
            String handshakeMessage = b2s(handshake);
            if(!handshakeMessage.equals(HANDSHAKE_MESSAGE)){
                logger.log(Level.WARNING, "Bad preface: " + handshakeMessage);
                return; // Kill connection
            }

            // Send required data
            Settings settings = new Settings();
            Window_Update wu = new Window_Update(CONNECTION_STREAMID, MAX_INCREMENT);
            byte[] settingsEncoded = settings.encode(null);
            byte[] wuEncoded = wu.encode(null);

            ByteBuffer newBuffer = ByteBuffer.allocate(settingsEncoded.length + wuEncoded.length);
            newBuffer.put(settingsEncoded);
            newBuffer.put(wuEncoded);

            context.getClntSock().write(newBuffer, newBuffer, new SetupWriteHandler(context, logger));

        } else {
            buffer.clear();
            context.getClntSock().read(buffer, buffer, this);
        }
    }

    /**
     * Turns a byte array into a string representation
     * @param b the byte array to transform
     * @return a string representation of the byte array
     */
    private String b2s(byte[] b) {
        return new String(b, ENC);
    }
}
