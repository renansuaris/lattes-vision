package com.lattesvision.backend.dto;

import java.util.List;

public record OrientationPageDTO(
        List<OrientationDetailDTO> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
