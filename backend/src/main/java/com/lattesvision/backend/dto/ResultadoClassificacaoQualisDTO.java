package com.lattesvision.backend.dto;

public record ResultadoClassificacaoQualisDTO(
        int totalArtigos,
        int classificados,
        int semIssn,
        int naoClassificados
) {
}
