package mini_tp7;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * ============================================================================
 *  CLIENTE DE LA NUBE  (mini TP7)
 * ============================================================================
 *
 *  QUÉ HACE
 *  --------
 *  1. Pide la IP del servidor (Enter solo = 127.0.0.1, la misma PC).
 *  2. Se conecta al puerto 5555.
 *  3. Muestra una consola "nube>" donde escribís comandos:
 *        LOGIN <usuario> <clave> | LIST | UPLOAD <nombre> | EXIT
 *  4. Por cada comando: lo MANDA, ESPERA la respuesta y la MUESTRA.
 *     (Es un walkie-talkie: hablo, escucho, hablo, escucho...)
 *
 *  ⚠️ ORDEN DE EJECUCIÓN: primero ▶ ServidorNube, después ▶ ClienteNube.
 *     Si no, sale "Connection refused" (llegaste y el local estaba cerrado).
 *
 *  📚 Del profe:
 *     - new Socket(IP, port) + Data streams        → Clase 5 (SocketTCP/conexion.java, cliente.java)
 *     - BufferedReader + while readLine + /exit    → Clase 5 (SocketTCPconsola/cliente.java)
 */
public class ClienteNube {

    public static void main(String[] args) throws IOException {

        // Teclado: caño System.in + adaptadores → readLine() te da la línea entera.
        // (Clase 2 → ProbandoClassReader.java)
        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

        // ---------------- PASO 1: ¿a qué IP me conecto? ----------------
        System.out.print(Colores.AMARILLO + "IP del servidor (Enter = 127.0.0.1): " + Colores.RESET);
        String ip = teclado.readLine();
        if (ip == null || ip.trim().isEmpty()) {
            ip = "127.0.0.1"; // "esta misma PC"
        }
        ip = ip.trim();

        // ---------------- PASO 2: conectarme ----------------
        // new Socket(...) "llama por teléfono". Si el servidor no está corriendo,
        // o la IP / el puerto están mal, acá salta "Connection refused".
        // try-with-resources: cierra el socket solo al terminar.
        try (Socket socket = new Socket(ip, ServidorNube.PUERTO);
             DataInputStream dis = new DataInputStream(socket.getInputStream());      // 🔊 escuchar
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) { // 🎤 hablar

            System.out.println(Colores.VERDE + "Conectado a " + ip + ":" + ServidorNube.PUERTO + Colores.RESET);
            System.out.println("Comandos: LOGIN <usuario> <clave> | LIST | UPLOAD <nombre> | EXIT");

            // ---------------- PASO 3: la consola ----------------
            while (true) {
                System.out.print(Colores.CYAN + "nube> " + Colores.RESET);
                String linea = teclado.readLine();

                if (linea == null) {          // se cerró la entrada (Ctrl+D / fin)
                    linea = "EXIT";
                }
                linea = linea.trim();
                if (linea.isEmpty()) {
                    continue;                 // Enter solo: no mando nada
                }

                // HABLAR: mando la línea tal cual; el servidor la interpreta.
                dos.writeUTF(linea);
                dos.flush();                  // ¡que salga YA!

                // ESCUCHAR: la primera respuesta siempre es "OK|..." o "ERROR|..."
                String respuesta = dis.readUTF();

                // Caso especial LIST: "OK|n" y después vienen n nombres más.
                String comando = linea.split("\\s+")[0].toUpperCase();
                if (comando.equals("LIST") && respuesta.startsWith("OK|")) {
                    int cantidad = Integer.parseInt(respuesta.substring(3)); // "OK|3" → 3
                    System.out.println(Colores.AMARILLO + "Archivos en la nube (" + cantidad + "):" + Colores.RESET);
                    for (int i = 0; i < cantidad; i++) {
                        System.out.println("  - " + dis.readUTF());          // un readUTF por nombre
                    }
                } else {
                    mostrar(respuesta);
                }

                // EXIT: lo mandamos y leímos la respuesta. RECIÉN AHORA salimos.
                // (Si cortáramos antes de mandarlo, el servidor no se entera.)
                if (comando.equals("EXIT")) {
                    break;
                }
            }
        }

        System.out.println("Sesión cerrada.");
    }

    /**
     * Muestra la respuesta del servidor:
     *   "OK|Bienvenido alumno"  → en verde, sin el "OK|"
     *   "ERROR|Usuario o..."    → en rojo, con [ERROR]
     *
     * substring(3) = "desde la posición 3 hasta el final" (saca "OK|").
     * substring(6) = saca "ERROR|" (6 letras).
     */
    private static void mostrar(String respuesta) {
        if (respuesta.startsWith("OK|")) {
            System.out.println(Colores.VERDE + respuesta.substring(3) + Colores.RESET);
        } else if (respuesta.startsWith("ERROR|")) {
            System.out.println(Colores.ROJO + "[ERROR] " + respuesta.substring(6) + Colores.RESET);
        } else {
            System.out.println(respuesta);
        }
    }
}
