/*******************************************************
 * Author: Andrew Walker
 * Assignment: Prog 6
 * Class: Data Comm
 *******************************************************/

package shiip.client;

/**
 * Client that doesn't use TLS
 */
public class ClientNoTLS {

    public static void main(String[] args) {
        Client.runClient(args, ClientState.PLAIN);
    }
}