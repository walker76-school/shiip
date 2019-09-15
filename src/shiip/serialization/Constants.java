/************************************************
 *
 * Author: Andrew Walker
 * Assignment: Prog0
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization;

/**
 * Holds constant values used for serialization and deserialization
 * by the Framer and Deframer
 *
 * @author Andrew Walker
 */
public class Constants {

    // Number of octets for the length field of the frame
    public static final int LENGTH_BYTES = 3;

    // Number of octets for the header field of the frame
    public static final int HEADER_BYTES = 6;

    // Maximum number of octets of the payload of the frame
    public static final int MAXIMUM_PAYLOAD_LENGTH_BYTES = 16384;

    // Bytemask for serializing length bits
    public static final int BYTEMASK = 0xff;

    // Shift for serializing length bits
    public static final int BYTESHIFT = 8;

    // Data identifier type
    static final byte DATA_TYPE = (byte)0x0;

    // Settings identifier type
    static final byte SETTINGS_TYPE = (byte)0x4;

    // Window_Update identifier type
    static final byte WINDOW_UPDATE_TYPE = (byte)0x8;
}
