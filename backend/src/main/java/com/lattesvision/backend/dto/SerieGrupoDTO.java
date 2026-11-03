package com.lattesvision.backend.dto;

public class SerieGrupoDTO {

    private Integer ano;

    private long artigos;

    private long trabalhosEventos;

    private long orientacoes;

    private long projetos;

    private long bancas;

    private long producoesTecnicas;

    public SerieGrupoDTO() {
    }

    public SerieGrupoDTO(Integer ano) {
        this.ano = ano;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public long getArtigos() {
        return artigos;
    }

    public void setArtigos(long artigos) {
        this.artigos = artigos;
    }

    public long getTrabalhosEventos() {
        return trabalhosEventos;
    }

    public void setTrabalhosEventos(long trabalhosEventos) {
        this.trabalhosEventos = trabalhosEventos;
    }

    public long getOrientacoes() {
        return orientacoes;
    }

    public void setOrientacoes(long orientacoes) {
        this.orientacoes = orientacoes;
    }

    public long getProjetos() {
        return projetos;
    }

    public void setProjetos(long projetos) {
        this.projetos = projetos;
    }

    public long getBancas() {
        return bancas;
    }

    public void setBancas(long bancas) {
        this.bancas = bancas;
    }

    public long getProducoesTecnicas() {
        return producoesTecnicas;
    }

    public void setProducoesTecnicas(long producoesTecnicas) {
        this.producoesTecnicas = producoesTecnicas;
    }

    public void adicionarArtigo() {
        this.artigos++;
    }

    public void adicionarTrabalhoEvento() {
        this.trabalhosEventos++;
    }

    public void adicionarOrientacao() {
        this.orientacoes++;
    }

    public void adicionarProjeto() {
        this.projetos++;
    }

    public void adicionarBanca() {
        this.bancas++;
    }

    public void adicionarProducaoTecnica() {
        this.producoesTecnicas++;
    }
}
