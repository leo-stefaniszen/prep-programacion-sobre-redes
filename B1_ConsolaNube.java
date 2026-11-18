package ejercicios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** B1 - Consola "nube>" que lee líneas del teclado hasta EXIT. */
public class B1_ConsolaNube {

    public static void main(String[] args) throws IOException {
        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("nube> ");
            String linea = teclado.readLine();
            if (linea == null || linea.trim().equalsIgnoreCase("EXIT")) {
                System.out.println("Chau!");
                break;
            }
            linea = linea.trim();
            System.out.println("Recibí: " + linea + " (" + linea.length() + " caracteres)");
        }
    }
}
