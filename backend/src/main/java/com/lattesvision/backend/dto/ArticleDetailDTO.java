package com.lattesvision.backend.dto;

import com.lattesvision.backend.model.EstratoQualis;
import com.lattesvision.backend.model.StatusClassificacaoQualis;

public record ArticleDetailDTO(
        Long id,
        Integer year,
        String title,
        String journal,
        String issn,
        EstratoQualis qualisStratum,
        StatusClassificacaoQualis qualisStatus
) {
}
