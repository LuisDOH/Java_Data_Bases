Guía Completa de la Clase Socket en Java

Un `Socket` representa un endpoint de comunicación TCP entre dos procesos a través de una red (requiere una dirección IP, un puerto y un mecanismo de transporte confiable).
---
1. Estructura y Ciclo de Vida
Internamente, un socket gestiona flujos bidireccionales mediante `InputStream` (recibe datos) y `OutputStream` (envía datos).
Proceso de Conexión (Handshake TCP)
```text
Socket -> DNS -> IP -> SYN -> SYN ACK -> ACK -> Conexión establecida
```
Estados del Socket
```text
CREATED → CONNECTING → CONNECTED → DATA TRANSFER → CLOSED
```
---
2. Constructores Principales
Por host y puerto:
```java
  Socket socket = new Socket("google.com", 80);
  ```
Usando `InetAddress`:
```java
  InetAddress address = InetAddress.getByName("google.com");
  Socket socket = new Socket(address, 80);
  ```
Con IP y puerto local específicos (útil en balanceadores, VPNs y servidores multi-homed):
```java
  Socket socket = new Socket(serverAddress, serverPort, localAddress, localPort);
  ```
---
3. Métodos de Conexión y Configuración Inicial
`connect(SocketAddress endpoint)`: Conecta un socket previamente creado.
`connect(SocketAddress endpoint, int timeout)`: Conecta especificando un tiempo máximo de espera (ej. `5000` ms) para evitar bloqueos indefinidos.
`bind(SocketAddress localaddr)`: Asocia explícitamente el socket a una IP y puerto local.
---
4. Envío y Recepción de Datos (E/S)
Son los métodos principales para la transferencia de información:
`getInputStream()`: Retorna el flujo de entrada (`InputStream`) para leer datos del servidor de forma bloqueante.
```java
  InputStream in = socket.getInputStream();
  byte[] buffer = new byte[1024];
  int bytesRead = in.read(buffer);
  ```
`getOutputStream()`: Retorna el flujo de salida (`OutputStream`) para enviar datos.
```java
  OutputStream out = socket.getOutputStream();
  out.write("Hola".getBytes());
  ```
---
5. Información Remota y Local
Permiten inspeccionar los extremos de la conexión TCP.
Método	Descripción	Ejemplo de Retorno
`getInetAddress()`	Obtiene la IP del host remoto.	`/142.250.190.78`
`getPort()`	Obtiene el puerto remoto.	`80`
`getRemoteSocketAddress()`	Obtiene IP y puerto remoto juntos.	`google.com/142.250.190.78:80`
`getLocalAddress()`	Obtiene la IP local utilizada.	`/192.168.1.100`
`getLocalPort()`	Obtiene el puerto local asignado.	`52341`
`getLocalSocketAddress()`	Obtiene IP y puerto local juntos.	`192.168.1.100:52341`
> **Nota importante:** `getPort()` devuelve el puerto **remoto** (por ejemplo, 80 para HTTP), no el puerto local. Para el puerto local se debe usar `getLocalPort()`.
---
6. Configuración de Tiempos y Propiedades TCP
Tiempos de Espera
`setSoTimeout(int timeout)` / `getSoTimeout()`: Define o consulta el tiempo máximo de espera en milisegundos para operaciones de lectura (`in.read()`). Si expira, lanza una `SocketTimeoutException`.
Propiedades Avanzadas TCP
Nagle (`setTcpNoDelay(boolean on)`): Activa (`true`) para enviar datos inmediatamente sin agrupar paquetes pequeños (ideal para videojuegos o trading en tiempo real).
KeepAlive (`setKeepAlive(boolean on)`): Envía comprobaciones periódicas en segundo plano para detectar conexiones inactivas o caídas.
ReuseAddress (`setReuseAddress(boolean on)`): Permite reutilizar puertos locales inmediatamente tras reiniciar un servidor.
Buffers de Red
`setSendBufferSize()` / `getSendBufferSize()`: Controla el tamaño del buffer de salida.
`setReceiveBufferSize()` / `getReceiveBufferSize()`: Controla el tamaño del buffer de entrada para prevenir pérdida de datos masivos.
---
7. Cierre de Conexión y Diagnóstico
Cierre Parcial y Total
`shutdownInput()`: Cierra únicamente el flujo de entrada (ya no se reciben datos, pero se puede seguir enviando).
`shutdownOutput()`: Cierra únicamente el flujo de salida (ideal para indicar fin de envío en ciertos protocolos).
`close()`: Cierra completamente el socket, liberando flujos, buffers y puertos del sistema operativo. Se recomienda utilizar bloques `try-with-resources` para evitar fugas de recursos.
Métodos de Diagnóstico (Estado)
`isConnected()`: Indica si el socket llegó a establecer conexión exitosamente.
`isBound()`: Verifica si está asociado a una dirección local.
`isClosed()`: Comprueba si el socket ya fue cerrado.
`isInputShutdown()` / `isOutputShutdown()`: Verifican el estado de cierre de los flujos individuales.
`toString()`: Retorna una representación textual del socket.
