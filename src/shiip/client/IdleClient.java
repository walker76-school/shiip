/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 3
 * Class: Data Comm
 *******************************************************/

package shiip.client;

import tls.TLSFactory;

import java.net.Socket;

/**
 * A TCP client for SHiiP frames
 * @author Andrew Walker
 */
public class IdleClient {

    // Minimum number of parameters allowed
    private static final int MIN_ARGS = 2;

    // Index of the host in the parameters
    private static final int HOST_NDX = 0;

    // Index of the port in the parameters
    private static final int PORT_NDX = 1;

    public static void main(String[] args) {
        if(args.length < MIN_ARGS){
            System.err.println("Usage: Client [host] [port]");
            return;
        }

        // Connect and do nothing
        try (Socket socket = TLSFactory.getClientSocket(args[HOST_NDX], Integer.parseInt(args[PORT_NDX]))) {
            for(int i = 0; i < 25; i++){
                System.out.println("Slept " + i + " seconds");
                Thread.sleep(1000);
            }
        } catch (NumberFormatException e){
            System.err.println("Usage: Client [host] [port]");
        } catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
}
