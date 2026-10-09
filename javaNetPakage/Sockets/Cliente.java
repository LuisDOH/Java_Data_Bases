package Sockets;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Cliente {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 5000);
            if(!socket.isConnected()){
                System.out.println("Error en la conexion con el servidor");
                return;
            }

            OutputStream salida = socket.getOutputStream();
            String peticion = "saludar: Luis";
            salida.write(peticion.getBytes(StandardCharsets.UTF_8));
            salida.flush();


            InputStream entrada = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesLeidos = entrada.read(buffer);
            if(bytesLeidos == -1){
                System.out.println("No se obtuvo respuesta del servidor");
                return;
            }

            String response = new String(buffer, 0, bytesLeidos, StandardCharsets.UTF_8);
            System.out.println("Respuesta Servidor >> " + response);

            socket.close();
        }
        catch (Exception err){
            System.out.println("Algo ha salido mal " + err);
        }
    }
}
