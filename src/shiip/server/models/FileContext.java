package shiip.server.models;

import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;

public class FileContext {
    private int streamID;
    private int position;
    private FileReadState state;
    private AsynchronousFileChannel channel;
    private Path path;

    public FileContext(int streamID, AsynchronousFileChannel channel, Path path) {
        this.streamID = streamID;
        this.position = 0;
        this.state = FileReadState.READING;
        this.channel = channel;
        this.path = path;
    }

    public int getStreamID() {
        return streamID;
    }

    public int getPosition() {
        return position;
    }

    public void incrementPosition(int increment){
        position += increment;
    }

    public FileReadState getState() {
        return state;
    }

    public void setState(FileReadState state) {
        this.state = state;
    }

    public AsynchronousFileChannel getChannel() {
        return channel;
    }

    public Path getPath() {
        return path;
    }

}
