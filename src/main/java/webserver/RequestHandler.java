package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);

            // 헤더 출력
            String line = br.readLine();
            String url = WebUtil.getURI(line);
            int contentLength = 0;

            while (!"".equals(line)) {
                if (null == line) {
                    return;
                }

                if (line.contains("Content-Length")) {
                    contentLength = WebUtil.getContentLength(line);
                }

                log.info(line);
                line = br.readLine();
            }

            log.info("##################요청처리###############################");

            // 요청 처리
            int index = url.indexOf("?");
            String requestPath = index > -1 ? url.substring(0, index) : url;

            if (requestPath.equals("/user/create")) {
                // String params = url.substring(index + 1);
                String content = IOUtils.readData(br, contentLength);
                Map<String, String> parameters = HttpRequestUtils.parseQueryString(content);
                User user = new User(parameters.get("userId"), parameters.get("password"), parameters.get("name"),
                        parameters.get("email"));

                // 로그 출력
                log.info(user.toString());
            }

            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + requestPath).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
