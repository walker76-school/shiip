package jack.serialization;

import java.util.ArrayList;
import java.util.List;

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

    private List<String> serviceList;

    /**
     * Construct response with empty host:port list
     */
    public Response() {
        serviceList = new ArrayList<>();
    }

    /**
     * Get the service (string representation) list where each service is
     * represented as name:port space (e.g., google:8000)
     *
     * @return service list
     */
    public List<String> getServiceList() {
        return serviceList;
    }

    /**
     * Add service to list The list of services must be sorted by Java's default
     * String order for the String representation of a service (e.g., name:port)
     * @param host new serice host
     * @param port new service port
     * @throws IllegalArgumentException if validation fails, including null host
     */
    public final void addService(String host, int port) throws IllegalArgumentException {
        String service = String.format("%s:%d", host, port);
        serviceList.add(service);
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
        return null;
    }

    @Override
    public String getOperation() {
        return "Response";
    }
}
