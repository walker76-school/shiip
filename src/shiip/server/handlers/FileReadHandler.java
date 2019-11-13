package shiip.server.handlers;

import shiip.serialization.*;
import shiip.server.ServerAIO;
import shiip.server.models.ClientConnectionContext;
import shiip.server.models.FileContext;
import shiip.server.models.FileReadState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileReadHandler implements CompletionHandler<Integer, ByteBuffer> {

    private final ClientConnectionContext connectionContext;
    private final FileContext fileContext;
    private final Logger logger;

    public FileReadHandler(ClientConnectionContext connectionContext, FileContext fileContext, Logger logger) {
        logger.log(Level.INFO, "new ReadHandler");
        this.connectionContext = connectionContext;
        this.fileContext = fileContext;
        this.logger = logger;
    }

    @Override
    public void completed(Integer bytesRead, ByteBuffer buf) {
        try {
            handleRead(buf, bytesRead);
        } catch (BadAttributeException e) {
            logger.log(Level.WARNING, "Handle Read Failed", e);
        }
    }

    @Override
    public void failed(Throwable ex, ByteBuffer v) {
        try {
            connectionContext.getClntSock().close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Close Failed", e);
        }
    }

    private void handleRead(ByteBuffer buf, int bytesRead) throws BadAttributeException {
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
        connectionContext.getClntSock().write(dataBuffer, dataBuffer, new DataWriteHandler(connectionContext, fileContext, logger));
    }
}