package shiip.server.handlers.read;

import shiip.serialization.BadAttributeException;
import shiip.server.models.ClientConnectionContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

    protected final ClientConnectionContext connectionContext;
    protected final Logger logger;

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

    protected void fail(){
        try {
            connectionContext.getClntSock().close();
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage());
        }
    }

    protected abstract void handleRead(ByteBuffer buf, int bytesRead) throws BadAttributeException;
}
