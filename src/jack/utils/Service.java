/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 5
 * Class: Data Comm
 *******************************************************/

package jack.utils;

/**
 * Representation of a service
 * @author Andrew Walker
 */
public class Service {
    private String host;
    private int port;

    /**
     * Constructs a Service from the given host and port
     * @param host host
     * @param port port
     */
    public Service(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Returns the host
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host
     * @param host the new host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the port
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port
     * @param port the new port
     */
    public void setPort(int port) {
        this.port = port;
    }
}
