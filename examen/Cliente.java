package examen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Código único otorgado por el servidor del profesor: ____-____

/**
 * CLIENTE con DOS hilos:
 *  - hilo "Escucha": lee todo lo que manda el servidor y lo muestra
 *    (intenta desencriptar en silencio; si no puede, muestra el original)
 *  - hilo main: lee el teclado y lo manda
 * Comandos: /hi · /logout · /cifrar <texto> (manda el texto cifrado) · texto común = chat
 */
public class Cliente {

    public static void main(String[] args) throws IOException {
        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

        System.out.print(Colores.AMARILLO + "Ingrese IP: " + Colores.RESET);
        String ip = teclado.readLine().trim();
        System.out.print(Colores.AMARILLO + "Ingrese puerto: " + Colores.RESET);
        int puerto = Integer.parseInt(teclado.readLine().trim());

        Socket socket = new Socket(ip, puerto);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        System.out.println(Colores.VERDE + "\n[SISTEMA]\tConectado a " + ip + ":" + puerto + Colores.RESET);
        System.out.println(Colores.AMARILLO + "[SISTEMA]\tComandos: /hi · /logout · /cifrar <texto>\n" + Colores.RESET);

        // ---------- Hilo 1: ESCUCHA al servidor ----------
        Thread escucha = new Thread(() -> {
            try {
                String linea;
                while ((linea = in.readLine()) != null) {
                    String msg = Cifrado.intentarDesencriptar(linea);   // silencioso
                    System.out.println(Colores.CIAN + "[SERVIDOR]\t" + msg + Colores.RESET);
                }
            } catch (IOException e) {
                // socket cerrado
            }
            System.out.println(Colores.AMARILLO + "[SISTEMA]\tConexión cerrada" + Colores.RESET);
        }, "Escucha");
        escucha.start();

        // ---------- Hilo 2 (main): TECLADO → servidor ----------
        String linea;
        while ((linea = teclado.readLine()) != null) {
            linea = linea.trim();
            if (linea.isEmpty()) {
                continue;
            }

            if (linea.startsWith("/cifrar ")) {
                String texto = linea.substring(8);
                try {
                    out.println(Cifrado.encriptar(texto));
                    System.out.println(Colores.MORADO + "[YO]\t\t" + texto + " (cifrado)" + Colores.RESET);
                } catch (Exception e) {
                    System.out.println(Colores.ROJO + "[ERROR]\tNo se pudo cifrar" + Colores.RESET);
                }
                continue;
            }

            out.println(linea);                                         // /hi, /logout o chat
            System.out.println(Colores.MORADO + "[YO]\t\t" + linea + Colores.RESET);

            if (linea.equals("/logout")) {
                break;
            }
        }

        try {
            Thread.sleep(300);          // deja que llegue el "Hasta luego"
        } catch (InterruptedException e) {
            // nada
        }
        socket.close();
    }
}
