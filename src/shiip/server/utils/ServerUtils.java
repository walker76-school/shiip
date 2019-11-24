/*******************************************************
 * Author: Andrew Walker
 * Assignment: Prog 6
 * Class: Data Comm
 *******************************************************/

package shiip.server.utils;

import shiip.server.models.ServerConstants;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static shiip.server.models.ServerConstants.ENC;

/**
 * Utilities for servers
 */
public class ServerUtils {

    /**
     * Configures a logger
     * @param logger the logger to configure
     * @return if successful configuration
     */
    public static boolean configureLogger(Logger logger) {
        try {
            logger.setUseParentHandlers(false);
            FileHandler handler = new FileHandler(ServerConstants.LOG_FILE);
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
        } catch (IOException e){
            logger.log(Level.WARNING, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Turns a byte array into a string representation
     * @param b the byte array to transform
     * @return a string representation of the byte array
     */
    public static String b2s(byte[] b) {
        return new String(b, ENC);
    }
}
