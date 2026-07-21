package com.lattesvision.backend.dto;

public record ResultadoImportacaoQualisDTO(
        long linhasLidas,
        int periodicosDistintos,
        int inseridos,
        int atualizados,
        int inalterados,
        int linhasRejeitadas,
        int issnsComDigitoInvalido
) {
}
