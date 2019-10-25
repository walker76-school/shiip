package jack.serialization;

import java.util.*;

/**
 * Response message
 *
 * The list of services from any method (e.g., toString, encode, etc.) must be
 * sorted by Java's default String order for the String representation of a
 * service (e.g., name:port)
 *
 * @version 1.0
 */
public class Response extends Message {

    private Set<String> serviceList;

    /**
     * Construct response with empty host:port list
     */
    public Response() {
        serviceList = new HashSet<>();
    }

    /**
     * Creates a Response message from a given byte array
     * @param payload payload
     */
    protected Response(String payload) throws IllegalArgumentException {
        serviceList = new TreeSet<>();
        if(!payload.isEmpty()){
            String[] services = payload.split("]\\[");
            for (String service : services) {

                String hostAndPort = service.replaceAll("[\\[\\]]", "");
                String[] serviceParts = hostAndPort.split(":");
                if (serviceParts.length != 2) {
                    throw new IllegalArgumentException("Invalid service - " + hostAndPort);
                }

                validateHost(serviceParts[0]);
                validatePort(serviceParts[1]);

                serviceList.add(hostAndPort);
            }
        }
    }

    /**
     * Get the service (string representation) list where each service is
     * represented as name:port space (e.g., google:8000)
     *
     * @return service list
     */
    public List<String> getServiceList() {
        return new ArrayList<>(serviceList);
    }

    /**
     * Add service to list The list of services must be sorted by Java's default
     * String order for the String representation of a service (e.g., name:port)
     * @param host new serice host
     * @param port new service port
     * @throws IllegalArgumentException if validation fails, including null host
     */
    public final void addService(String host, int port) throws IllegalArgumentException {
        serviceList.add(String.format("%s:%d", validateHost(host), validatePort(port)));
    }

    /**
     * Returns string of the form
     * RESPONSE [name:port space]*
     *
     * For example
     * RESPONSE [wind:8000][fire:7000]
     */
    public String toString() {
        StringBuilder builder = new StringBuilder("RESPONSE ");
        for (String service : serviceList){
            builder.append(String.format("[%s]", service));
        }

        return builder.toString();
    }

    @Override
    public byte[] encode() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s ", getOperation()));
        for(String service : serviceList){
            builder.append(String.format("[%s]", service));
        }
        return builder.toString().getBytes(ENC);
    }

    @Override
    public String getOperation() {
        return "R";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Response response = (Response) o;

        return serviceList.equals(response.serviceList);
    }

    @Override
    public int hashCode() {
        return serviceList.hashCode();
    }
}
