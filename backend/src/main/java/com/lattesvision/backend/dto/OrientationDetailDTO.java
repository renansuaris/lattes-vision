package com.lattesvision.backend.dto;

public record OrientationDetailDTO(
        Long id,
        Integer year,
        String orientedName,
        String workTitle,
        String type,
        String status
) {
}
