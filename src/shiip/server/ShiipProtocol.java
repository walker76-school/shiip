package shiip.server;

import java.util.logging.Logger;

public abstract class ShiipProtocol implements Runnable {

    @Override
    public abstract void run();

    protected static Logger getLogger(){
        return Logger.getLogger("shiipServer");
    }

}
