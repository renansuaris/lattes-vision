package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Requisicao para analise ad-hoc de um grupo de pesquisadores")
public class GrupoAdhocRequestDTO {

    @ArraySchema(schema = @Schema(description = "ID Lattes de 16 digitos", example = "7364915463901013"))
    private List<String> idsLattes;

    public List<String> getIdsLattes() {
        return idsLattes;
    }

    public void setIdsLattes(List<String> idsLattes) {
        this.idsLattes = idsLattes;
    }
}
