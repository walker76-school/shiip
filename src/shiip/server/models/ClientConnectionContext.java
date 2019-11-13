package shiip.server.models;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import shiip.serialization.NIODeframer;
import shiip.serialization.NIOFramer;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;

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

    public ClientConnectionContext(String documentRoot, AsynchronousSocketChannel clntSock) {
        this.documentRoot = documentRoot;
        deframer = new NIODeframer();
        framer = new NIOFramer();
        decoder = new Decoder(MAX_TABLE_SIZE, MAX_TABLE_SIZE);
        encoder = new Encoder(MAX_TABLE_SIZE);
        this.clntSock = clntSock;
        streamIDs =  new ArrayList<>();
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

    public List<Integer> getStreamIDs() {
        return streamIDs;
    }

    public boolean containsStreamID(Integer streamID){
        return this.streamIDs.contains(streamID);
    }

    public void addStream(Integer streamID){
        this.streamIDs.add(streamID);
    }
}
