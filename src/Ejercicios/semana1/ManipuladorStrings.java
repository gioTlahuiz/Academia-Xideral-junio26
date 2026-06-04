package Ejercicios.semana1;

public class ManipuladorStrings {
    public static String invertir(String s) {
        // TODO: usar StringBuilder.reverse()
        String reverse = new StringBuilder(s).reverse().toString();
        return reverse;
    }

    public static boolean esPalindromo(String s) {
        // TODO: limpiar (toLowerCase, replaceAll espacios)
        s = s.toLowerCase().replaceAll("[^a-z]", "");
        // TODO: comparar con su version invertida
        return s.equals(invertir(s));
    }

    public static int contarVocales(String s) {
        int count = 0;
        String vocales = "aeiouAEIOU";
        // TODO: recorrer cada caracter, verificar si es vocal
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if(vocales.indexOf(c) != -1) {
                count++;
            }
        }
        return count;
    }

    public static String construirPiramide(int niveles) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= niveles; i++) {
            // TODO: agregar espacios (niveles - i)
            sb.append(" ".repeat(niveles - i));
            // TODO: agregar asteriscos (2*i - 1)
            sb.append("*".repeat(2*i-1));
            // TODO: agregar salto de linea
            sb.append("\n");

            System.out.println();
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("Invertir 'Hola Mundo': "
                + invertir("Hola Mundo"));
        System.out.println("'Anita lava la tina' es palindromo: "
                + esPalindromo("Anita lava la tina"));
        System.out.println("Vocales en 'Murcielago': "
                + contarVocales("Murcielago"));
        System.out.println("Piramide de 5 niveles:");
        System.out.println(construirPiramide(5));
    }
}
