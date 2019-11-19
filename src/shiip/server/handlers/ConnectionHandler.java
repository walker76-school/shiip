package shiip.server.handlers;

import shiip.server.models.ClientConnectionContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {

    // Size of the Handshake buffer
    private static final int BUFSIZE = 24;

    private AsynchronousServerSocketChannel listenChannel;
    private final String documentRoot;
    private Logger logger;

    public ConnectionHandler(AsynchronousServerSocketChannel listenChannel, String documentRoot, Logger logger) {
        logger.log(Level.INFO, "new ConnectionHandler");
        this.listenChannel = listenChannel;
        this.documentRoot = documentRoot;
        this.logger = logger;
    }

    @Override
    public void completed(AsynchronousSocketChannel clntChan, Void attachment) {
        listenChannel.accept(null, this);
        try {
            handleAccept(clntChan);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable e, Void attachment) {
        logger.log(Level.WARNING, "Close Failed", e);
    }

    /**
     * Called after each accept completion
     *
     * @param clntChan channel of new client
     */
    public void handleAccept(final AsynchronousSocketChannel clntChan) throws IOException {
        logger.log(Level.INFO, "ConnectionHandler - handleAccept");

        // Create Connection Context
        ClientConnectionContext connectionContext = new ClientConnectionContext(documentRoot, clntChan);

        ByteBuffer newBuffer = ByteBuffer.allocate(BUFSIZE);
        clntChan.read(newBuffer, newBuffer, new HandshakeHandler(connectionContext, logger));
    }
}
