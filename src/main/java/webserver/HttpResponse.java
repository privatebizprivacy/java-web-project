package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);

    private DataOutputStream dos;

    private Map<String, String> headers;

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
        this.headers = new HashMap<>();
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void forward(String url) throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(body.length);
        responseBody(body);
    }

    public void forwardBody(String url) throws IOException {
    }

    public void response200Header(int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            processHeaders();
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }
    }

    public void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }
    }

    public void sendRedirect(String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + path + " \r\n");
            processHeaders();
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }
    }

    public void processHeaders() {
        try {
            for (Entry<String, String> entry : headers.entrySet()) {
                dos.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
            }
        } catch (IOException e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }
    }
}
