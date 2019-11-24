/*******************************************************
 * Author: Andrew Walker
 * Assignment: Prog 6
 * Class: Data Comm
 *******************************************************/

package shiip.server.models;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import shiip.serialization.NIODeframer;
import shiip.serialization.NIOFramer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Selector;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

/**
 * Context for a client connection
 */
public class ClientConnectionContext {

    // Table size for Encoder and Decoder
    private static final int MAX_TABLE_SIZE = 4096;

    private final String documentRoot;
    private final NIODeframer deframer;
    private final NIOFramer framer;
    private final Decoder decoder;
    private final Encoder encoder;
    private final AsynchronousSocketChannel clntSock;
    private final List<Integer> streamIDs;
    private final Map<Integer, FileInputStream> selector;
    private final Queue<ByteBuffer> queue;

    /**
     * Creates a client connection
     * @param documentRoot the root directory
     * @param clntSock the client socket
     */
    public ClientConnectionContext(String documentRoot, AsynchronousSocketChannel clntSock) {
        this.documentRoot = documentRoot;
        deframer = new NIODeframer();
        framer = new NIOFramer();
        decoder = new Decoder(MAX_TABLE_SIZE, MAX_TABLE_SIZE);
        encoder = new Encoder(MAX_TABLE_SIZE);
        this.clntSock = clntSock;
        streamIDs =  new ArrayList<>();
        selector = new ConcurrentHashMap<>();
        queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Returns the document root
     * @return the document root
     */
    public String getDocumentRoot() {
        return documentRoot;
    }

    /**
     * Returns the deframer
     * @return the deframer
     */
    public NIODeframer getDeframer() {
        return deframer;
    }

    /**
     * Returns the framer
     * @return the framer
     */
    public NIOFramer getFramer() {
        return framer;
    }

    /**
     * Returns the decoder
     * @return the decoder
     */
    public Decoder getDecoder() {
        return decoder;
    }

    /**
     * Returns the encoder
     * @return the encoder
     */
    public Encoder getEncoder() {
        return encoder;
    }

    /**
     * Returns the client socket
     * @return the client socket
     */
    public AsynchronousSocketChannel getClntSock() {
        return clntSock;
    }

    /**
     * Returns a map of streamID to FileInputStream
     * @return a map of streamID to FileInputStream
     */
    public Map<Integer, FileInputStream> getSelector() {
        return selector;
    }

    /**
     * Returns a list of stream IDs
     * @return a list of stream IDs
     */
    public List<Integer> getStreamIDs() {
        return new ArrayList<>(selector.keySet());
    }

    /**
     * Checks if the client has already used that stream ID
     * @param streamID the stream ID to check
     * @return if the client has already used that stream ID
     */
    public boolean containsStreamID(Integer streamID){
        return this.streamIDs.contains(streamID);
    }

    /**
     * Adds a new stream
     * @param streamID streamID
     * @param stream FileInputStream for that stream ID
     */
    public void addStream(Integer streamID, FileInputStream stream){
        this.selector.put(streamID, stream);
    }

    /**
     * Returns a queue of awaiting messages
     * @return a queue of awaiting messages
     */
    public Queue<ByteBuffer> getQueue() {
        return queue;
    }
}
