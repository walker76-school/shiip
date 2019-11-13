package shiip.server.handlers;

import shiip.serialization.BadAttributeException;
import shiip.server.models.ClientConnectionContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

    // Key for finding the path
    private static final String PATH_KEY = ":path";

    // Key for finding the status
    private static final String STATUS_KEY = ":status";

    protected final ClientConnectionContext connectionContext;
    protected final Logger logger;

    public ReadHandler(ClientConnectionContext connectionContext, Logger logger) {
        logger.log(Level.INFO, "new ReadHandler");
        this.connectionContext = connectionContext;
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
            connectionContext.getClntSock().close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Close Failed", e);
        }
    }

    protected abstract void handleRead(ByteBuffer buf, int bytesRead) throws BadAttributeException;
}
