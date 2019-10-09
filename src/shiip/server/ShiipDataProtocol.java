package shiip.server;

import shiip.serialization.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static shiip.server.Server.MAXDATASIZE;
import static shiip.server.Server.MINDATAINTERVAL;

public class ShiipDataProtocol implements Runnable {

    // String to extract the path from Headers
    private static final String PATH_KEY = ":path";

    private final Framer framer;
    private final Logger logger;
    private final int streamID;
    private final String filePath;

    public ShiipDataProtocol(Framer framer, Integer streamID, String filePath, Logger logger) {
        this.framer = framer;
        this.logger = logger;
        this.streamID = streamID;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try {

            byte[] buffer = new byte[MAXDATASIZE];
            FileInputStream in = new FileInputStream(filePath);

            int read;
            long currentTime = System.currentTimeMillis();
            boolean sentIsEnd = false;
            while((read = in.readNBytes(buffer, 0, MAXDATASIZE)) != -1){

                while(System.currentTimeMillis() - currentTime < MINDATAINTERVAL){
                    // Do nothing but sleep
                    Thread.sleep(MINDATAINTERVAL);
                }

                if(read < MAXDATASIZE){
                    sentIsEnd = true;
                }

                Data data = new Data(streamID, read < MAXDATASIZE, buffer);
                framer.putFrame(data.encode(null));
                currentTime = System.currentTimeMillis();
            }

            // If we haven't sent the end of the data because the length of the
            // file was evenly divisible by MAXDATASIZE then send a Data isEnd
            if(!sentIsEnd){
                Data data = new Data(streamID, true, new byte[]{});
                framer.putFrame(data.encode(null));
            }

        } catch (IOException | BadAttributeException | InterruptedException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }
}