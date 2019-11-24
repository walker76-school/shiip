/*******************************************************
 * Author: Andrew Walker
 * Assignment: Prog 6
 * Class: Data Comm
 *******************************************************/

package shiip.server.handlers.write;

import shiip.serialization.BadAttributeException;
import shiip.server.models.ClientConnectionContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A generic write handler
 */
public abstract class WriteHandler implements CompletionHandler<Integer, ByteBuffer> {

    protected ClientConnectionContext context;
    protected Logger logger;

    /**
     * Constructs a WriteHandler from a given client context and logger
     * @param context the client context
     * @param logger logger
     */
    public WriteHandler(ClientConnectionContext context, Logger logger) {
        this.context = context;
        this.logger = logger;
    }

    @Override
    public void completed(Integer bytesWritten, ByteBuffer buf) {
        if (buf.hasRemaining()) { // More to write
            context.getClntSock().write(buf, buf, this);
        } else { // Back to reading
            buf.clear();
            try {
                handleWriteCompleted();
            } catch (IOException | BadAttributeException e) {
                failed(e, null);
            }
        }
    }

    @Override
    public void failed(Throwable ex, ByteBuffer buf) {
        try {
            logger.log(Level.WARNING, ex.getMessage());
            context.getClntSock().close();
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage());
        }
    }

    /**
     * Handles the next actions when write is completed
     * @throws IOException if communication problem
     * @throws BadAttributeException if invalid messages
     */
    protected abstract void handleWriteCompleted() throws IOException, BadAttributeException;
}
