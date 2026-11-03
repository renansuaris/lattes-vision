package com.lattesvision.backend.dto;

import java.util.List;

public record ProjectPageDTO(
        List<ProjectDetailDTO> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
