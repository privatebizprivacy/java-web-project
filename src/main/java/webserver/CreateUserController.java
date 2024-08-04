package webserver;

import java.util.Map;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class CreateUserController extends AbstractController {

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        // TODO Auto-generated method stub
        super.doPost(request, response);

        String body = request.getBody();
        Map<String, String> params = HttpRequestUtils.parseQueryString(body);
        User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));

        DataBase.addUser(user);
        response.sendRedirect("index.html");
    }
}
