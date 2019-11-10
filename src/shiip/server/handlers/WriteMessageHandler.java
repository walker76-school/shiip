package shiip.server.handlers;

import shiip.serialization.Message;
import shiip.server.ServerAIO;
import shiip.server.models.ClientConnectionContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WriteMessageHandler implements CompletionHandler<Integer, ByteBuffer> {

    private ClientConnectionContext context;
    private Logger logger;

    public WriteMessageHandler(ClientConnectionContext context, Message toSend, CompletionHandler nextHandler, Logger logger) {
        this.context = context;
        this.logger = logger;
    }

    @Override
    public void completed(Integer bytesWritten, ByteBuffer buf) {
        try {
            handleWrite(context, buf);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Handle Write Failed", e);
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
            context.getClntSock().read(buf, buf, new ServerAIO.ReadHandler(clntChan, logger));
        }
    }
}
