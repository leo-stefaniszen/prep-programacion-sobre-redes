package mini_tp7;

/**
 * ============================================================================
 *  COLORES — constantes para pintar la consola
 * ============================================================================
 *
 *  Son "códigos ANSI": textos especiales que la consola NO imprime, sino que
 *  los usa para cambiar el color de lo que viene después.
 *
 *  Uso:   System.out.println(Colores.VERDE + "Todo bien" + Colores.RESET);
 *         (RESET vuelve al color normal; si no lo ponés, queda todo verde)
 *
 *  📚 Del profe: Clase 5 → SocketTCP/conexion.java (ANSI_GREEN, ANSI_RED...)
 */
public class Colores {

    public static final String RESET    = "\u001B[0m";
    public static final String ROJO     = "\u001B[31m";
    public static final String VERDE    = "\u001B[32m";
    public static final String AMARILLO = "\u001B[33m";
    public static final String AZUL     = "\u001B[34m";
    public static final String CYAN     = "\u001B[36m";
}
