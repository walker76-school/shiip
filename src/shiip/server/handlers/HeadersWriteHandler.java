package shiip.server.handlers;

import shiip.serialization.BadAttributeException;
import shiip.serialization.Data;
import shiip.server.ServerAIO;
import shiip.server.models.ClientConnectionContext;
import shiip.server.models.FileContext;
import shiip.server.models.HeadersState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeadersWriteHandler extends WriteHandler {

    private FileContext fileContext;
    private HeadersState state;

    public HeadersWriteHandler(ClientConnectionContext connectionContext, HeadersState state, FileContext fileContext, Logger logger) {
        super(connectionContext, logger);
        logger.log(Level.INFO, "new HeadersWriteHandler");
        this.state = state;
        this.fileContext = fileContext;
    }

    @Override
    protected void handleWriteCompleted() throws IOException, BadAttributeException {
        logger.log(Level.INFO, "HeadersWriteHandler " + fileContext.getStreamID() + " completed");
        if(this.state.equals(HeadersState.GOOD)) {
            context.addStream(fileContext.getStreamID(), fileContext.getStream());
            if (context.getStreamIDs().size() == 1) { // The first headers
//                byte[] fileBuffer = new byte[ServerAIO.MAXDATASIZE];
//                int bytesRead = fileContext.getStream().read(fileBuffer);
//                Data data = new Data(fileContext.getStreamID(), true, Arrays.copyOfRange(fileBuffer, 0, bytesRead));
//                byte[] encodedMessage = context.getFramer().putFrame(data.encode(null));
//                ByteBuffer buffer = ByteBuffer.wrap(encodedMessage);
                ByteBuffer buffer = ByteBuffer.allocate(0);
                context.getClntSock().write(buffer, buffer, new FileWriteHandler(context, logger));
            }
        }
    }
}
