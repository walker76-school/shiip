package shiip.server.handlers;


import shiip.server.models.ClientConnectionContext;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {

    // Size of the Handshake buffer
    private static final int BUFSIZE = 1;

    private AsynchronousServerSocketChannel listenChannel;
    private Logger logger;

    public ConnectionHandler(AsynchronousServerSocketChannel listenChannel, Logger logger) {
        this.listenChannel = listenChannel;
        this.logger = logger;
    }

    @Override
    public void completed(AsynchronousSocketChannel clntChan, Void attachment) {
        listenChannel.accept(null, this);
        handleAccept(clntChan);
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
    public void handleAccept(final AsynchronousSocketChannel clntChan) {
        ByteBuffer buf = ByteBuffer.allocateDirect(BUFSIZE);

        // Create Connection Context
        ClientConnectionContext connectionContext = new ClientConnectionContext(clntChan);

        clntChan.read(buf, buf, new HandshakeHandler(connectionContext, logger));
    }
}
