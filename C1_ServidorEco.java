package ejercicios;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** C1 - Servidor eco: responde "OK|" + el texto en mayúsculas. Atiende UN cliente. */
public class C1_ServidorEco {

    public static final int PUERTO = 6000;

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PUERTO)) {
            System.out.println("Esperando cliente en el puerto " + PUERTO + "...");
            Socket socket = server.accept();
            System.out.println("Se conectó " + socket.getInetAddress().getHostAddress());
            atender(socket);
        }
    }

    /** Lo reusa D2 para atender cada cliente en su propio hilo. */
    public static void atender(Socket socket) {
        String hilo = Thread.currentThread().getName();
        try (socket;
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            while (true) {
                String msg = dis.readUTF();
                System.out.println("[" + hilo + "] recibí: " + msg);
                if (msg.equalsIgnoreCase("EXIT")) {
                    dos.writeUTF("OK|Chau");
                    dos.flush();
                    break;
                }
                dos.writeUTF("OK|" + msg.toUpperCase());
                dos.flush();
            }
        } catch (IOException e) {
            System.out.println("[" + hilo + "] cliente desconectado: " + e.getMessage());
        }
        System.out.println("[" + hilo + "] fin de la sesión");
    }
}
