package webserver;

public interface Controller {
    public void service(HttpRequest req, HttpResponse res);
}
