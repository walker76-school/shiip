package shiip.server.handlers;

import shiip.server.ServerAIO;
import shiip.server.models.ClientConnectionContext;
import shiip.server.models.FileContext;
import shiip.server.models.FileReadState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeadersWriteHandler extends WriteHandler {

    private FileContext fileContext;

    public HeadersWriteHandler(ClientConnectionContext connectionContext, FileContext fileContext, Logger logger) {
        super(connectionContext, logger);
        logger.log(Level.INFO, "new HeadersWriteHandler");
        this.fileContext = fileContext;
    }

    @Override
    protected synchronized void handleWriteCompleted() {
        logger.log(Level.INFO, "HeadersWriteHandler " + fileContext.getStreamID() + " completed - " + context.getBufferedMessages().size() + " remaining buffered messages");
        if (context.getBufferedMessages().size() > 0) {
            ByteBuffer msgBuffer = context.getBufferedMessages().remove(0);
            context.getClntSock().write(msgBuffer, msgBuffer, this);
        } else {
            ByteBuffer fileBuffer = ByteBuffer.allocate(ServerAIO.MAXDATASIZE);
            if (!fileContext.getState().equals(FileReadState.DONE)) {
                fileContext.getChannel().read(fileBuffer, fileContext.getPosition(), fileBuffer, new FileReadHandler(context, fileContext, logger));
            }
        }
    }
}
