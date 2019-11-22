package shiip.server.handlers.write;

import shiip.serialization.BadAttributeException;
import shiip.serialization.Data;
import shiip.server.ServerAIO;
import shiip.server.models.ClientConnectionContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

public class FileWriteHandler extends WriteHandler {

    private static Random rand = new Random();

    public FileWriteHandler(ClientConnectionContext context, Logger logger) {
        super(context, logger);
    }

    @Override
    protected void handleWriteCompleted() throws IOException, BadAttributeException {
        if(!context.getQueue().isEmpty()){
            ByteBuffer buffer = context.getQueue().remove();
            context.getClntSock().write(buffer, buffer, this);
        } else if (context.getStreamIDs().size() > 0) {
            Integer streamID = context.getStreamIDs().get(rand.nextInt(context.getStreamIDs().size()));
            InputStream fileInputStream = context.getSelector().get(streamID);

            byte[] fileBuffer = new byte[ServerAIO.MAXDATASIZE];
            int bytesRead = fileInputStream.read(fileBuffer);
            Data data;
            if (bytesRead == -1) {
                data = new Data(streamID, true, new byte[]{});
                context.getSelector().remove(streamID);
            } else {
                data = new Data(streamID, false, Arrays.copyOfRange(fileBuffer, 0, bytesRead));
            }

            byte[] encodedMessage = context.getFramer().putFrame(data.encode(null));
            ByteBuffer buffer = ByteBuffer.wrap(encodedMessage);
            context.getClntSock().write(buffer, buffer, this);
        }
    }
}
