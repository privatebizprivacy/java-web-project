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

    public void doGet(HttpRequest request, HttpResponse response) {
        request.httpRequestProcess();
    };

    public void doPost(HttpRequest request, HttpResponse response) {
        request.httpRequestProcess();
    };
}
