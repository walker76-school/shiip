/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 4
 * Class: Data Comm
 *******************************************************/

package jack.utils;

import static jack.serialization.Constants.*;

/**
 * Utility class for handling payloads
 */
public class Utils {

    /**
     * Checks the validity of the port
     * @param port port
     * @throws IllegalArgumentException if invalid port
     * @return validated port
     */
    public static int validatePort(int port) throws IllegalArgumentException{
        if(port < PORT_LOW || port > PORT_HIGH){
            throw new IllegalArgumentException("Invalid port: " + port);
        }
        return port;
    }

    /**
     * Checks the validity of the port
     * @param portString string representation of port
     * @throws IllegalArgumentException if invalid port
     * @return validated port
     */
    public static int validatePort(String portString) throws IllegalArgumentException{
        try {
            int port = Integer.parseInt(portString);
            return validatePort(port);
        } catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid port: " + portString, e);
        }
    }

    /**
     * Checks the validity of the host
     * @param host host
     * @throws IllegalArgumentException if invalid host
     * @return validated host
     */
    public static String validateHost(String host) throws IllegalArgumentException{
        if(host == null || host.isEmpty() || !host.matches(HOST_REGEX)){
            throw new IllegalArgumentException("Invalid host: " + host);
        }
        return host;
    }

    /**
     *
     * Checks the validity of the query
     * @param query query
     * @throws IllegalArgumentException if invalid query
     * @return validated query
     */
    public static String validateQuery(String query) throws IllegalArgumentException {
        return query != null && (query.isEmpty() || query.equals(WILDCARD)) ? query : validateHost(query);
    }

    /**
     * Constructs a service from a payload
     * @param payload the payload for a service
     * @return a service from payload
     * @throws IllegalArgumentException if invalid service
     */
    public static Service buildService(String payload) throws IllegalArgumentException {
        String[] tokens = payload.split(SERVICE_REGEX);
        if(tokens.length != SERVICE_TOKEN_LEN){
            throw new IllegalArgumentException("Bad parameters: Invalid service");
        }
        String host = validateHost(tokens[HOST_NDX]);
        int port = validatePort(tokens[PORT_NDX]);

        return new Service(host, port);
    }

    /**
     * Validates that a string is a service
     * @param service the string representation
     * @throws IllegalArgumentException if invalid service
     */
    public static void validateService(String service) throws IllegalArgumentException {
        String[] serviceParts = service.split(SERVICE_REGEX);
        if (serviceParts.length != SERVICE_TOKEN_LEN) {
            throw new IllegalArgumentException("Invalid service - " + service);
        }

        Utils.validateHost(serviceParts[HOST_NDX]);
        Utils.validatePort(serviceParts[PORT_NDX]);
    }

}
