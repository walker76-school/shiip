/*******************************************************
 * Author: Andrew Walker
 * Assignment: Prog 6
 * Class: Data Comm
 *******************************************************/

package shiip.server.handlers.write;

import shiip.serialization.BadAttributeException;
import shiip.serialization.Data;
import shiip.server.ServerAIO;
import shiip.server.models.ClientConnectionContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritePendingException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * A write handler for writing data frames
 */
public class FileWriteHandler extends WriteHandler {

    private static Random rand = new Random();

    private int toRemove;

    /**
     * Constructs a FileWriteHandler from a given context and logger
     * @param context client context
     * @param logger logger
     */
    public FileWriteHandler(ClientConnectionContext context, Logger logger) {
        super(context, logger);
        toRemove = -1;
    }

    @Override
    protected void handleWriteCompleted() throws IOException, BadAttributeException {

        if(toRemove != -1){
            context.getSelector().remove(toRemove);
            toRemove = -1;
        }

        if(!context.getQueue().isEmpty()){
            ByteBuffer buffer = context.getQueue().remove();
            context.getClntSock().write(buffer, buffer, this);
        } else if (context.getStreamIDs().size() > 0) {
            int streamID = -1;
            while(streamID == -1){
                try {
                    List<Integer> streamIDs = context.getStreamIDs();
                    streamID = streamIDs.get(rand.nextInt(streamIDs.size()));
                } catch (IndexOutOfBoundsException e){
                    streamID = -1;
                }
            }

            InputStream fileInputStream = context.getSelector().get(streamID);
            byte[] fileBuffer = new byte[ServerAIO.MAXDATASIZE];
            int bytesRead = fileInputStream.read(fileBuffer);
            Data data;
            if (bytesRead == -1) {
                data = new Data(streamID, true, new byte[]{});
                toRemove = streamID;
            } else {
                data = new Data(streamID, false, Arrays.copyOfRange(fileBuffer, 0, bytesRead));
            }

            byte[] encodedMessage = context.getFramer().putFrame(data.encode(null));
            ByteBuffer buffer = ByteBuffer.wrap(encodedMessage);
            try {
                context.getClntSock().write(buffer, buffer, this);
            } catch (WritePendingException e){
                context.getQueue().add(buffer);
            }
        }
    }
}
