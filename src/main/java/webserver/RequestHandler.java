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

import db.DataBase;
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
            boolean isLogined = false;
            int contentLength = 0;

            while (!"".equals(line)) {
                if (null == line) {
                    return;
                }

                if (line.startsWith("Content-Length:")) {
                    contentLength = WebUtil.getContentLength(line);
                }

                if (line.startsWith("Cookie:")) {
                    Map<String, String> cookies = HttpRequestUtils.parseCookies(line);
                    isLogined = Boolean.parseBoolean(cookies.get("logined"));
                }

                log.info(line);
                line = br.readLine();
            }

            // 요청 처리
            int index = url.indexOf("?");
            String requestPath = index > -1 ? url.substring(0, index) : url;
            String path = requestPath;

            if (requestPath.equals("/user/create")) {
                // String params = url.substring(index + 1);

                String content = IOUtils.readData(br, contentLength);
                Map<String, String> parameters = HttpRequestUtils.parseQueryString(content);
                User user = new User(parameters.get("userId"), parameters.get("password"), parameters.get("name"),
                        parameters.get("email"));

                // 로그 출력
                log.info(user.toString());
                DataBase.addUser(user);
                path = "/index.html";
            } else if (requestPath.equals("/user/login")) {
                String content = IOUtils.readData(br, contentLength);
                Map<String, String> parameters = HttpRequestUtils.parseQueryString(content);

                User user = DataBase.findUserById(parameters.get("userId"));

                if (null != user && user.getPassword().equals(parameters.get("password"))) {
                    path = "/index.html";
                    isLogined = true;
                } else {
                    path = "/user/login_failed.html";
                }

            }

            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());

            if (requestPath.equals("/user/create") || requestPath.equals("/user/login")) {
                response302Header(dos, path, isLogined);
            } else if (requestPath.equals("/user/list")) {

                if (isLogined) {

                    StringBuilder sb = new StringBuilder("");

                    int num = 0;

                    for (User user : DataBase.findAll()) {
                        num++;
                        sb.append("<tr>");
                        sb.append("<th>");
                        sb.append(num);
                        sb.append("</th> <th>");
                        sb.append(user.getUserId());
                        sb.append("</th> <th>");
                        sb.append(user.getName());
                        sb.append("</th> <th>");
                        sb.append(user.getEmail());
                        sb.append("</th><th></th>");
                        sb.append("</tr>");
                    }

                    body = new String(body).replace("{}", sb.toString()).getBytes();

                    response200Header(dos, body.length, isLogined);

                } else {
                    path = "/index.html";
                    body = Files.readAllBytes(new File("./webapp" + path).toPath());
                    response302Header(dos, path, isLogined);
                }

            } else {
                response200Header(dos, body.length, isLogined);
            }

            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, boolean isLogined) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            if (isLogined) {
                dos.writeBytes("Set-Cookie: logined=true \r\n");
            } else {
                dos.writeBytes("Set-Cookie: logined=false \r\n");
            }
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String path, boolean isLogined) {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            if (isLogined) {
                dos.writeBytes("Set-Cookie: logined=true \r\n");
            } else {
                dos.writeBytes("Set-Cookie: logined=false \r\n");
            }
            dos.writeBytes("\r\n");

        } catch (IOException e) {
            // TODO: handle exception
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
