package examen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Código único otorgado por el servidor del profesor: ____-____

/**
 * SERVIDOR LOCAL multicliente:
 *  - un hilo por cliente
 *  - bienvenida en texto plano al conectarse
 *  - /hi → bienvenida otra vez · /logout → cierra ese cliente
 *  - texto sin "/" → se reenvía a TODOS (incluido quien lo mandó)
 *  - otro comando con "/" → COMANDO DESCONOCIDO
 */
public class Servidor {

    static final int PUERTO = 5000;
    static final String BIENVENIDA = "BIENVENIDO AL SERVIDOR LOCAL";

    /** Los "canales de salida" de todos los conectados, para el chat. */
    static final List<PrintWriter> clientes = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PUERTO);
        System.out.println(Colores.AMARILLO + "[SISTEMA]\tServidor escuchando en el puerto " + PUERTO + Colores.RESET);

        int numero = 0;
        while (true) {
            Socket socket = server.accept();                       // main: espera conexiones
            numero++;
            String nombre = "Cliente-" + numero;
            System.out.println(Colores.VERDE + "[CONEXIÓN]\t" + nombre + " desde "
                    + socket.getInetAddress().getHostAddress() + Colores.RESET);
            new Thread(() -> atender(socket, nombre), nombre).start(); // un hilo por cliente
        }
    }

    /** Lo ejecuta el hilo de cada cliente. */
    static void atender(Socket socket, String nombre) {
        PrintWriter out = null;
        try {
            // BufferedReader para LEER líneas, PrintWriter para ESCRIBIR líneas.
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(socket.getOutputStream(), true);  // true = autoFlush en println

            out.println(BIENVENIDA);                               // bienvenida SIEMPRE en plano
            clientes.add(out);

            String linea;
            while ((linea = in.readLine()) != null) {              // null = el cliente se desconectó
                // Si llegó cifrado lo desencripta; si era plano, queda igual.
                String msg = Cifrado.intentarDesencriptar(linea).trim();
                System.out.println(Colores.CIAN + "[" + nombre + "]\t" + msg + Colores.RESET);

                if (msg.equals("/hi")) {
                    out.println(BIENVENIDA);
                } else if (msg.equals("/logout")) {
                    out.println("Hasta luego");
                    break;
                } else if (msg.startsWith("/")) {
                    out.println("COMANDO DESCONOCIDO");
                } else if (!msg.isEmpty()) {
                    enviarATodos(nombre + ": " + msg);
                }
            }
        } catch (IOException e) {
            // se cortó la conexión: no hacemos nada, se limpia abajo
        } finally {
            if (out != null) {
                clientes.remove(out);
            }
            try {
                socket.close();
            } catch (IOException e) {
                // ya estaba cerrado
            }
            System.out.println(Colores.AMARILLO + "[SISTEMA]\t" + nombre + " se desconectó" + Colores.RESET);
        }
    }

    /** Reenvía a todos los conectados (incluido quien lo mandó). */
    static void enviarATodos(String msg) {
        synchronized (clientes) {                              // nadie toca la lista mientras recorro
            for (PrintWriter c : clientes) {
                c.println(msg);
            }
        }
    }
}
