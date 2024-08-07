package webserver;

import java.util.Map;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class LoginController extends AbstractController {

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        super.doPost(request, response);
        User user = DataBase.findUserById(request.getParameter("userId"));

        if (user != null) {
            if (user.login(request.getParameter("password"))) {
                response.addHeader("Set-cookie", "logined=true");
                response.sendRedirect("/index.html");
            }
        } else {
            response.forward("/user/login_failed.html");
        }
    }
}
