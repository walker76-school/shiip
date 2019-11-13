package shiip.server.handlers;

import shiip.serialization.*;
import shiip.server.models.ClientConnectionContext;
import shiip.server.models.FileContext;
import shiip.server.models.FileReadState;

import java.nio.ByteBuffer;
import java.nio.channels.WritePendingException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileReadHandler extends ReadHandler {

    private final FileContext fileContext;

    public FileReadHandler(ClientConnectionContext connectionContext, FileContext fileContext, Logger logger) {
        super(connectionContext, logger);
        logger.log(Level.INFO, "new FileReadHandler");
        this.fileContext = fileContext;
    }

    protected void handleRead(ByteBuffer buf, int bytesRead) throws BadAttributeException {
        if(bytesRead < 0){
            fileContext.setState(FileReadState.DONE);
            Data data = new Data(fileContext.getStreamID(), true, new byte[]{});
            sendData(data);
        } else {
            fileContext.incrementPosition(bytesRead);
            byte[] msgData = Arrays.copyOfRange(buf.array(), 0, bytesRead);
            Data data = new Data(fileContext.getStreamID(), false, msgData);
            sendData(data);
        }
    }

    private void sendData(Data data){
        byte[] encoded = connectionContext.getFramer().putFrame(data.encode(null));
        ByteBuffer dataBuffer = ByteBuffer.wrap(encoded);
        try {
            connectionContext.getClntSock().write(dataBuffer, dataBuffer, new FileWriteHandler(connectionContext, fileContext, logger));
        } catch (WritePendingException e){
            connectionContext.getBufferedMessages().add(dataBuffer);
        }
    }
}