/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 3
 * Class: Data Comm
 *******************************************************/

package shiip.server;

import shiip.serialization.*;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static shiip.server.Server.MAXDATASIZE;
import static shiip.server.Server.MINDATAINTERVAL;

/**
 * Protocol for serving a file
 *
 * @author Andrew Walker
 */
public class ShiipDataProtocol implements Runnable {

    private final Framer framer;
    private final Logger logger;
    private final int streamID;
    private final String filePath;

    /**
     * Constructor for a new Data Protocol
     * @param framer the Framer for sending frames
     * @param streamID the stream to serve the file on
     * @param filePath the file to server
     * @param logger logger
     */
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

            long currentTime = System.currentTimeMillis();
            while(in.read(buffer, 0, MAXDATASIZE) != -1){

                // Wait the full interval before sending
                while(System.currentTimeMillis() - currentTime < MINDATAINTERVAL){
                    // Do nothing but sleep
                    Thread.sleep(MINDATAINTERVAL);
                }

                // Send the Data frame
                Data data = new Data(streamID, false, buffer);
                framer.putFrame(data.encode(null));

                // Reset the waiting interval
                currentTime = System.currentTimeMillis();
            }

            // Send final data frame to show isEnd
            Data data = new Data(streamID, true, new byte[]{});
            framer.putFrame(data.encode(null));

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            // Stream is closed
        }
    }
}