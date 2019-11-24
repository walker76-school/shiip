/*******************************************************
 * Author: Andrew Walker
 * Assignment: Prog 6
 * Class: Data Comm
 *******************************************************/

package shiip.server.handlers.read;

import shiip.serialization.BadAttributeException;
import shiip.server.models.ClientConnectionContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic ReadHandler for reading from a client
 */
public abstract class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

    protected final ClientConnectionContext connectionContext;
    protected final Logger logger;

    /**
     * Constructs a generic ReadHandler from a client context and logger
     * @param connectionContext client context
     * @param logger logger
     */
    public ReadHandler(ClientConnectionContext connectionContext, Logger logger) {
        this.connectionContext = connectionContext;
        this.logger = logger;
    }

    @Override
    public void completed(Integer bytesRead, ByteBuffer buf) {
        try {
            handleRead(buf, bytesRead);
        } catch (BadAttributeException e) {
            failed(e, null);
        }
    }

    @Override
    public void failed(Throwable ex, ByteBuffer v) {
        logger.log(Level.INFO, ex.getMessage());
        fail();
    }

    /**
     * Internal fail to close the socket
     */
    protected void fail(){
        try {
            connectionContext.getClntSock().close();
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage());
        }
    }

    /**
     * Handles next actions after a read is completed
     * @param buf the bytes read
     * @param bytesRead number of bytes read
     * @throws BadAttributeException if invalid message
     */
    protected abstract void handleRead(ByteBuffer buf, int bytesRead) throws BadAttributeException;
}
