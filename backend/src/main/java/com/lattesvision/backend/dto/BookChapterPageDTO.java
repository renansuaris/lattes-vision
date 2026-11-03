package com.lattesvision.backend.dto;

import java.util.List;

public record BookChapterPageDTO(
        List<BookChapterDetailDTO> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
