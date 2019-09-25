package shiip.client;

import java.util.Arrays;

public class Client {

    public static void main(String[] args) {
        if(args.length < 3){
            System.err.println("Usage: Client [host] [port] [paths]...");
            System.exit(-1);
        }

        String[] paths = Arrays.copyOfRange(args, 2, args.length);
        for(String path : paths){
            System.out.println(path);
        }
    }
}
