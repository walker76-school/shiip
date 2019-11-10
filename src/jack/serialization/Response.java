/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.serialization;

import jack.utils.Service;
import jack.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

import static jack.serialization.Constants.*;

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

    // Regex to split the services
    private static final String SPLIT_REGEX = " ";

    // Regex for payload
    private static final String SERVICE_PAYLOAD_REGEX = "([a-zA-Z0-9.-]+:[0-9]+ )*";

    private Set<Service> serviceSet;

    /**
     * Construct response with empty host:port list
     */
    public Response() {
        serviceSet = new TreeSet<>();
    }

    /**
     * Creates a Response message from a given byte array
     * @param payload payload
     */
    protected Response(String payload) throws IllegalArgumentException {
        serviceSet = new TreeSet<>();
        if(!payload.isEmpty()){

            if(!payload.matches(SERVICE_PAYLOAD_REGEX)){
                throw new IllegalArgumentException("Invalid payload - " + payload);
            }

            String[] services = payload.split(SPLIT_REGEX);
            for (String serviceString : services) {
                Service service = Utils.buildService(serviceString);
                serviceSet.add(service);
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
        return serviceSet.stream()
                .map(x -> String.format("%s:%d", x.getHost(), x.getPort()))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Add service to list The list of services must be sorted by Java's default
     * String order for the String representation of a service (e.g., name:port)
     * @param host new serice host
     * @param port new service port
     * @throws IllegalArgumentException if validation fails, including null host
     */
    public final void addService(String host, int port) throws IllegalArgumentException {
        Utils.validateHost(host);
        Utils.validatePort(port);
        Service service = new Service(host, port);
        serviceSet.add(service);
        if(encode().length > MAX_LENGTH){
            serviceSet.remove(service);
            throw new IllegalArgumentException("Oversized payload");
        }
    }

    /**
     * Returns string of the form
     * RESPONSE [name:port space]*
     *
     * For example
     * RESPONSE [wind:8000][fire:7000]
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("RESPONSE ");
        for (Service service : serviceSet){
            builder.append(String.format("[%s:%d]", service.getHost(), service.getPort()));
        }

        return builder.toString();
    }

    @Override
    public byte[] encode() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s ", getOperation()));
        for(Service service : serviceSet){
            builder.append(String.format("%s:%d ", service.getHost(), service.getPort()));
        }
        return builder.toString().getBytes(ENC);
    }

    @Override
    public String getOperation() {
        return RESPONSE_OP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Response response = (Response) o;

        return serviceSet.equals(response.serviceSet);
    }

    @Override
    public int hashCode() {
        return serviceSet.hashCode();
    }
}
