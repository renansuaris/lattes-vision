package com.lattesvision.backend.dto;

import java.util.List;

public record EventWorkPageDTO(
        List<EventWorkDetailDTO> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
