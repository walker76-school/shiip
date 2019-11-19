package shiip.server.models;

import java.io.FileInputStream;

public class FileContext {
    private int streamID;
    private FileInputStream stream;

    public FileContext(int streamID, FileInputStream stream) {
        this.streamID = streamID;
        this.stream = stream;
    }

    public int getStreamID() {
        return streamID;
    }

    public FileInputStream getStream() {
        return stream;
    }
}
