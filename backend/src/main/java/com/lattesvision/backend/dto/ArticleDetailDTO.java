package com.lattesvision.backend.dto;

import com.lattesvision.backend.model.EstratoQualis;
import com.lattesvision.backend.model.StatusClassificacaoQualis;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de um artigo para auditoria no dashboard")
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
