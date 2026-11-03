package com.lattesvision.backend.dto;

import com.lattesvision.backend.model.CategoriaProducaoTecnica;

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
