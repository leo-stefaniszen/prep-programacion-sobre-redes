package ejercicios;

/** E2 - Métodos que reciben y devuelven arrays. */
public class E2_Invertir {

    static int[] invertir(int[] v) {
        int[] nuevo = new int[v.length];
        for (int i = 0; i < v.length; i++) {
            nuevo[i] = v[v.length - 1 - i];
        }
        return nuevo;
    }

    static void mostrar(String titulo, int[] v) {
        System.out.print(titulo + ": ");
        for (int x : v) {
            System.out.print(x + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int[] numeros = { 1, 2, 3, 4, 5 };
        mostrar("Original", numeros);
        mostrar("Invertido", invertir(numeros));
    }
}
