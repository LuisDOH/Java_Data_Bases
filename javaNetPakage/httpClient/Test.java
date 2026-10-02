import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Test {
    public static void main(String[] args) {
        try {
            // 1. Crear el cliente HTTP
            HttpClient client = HttpClient.newHttpClient();

            // 2. Construir la petición HTTP (GET)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://stlprolab.net/"))
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", "Java HttpClient") // GitHub requiere User-Agent
                    .GET() // Indica que es una petición GET
                    .build();

            // 3. Enviar la petición y recibir la respuesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 4. Leer los datos de la respuesta
            int codigoRespuesta = response.statusCode();
            String respuesta = response.body();

            System.out.println("Código HTTP: " + codigoRespuesta);
            System.out.println("Respuesta (primeros 100 caracteres): " + respuesta);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
