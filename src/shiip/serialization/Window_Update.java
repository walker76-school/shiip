package shiip.serialization;

/**
 * Window_Update frame
 * @author Andrew Walker
 */
public class Window_Update extends Message {

    private int increment;

    /**
     * Creates Window_Update message from given values
     * @param streamID stream ID
     * @param increment
     * @throws BadAttributeException if attribute invalid (set protocol spec)
     */
    public Window_Update(int streamID, int increment) throws BadAttributeException {
        this.code = 0x8;
        this.streamID = streamID;
        setIncrement(increment);
    }

    /**
     * Get increment value
     * @return increment value
     */
    public int getIncrement(){
        return this.increment;
    }

    /**
     * Set increment value
     * @param increment increment value
     * @throws BadAttributeException if invalid
     */
    public final void setIncrement(int increment) throws BadAttributeException {
        if(increment <= 0){
            throw new BadAttributeException("Increment cannot be negative", "increment");
        }
        this.increment = increment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Window_Update that = (Window_Update) o;

        return increment == that.increment;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + increment;
        return result;
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
        return String.format("Window_Update: StreamID=%d increment=%d", streamID, increment);
    }

}
