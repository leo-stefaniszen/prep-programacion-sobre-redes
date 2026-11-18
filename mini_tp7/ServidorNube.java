package mini_tp7;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * ============================================================================
 *  SERVIDOR DE LA NUBE  (mini TP7: SIN hilos y SIN archivos)
 * ============================================================================
 *
 *  QUÉ HACE
 *  --------
 *  1. Abre el puerto 5555 y espera clientes.
 *  2. Atiende a UN cliente por vez (no hay hilos): conversa con él hasta que
 *     manda EXIT o se desconecta, y recién ahí vuelve a esperar al siguiente.
 *  3. Entiende estos comandos:
 *        LOGIN <usuario> <clave>   → inicia sesión
 *        LIST                      → muestra los "archivos" de la nube
 *        UPLOAD <nombre>           → agrega un nombre a la nube (en memoria)
 *        EXIT                      → termina la sesión
 *     Sin LOGIN, todo (menos EXIT) responde error.
 *
 *  EL PROTOCOLO (cómo se hablan cliente y servidor)
 *  ------------------------------------------------
 *  - Todo viaja como texto con writeUTF / readUTF.
 *  - El CLIENTE siempre habla primero (manda un comando).
 *  - El SERVIDOR siempre responde con "OK|mensaje" o "ERROR|mensaje".
 *  - Excepción: LIST responde "OK|n" y DESPUÉS manda n textos más (un nombre
 *    cada uno). Así el cliente sabe cuántos readUTF() tiene que hacer.
 *
 *  📚 Del profe:
 *     - ServerSocket / accept / DataInput/OutputStream → Clase 5 (SocketTCP/servidor.java)
 *     - split / toUpperCase / equals                   → Clase 1 (cadenas.java)
 *     - Map y ArrayList                                → Clase 2 (Colecciones.java)
 */
public class ServidorNube {

    // ------------------------------------------------------------------------
    // CONFIGURACIÓN
    // ------------------------------------------------------------------------

    /** Puerto donde escucha el servidor. El cliente tiene que usar EL MISMO. */
    public static final int PUERTO = 5555;

    /**
     * Usuarios válidos:  usuario → clave.
     * Un Map es como una agenda: buscás por el nombre (clave del Map) y te da
     * el valor (la contraseña). Si el usuario no existe, get() devuelve null.
     */
    private static final Map<String, String> USUARIOS = Map.of(
            "admin", "1234",
            "alumno", "clave123",
            "invitado", "0000"
    );

    /**
     * "La nube": como no usamos archivos, guardamos solo los NOMBRES en una
     * lista en memoria. Se pierde al cerrar el servidor (no se guarda en disco).
     * Es static para que la compartan TODOS los clientes que se van conectando.
     */
    private static final ArrayList<String> nube = new ArrayList<>();

    // ------------------------------------------------------------------------
    // MAIN: el bucle que atiende clientes de a uno
    // ------------------------------------------------------------------------

    public static void main(String[] args) throws IOException {

        // PASO 1: abrir la "central telefónica" en el puerto.
        //   Si el puerto está ocupado (ej: dejaste otro servidor abierto),
        //   acá salta "Address already in use".
        ServerSocket server = new ServerSocket(PUERTO);

        log(Colores.VERDE, "SERVIDOR", "Escuchando en el puerto " + PUERTO);
        mostrarIPs(); // para saber qué IP tiene que escribir el cliente

        // PASO 2: atender clientes para siempre, DE A UNO.
        while (true) {

            // accept() BLOQUEA: el programa se queda congelado en esta línea
            // hasta que un cliente se conecta. Cuando llega uno, devuelve
            // un Socket = "el teléfono" conectado con ESE cliente.
            log(Colores.AMARILLO, "SERVIDOR", "Esperando un cliente...");
            Socket socket = server.accept();

            String ip = socket.getInetAddress().getHostAddress();
            log(Colores.AZUL, "CONEXIÓN", "Se conectó " + ip);

            // PASO 3: conversar con este cliente hasta que se vaya.
            //   Mientras tanto, si llega OTRO cliente, queda esperando en la
            //   fila (para atender a varios a la vez harían falta hilos).
            atenderCliente(socket, ip);

            // Cuando atenderCliente() termina, el while vuelve al accept()
            // y espera al próximo.
        }
    }

    // ------------------------------------------------------------------------
    // LA CONVERSACIÓN CON UN CLIENTE
    // ------------------------------------------------------------------------

    /**
     * Habla con un cliente hasta que manda EXIT o se desconecta.
     *
     * Las variables de la SESIÓN (usuario logueado, streams) son locales a este
     * método: cuando el cliente se va, se borran y el próximo arranca de cero.
     */
    private static void atenderCliente(Socket socket, String ip) {

        // null = todavía no hizo LOGIN
        String usuario = null;

        // try-with-resources: al salir del try (bien o con error) cierra
        // automáticamente el socket y los dos streams. No hace falta close().
        try (socket;
             DataInputStream dis = new DataInputStream(socket.getInputStream());     // 🔊 para ESCUCHAR
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) { // 🎤 para HABLAR

            while (true) {

                // ESCUCHAR: readUTF() espera hasta que el cliente mande algo.
                String linea = dis.readUTF().trim();
                if (linea.isEmpty()) {
                    continue; // línea vacía: la ignoramos y esperamos otra
                }

                // SEPARAR comando y argumento:
                //   "upload  foto.jpg" → trim → split("\\s+", 2) → ["upload", "foto.jpg"]
                //   \\s+ = "uno o más espacios"; el 2 = "máximo 2 pedazos"
                String[] partes = linea.split("\\s+", 2);
                String comando = partes[0].toUpperCase();              // solo el comando en mayúsculas
                String argumento = partes.length > 1 ? partes[1] : ""; // "" si no vino argumento

                // Log del comando (la clave del LOGIN no se muestra)
                log(Colores.CYAN, "COMANDO", ip + " -> "
                        + (comando.equals("LOGIN") ? "LOGIN ****" : linea));

                // ---------------- EXIT: siempre permitido ----------------
                if (comando.equals("EXIT")) {
                    responder(dos, "OK|Hasta luego");
                    break; // salimos del while → se cierra la conexión
                }

                // ---------------- LOGIN: siempre permitido ----------------
                if (comando.equals("LOGIN")) {
                    usuario = login(dos, argumento, ip); // devuelve el usuario o null
                    continue;                            // volvemos a escuchar
                }

                // ---------------- El resto: EXIGE login ----------------
                if (usuario == null) {
                    log(Colores.ROJO, "DENEGADO", ip + " intentó " + comando + " sin LOGIN");
                    responder(dos, "ERROR|Debe autenticarse primero: LOGIN <usuario> <clave>");
                    continue;
                }

                switch (comando) {
                    case "LIST":
                        listar(dos);
                        break;
                    case "UPLOAD":
                        subir(dos, argumento, usuario);
                        break;
                    default:
                        responder(dos, "ERROR|Comando desconocido: " + comando);
                }
            }

        } catch (EOFException e) {
            // readUTF() tira EOFException si el cliente cerró la ventana
            // sin mandar EXIT ("se cortó la llamada").
            log(Colores.AMARILLO, "AVISO", ip + " se fue sin mandar EXIT");
        } catch (IOException e) {
            // Cualquier otro problema de red.
            log(Colores.ROJO, "ERROR", ip + ": " + e.getMessage());
        }

        log(Colores.AMARILLO, "DESCONECTADO", ip + (usuario != null ? " (" + usuario + ")" : ""));
    }

    // ------------------------------------------------------------------------
    // COMANDOS
    // ------------------------------------------------------------------------

    /**
     * LOGIN <usuario> <clave>
     * @return el nombre de usuario si las credenciales son correctas, o null.
     */
    private static String login(DataOutputStream dos, String argumento, String ip) throws IOException {
        // El argumento tiene que ser exactamente "usuario clave" (2 palabras).
        String[] datos = argumento.split("\\s+");
        if (datos.length != 2) {
            responder(dos, "ERROR|Uso: LOGIN <usuario> <clave>");
            return null;
        }

        String u = datos[0];
        String c = datos[1];

        if (credencialesValidas(u, c)) {
            log(Colores.VERDE, "LOGIN", ip + " inició sesión como " + u);
            responder(dos, "OK|Bienvenido " + u);
            return u;
        } else {
            log(Colores.ROJO, "LOGIN", ip + " credenciales incorrectas");
            responder(dos, "ERROR|Usuario o clave incorrectos");
            return null;
        }
    }

    /**
     * ¿Existe el usuario y la clave coincide?
     *
     * Se escribe c.equals(...) y NO USUARIOS.get(u).equals(c), porque si el
     * usuario no existe, get(u) devuelve null y null.equals(...) explota con
     * NullPointerException. En cambio "1234".equals(null) da false, sin explotar.
     *
     * ⚠️ NUNCA comparar Strings con == : compara si es el mismo objeto en
     *    memoria, no si dicen lo mismo. La clave llega por la red → es otro objeto.
     */
    private static boolean credencialesValidas(String u, String c) {
        return c.equals(USUARIOS.get(u));
    }

    /**
     * LIST
     * Protocolo: primero "OK|n" (cuántos hay) y después n writeUTF, uno por
     * nombre. El cliente lee el n y hace exactamente n readUTF().
     */
    private static void listar(DataOutputStream dos) throws IOException {
        dos.writeUTF("OK|" + nube.size());
        for (String nombre : nube) {      // for-each: recorre toda la lista
            dos.writeUTF(nombre);
        }
        dos.flush();                      // un solo flush al final: sale todo junto
    }

    /**
     * UPLOAD <nombre>
     * Versión SIN archivos: solo agrega el nombre a la lista "nube".
     * (En el TP7 real, acá además se reciben los bytes del archivo.)
     */
    private static void subir(DataOutputStream dos, String nombre, String usuario) throws IOException {
        if (nombre.isEmpty()) {
            responder(dos, "ERROR|Uso: UPLOAD <nombre>");
            return;
        }
        if (nube.contains(nombre)) {
            responder(dos, "ERROR|Ya existe " + nombre + " en la nube");
            return;
        }
        nube.add(nombre);
        log(Colores.VERDE, "UPLOAD", usuario + " subió " + nombre);
        responder(dos, "OK|Subido " + nombre);
    }

    // ------------------------------------------------------------------------
    // AYUDANTES
    // ------------------------------------------------------------------------

    /**
     * Manda un texto al cliente y lo "empuja" con flush().
     * Sin flush() el mensaje puede quedar trabado en el buffer y el cliente
     * se queda esperando para siempre.
     */
    private static void responder(DataOutputStream dos, String msg) throws IOException {
        dos.writeUTF(msg);
        dos.flush();
    }

    /** Imprime una línea de log con color y etiqueta: [ETIQUETA] mensaje */
    private static void log(String color, String etiqueta, String msg) {
        System.out.println(color + "[" + etiqueta + "] " + msg + Colores.RESET);
    }

    /**
     * Muestra las IPs de esta PC (lo mismo que ver ipconfig).
     * Sirve para saber qué IP escribir en el cliente si está en OTRA PC.
     * Si es en la misma PC, el cliente puede usar 127.0.0.1 o localhost.
     */
    private static void mostrarIPs() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback()) continue;   // salteamos las apagadas y la local
                for (InetAddress ia : Collections.list(ni.getInetAddresses())) {
                    if (ia instanceof Inet4Address) {           // solo IPv4 (las de 4 números)
                        log(Colores.CYAN, "SERVIDOR", "IP disponible: " + ia.getHostAddress());
                    }
                }
            }
        } catch (IOException e) {
            // Es solo informativo: si falla, el servidor anda igual.
        }
    }
}
