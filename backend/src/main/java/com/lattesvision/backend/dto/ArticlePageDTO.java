package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Pagina de artigos para auditoria no dashboard")
public record ArticlePageDTO(
        List<ArticleDetailDTO> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
