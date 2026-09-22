package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Pagina de trabalhos completos em eventos para auditoria no dashboard")
public record EventWorkPageDTO(
        List<EventWorkDetailDTO> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
