package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private InputStream in;

    private String method;

    private String path;

    private Map<String, String> parameters;

    private Map<String, String> headers;

    private String body;

    public HttpRequest(InputStream in) {
        this.in = in;
        this.parameters = new HashMap<>();
        this.headers = new HashMap<>();
    }

    public void httpRequestProcess() {

        InputStreamReader isr = null;
        BufferedReader br = null;

        try {

            isr = new InputStreamReader(in, "UTF-8");
            br = new BufferedReader(isr);

            String line = br.readLine();
            if (line == null) {
                return;
            }

            String[] firstLine = line.split(" ");

            this.method = firstLine[0];

            int index = firstLine[1].indexOf('?');
            if (index != -1) {
                this.path = firstLine[1].substring(0, index);
                this.parameters.putAll(HttpRequestUtils.parseQueryString(firstLine[1].substring(index + 1)));
            } else {
                this.path = firstLine[1];
            }

            while (!line.equals("")) {

                line = br.readLine();

                if (line.contains(": ")) {
                    int keyIndex = line.indexOf(':');
                    int valueIndex = line.indexOf(' ') + 1;
                    String key = line.substring(0, keyIndex);
                    String value = line.substring(valueIndex);
                    this.headers.put(key, value);
                }

                log.debug("header : {}", line);
            }

            int contentLength = Integer.parseInt(this.headers.getOrDefault("Content-Length", "0"));

            if (this.method.equals("POST") && contentLength > 0) {
                body = IOUtils.readData(br, contentLength);
            }

        } catch (IOException e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }
    }

    public String getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public String getBody() {
        return this.body;
    }

    public String getHeader(String header) {
        return this.headers.get(header);
    }

    public String getParameter(String parameter) {
        return this.parameters.get(parameter);
    }
}
