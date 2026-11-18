package ejercicios;

/** A1 - Intérprete de comandos: separa comando y argumento como el TP7. */
public class A1_Comandos {

    public static void main(String[] args) {
        String[] lineas = { "login alumno clave123", "  list ", "upload foto.jpg", "hola" };

        for (String linea : lineas) {
            String[] partes = linea.trim().split("\\s+", 2);
            String comando = partes[0].toUpperCase();
            String argumento = partes.length > 1 ? partes[1] : "";

            switch (comando) {
                case "LOGIN":
                case "LIST":
                case "UPLOAD":
                    System.out.println("Comando: " + comando + " | Arg: " + argumento);
                    break;
                default:
                    System.out.println("Comando desconocido: " + comando);
            }
        }
    }
}
