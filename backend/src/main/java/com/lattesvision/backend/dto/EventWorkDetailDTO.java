package com.lattesvision.backend.dto;

public record EventWorkDetailDTO(
        Long id,
        Integer year,
        String title,
        String eventName,
        String authors,
        String type
) {
}
