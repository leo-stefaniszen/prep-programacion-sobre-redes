package ejercicios;

/** E1 - Recorrer un array: más alta, promedio y aprobados. */
public class E1_Notas {

    public static void main(String[] args) {
        int[] notas = { 7, 9, 4, 10, 6 };

        int masAlta = notas[0];
        int suma = 0;
        int aprobados = 0;

        for (int i = 0; i < notas.length; i++) {
            if (notas[i] > masAlta) {
                masAlta = notas[i];
            }
            suma += notas[i];
            if (notas[i] >= 6) {
                aprobados++;
            }
        }

        double promedio = (double) suma / notas.length; // sin (double) daría 7
        System.out.println("Notas: " + notas.length);
        System.out.println("Más alta: " + masAlta);
        System.out.println("Promedio: " + promedio);
        System.out.println("Aprobados: " + aprobados);
    }
}
