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

public class Client {

    private static final Charset ENC = StandardCharsets.US_ASCII;
    private static final int MAX_TABLE_SIZE = 4096;
    private static final String HANDSHAKE_MESSAGE = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";

    private static Socket socket;
    private static Framer framer;
    private static Deframer deframer;
    private static Encoder encoder;
    private static Decoder decoder;

    public static void main(String[] args) throws Exception{
        if(args.length < 3){
            System.err.println("Usage: Client [host] [port] [paths...]");
            return;
        }

        Map<Integer, FileOutputStream> ongoingDownloads;
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            openConnection(host, port);
            ongoingDownloads = startStreams(Arrays.copyOfRange(args, 2, args.length), host);
        } catch (BadAttributeException e){
            System.err.println(e.getMessage());
            closeConnection();
            return;
        } catch (NumberFormatException e){
            System.err.println("Usage: Client [host] [port] [paths...]");
            return;
        } catch (Exception e){
            System.err.println(e.getMessage());
            return;
        }

        while(ongoingDownloads.size() > 0){

            Message m = getMessage();
            if(m == null){
                continue;
            }

            switch(m.getCode()){
                case Constants.DATA_TYPE: handleData(m, ongoingDownloads); break;
                case Constants.HEADERS_TYPE:  handleHeaders(m, ongoingDownloads); break;
                case Constants.SETTINGS_TYPE: handleSettings(m); break;
                case Constants.WINDOW_UPDATE_TYPE: handleWindowUpdate(m); break;
            }
        }

        closeConnection();
    }

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

    private static void closeConnection() throws Exception {
        if(socket != null){
            socket.close();
        }
    }

    private static Map<Integer, FileOutputStream> startStreams(String[] paths, String host) throws Exception {
        Map<Integer, FileOutputStream> ongoingDownloads = new TreeMap<>();
        for(int i = 0; i < paths.length; i++){
            int streamID = 1 + i * 2;
            String path = paths[i];
            Headers headers;
            Map<String, String> options = Map.of(
                    ":method", "GET",
                    ":path", path,
                    ":authority", host,
                    ":scheme", "https",
                    "user-agent", "Mozilla/5.0"
            );

            headers = encodeHeaders(streamID, true, options);
            framer.putFrame(headers.encode(encoder));
            ongoingDownloads.put(streamID, new FileOutputStream(path.replaceAll("/", "-")));
        }
        return ongoingDownloads;
    }

    private static Headers encodeHeaders(int streamID, boolean isEnd, Map<String, String> options) throws BadAttributeException {
        // Create request header for default page
        Headers headers = new Headers(streamID, isEnd);
        for(Map.Entry<String, String> entry : options.entrySet()){
            headers.addValue(entry.getKey(), entry.getValue());
        }
        return headers;
    }

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
        }
    }

    private static void handleHeaders(Message m, Map<Integer, FileOutputStream> ongoingDownloads){
        Headers h = (Headers) m;

        if(!ongoingDownloads.containsKey(h.getStreamID())){
            System.err.println("Unexpected stream ID: " + h);
            return;
        }

        if(h.getNames().contains(":status")) {
            System.out.println("Received message: " + m.toString());

            if(!h.getValue(":status").startsWith("200")){
                System.err.println("Bad status: " + h.getValue(":status"));
                ongoingDownloads.remove(h.getStreamID());
            }
        }
    }

    private static void handleSettings(Message m){
        System.out.println("Received message: " + m.toString());
    }

    private static void handleWindowUpdate(Message m){
        System.out.println("Received message: " + m.toString());
    }
}
