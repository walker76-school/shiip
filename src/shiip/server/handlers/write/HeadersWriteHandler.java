/*******************************************************
 * Author: Andrew Walker
 * Assignment: Prog 6
 * Class: Data Comm
 *******************************************************/

package shiip.server.handlers.write;

import shiip.server.models.ClientConnectionContext;
import shiip.server.models.FileContext;
import shiip.server.models.HeadersState;

import java.nio.ByteBuffer;
import java.nio.channels.WritePendingException;
import java.util.logging.Logger;

/**
 * A write handler for writing Headers
 */
public class HeadersWriteHandler extends WriteHandler {

    private FileContext fileContext;
    private HeadersState state;

    /**
     * Constructs a HeadersWriteHandler from a given context, logger and file
     * @param connectionContext the client context
     * @param state the state of the headers
     * @param fileContext the file context attached to a stream
     * @param logger logger
     */
    public HeadersWriteHandler(ClientConnectionContext connectionContext, HeadersState state, FileContext fileContext, Logger logger) {
        super(connectionContext, logger);
        this.state = state;
        this.fileContext = fileContext;
    }

    @Override
    protected void handleWriteCompleted() {
        if(this.state.equals(HeadersState.GOOD)) {
            context.addStream(fileContext.getStreamID(), fileContext.getStream());
            ByteBuffer buffer = ByteBuffer.allocate(0);
            try {
                context.getClntSock().write(buffer, buffer, new FileWriteHandler(context, logger));
            } catch (WritePendingException e){
                // To kickstart the loop if broken
            }
        }
    }
}
