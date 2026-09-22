package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de uma participacao em banca para auditoria no dashboard")
public record BancaDetailDTO(
        Long id,
        Integer year,
        String type,
        String candidateName,
        String workTitle
) {
}
