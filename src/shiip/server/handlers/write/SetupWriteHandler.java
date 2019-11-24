/*******************************************************
 * Author: Andrew Walker
 * Assignment: Prog 6
 * Class: Data Comm
 *******************************************************/

package shiip.server.handlers.write;

import shiip.server.handlers.read.MessageReadHandler;
import shiip.server.models.ClientConnectionContext;
import shiip.server.models.ServerConstants;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Handler for writing the required setup objects
 */
public class SetupWriteHandler extends WriteHandler {

    /**
     * Constructs a SetupWriteHandler from a context and logger
     * @param context client context
     * @param logger logger
     */
    public SetupWriteHandler(ClientConnectionContext context, Logger logger) {
        super(context, logger);
    }

    @Override
    protected void handleWriteCompleted() {
        ByteBuffer buffer = ByteBuffer.allocate(ServerConstants.MAXIMUM_LENGTH);
        context.getClntSock().read(buffer, buffer, new MessageReadHandler(context, logger));
    }
}
