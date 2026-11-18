package ejercicios;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/** C1 - Cliente eco: manda lo que escribís y muestra la respuesta. Sirve también para D2. */
public class C1_ClienteEco {

    public static void main(String[] args) throws IOException {
        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

        try (Socket socket = new Socket("127.0.0.1", C1_ServidorEco.PUERTO);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            System.out.println("Conectado. Escribí algo (EXIT para salir).");
            while (true) {
                System.out.print("> ");
                String linea = teclado.readLine();
                if (linea == null) {
                    linea = "EXIT";
                }
                dos.writeUTF(linea);
                dos.flush();
                String resp = dis.readUTF();
                System.out.println(resp);
                if (linea.equalsIgnoreCase("EXIT")) {
                    break;
                }
            }
        }
    }
}
