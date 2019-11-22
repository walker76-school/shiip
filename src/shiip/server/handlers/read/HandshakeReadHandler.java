package shiip.server.handlers.read;

import shiip.serialization.BadAttributeException;
import shiip.serialization.Settings;
import shiip.serialization.Window_Update;
import shiip.server.handlers.write.SetupWriteHandler;
import shiip.server.models.ClientConnectionContext;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HandshakeReadHandler extends ReadHandler {

    // Initial HTTP handshake message
    private static final String HANDSHAKE_MESSAGE = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";

    // StreamID for the connection
    private static final int CONNECTION_STREAMID = 0;

    // Maximum sized payload for a frame
    private static final int MAX_INCREMENT = 16384;

    // Encoding for handshake message
    private static final Charset ENC = StandardCharsets.US_ASCII;

    private final ByteBuffer localBuffer;

    public HandshakeReadHandler(ClientConnectionContext connectionContext, Logger logger) {
        super(connectionContext, logger);
        this.localBuffer = ByteBuffer.allocate(HANDSHAKE_MESSAGE.getBytes(ENC).length);
    }

    @Override
    protected void handleRead(ByteBuffer buffer, int bytesRead) throws BadAttributeException {
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

            connectionContext.getClntSock().write(newBuffer, newBuffer, new SetupWriteHandler(connectionContext, logger));

        } else {
            buffer.clear();
            connectionContext.getClntSock().read(buffer, buffer, this);
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
