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

    private static Socket socket;
    private static Framer framer;
    private static Deframer deframer;
    private static Encoder encoder;
    private static Decoder decoder;

    public static void main(String[] args) {
        if(args.length < 3){
            System.err.println("Usage: Client [host] [port] [paths...]");
            return;
        }

        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            openConnection(host, port);
            Map<Integer, FileOutputStream> ongoingDownloads =
                    startStreams(Arrays.copyOfRange(args, 2, args.length), host);

            while(ongoingDownloads.size() > 0){

                Message m = getMessage();
                if(m != null){
                    switch(m.getCode()){
                        case Constants.DATA_TYPE: handleData(m, ongoingDownloads); break;
                        case Constants.HEADERS_TYPE:  handleHeaders(m, ongoingDownloads); break;
                        case Constants.SETTINGS_TYPE: handleSettings(m); break;
                        case Constants.WINDOW_UPDATE_TYPE: handleWindowUpdate(m); break;
                    }
                }
            }

            closeConnection();

        } catch (NumberFormatException e){
            System.err.println("Usage: Client [host] [port] [paths...]");
        } catch (Exception e){
            System.err.println(e.getMessage());
            try {
                closeConnection();
            } catch (Exception ex){
                System.err.println(ex.getMessage());
            }
        }
    }

    /**
     * Opens a socket connection and establishes necessary tools for communication
     * @param host the host to connect to
     * @param port the port to connect on
     * @throws Exception if issue opening socket or establishing tools (see spec)
     */
    private static void openConnection(String host, Integer port) throws Exception {

        socket = TLSFactory.getClientSocket(host, port);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        deframer = new Deframer(in);
        framer = new Framer(out);

        // Set up De/Encoder
        decoder = new Decoder(MAX_TABLE_SIZE, MAX_TABLE_SIZE);
        encoder = new Encoder(MAX_TABLE_SIZE);

        // Send connection preface and Settings frame
        out.write(HANDSHAKE_MESSAGE.getBytes(ENC));
        framer.putFrame(new Settings().encode(null));
    }

    /**
     * Closes the socket connection
     * @throws Exception if error closing socket
     */
    private static void closeConnection() throws Exception {
        if(socket != null){
            socket.close();
        }
    }

    /**
     * Establishes streams for every filepath
     * @param paths a list of files to download from the server
     * @param host the host to download from
     * @return a map of streamIDs to local FileOutputStreams
     * @throws Exception if issue encoding Headers
     */
    private static Map<Integer, FileOutputStream> startStreams(String[] paths, String host)
                                                                throws Exception {
        Map<Integer, FileOutputStream> ongoingDownloads = new TreeMap<>();
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

            Headers headers = encodeHeaders(streamID, i == paths.length - 1, options);
            framer.putFrame(headers.encode(encoder));
            ongoingDownloads.put(streamID, new FileOutputStream(path.replaceAll("/", "-")));
        }
        return ongoingDownloads;
    }

    /**
     * Encodes a Headers object
     * @param streamID the streamID of the Headers
     * @param isEnd if the header is the last one
     * @param options a map of HTTP header options
     * @return a headers object
     * @throws BadAttributeException if invalid streamID or option
     */
    private static Headers encodeHeaders(int streamID, boolean isEnd,
                                         Map<String, String> options)
                                                throws BadAttributeException {
        // Create request header for default page
        Headers headers = new Headers(streamID, isEnd);
        for(Map.Entry<String, String> entry : options.entrySet()){
            headers.addValue(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    /**
     * Retrieves the next message from the server
     * @return the next message from the server
     */
    private static Message getMessage(){
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
            Window_Update wu = new Window_Update(d.getStreamID(), d.getData().length);
            framer.putFrame(wu.encode(null));

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
