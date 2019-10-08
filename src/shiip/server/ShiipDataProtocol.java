package shiip.server;

import shiip.serialization.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ShiipDataProtocol extends ShiipProtocol {

    public static final int BUFFER_SIZE = 4096;

    private final Framer framer;
    private final int streamID;
    private final String filePath;

    public ShiipDataProtocol(Framer framer, Integer streamID, String filePath) {
        this.framer = framer;
        this.streamID = streamID;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            FileInputStream in = new FileInputStream(filePath);

            int read;
            while((read = in.readNBytes(buffer, 0, BUFFER_SIZE)) != -1){
                Data data = new Data(streamID, read < BUFFER_SIZE, buffer);
                framer.putFrame(data.encode(null));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadAttributeException e) {
            e.printStackTrace();
        }
    }
}