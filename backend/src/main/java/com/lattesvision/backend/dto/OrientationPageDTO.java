package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Pagina de orientacoes para auditoria no dashboard")
public record OrientationPageDTO(
        List<OrientationDetailDTO> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
