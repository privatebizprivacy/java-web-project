package webserver;

public abstract class AbstractController implements Controller {

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        // TODO Auto-generated method stub
        if (request.getMethod().equals("GET"))
            doGet(request, response);

        if (request.getMethod().equals("POST"))
            doPost(request, response);
    }

    protected void doGet(HttpRequest request, HttpResponse response) {
    };

    protected void doPost(HttpRequest request, HttpResponse response) {
    };
}
