package com.lattesvision.backend.dto;

public class SerieTemporalDTO {

    private Integer ano;

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
