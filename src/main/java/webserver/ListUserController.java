package webserver;

import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class ListUserController extends AbstractController {

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        // TODO Auto-generated method stub
        super.doGet(request, response);

        boolean logined = false;
        String header = request.getHeader("Set-Cookie");

        if (header != null) {
            Map<String, String> cookies = HttpRequestUtils.parseCookies(header);
            logined = isLogined(cookies.getOrDefault("isLogined", "false"));
        }

        if (!logined) {
            Collection<User> users = DataBase.findAll();
            StringBuilder sb = new StringBuilder();
            sb.append("<table border='1'>");
            for (User user : users) {
                sb.append("<tr>");
                sb.append("<td>" + user.getUserId() + "</td>");
                sb.append("<td>" + user.getName() + "</td>");
                sb.append("<td>" + user.getEmail() + "</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");

            response.forwardBody(sb.toString());
        }
    }

    private boolean isLogined(String line) {
        return Boolean.parseBoolean(line);
    }
}
