package shiip.serialization;

/**
 * Settings message
 * @author Andrew Walker
 */
public class Settings extends Message {

    /**
     * Creates Settings message
     * @throws BadAttributeException if attribute invalid (not thrown in this case)
     */
    public Settings() throws BadAttributeException {
        this.streamID = 0;
        this.code = (byte) 0x4;
    }

    /**
     * Returns string of the form
     * Settings: StreamID=0
     *
     * For example
     * Settings: StreamID=0
     */
    @Override
    public String toString() {
        return "Settings: StreamID=0";
    }

}
