package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de uma serie temporal para graficos")
public class SerieTemporalDTO {

    @Schema(description = "Ano de referencia", example = "2023")
    private Integer ano;

    @Schema(description = "Quantidade produzida no ano", example = "5")
    private long quantidade;

    public SerieTemporalDTO() {
    }

    public SerieTemporalDTO(Integer ano, long quantidade) {
        this.ano = ano;
        this.quantidade = quantidade;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(long quantidade) {
        this.quantidade = quantidade;
    }
}
