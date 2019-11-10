/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.utils;

/**
 * Representation of a service
 * @author Andrew Walker
 */
public class Service implements Comparable<Service> {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;

        if (port != service.port) return false;
        return host.equals(service.host);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }

    @Override
    public int compareTo(Service that) {
        if (host.equals(that.host)) {
            return port - that.port;
        }
        return host.compareTo(that.host);
    }
}
