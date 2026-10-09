package Sockets;

import javax.imageio.spi.ServiceRegistry;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Servidor {
    public static void main(String[] args) {
        try {
            // 1 CREACION DEL SOCKET
            ServerSocket server = new ServerSocket(5000);

            // Preparamos nuestro server para recibir peticiones de manera continua
            while(true){
                Socket cliente = null;
                System.out.println("Esterando nueva conexion con cliente ... ");

                // 2 CONEXION CON EL CLIENTE
                cliente = server.accept();
                System.out.println("Cliente conectado con exito: " + cliente.toString());

                // 4 LECTURA Y PROCESAMIENTO DE LA ENTRADA
                InputStream entrada =  cliente.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesLeidos =  entrada.read(buffer);

                //Verificar si hay texto de entrada
                if(bytesLeidos == -1){
                    System.out.println("No hay mensaje disponible por parte del cliente ...");
                    return;
                }

                String request = new String(buffer, 0, bytesLeidos, StandardCharsets.UTF_8);
                System.out.println("Peticion leida con exito, Cliente >> " + request);

                // 5 PROCESANDO LA RESPUESTA
                OutputStream salida = cliente.getOutputStream();
//                String response = "Proceso completado en el servidor";
                String response = procesarPeticion(request);

                salida.write(response.getBytes(StandardCharsets.UTF_8));
                salida.flush();

                // 6 CERRAR CONEXION
                cliente.close();
            }
        }
        catch (Exception err){
            System.out.println("Algo ha salido mal " + err);
        }
    }


    public static String procesarPeticion(String peticion){
        if (peticion == null || peticion.trim().isEmpty()) {
            return "ERROR: Petición vacía";
        }

        String[] partes = peticion.split(":", 2);
        String request = partes[0].toUpperCase().trim();
        String parametro = partes.length > 1 ? partes[1].trim() : "";

        switch (request){
            case "SALUDAR":
                return "Hola " + parametro;
            case "PU":
                return "Informacion agregada con exito" + parametro;

            default:
                return "La operacion en invalida";
        }
    }
}
