package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Serie anual de orientacoes separada por status")
public class SerieOrientacaoStatusDTO {

    @Schema(description = "Ano de referencia", example = "2024")
    private Integer ano;

    @Schema(description = "Quantidade de orientacoes concluidas no ano", example = "3")
    private long concluidas;

    @Schema(description = "Quantidade de orientacoes em andamento no ano", example = "2")
    private long emAndamento;

    public SerieOrientacaoStatusDTO() {
    }

    public SerieOrientacaoStatusDTO(Integer ano) {
        this.ano = ano;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public long getConcluidas() {
        return concluidas;
    }

    public void setConcluidas(long concluidas) {
        this.concluidas = concluidas;
    }

    public long getEmAndamento() {
        return emAndamento;
    }

    public void setEmAndamento(long emAndamento) {
        this.emAndamento = emAndamento;
    }

    public void adicionarConcluida() {
        this.concluidas++;
    }

    public void adicionarEmAndamento() {
        this.emAndamento++;
    }
}
