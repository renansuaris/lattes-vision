package com.lattesvision.backend.dto;

import com.lattesvision.backend.model.CategoriaProducaoTecnica;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Producao tecnica persistida, com descricao original para auditoria")
public record TechnicalProductionDetailDTO(
        Long id,
        Integer year,
        CategoriaProducaoTecnica category,
        String title,
        String authors,
        String nature,
        String rawDescription
) {
}
