package com.lattesvision.backend.dto;

import java.util.List;

public record TechnicalProductionPageDTO(
        List<TechnicalProductionDetailDTO> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
