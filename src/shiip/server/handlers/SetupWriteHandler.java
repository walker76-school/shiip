package shiip.server.handlers;

import shiip.server.models.ClientConnectionContext;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SetupWriteHandler extends WriteHandler {

    private static final int MAXIMUM_LENGTH = 16393;

    public SetupWriteHandler(ClientConnectionContext context, Logger logger) {
        super(context, logger);
        logger.log(Level.INFO, "new SetupWriteHandler");
    }

    @Override
    protected void handleWriteCompleted() {
        System.out.println( "SetupWriteHandler completed - " + context.getBufferedMessages().size() + " remaining buffered messages");
        ByteBuffer buffer = ByteBuffer.allocate(MAXIMUM_LENGTH);
        context.getClntSock().read(buffer, buffer, new MessageReadHandler(context, logger));
    }
}
