/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 3
 * Class: Data Comm
 *******************************************************/

package shiip.server.protocols;

import shiip.serialization.Framer;
import shiip.serialization.Data;

import java.io.FileInputStream;
import java.util.Arrays;
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

    // Framer for sending frames
    private final Framer framer;

    // Logger for logging info and errors
    private final Logger logger;

    // streamID to send frames on
    private final int streamID;

    // The file to send
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

            // Continue to read the data
            int numRead;
            while((numRead = in.read(buffer, 0, MAXDATASIZE)) != -1){

                // Send the Data frame
                Data data = new Data(streamID, false, buffer);
                framer.putFrame(data.encode(null));

                // Sleep for the interval
                Thread.sleep(MINDATAINTERVAL);
            }

            // Send final data frame to show isEnd
            Data data = new Data(streamID, true, new byte[]{});
            framer.putFrame(data.encode(null));

        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
            // Stream is closed
        }
    }
}