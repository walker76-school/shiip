/*******************************************************
 * Author: Andrew Walker
 * Assignment: Prog 6
 * Class: Data Comm
 *******************************************************/

package shiip.server.models;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ServerConstants {

    // File for log
    public static final String LOG_FILE = "./connections.log";

    // Initial HTTP handshake message
    public static final String HANDSHAKE_MESSAGE = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";

    // StreamID for the connection
    public static final int CONNECTION_STREAMID = 0;

    // Maximum sized payload for a frame
    public static final int MAX_INCREMENT = 16384;

    public static final int HANDSHAKE_LENGTH = 24;

    // Encoding for handshake message
    public static final Charset ENC = StandardCharsets.US_ASCII;

    public static final int MAXIMUM_LENGTH = 16393;

}
