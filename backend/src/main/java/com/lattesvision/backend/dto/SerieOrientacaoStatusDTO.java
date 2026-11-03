package com.lattesvision.backend.dto;

public class SerieOrientacaoStatusDTO {

    private Integer ano;

    private long concluidas;

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
