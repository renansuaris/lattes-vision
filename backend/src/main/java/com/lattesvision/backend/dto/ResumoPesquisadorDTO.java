package com.lattesvision.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ResumoPesquisadorDTO {

    private String idLattes;

    private String nomeCompleto;

    private LocalDate dataUltimaAtualizacao;

    private LocalDateTime dataSincronizacao;

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

    public LocalDate getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }

    public void setDataUltimaAtualizacao(LocalDate dataUltimaAtualizacao) {
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
    }

    public LocalDateTime getDataSincronizacao() {
        return dataSincronizacao;
    }

    public void setDataSincronizacao(LocalDateTime dataSincronizacao) {
        this.dataSincronizacao = dataSincronizacao;
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
