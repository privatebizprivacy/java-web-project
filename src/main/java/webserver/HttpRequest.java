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

    private Map<String, String> headers;

    private Map<String, String> params;

    private Map<String, String> cookies;

    RequestLine requestLine;

    public HttpRequest(InputStream in) {
        this.in = in;
        this.params = new HashMap<>();
        this.headers = new HashMap<>();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            if (line == null) {
                return;
            }

            requestLine = new RequestLine(line);

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

            cookies = HttpRequestUtils.parseCookies(getHeader("Cookie"));

            if (getMethod().isPost()) {
                String body = IOUtils.readData(br, Integer.parseInt(headers.getOrDefault("Content-Length", "0")));
                params = HttpRequestUtils.parseQueryString(body);
            } else {
                params = requestLine.getParams();
            }
        } catch (IOException e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }

    }

    public HttpMethod getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String header) {
        return headers.get(header);
    }

    public String getParameter(String parameter) {
        return params.get(parameter);
    }

    public String getCookie(String key) {
        return cookies.get(key);
    }
}
