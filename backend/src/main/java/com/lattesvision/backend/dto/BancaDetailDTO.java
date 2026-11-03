package com.lattesvision.backend.dto;

public record BancaDetailDTO(
        Long id,
        Integer year,
        String type,
        String candidateName,
        String workTitle
) {
}
