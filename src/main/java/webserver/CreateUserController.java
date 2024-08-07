package webserver;

import db.DataBase;
import model.User;

public class CreateUserController extends AbstractController {

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        // TODO Auto-generated method stub
        super.doPost(request, response);

        User user = new User(request.getParameter("userId"), request.getParameter("password"),
                request.getParameter("name"), request.getParameter("email"));

        DataBase.addUser(user);
        response.sendRedirect("index.html");
    }
}
