# InetAddress
La clase InetAddress pertenece al paquete java.net y representa una dirección IP dentro de Java. Puede manejar tanto direcciones IPv4 (32 bits) como IPv6 (128 bits). Un objeto InetAddress contiene una dirección IP y, opcionalmente, el nombre de host asociado.

InetAddress actúa como la abstracción que permite convertir entre nombres de host e IPs.

````
google.com
      ↓
 InetAddress
      ↓
142.250.190.78
````


A diferencia de otras clases, la clase InetAddress se instancia mediante Factory Method, por lo que no cuenta con un constructor publico.

## Metodos importantes
### getByName()
Obtiene una dirección IP a partir de un hostname. Java consulta el sistema de resolución de nombres configurado (normalmente DNS) para obtener la IP correspondiente

````java
InetAddress address = InetAddress.getByName("google.com");

System.out.println(address);
````

### getAllByName()
Obtiene todas las IPs asociadas a un host. Es decir la lista de todas las Ips registradas en el DNS.

````java
InetAddress[] addresses =
        InetAddress.getAllByName("google.com");

for(InetAddress address : addresses){
    System.out.println(address);
}

````


### getLocalHost()
Obtiene información del equipo local.

````java
InetAddress local =
        InetAddress.getLocalHost();

System.out.println(local);

````


### getHostName()

Retorna el nombre del host.

````java
InetAddress address =
        InetAddress.getByName("google.com");

System.out.println(
        address.getHostName());

````

### getHostAddress()

Retorna únicamente la IP del host.

````java
InetAddress address =
        InetAddress.getByName("google.com");

System.out.println(
        address.getHostAddress());

````

### getCanonicalHostName()

Obtiene el nombre de dominio completamente calificado (FQDN).

````java
System.out.println(
    address.getCanonicalHostName());
````


## Método para pruebas de connectividad isReachable()

Este metodo, permite realizar pruebas rapidas de accesibilidad del host antes de lanzar un proceso mas grande. Requiere de una IP para poder verificar si esta se encuentra accessible.

````java
InetAddress host =
        InetAddress.getByName("google.com");

boolean reachable =
        host.isReachable(5000);

System.out.println(reachable);
````

> *** Nota: Importante: no garantiza que el host responda igual que un comando ping, ya que depende del sistema operativo, permisos y configuración de red.***

## UnknownHostException
Al ser un proceso que esta sujeto a la accesibilidad del host o de la red, se establece esta excepción para gestionar errores cuando Java no puede resolver un nombre usando DNS o los mecanismos configurados de resolución de nombres.


````java
try {

    InetAddress address =
        InetAddress.getByName("servidorInexistente");

}
catch (UnknownHostException ex) {

    System.out.println(
        "Host no encontrado");
}
````


## Identificación tipos de direcciones

Loopback: epresenta al propio equipo.
````java
address.isLoopbackAddress()
````

Multicast: Identifica un grupo de dispositivos.
````java
address.isMulticastAddress()
````

Site-Local: Direcciones privadas.
````java
address.isSiteLocalAddress()
````

