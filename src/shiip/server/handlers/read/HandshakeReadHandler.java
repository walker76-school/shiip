/*******************************************************
 * Author: Andrew Walker
 * Assignment: Prog 6
 * Class: Data Comm
 *******************************************************/

package shiip.server.handlers.read;

import shiip.serialization.BadAttributeException;
import shiip.serialization.Settings;
import shiip.serialization.Window_Update;
import shiip.server.handlers.write.SetupWriteHandler;
import shiip.server.models.ClientConnectionContext;
import shiip.server.utils.ServerUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import static shiip.server.models.ServerConstants.*;

/**
 * ReadHandler for the handshake message
 */
public class HandshakeReadHandler extends ReadHandler {

    private final ByteBuffer localBuffer;

    /**
     * Constructs a HandshakeReadHandler from a client context and logger
     * @param connectionContext client context
     * @param logger logger
     */
    public HandshakeReadHandler(ClientConnectionContext connectionContext, Logger logger) {
        super(connectionContext, logger);
        this.localBuffer = ByteBuffer.allocate(HANDSHAKE_LENGTH);
    }

    @Override
    protected void handleRead(ByteBuffer buffer, int bytesRead) throws BadAttributeException {
        localBuffer.put(buffer.array());

        if(localBuffer.position() == HANDSHAKE_LENGTH){
            // Read the handshake message
            byte[] handshake = localBuffer.array();

            // Check the handshake message
            String handshakeMessage = ServerUtils.b2s(handshake);
            if(!handshakeMessage.equals(HANDSHAKE_MESSAGE)){
                logger.log(Level.WARNING, "Bad preface: " + handshakeMessage);
                fail(); // Kill connection
                return;
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
}
