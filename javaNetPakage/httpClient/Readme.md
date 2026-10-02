# Cliente java.net.http.HttpClient

El cliente `java.net.http.HttpClient` (introducido de forma definitiva en Java 11) reemplaza el antiguo `HttpURLConnection` con una API moderna, fluida y con soporte nativo para HTTP/2, ejecución asíncrona y reactiva.


## Componentes clave del módulo `java.net.http`
* `HttpClient`: La entidad principal encargada de gestionar la configuración de las conexiones (versión HTTP, tiempo de espera, redirecciones).

* `HttpRequest`: Representa la petición HTTP (URL, método, encabezados, cuerpo). Se construye mediante un patrón Builder.

* `HttpResponse<T>`: Contiene el resultado de la petición (código de estado, encabezados y el cuerpo convertido al tipo especificado T).

* `BodyHandlers`: Define cómo procesar la respuesta entrante (como un String, arreglo de bytes, archivo, etc.).

### `HttpClient`: Condiguracion del cliente 

<img width="1203" height="460" alt="image" src="https://github.com/user-attachments/assets/2c8088c0-6737-4902-b95a-ab9b3a444127" />

### `HttpRequest`: Definición de la Petición

Define la meta-información y el cuerpo enviado. Se construye con HttpRequest.newBuilder().

* URI / Endpoint: `.uri(URI.create("https://..."))`

* Timeout de la Petición: `.timeout(Duration.ofSeconds(5))` (cancela la petición si el servidor no responde dentro del tiempo).

__Encabezados:__

*`.header("Content-Type", "application/json")` (añade o reemplaza un header)

*`.headers("Key1", "Val1", "Key2", "Val2")` (añade múltiples en pares)


__Métodos HTTP y `BodyPublishers` (Publicadores del Cuerpo)__

Para enviar datos en POST, PUT o PATCH, se utiliza un BodyPublisher:

````java
// Métodos sin cuerpo
.GET()
.DELETE()

// Métodos con cuerpo (reciben un HttpRequest.BodyPublisher)
.POST(HttpRequest.BodyPublishers.ofString("{\"key\":\"value\"}"))
.PUT(HttpRequest.BodyPublishers.ofByteArray(bytes))
.method("PATCH", HttpRequest.BodyPublishers.ofFile(Path.of("data.json")))

````

*** Ejemplo de implementación ***

````java
// 1. Crear el cliente HTTP
            HttpClient client = HttpClient.newHttpClient();

            // 2. Construir la petición HTTP (GET)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://stlprolab.net/"))
                    .header("User-Agent", "Java HttpClient") // GitHub requiere User-Agent
                    .GET() // Indica que es una petición GET
                    .build();});
````


__Generadores de cuerpo comunes (`HttpRequest.BodyPublishers`):__

* `noBody()`: Peticiones POST/PUT vacías.
* `ofString(String)`: Envía texto (JSON, XML, texto plano).
* `ofFile(Path)`: Transfiere un archivo local eficientemente.
* `ofByteArray(byte[])`: Envía un arreglo de bytes crudo.
* `ofInputStream(() -> ...)`: Útil para streams de gran tamaño.

### Procesador de la respuesta `BodyHandlers`
Cuando la respuesta llega del servidor, el `HttpClient` necesita saber cómo transformar los bytes recibidos en un objeto Java. Para eso se usan los `BodyHandlers`.


___Tipos de `HttpResponse.BodyHandlers`__
Dependiendo del tipo de respuesta que esperamos de nuestra petición, podemos seleccionar uno de los siguientes tipos de procesadores.

* `BodyHandlers.ofString()`: Convierte la respuesta en un String (ideal para JSON/XML).
* `BodyHandlers.ofFile(Path.of("download.pdf"))`: Escribe el cuerpo directamente en disco sin cargar todo en memoria RAM.
* `BodyHandlers.ofByteArray()`: Retorna byte[] (imágenes, archivos binarios).
* `BodyHandlers.ofLines()`: Retorna un Stream<String> para procesar respuesta línea por línea.
* `BodyHandlers.discarding()`: Ignora el cuerpo (útil cuando solo importa el código de estado HTTP).


### Procesador de la respuesta `HttpResponse`
Una vez recibido, el objeto `HttpResponse<T>` puede exponer los siguientes metodos que podemos aprovechar:

* `.statusCode()`: Código HTTP (200, 404, 500, etc.).
* `.body()`: El cuerpo transformado según el BodyHandler usado.
* `.headers()`: HttpHeaders recibidos en la respuesta.
* `.uri()`: La URI final (útil si hubo redirecciones).

> ***Nota:*** Si bien a la hora de recibir la respuesta en un proceso sincrono lo anterior es suficiente, eso cambia sí se trata de una petición ***Asíncrona***. 

## Procesos asíncronicos
El primer cambio a notar es que a la hora de enviar la peticíon al servidor el método de envio cambiara de `client.send(...)` a `client.sendAsync(...)`. Además, se debe implementar un encadenamiendo para el procesamiento de la respuesta.

El método `client.sendAsync(...)` no devuelve la respuesta inmediatamente; devuelve una promesa de respuesta llamada `CompletableFuture<HttpResponse<T>>`.

El encadenamiento es la capacidad de concatenar operaciones reactivas que se ejecutarán automáticamente una tras otra en segundo plano a medida que los datos estén disponibles, sin bloquear el hilo principal de la aplicación.


````

Petición de Red ---> [ sendAsync ]
                          |
             (al recibir respuesta de red)
                          v
                    [ .thenApply ]  ---> Transforma los datos (ej. extraer status/body)
                          |
              (al terminar transformación)
                          v
                   [ .thenAccept ]  ---> Consume el resultado final (ej. imprimir/guardar)

````

### Operadores de Encadenamiento más Frecuentes
1. `thenApply(Function)` (Transformación): Recibe el resultado actual, le aplica una función y devuelve un nuevo valor.

````java
future.thenApply(response -> response.body().toUpperCase());
````

2. `thenAccept(Consumer)` (Consumo): Recibe el resultado final y realiza una acción sin retornar nada (efecto secundario).

````java
future.thenAccept(body -> System.out.println("Imprimiendo: " + body));
````


3. `exceptionally(Function)` (Manejo de Errores): Captura cualquier excepción ocurrida en cualquier paso anterior del flujo.

````java
future.exceptionally(ex -> {
    System.err.println("Falló la petición: " + ex.getMessage());
    return null;
});
````

4. `thenCompose(Function)` (Encadenar otra tarea asíncrona): Sirve para hacer una segunda petición HTTP que dependa del resultado de la primera.
