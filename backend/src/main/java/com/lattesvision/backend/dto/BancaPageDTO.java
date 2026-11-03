package com.lattesvision.backend.dto;

import java.util.List;

public record BancaPageDTO(
        List<BancaDetailDTO> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
