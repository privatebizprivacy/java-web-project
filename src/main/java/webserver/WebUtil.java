package webserver;

public class WebUtil {

    public static String getURI(String header) {

        String[] tokens = header.split(" ");

        if (tokens.length > 1 && tokens[0].equals("GET")) {
            return tokens[1];
        }

        return null;
    }

    public static int getContentLength(String header) {

        String[] tokens = header.split(" ");

        if (tokens.length > 1 && tokens[0].equals("Content-Length:")) {
            return Integer.parseInt(tokens[1]);
        }

        return 0;
    }

}
