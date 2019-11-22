package shiip.server.handlers.write;

import shiip.server.handlers.read.MessageReadHandler;
import shiip.server.models.ClientConnectionContext;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class SetupWriteHandler extends WriteHandler {

    private static final int MAXIMUM_LENGTH = 16393;

    public SetupWriteHandler(ClientConnectionContext context, Logger logger) {
        super(context, logger);
    }

    @Override
    protected void handleWriteCompleted() {
        ByteBuffer buffer = ByteBuffer.allocate(MAXIMUM_LENGTH);
        context.getClntSock().read(buffer, buffer, new MessageReadHandler(context, logger));
    }
}
