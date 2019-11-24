/*******************************************************
 * Author: Andrew Walker
 * Assignment: Prog 6
 * Class: Data Comm
 *******************************************************/

package shiip.server.models;

import java.io.FileInputStream;

/**
 * Context about a particular file that must be streamed
 */
public class FileContext {

    private int streamID;
    private FileInputStream stream;

    /**
     * Constructs a FileContext from a given stream and file stream
     * @param streamID stream ID
     * @param stream file input stream
     */
    public FileContext(int streamID, FileInputStream stream) {
        this.streamID = streamID;
        this.stream = stream;
    }

    /**
     * Returns the stream ID
     * @return stream ID
     */
    public int getStreamID() {
        return streamID;
    }

    /**
     * Returns the file input stream
     * @return the file input stream
     */
    public FileInputStream getStream() {
        return stream;
    }
}
