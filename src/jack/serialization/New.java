/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.serialization;

import jack.utils.Service;
import jack.utils.Utils;

import static jack.serialization.Constants.*;

/**
 * New message
 *
 * @version 1.0
 */
public class New extends Message{

    private String host;
    private int port;

    /**
     * Creates a New message from given values
     * @param host host ID
     * @param port port
     * @throws IllegalArgumentException if any validation problem with host and/or port, including null, etc.
     */
    public New(String host, int port) throws IllegalArgumentException {
        setHost(host);
        setPort(port);
    }

    /**
     * Creates a New message from a given byte array
     * @param payload payload
     * @throws IllegalArgumentException if any validation problem with host and/or port, including null, etc.
     */
    protected New(String payload) throws IllegalArgumentException {

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
     * NEW [name:port]
     *
     * For example
     * NEW [google.com:8080]
     */
    @Override
    public String toString() {
        return String.format("NEW [%s:%d]", host, port);
    }

    @Override
    public byte[] encode() {
        return String.format("%s %s:%d", getOperation(), getHost(), getPort()).getBytes(ENC);
    }

    @Override
    public String getOperation() {
        return NEW_OP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        New aNew = (New) o;

        if (port != aNew.port) return false;
        return host.equals(aNew.host);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = HASH_PRIME * result + port;
        return result;
    }
}
