package webserver;

import java.util.Map;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class LoginController extends AbstractController {

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        super.doPost(request, response);
        String body = request.getBody();
        Map<String, String> params = HttpRequestUtils.parseQueryString(body);
        User user = DataBase.findUserById(params.get(params.get("userId")));

        if (user != null) {
            if (user.login(params.get("password"))) {
                response.addHeader("Set-cookie", "logined=true");
                response.sendRedirect("/index.html");
            }
        } else {
            response.forward("/user/login_failed.html");
        }

    }
}
