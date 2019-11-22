package shiip.server.handlers.write;

import shiip.server.models.ClientConnectionContext;
import shiip.server.models.FileContext;
import shiip.server.models.HeadersState;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class HeadersWriteHandler extends WriteHandler {

    private FileContext fileContext;
    private HeadersState state;

    public HeadersWriteHandler(ClientConnectionContext connectionContext, HeadersState state, FileContext fileContext, Logger logger) {
        super(connectionContext, logger);
        this.state = state;
        this.fileContext = fileContext;
    }

    @Override
    protected void handleWriteCompleted() {
        if(this.state.equals(HeadersState.GOOD)) {
            context.addStream(fileContext.getStreamID(), fileContext.getStream());
            if (context.getStreamIDs().size() == 1) { // The first headers
                ByteBuffer buffer = ByteBuffer.allocate(0);
                context.getClntSock().write(buffer, buffer, new FileWriteHandler(context, logger));
            }
        }
    }
}
