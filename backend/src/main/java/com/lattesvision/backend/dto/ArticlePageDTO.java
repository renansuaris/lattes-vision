package com.lattesvision.backend.dto;

import java.util.List;

public record ArticlePageDTO(
        List<ArticleDetailDTO> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
