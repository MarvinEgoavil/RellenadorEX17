package com.alucardstudio.rellenadorex17;

public class NieValidator {

    private static final String LETRAS =
            "TRWAGMYFPDXBNJZSQVHLCKE";

    public static boolean validar(String nie) {

        if (nie == null) {
            return false;
        }

        nie = nie.trim().toUpperCase();

        if (!nie.matches("^[XYZ]\\d{7}[A-Z]$")) {
            return false;
        }

        String numero;

        switch (nie.charAt(0)) {
            case 'X' -> numero = "0" + nie.substring(1, 8);
            case 'Y' -> numero = "1" + nie.substring(1, 8);
            case 'Z' -> numero = "2" + nie.substring(1, 8);
            default -> {
                return false;
            }
        }

        int valor = Integer.parseInt(numero);

        char letraCorrecta =
                LETRAS.charAt(valor % 23);

        return letraCorrecta == nie.charAt(8);
    }
}