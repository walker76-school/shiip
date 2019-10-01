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

    public static void main(String[] args) throws Exception{
        if(args.length < 3){
            System.err.println("Usage: Client [host] [port] [paths...]");
            System.exit(-1);
        }

        String host = args[0];
        int port = 0;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e){
            System.err.println("Usage: Client [host] [port] [paths...]");
            System.exit(-1);
        }

        Socket socket;
        InputStream in;
        OutputStream out;

        try {
            socket = TLSFactory.getClientSocket(host, port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (Exception e){
            System.err.println(e.getMessage());
            return;
        }

        Deframer deframer = new Deframer(in);
        Framer framer = new Framer(out);

        // Set up De/Encoder
        Decoder decoder = new Decoder(4096, 4096);
        Encoder encoder = new Encoder(4096);

        // Send connection preface and Settings frame
        out.write("PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes(ENC));
        framer.putFrame(new Settings().encode(null));

        Map<Integer, FileOutputStream> ongoingDownloads = new TreeMap<>();
        Map<Integer, String> streamPaths = new TreeMap<>();
        String[] paths = Arrays.copyOfRange(args, 2, args.length);
        for(int i = 0; i < paths.length; i++){
            int streamID = 1 + i * 2;
            String path = paths[i];
            Headers headers;
            try{
                headers = encodeHeaders(streamID, false, host, path);
            } catch (BadAttributeException e){
                System.err.println("Bad encoding headers");
                continue;
            }
            framer.putFrame(headers.encode(encoder));
            ongoingDownloads.put(streamID, new FileOutputStream(path.replaceAll("/", "-")));
            streamPaths.put(streamID, path);
        }

        while(ongoingDownloads.size() > 0){

            byte[] framedBytes;
            try {
                framedBytes = deframer.getFrame();
            } catch (IOException | IllegalArgumentException e){
                System.err.println("Unable to parse: " + e.getMessage());
                continue;
            }

            Message m;
            try {
                // Get and print next frame + message
                m = Message.decode(framedBytes, decoder);
            } catch (BadAttributeException | NullPointerException e){
                System.err.println("Invalid Message: " + e.getMessage());
                continue;
            }

            //System.out.println(m);
            // If Data message, dump data to file
            if (m instanceof Data) {
                Data d = (Data) m;
                if(!ongoingDownloads.containsKey(d.getStreamID())){
                    System.err.println("Unexpected stream ID: " + d);
                }
                System.out.println("Received message: " + m);

                Window_Update wu = new Window_Update(d.getStreamID(), d.getData().length);
                out.write(wu.encode(null));

                // Write data
                ongoingDownloads.get(d.getStreamID()).write(d.getData());

                // If Data message has end flag, then we are done
                if (d.isEnd()) {
                    ongoingDownloads.remove(d.getStreamID());
                    // out.write(encodeHeaders(d.getStreamID(), true, host, streamPaths.get(d.getStreamID())).encode(encoder));
                }
            } else if (m instanceof Settings || m instanceof Window_Update){
                System.out.println("Received message: " + m);
            } else if (m instanceof Headers){
                Headers h = (Headers) m;

                if(!ongoingDownloads.containsKey(h.getStreamID())){
                    System.err.println("Unexpected stream ID: " + h);
                }

                if(h.getNames().contains(":status")) {
                    System.out.println("Received message: " + m.toString());
                    int status = Integer.parseInt(h.getValue(":status"));
                    if(status != 200){
                        System.err.println("Bad status: " + status);
                        // Terminate Stream?
                    }
                }
            }
        }

    }

    private static Headers encodeHeaders(int streamID, boolean isEnd, String host, String path) throws BadAttributeException {
        // Create request header for default page
        Headers headers = new Headers(streamID, isEnd);
        headers.addValue(":method", "GET");
        headers.addValue(":authority", host);
        headers.addValue(":scheme", "https");
        headers.addValue(":path", path);
        headers.addValue("accept-encoding", "deflate");

        return headers;
    }
}
