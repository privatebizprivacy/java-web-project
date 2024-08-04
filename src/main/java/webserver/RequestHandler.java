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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    private Map<String, Controller> controllers;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
        this.controllers = new HashMap<>();

        this.controllers.put("/user/create", new CreateUserController());
        this.controllers.put("/user/login", new LoginController());
        this.controllers.put("/user/list", new ListUserController());
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream();
                OutputStream out = connection.getOutputStream();) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            Controller controller = this.controllers.get(request.getPath());

            String contentLength = request.getHeader("Content-Length");
            if (contentLength != null) {
                response.addHeader("Content-Length", contentLength);
            }

            if (controller != null) {
                controller.service(request, response);
            } else {
                response.forward(request.getPath());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void run2() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream();
                OutputStream out = connection.getOutputStream();) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            BufferedReader br = new BufferedReader(reader);

            // 헤더 출력
            String line = br.readLine();
            String url = WebUtil.getURI(line);
            int contentLength = 0;
            String contentType = "text/html;charset=utf-8";
            boolean isLogined = false;

            log.debug("request line : {}", line);

            if (null == line) {
                return;
            }

            while (!"".equals(line)) {

                if (line.startsWith("Content-Length:")) {
                    contentLength = WebUtil.getContentLength(line);
                }

                if (line.startsWith("Cookie:")) {
                    String cookieLine = line.replace("Cookie: ", "");
                    Map<String, String> cookies = HttpRequestUtils.parseCookies(cookieLine);
                    isLogined = Boolean.parseBoolean(cookies.get("logined"));
                }

                if (line.startsWith("Accept: text/css")) {
                    contentType = "text/css";
                }

                line = br.readLine();
                log.debug("header : {}", line);
            }

            // 요청 처리
            String requestPath = getRequestPath(url);
            String path = requestPath;

            if (requestPath.equals("/user/create")) {
                String content = IOUtils.readData(br, contentLength);
                User user = createUser(content);
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
            byte[] body = null;

            if (requestPath.equals("/index.html")) {
                path = "/index.html";
                body = Files.readAllBytes(new File("./webapp" + path).toPath());
                response200Header(dos, body.length, contentType);
                dos.writeBytes("\r\n");
                responseBody(dos, body);
            } else if (requestPath.equals("/user/create")) {
                body = Files.readAllBytes(new File("./webapp" + path).toPath());
                response302Header(dos, path);
            } else if (requestPath.equals("/user/login")) {
                body = Files.readAllBytes(new File("./webapp" + path).toPath());
                response302Header(dos, path);
                if (isLogined) {
                    dos.writeBytes("Set-Cookie: logined=true; \r\n");
                } else {
                    dos.writeBytes("Set-Cookie: logined=false; \r\n");
                }
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

                    path = "/user/list.html";
                    body = Files.readAllBytes(new File("./webapp" + path).toPath());
                    body = new String(body).replace("{}", sb.toString()).getBytes();
                    response200Header(dos, body.length, contentType);
                    dos.writeBytes("\r\n");
                    responseBody(dos, body);
                } else {
                    log.info("실패함");
                    path = "/index.html";
                    body = Files.readAllBytes(new File("./webapp" + path).toPath());
                    response302Header(dos, path);
                }

            } else {
                body = Files.readAllBytes(new File("./webapp" + path).toPath());
                response200Header(dos, body.length, contentType);
                dos.writeBytes("\r\n");
                responseBody(dos, body);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String ContentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + ContentType + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            dos.writeBytes("Location: " + path + "\r\n");
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

    private String getRequestPath(String url) {

        int index = url.indexOf("?");
        String requestPath = index > -1 ? url.substring(0, index) : url;
        return requestPath;
    }

    private User createUser(String content) {
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(content);
        User user = new User(parameters.get("userId"), parameters.get("password"), parameters.get("name"),
                parameters.get("email"));
        return user;
    }

    private User getUser(String content) {
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(content);
        User user = DataBase.findUserById(parameters.get("userId"));
        return user;
    }
}
