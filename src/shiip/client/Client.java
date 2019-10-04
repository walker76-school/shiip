/*******************************************************
 * Author: Andrew walker
 * Assignment: Prog 2
 * Class: Data Comm
 *******************************************************/

package shiip.client;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import shiip.serialization.*;
import tls.TLSFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A TCP client for SHiiP frames
 */
public class Client {

    private static final Charset ENC = StandardCharsets.US_ASCII;
    private static final int MAX_TABLE_SIZE = 4096;
    private static final String HANDSHAKE_MESSAGE = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";
    private static final int HOST_NDX = 0;
    private static final int PORT_NDX = 1;
    private static final int PATHS_NDX = 2;

    private static Framer framer;

    public static void main(String[] args) {
        if(args.length < 3){
            System.err.println("Usage: Client [host] [port] [paths...]");
            return;
        }

        Decoder decoder = new Decoder(MAX_TABLE_SIZE, MAX_TABLE_SIZE);

        String host = args[HOST_NDX];
        try (Socket socket = TLSFactory.getClientSocket(host, Integer.parseInt(args[PORT_NDX]))) {

            Deframer deframer = openConnection(socket);
            Map<Integer, FileOutputStream> ongoingDownloads = new TreeMap<>();
            startStreams(Arrays.copyOfRange(args, PATHS_NDX, args.length), host, ongoingDownloads);

            while(ongoingDownloads.size() > 0){

                Message m = getMessage(deframer, decoder);
                if(m != null){
                    switch(m.getCode()){
                        case Constants.DATA_TYPE: handleData(m, ongoingDownloads); break;
                        case Constants.HEADERS_TYPE:  handleHeaders(m, ongoingDownloads); break;
                        case Constants.SETTINGS_TYPE: handleSettings(m); break;
                        case Constants.WINDOW_UPDATE_TYPE: handleWindowUpdate(m); break;
                    }
                }
            }

        } catch (NumberFormatException e){
            System.err.println("Usage: Client [host] [port] [paths...]");
        } catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    /**
     * Opens a socket connection and establishes necessary tools for communication
     * @param socket the socket to setup connection with
     * @throws Exception if issue opening socket or establishing tools (see spec)
     */
    private static Deframer openConnection(Socket socket) throws Exception {

        OutputStream out = socket.getOutputStream();

        Deframer deframer = new Deframer(socket.getInputStream());
        framer = new Framer(out);

        // Send connection preface and Settings frame
        out.write(HANDSHAKE_MESSAGE.getBytes(ENC));
        framer.putFrame(new Settings().encode(null));
        return deframer;
    }

    /**
     * Establishes streams for every filepath
     * @param paths a list of files to download from the server
     * @param host the host to download from
     * @param ongoingDownloads collection of outputstreams for each stream
     * @throws Exception if issue encoding Headers
     */
    private static void startStreams(String[] paths,
                                     String host,
                                     Map<Integer, FileOutputStream> ongoingDownloads)
                                     throws Exception {

        Encoder encoder = new Encoder(MAX_TABLE_SIZE);
        for(int i = 0; i < paths.length; i++){
            int streamID = 1 + i * 2;
            String path = paths[i];
            Map<String, String> options = Map.of(
                    ":method", "GET",
                    ":path", path,
                    ":authority", host,
                    ":scheme", "https",
                    "user-agent", "Mozilla/5.0"
            );

            // Create request header for default page
            Headers headers = new Headers(streamID, i == paths.length - 1);
            for(Map.Entry<String, String> entry : options.entrySet()){
                headers.addValue(entry.getKey(), entry.getValue());
            }
            framer.putFrame(headers.encode(encoder));
            ongoingDownloads.put(streamID, new FileOutputStream(path.replaceAll("/", "-")));
        }
    }

    /**
     * Retrieves the next message from the server
     * @return the next message from the server
     */
    private static Message getMessage(Deframer deframer, Decoder decoder){
        try {
            byte[] framedBytes = deframer.getFrame();
            return Message.decode(framedBytes, decoder);
        } catch (IOException | IllegalArgumentException e){
            System.err.println("Unable to parse: " + e.getMessage());
            return null;
        } catch (BadAttributeException | NullPointerException e){
            System.err.println("Invalid Message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handler for a Data message
     * @param m the Data message
     * @param ongoingDownloads the map of streamID to local FileOutputStreams
     */
    private static void handleData(Message m, Map<Integer, FileOutputStream> ongoingDownloads){
        Data d = (Data) m;
        if(!ongoingDownloads.containsKey(d.getStreamID())){
            System.err.println("Unexpected stream ID: " + d);
            return;
        }
        System.out.println("Received message: " + m);
        try {
            framer.putFrame(new Window_Update(0, d.getData().length).encode(null));
            framer.putFrame(new Window_Update(d.getStreamID(), d.getData().length).encode(null));

            // Write data
            ongoingDownloads.get(d.getStreamID()).write(d.getData());

            // If Data message has end flag, then we are done
            if (d.isEnd()) {
                ongoingDownloads.get(d.getStreamID()).close();
                ongoingDownloads.remove(d.getStreamID());
            }

        } catch (IOException | BadAttributeException e){
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    /**
     * Handler for a Headers message
     * @param m the Headers message
     * @param ongoingDownloads  the map of streamID to local FileOutputStreams
     */
    private static void handleHeaders(Message m, Map<Integer, FileOutputStream> ongoingDownloads) {
        Headers h = (Headers) m;

        if(!ongoingDownloads.containsKey(h.getStreamID())){
            System.err.println("Unexpected stream ID: " + h);
            return;
        }

        System.out.println("Received message: " + m.toString());
        if(!h.getNames().contains(":status") || !h.getValue(":status").startsWith("200")) {
            System.err.println("Bad status: " + h.getValue(":status"));
            ongoingDownloads.remove(h.getStreamID());
        }
    }

    /**
     * Handler for a Settings message
     * @param m the Settings message
     */
    private static void handleSettings(Message m) {
        System.out.println("Received message: " + m.toString());
    }

    /**
     * Handler for a Window_Update message
     * @param m the Window_Update message
     */
    private static void handleWindowUpdate(Message m) {
        System.out.println("Received message: " + m.toString());
    }
}
