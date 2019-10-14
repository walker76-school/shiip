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

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A TCP client for SHiiP frames
 * @author Andrew Walker
 */
public class SlowClient {

    // Table size for Encoder and Decoder
    private static final int MAX_TABLE_SIZE = 4096;

    // Encoding for handshake message
    private static final Charset ENC = StandardCharsets.US_ASCII;

    // Initial HTTP handshake message
    private static final String HANDSHAKE_MESSAGE = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";

    // Valid status for a Headers
    private static final String VALID_STATUS = "200";

    // Status key for Headers
    private static final String STATUS_KEY = ":status";

    // Minimum number of parameters allowed
    private static final int MIN_ARGS = 3;

    // Index of the host in the parameters
    private static final int HOST_NDX = 0;

    // Index of the port in the parameters
    private static final int PORT_NDX = 1;

    // Index of the start of the paths in the parameters
    private static final int PATHS_NDX = 2;

    // StreamID for the connection
    private static final int CONNECTION_STREAMID = 0;

    // Framer to send frames to the socket
    private static Framer framer;

    // Open streams
    private static Map<Integer, String> pathMap;

    public static void main(String[] args) {
        if(args.length < MIN_ARGS){
            System.err.println("Usage: Client [host] [port] [paths...]");
            return;
        }

        Decoder decoder = new Decoder(MAX_TABLE_SIZE, MAX_TABLE_SIZE);

        String host = args[HOST_NDX];
        try (Socket socket = TLSFactory.getClientSocket(host, Integer.parseInt(args[PORT_NDX]))) {

            Deframer deframer = openConnection(socket);
            Map<Integer, FileOutputStream> ongoingDownloads = new TreeMap<>();
            String[] paths = Arrays.copyOfRange(args, PATHS_NDX, args.length);
            startStreams(paths, host);

            while(!pathMap.isEmpty()){

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
        pathMap = new TreeMap<>();

        // Send connection preface and Settings frame
        out.write(HANDSHAKE_MESSAGE.getBytes(ENC));
        framer.putFrame(new Settings().encode(null));
        return deframer;
    }

    /**
     * Establishes streams for every filepath
     * @param paths a list of files to download from the server
     * @param host the host to download from
     * @throws Exception if issue encoding Headers
     */
    private static void startStreams(String[] paths,
                                     String host)
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
            pathMap.put(streamID, path);
            Thread.sleep(5000);
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
        if(!pathMap.containsKey(d.getStreamID())){
            System.err.println("Unexpected stream ID: " + d);
            return;
        }

        System.out.println("Received message: " + m);
        try {

            if(d.getData().length > 0) {
                framer.putFrame(new Window_Update(CONNECTION_STREAMID, d.getData().length).encode(null));
                framer.putFrame(new Window_Update(d.getStreamID(), d.getData().length).encode(null));
            }

            // Write data
            FileOutputStream out = ongoingDownloads.get(d.getStreamID());
            if(out == null) {
                return;
            }

            out.write(d.getData());

            // If Data message has end flag, then we are done
            if (d.isEnd()) {
                out.close();
                ongoingDownloads.remove(d.getStreamID());
                pathMap.remove(d.getStreamID());
            }

        } catch (IOException | BadAttributeException e){
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Handler for a Headers message
     * @param m the Headers message
     * @param ongoingDownloads  the map of streamID to local FileOutputStreams
     */
    private static void handleHeaders(Message m, Map<Integer, FileOutputStream> ongoingDownloads) throws IOException {
        Headers h = (Headers) m;

        if(!pathMap.containsKey(h.getStreamID())){
            System.err.println("Unexpected stream ID: " + h);
            return;
        }

        System.out.println("Received message: " + m.toString());
        if(!h.getNames().contains(STATUS_KEY) || !h.getValue(STATUS_KEY).startsWith(VALID_STATUS)) {
            System.err.println("Bad status: " + h.getValue(STATUS_KEY));
            ongoingDownloads.remove(h.getStreamID());
            pathMap.remove(h.getStreamID());
        } else {
            ongoingDownloads.put(h.getStreamID(), new FileOutputStream(pathMap.get(h.getStreamID()).replaceAll("/", "-")));
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
