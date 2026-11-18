package ejercicios;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** D2 - Servidor eco multi-cliente: un hilo por cliente (igual que ServidorNube). Cliente: C1_ClienteEco. */
public class D2_ServidorEcoMulti {

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(C1_ServidorEco.PUERTO)) {
            System.out.println("Servidor multi-cliente en el puerto " + C1_ServidorEco.PUERTO);
            int numero = 0;
            while (true) {
                Socket socket = server.accept();          // espera...
                numero++;
                System.out.println("Cliente " + numero + " conectado");
                new Thread(() -> C1_ServidorEco.atender(socket), "CLIENTE-" + numero).start();
                // ...y vuelve enseguida al accept()
            }
        }
    }
}
