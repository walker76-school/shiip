package jack.serialization;

import java.util.Objects;

/**
 * ACK message
 *
 * @version 1.0
 */
public class ACK extends Message {

    private String host;
    private int port;

    /**
     * Create an ACK message from given values
     * @param host host ID
     * @param port port
     * @throws IllegalArgumentException if any validation problem with host and/or port, including null, etc.
     */
    public ACK(String host, int port) throws IllegalArgumentException {
        setHost(host);
        setPort(port);
    }

    /**
     * Creates a ACK message from a given byte array
     * @param msgBytes byte array
     * @throws IllegalArgumentException if any validation problem with host and/or port, including null, etc.
     */
    public ACK(byte[] msgBytes) throws IllegalArgumentException {
        String message = new String(msgBytes, ENC);
        String[] tokens = message.split(" ");
        if(tokens.length != 2){
            throw new IllegalArgumentException("Invalid message");
        }

        String hostAndPort = tokens[1];
        String[] serviceTokens = hostAndPort.split(":");
        if(serviceTokens.length != 2){
            throw new IllegalArgumentException("Invalid service");
        }
        String host = serviceTokens[0];
        setHost(host);

        try{
            String portString = serviceTokens[1];
            int port = Integer.parseInt(portString);
            setPort(port);
        } catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid port", e);
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
        return String.format("ACK [%s:%d]", host, port);
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
        if (host == null){
            throw new IllegalArgumentException("Host cannot be null");
        }

        this.host = host;
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
        if (port < 0){
            throw new IllegalArgumentException("Port must be positive");
        }

        this.port = port;
    }

    @Override
    public byte[] encode() {
        return null;
    }

    @Override
    public String getOperation() {
        return "ACK";
    }
}
