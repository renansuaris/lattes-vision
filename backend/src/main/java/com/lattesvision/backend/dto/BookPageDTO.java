package com.lattesvision.backend.dto;

import java.util.List;

public record BookPageDTO(
        List<BookDetailDTO> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
