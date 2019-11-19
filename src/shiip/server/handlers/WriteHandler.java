package shiip.server.handlers;

import shiip.serialization.BadAttributeException;
import shiip.server.models.ClientConnectionContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class WriteHandler implements CompletionHandler<Integer, ByteBuffer> {

    private static final int MAXIMUM_LENGTH = 16393;

    protected ClientConnectionContext context;
    protected Logger logger;

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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BadAttributeException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void failed(Throwable ex, ByteBuffer buf) {
        try {
            context.getClntSock().close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Close Failed", e);
        }
    }

    protected abstract void handleWriteCompleted() throws IOException, BadAttributeException;
}
