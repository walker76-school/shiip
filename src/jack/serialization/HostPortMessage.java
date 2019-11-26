package jack.serialization;

import jack.utils.Service;
import jack.utils.Utils;

import static jack.serialization.Constants.*;
import static jack.serialization.Constants.HASH_PRIME;

public abstract class HostPortMessage extends Message {

    private String host;
    private int port;

    /**
     * Create an HostPortMessage message from given values
     * @param host host ID
     * @param port port
     * @throws IllegalArgumentException if any validation problem with host and/or port, including null, etc.
     */
    public HostPortMessage(String host, int port) throws IllegalArgumentException {
        setHost(host);
        setPort(port);
    }

    /**
     * Creates a HostPortMessage message from a given byte array
     * @param payload payload
     * @throws IllegalArgumentException if any validation problem with host and/or port, including null, etc.
     */
    protected HostPortMessage(String payload) throws IllegalArgumentException {

        Service service = Utils.buildService(payload);

        setHost(service.getHost());
        setPort(service.getPort());

    }

    /**
     * Get the host
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * Set the service
     * @param host new host
     * @throws IllegalArgumentException if validation failure, including null host
     */
    public final void setHost(String host) throws IllegalArgumentException {
        String oldhost = this.host;
        this.host = Utils.validateHost(host);
        if(encode().length > MAX_LENGTH){
            this.host = oldhost;
            throw new IllegalArgumentException("Oversized host");
        }
    }

    /**
     * Get the port
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the port
     * @param port new port
     * @throws IllegalArgumentException if validation fails
     */
    public final void setPort(int port) throws IllegalArgumentException {
        int oldport = this.port;
        this.port = Utils.validatePort(port);
        if(encode().length > MAX_LENGTH){
            this.port = oldport;
            throw new IllegalArgumentException("Oversized port");
        }
    }

    /**
     * Returns string of the form
     * ACK [name:port]
     *
     * For example
     * ACK [google.com:8080]
     */
    @Override
    public String toString() {
        return String.format("%s [%s:%d]", getName(), host, port);
    }

    @Override
    public byte[] encode() {
        return String.format("%s %s:%d", getOperation(), getHost(), getPort()).getBytes(ENC);
    }

    public abstract String getOperation();

    protected abstract String getName();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HostPortMessage that = (HostPortMessage) o;

        if (port != that.port) return false;
        return host.equals(that.host);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = HASH_PRIME * result + port;
        return result;
    }
}
