package webserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;

public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private InputStream in;

    private String method;

    private String path;

    private Map<String, String> parameters;

    private Map<String, String> headers;

    public HttpRequest(InputStream in) {
        this.in = in;
        init();
    }

    private void init() {

        try (InputStreamReader isr = new InputStreamReader(this.in);
                BufferedReader br = new BufferedReader(isr);) {

            String line = br.readLine();
            String[] firstLine = line.split(" ");

            int index = firstLine[1].indexOf('?');
            this.method = firstLine[0];
            this.path = firstLine[1].substring(0, index);
            this.parameters = HttpRequestUtils.parseQueryString(firstLine[1].substring(index + 1));
            this.headers = new HashMap<>();

            while ((line = br.readLine()) != "") {

                if (line.contains(": ")) {
                    int keyIndex = line.indexOf(':');
                    int valueIndex = line.indexOf(' ') + 1;
                    String key = line.substring(0, keyIndex);
                    String value = line.substring(valueIndex);
                    this.headers.put(key, value);
                }

            }

            line = br.readLine();

            if (line != null) {
                
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

    public String getHeader(String header) {
        return this.headers.get(header);
    }

    public String getParameter(String parameter) {
        return this.parameters.get(parameter);
    }

}
