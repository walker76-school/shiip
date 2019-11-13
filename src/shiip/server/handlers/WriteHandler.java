package shiip.server.handlers;

import shiip.server.ServerAIO;
import shiip.server.models.ClientConnectionContext;
import shiip.server.models.FileContext;
import shiip.server.models.WriteState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WriteHandler implements CompletionHandler<Integer, ByteBuffer> {

    private static final int MAXIMUM_LENGTH = 16393;

    private ClientConnectionContext context;
    private WriteState state;
    private FileContext fileContext;
    private Logger logger;

    public WriteHandler(ClientConnectionContext context, WriteState state, FileContext fileContext, Logger logger) {
        logger.log(Level.INFO, "new WriteHandler");
        this.context = context;
        this.state = state;
        this.fileContext = fileContext;
        this.logger = logger;
    }

    @Override
    public void completed(Integer bytesWritten, ByteBuffer buf) {
        try {
            handleWrite(context, buf);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error writing file " + e.getMessage());
        }
    }

    @Override
    public void failed(Throwable ex, ByteBuffer buf) {
        try {
            context.getClntSock().close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Close Failed", e);
        }
    }

    private void handleWrite(final ClientConnectionContext context, ByteBuffer buf) throws IOException {
        if (buf.hasRemaining()) { // More to write
            context.getClntSock().write(buf, buf, this);
        } else { // Back to reading
            buf.clear();
            switch (this.state){
                case SETUP:
                    ByteBuffer buffer = ByteBuffer.allocate(MAXIMUM_LENGTH);
                    context.getClntSock().read(buffer, buffer, new ReadHandler(context, logger));
                    break;
                case HEADERS:
                    ByteBuffer fileBuffer = ByteBuffer.allocate(ServerAIO.MAXDATASIZE);
                    fileContext.getChannel().read(fileBuffer, 0, fileBuffer, new FileReadHandler(context, fileContext, logger));
            }
        }
    }
}
