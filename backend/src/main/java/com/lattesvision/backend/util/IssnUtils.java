package com.lattesvision.backend.util;

import java.util.Locale;

public final class IssnUtils {

    private IssnUtils() {
    }

    public static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String normalizado = valor.replaceAll("[^0-9Xx]", "").toUpperCase(Locale.ROOT);
        if (!normalizado.matches("\\d{7}[\\dX]")) {
            return null;
        }
        return normalizado;
    }

    public static String formatar(String valor) {
        String normalizado = normalizar(valor);
        if (normalizado == null) {
            return null;
        }
        return normalizado.substring(0, 4) + "-" + normalizado.substring(4);
    }

    public static boolean possuiDigitoVerificadorValido(String valor) {
        String normalizado = normalizar(valor);
        if (normalizado == null) {
            return false;
        }

        int soma = 0;
        for (int i = 0; i < 7; i++) {
            soma += Character.digit(normalizado.charAt(i), 10) * (8 - i);
        }
        int digito = (11 - soma % 11) % 11;
        char esperado = digito == 10 ? 'X' : Character.forDigit(digito, 10);
        return normalizado.charAt(7) == esperado;
    }
}
