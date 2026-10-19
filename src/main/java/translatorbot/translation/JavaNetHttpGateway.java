package translatorbot.translation;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class JavaNetHttpGateway implements HttpGateway {

    private final HttpClient client;

    public JavaNetHttpGateway() {
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public String get(String url) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Запрос прерван", e);
        }
    }
}
