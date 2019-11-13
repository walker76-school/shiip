package shiip.server.handlers;

import shiip.server.models.ClientConnectionContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BadHeadersWriteHandler extends WriteHandler {

    public BadHeadersWriteHandler(ClientConnectionContext context, Logger logger) {
        super(context, logger);
        logger.log(Level.INFO, "newBadHeadersWriteHandler");
    }

    @Override
    protected void handleWriteCompleted() {
        logger.log(Level.INFO, "BadHeadersWriteHandler completed - " + context.getBufferedMessages().size() + " remaining buffered messages");
        // I don't need to do anything when there's a bad Headers
    }
}
