package com.lattesvision.backend.dto;

import java.util.List;

public class ResumoGrupoDTO {

    private long totalPesquisadores;

    private List<ResumoPesquisadorDTO> pesquisadores;

    private long totalArtigos;

    private long totalOrientacoes;

    private long totalOrientacoesConcluidas;

    private long totalOrientacoesEmAndamento;

    private long totalProjetos;

    private long totalBancas;

    private long totalProducoesTecnicas;

    private long totalTrabalhosEventos;

    private long totalLivros;

    private long totalCapitulos;

    private long totalProducaoBibliografica;

    public long getTotalPesquisadores() {
        return totalPesquisadores;
    }

    public void setTotalPesquisadores(long totalPesquisadores) {
        this.totalPesquisadores = totalPesquisadores;
    }

    public List<ResumoPesquisadorDTO> getPesquisadores() {
        return pesquisadores;
    }

    public void setPesquisadores(List<ResumoPesquisadorDTO> pesquisadores) {
        this.pesquisadores = pesquisadores;
    }

    public long getTotalArtigos() {
        return totalArtigos;
    }

    public void setTotalArtigos(long totalArtigos) {
        this.totalArtigos = totalArtigos;
    }

    public long getTotalOrientacoes() {
        return totalOrientacoes;
    }

    public void setTotalOrientacoes(long totalOrientacoes) {
        this.totalOrientacoes = totalOrientacoes;
    }

    public long getTotalOrientacoesConcluidas() {
        return totalOrientacoesConcluidas;
    }

    public void setTotalOrientacoesConcluidas(long totalOrientacoesConcluidas) {
        this.totalOrientacoesConcluidas = totalOrientacoesConcluidas;
    }

    public long getTotalOrientacoesEmAndamento() {
        return totalOrientacoesEmAndamento;
    }

    public void setTotalOrientacoesEmAndamento(long totalOrientacoesEmAndamento) {
        this.totalOrientacoesEmAndamento = totalOrientacoesEmAndamento;
    }

    public long getTotalProjetos() {
        return totalProjetos;
    }

    public void setTotalProjetos(long totalProjetos) {
        this.totalProjetos = totalProjetos;
    }

    public long getTotalBancas() {
        return totalBancas;
    }

    public void setTotalBancas(long totalBancas) {
        this.totalBancas = totalBancas;
    }

    public long getTotalProducoesTecnicas() {
        return totalProducoesTecnicas;
    }

    public void setTotalProducoesTecnicas(long totalProducoesTecnicas) {
        this.totalProducoesTecnicas = totalProducoesTecnicas;
    }

    public long getTotalTrabalhosEventos() {
        return totalTrabalhosEventos;
    }

    public void setTotalTrabalhosEventos(long totalTrabalhosEventos) {
        this.totalTrabalhosEventos = totalTrabalhosEventos;
    }

    public long getTotalLivros() {
        return totalLivros;
    }

    public void setTotalLivros(long totalLivros) {
        this.totalLivros = totalLivros;
    }

    public long getTotalCapitulos() {
        return totalCapitulos;
    }

    public void setTotalCapitulos(long totalCapitulos) {
        this.totalCapitulos = totalCapitulos;
    }

    public long getTotalProducaoBibliografica() {
        return totalProducaoBibliografica;
    }

    public void setTotalProducaoBibliografica(long totalProducaoBibliografica) {
        this.totalProducaoBibliografica = totalProducaoBibliografica;
    }
}
