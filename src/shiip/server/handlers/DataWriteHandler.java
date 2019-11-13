package shiip.server.handlers;

import shiip.server.ServerAIO;
import shiip.server.models.ClientConnectionContext;
import shiip.server.models.FileContext;
import shiip.server.models.FileReadState;
import shiip.server.models.WriteState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataWriteHandler implements CompletionHandler<Integer, ByteBuffer> {

    private ClientConnectionContext context;
    private FileContext fileContext;
    private Logger logger;

    public DataWriteHandler(ClientConnectionContext context, FileContext fileContext, Logger logger) {
        this.context = context;
        this.fileContext = fileContext;
        this.logger = logger;
    }

    @Override
    public void completed(Integer bytesWritten, ByteBuffer buf) {
        handleWrite(context, buf);
    }

    @Override
    public void failed(Throwable ex, ByteBuffer buf) {
        try {
            context.getClntSock().close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Close Failed", e);
        }
    }

    private void handleWrite(final ClientConnectionContext context, ByteBuffer buf) {
        if (buf.hasRemaining()) { // More to write
            context.getClntSock().write(buf, buf, this);
        } else {
            ByteBuffer fileBuffer = ByteBuffer.allocate(ServerAIO.MAXDATASIZE);
            if(!fileContext.getState().equals(FileReadState.DONE)) {
                fileContext.getChannel().read(fileBuffer, fileContext.getPosition(), fileBuffer, new FileReadHandler(context, fileContext, logger));
            }
        }
    }
}
