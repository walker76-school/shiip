package shiip.serialization;

/**
 * Window_Update frame
 * @author Andrew Walker
 */
public class Windows_Update extends Message {

    /**
     * Creates Window_Update message from given values
     * @param streamID stream ID
     * @param increment
     * @throws BadAttributeException if attribute invalid (set protocol spec)
     */
    public Windows_Update(int streamID, int increment) throws BadAttributeException {

    }

    /**
     * Get increment value
     * @return increment value
     */
    public int getIncrement(){
        return -1;
    }

    /**
     * Set increment value
     * @param increment increment value
     * @throws BadAttributeException if invalid
     */
    public void setIncrement(int increment) throws BadAttributeException {

    }

    /**
     * Returns string of the form
     * Window_Update: StreamID=<streamid> increment=<inc>
     *
     * For example
     * Window_Update: StreamID=5 increment=1024
     */
    @Override
    public String toString() {
        return null;
    }

}
