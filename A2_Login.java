package ejercicios;

import java.util.Map;

/** A2 - Login con Map, igual que credencialesValidas() del TP7. */
public class A2_Login {

    static final Map<String, String> USUARIOS = Map.of(
            "admin", "1234",
            "alumno", "clave123");

    static boolean credencialesValidas(String usuario, String clave) {
        String esperada = USUARIOS.get(usuario); // null si el usuario no existe
        return esperada != null && esperada.equals(clave);
    }

    public static void main(String[] args) {
        System.out.println("alumno / clave123 -> " + credencialesValidas("alumno", "clave123"));
        System.out.println("alumno / xxx      -> " + credencialesValidas("alumno", "xxx"));
        System.out.println("pepe   / 1234     -> " + credencialesValidas("pepe", "1234"));
    }
}
