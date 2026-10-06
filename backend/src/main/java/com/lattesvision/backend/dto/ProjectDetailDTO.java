package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Projeto persistido; year corresponde ao ano de inicio e title preserva a descricao coletada")
public record ProjectDetailDTO(Long id, Integer year, String type, String title) {
}
