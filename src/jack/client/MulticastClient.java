/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 7
 * Class: Data Comm
 *******************************************************/

package jack.client;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

/**
 * Connects to a multicast group and prints all Jack messages
 */
public class MulticastClient {
    // Minimum number of parameters allowed
    private static final int MIN_ARGS = 2;

    // Index of the op in the parameters
    private static final int HOST_NDX = 0;

    // Index of the start of the payload in the parameters
    private static final int PORT_NDX = 1;

    public static void main(String[] args) {
        if(args.length < MIN_ARGS){
            System.err.println("Usage: Client [host] [port]");
            return;
        }

        try (MulticastSocket sock = connectSocket(args)){

            // Send off read thread
            Receiver receiver = new Receiver(sock);
            Thread thread = new Thread(receiver);
            thread.start();

            // Wait for end
            Scanner in = new Scanner(System.in);
            String line = "";
            while(!line.equals("quit")){
                line = in.nextLine();
            }

            disconnectSocket(args, sock);

        } catch (NumberFormatException e){
            System.err.println("Bad parameters: Invalid port");
        } catch(IOException e){
            System.err.println("Communication problem: " + e.getMessage());
        }
    }

    /**
     * Connects the datagram socket to the given host and port
     * @param args the command line args
     * @return a datagram socket connected to th given host and port
     * @throws IOException if communication problem
     */
    private static MulticastSocket connectSocket(String[] args) throws IOException {

        // Connect to the server
        InetAddress addr = InetAddress.getByName(args[HOST_NDX]); // Destination addr
        int port = Integer.parseInt(args[PORT_NDX]); // Destination port

        MulticastSocket sock = new MulticastSocket(port);
        sock.joinGroup(addr);

        return sock;
    }

    /**
     * Connects the datagram socket to the given host and port
     * @param args the command line args
     * @param socket the socket to disconnect
     * @throws IOException if communication problem
     */
    private static void disconnectSocket(String[] args, MulticastSocket socket) throws IOException {

        // Disconnect from the server
        InetAddress addr = InetAddress.getByName(args[HOST_NDX]);
        socket.leaveGroup(addr);
        socket.close();
    }
}
