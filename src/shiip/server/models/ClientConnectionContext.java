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

    public ClientConnectionContext(String documentRoot, AsynchronousSocketChannel clntSock) throws IOException {
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

    public String getDocumentRoot() {
        return documentRoot;
    }

    public NIODeframer getDeframer() {
        return deframer;
    }

    public NIOFramer getFramer() {
        return framer;
    }

    public Decoder getDecoder() {
        return decoder;
    }

    public Encoder getEncoder() {
        return encoder;
    }

    public AsynchronousSocketChannel getClntSock() {
        return clntSock;
    }

    public Map<Integer, FileInputStream> getSelector() {
        return selector;
    }

    public List<Integer> getStreamIDs() {
        return new ArrayList<>(selector.keySet());
    }

    public boolean containsStreamID(Integer streamID){
        return this.streamIDs.contains(streamID);
    }

    public void addStream(Integer streamID, FileInputStream stream){
        this.selector.put(streamID, stream);
    }

    public Queue<ByteBuffer> getQueue() {
        return queue;
    }
}
