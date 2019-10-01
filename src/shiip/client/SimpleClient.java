package shiip.client;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;

import shiip.serialization.BadAttributeException;
import shiip.serialization.Data;
import shiip.serialization.Deframer;
import shiip.serialization.Framer;
import shiip.serialization.Headers;
import shiip.serialization.Message;
import shiip.serialization.Settings;
import tls.TLSFactory;

public class SimpleClient {

    private static final Charset ENC = StandardCharsets.US_ASCII;

    public static void main(String[] args) throws Exception {
        // Set up connection and (de)framer
        Socket s = TLSFactory.getClientSocket("duckduckgo.com", 443);
        OutputStream out = s.getOutputStream();
        Deframer deframer = new Deframer(s.getInputStream());
        Framer framer = new Framer(out);

        // Set up De/Encoder
        Decoder decoder = new Decoder(4096, 4096);
        Encoder encoder = new Encoder(4096);

        // Send connection preface and Settings frame
        out.write("PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes(ENC));
        framer.putFrame(new Settings().encode(null));

        // Create request header for default page
        Headers header = new Headers(1, false);
        header.addValue(":method", "GET");
        header.addValue(":authority", "duckduckgo.com");
        header.addValue(":scheme", "https");
        header.addValue(":path", "/");
        header.addValue("accept-encoding", "deflate");

        // Send request
        framer.putFrame(header.encode(encoder));
        System.out.println(header);

        // Open file to dump HTML
        FileOutputStream fout = new FileOutputStream("pagedump");
        while (true) {
            try {
                // Get and print next frame + message
                Message m = Message.decode(deframer.getFrame(), decoder);
                System.out.println(m);
                // If Data message, dump data to file
                if (m instanceof Data) {
                    Data d = (Data) m;
                    fout.write(d.getData());
                    // If Data message has end flag, then we are done
                    if (d.isEnd()) {
                        break;
                    }
                }
            } catch (BadAttributeException e) {
                System.err.println(e.getMessage());
            }
        }
        s.close();
        fout.close();
    }
}