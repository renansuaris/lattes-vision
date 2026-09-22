package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de um trabalho completo em evento para auditoria no dashboard")
public record EventWorkDetailDTO(
        Long id,
        Integer year,
        String title,
        String eventName,
        String authors,
        String type
) {
}
