package shiip.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TCP Echo Server using Asynchronous I/O
 *
 * The main() creates a TCP server socket channel, sets up the socket including
 * binding and setting the accept completion handler, and non-busily waits (forever).
 *
 * @author Jeff Donahoo
 * @version 0.1
 */
public class BasicTCPEchoAIO {

    /**
     * Buffer size (bytes)
     */
    private static final int BUFSIZE = 256;
    /**
     * Global logger
     */
    private static final Logger logger = Logger.getLogger("Basic");

    public static void main(String[] args) throws IOException {
        if (args.length != 1) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Port>");
        }

        try (AsynchronousServerSocketChannel listenChannel = AsynchronousServerSocketChannel.open()) {
            // Bind local port
            listenChannel.bind(new InetSocketAddress(Integer.parseInt(args[0])));

            // Create accept handler
            listenChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

                @Override
                public void completed(AsynchronousSocketChannel clntChan, Void attachment) {
                    listenChannel.accept(null, this);
                    try {
                        handleAccept(clntChan);
                    } catch (IOException e) {
                        failed(e, null);
                    }
                }

                @Override
                public void failed(Throwable e, Void attachment) {
                    logger.log(Level.WARNING, "Close Failed", e);
                }
            });
            // Block until current thread dies
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Server Interrupted", e);
        }
    }

    /**
     * Called after each accept completion
     *
     * @param clntChan channel of new client
     * @throws IOException if I/O problem
     */
    public static void handleAccept(final AsynchronousSocketChannel clntChan) throws IOException {
        ByteBuffer buf = ByteBuffer.allocateDirect(BUFSIZE);
        clntChan.read(buf, buf, new CompletionHandler<Integer, ByteBuffer>() {
            public void completed(Integer bytesRead, ByteBuffer buf) {
                try {
                    handleRead(clntChan, buf, bytesRead);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Handle Read Failed", e);
                }
            }

            public void failed(Throwable ex, ByteBuffer v) {
                try {
                    clntChan.close();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Close Failed", e);
                }
            }
        });
    }

    /**
     * Called after each read completion
     *
     * @param clntChan channel of new client
     * @param buf byte buffer used in read
     * @throws IOException if I/O problem
     */
    public static void handleRead(final AsynchronousSocketChannel clntChan, ByteBuffer buf, int bytesRead)
            throws IOException {
        if (bytesRead == -1) { // Did the other end close?
            clntChan.close();
        } else if (bytesRead > 0) {
            buf.flip(); // prepare to write
            clntChan.write(buf, buf, new CompletionHandler<Integer, ByteBuffer>() {
                public void completed(Integer bytesWritten, ByteBuffer buf) {
                    try {
                        handleWrite(clntChan, buf);
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Handle Write Failed", e);
                    }
                }

                public void failed(Throwable ex, ByteBuffer buf) {
                    try {
                        clntChan.close();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Close Failed", e);
                    }
                }
            });
        }
    }

    /**
     * Called after each write
     *
     * @param clntChan channel of new client
     * @param buf byte buffer used in write
     * @throws IOException if I/O problem
     */
    public static void handleWrite(final AsynchronousSocketChannel clntChan, ByteBuffer buf) throws IOException {
        if (buf.hasRemaining()) { // More to write
            clntChan.write(buf, buf, new CompletionHandler<Integer, ByteBuffer>() {
                public void completed(Integer bytesWritten, ByteBuffer buf) {
                    try {
                        handleWrite(clntChan, buf);
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Handle Write Failed", e);
                    }
                }

                public void failed(Throwable ex, ByteBuffer buf) {
                    try {
                        clntChan.close();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Close Failed", e);
                    }
                }
            });
        } else { // Back to reading
            buf.clear();
            clntChan.read(buf, buf, new CompletionHandler<Integer, ByteBuffer>() {
                public void completed(Integer bytesRead, ByteBuffer buf) {
                    try {
                        handleRead(clntChan, buf, bytesRead);
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Handle Read Failed", e);
                    }
                }

                public void failed(Throwable ex, ByteBuffer v) {
                    try {
                        clntChan.close();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Close Failed", e);
                    }
                }
            });
        }
    }
}
