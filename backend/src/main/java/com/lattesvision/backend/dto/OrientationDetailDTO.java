package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de uma orientacao para auditoria no dashboard")
public record OrientationDetailDTO(
        Long id,
        Integer year,
        String orientedName,
        String workTitle,
        String type,
        String status
) {
}
