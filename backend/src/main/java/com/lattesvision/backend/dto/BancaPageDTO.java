package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Pagina de participacoes em bancas para auditoria no dashboard")
public record BancaPageDTO(
        List<BancaDetailDTO> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
