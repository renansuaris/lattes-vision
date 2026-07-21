package com.lattesvision.backend.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IssnUtilsTest {

    @Test
    void deveNormalizarEFormatarIssn() {
        assertThat(IssnUtils.normalizar(" 0001-0782 ")).isEqualTo("00010782");
        assertThat(IssnUtils.formatar("1234567x")).isEqualTo("1234-567X");
    }

    @Test
    void deveValidarDigitoVerificadorSemDescartarFormatoValido() {
        assertThat(IssnUtils.possuiDigitoVerificadorValido("0001-0782")).isTrue();
        assertThat(IssnUtils.normalizar("2125-8165")).isEqualTo("21258165");
        assertThat(IssnUtils.possuiDigitoVerificadorValido("2125-8165")).isFalse();
    }

    @Test
    void deveRejeitarValorSemFormatoDeIssn() {
        assertThat(IssnUtils.normalizar("invalido")).isNull();
        assertThat(IssnUtils.formatar(null)).isNull();
    }
}
