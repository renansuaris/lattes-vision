package com.lattesvision.backend.dto;

public class RankingPesquisadorDTO {

    private String idLattes;
    private String nomeCompleto;
    private String metrica;
    private long quantidade;

    public RankingPesquisadorDTO() {
    }

    public RankingPesquisadorDTO(String idLattes, String nomeCompleto, String metrica, long quantidade) {
        this.idLattes = idLattes;
        this.nomeCompleto = nomeCompleto;
        this.metrica = metrica;
        this.quantidade = quantidade;
    }

    public String getIdLattes() {
        return idLattes;
    }

    public void setIdLattes(String idLattes) {
        this.idLattes = idLattes;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getMetrica() {
        return metrica;
    }

    public void setMetrica(String metrica) {
        this.metrica = metrica;
    }

    public long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(long quantidade) {
        this.quantidade = quantidade;
    }
}
