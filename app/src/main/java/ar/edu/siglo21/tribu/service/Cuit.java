package ar.edu.siglo21.tribu.service;

import java.util.regex.Pattern;

public final class Cuit {
    private static final Pattern FORMATO = Pattern.compile("\\d{2}-\\d{8}-\\d");
    private static final int[] PESOS = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private Cuit() {
    }

    public static boolean esValida(String cuit) {
        if (cuit == null || !FORMATO.matcher(cuit).matches()) {
            return false;
        }
        String digitos = cuit.replace("-", "");
        int suma = 0;
        for (int i = 0; i < PESOS.length; i++) {
            suma += (digitos.charAt(i) - '0') * PESOS[i];
        }
        int verificador = 11 - suma % 11;
        if (verificador == 11) {
            verificador = 0;
        } else if (verificador == 10) {
            return false;
        }
        return verificador == digitos.charAt(10) - '0';
    }
}
