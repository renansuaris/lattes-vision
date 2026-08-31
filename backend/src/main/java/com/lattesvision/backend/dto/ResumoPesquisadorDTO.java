package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Resumo agregado de um pesquisador para exibicao em dashboard")
public class ResumoPesquisadorDTO {

    @Schema(description = "ID de 16 digitos do Curriculo Lattes", example = "7364915463901013")
    private String idLattes;

    @Schema(description = "Nome completo do pesquisador", example = "Renan Alencar Soares")
    private String nomeCompleto;

    @Schema(description = "Data da ultima atualizacao informada no Curriculo Lattes")
    private LocalDate dataUltimaAtualizacao;

    @Schema(description = "Data e hora da ultima sincronizacao feita pelo LattesVision")
    private LocalDateTime dataSincronizacao;

    @Schema(description = "Total de artigos persistidos para o pesquisador", example = "12")
    private long totalArtigos;

    @Schema(description = "Total de orientacoes persistidas para o pesquisador", example = "8")
    private long totalOrientacoes;

    @Schema(description = "Total de orientacoes concluidas", example = "5")
    private long totalOrientacoesConcluidas;

    @Schema(description = "Total de orientacoes em andamento", example = "3")
    private long totalOrientacoesEmAndamento;

    @Schema(description = "Total de projetos persistidos para o pesquisador", example = "4")
    private long totalProjetos;

    @Schema(description = "Total de bancas persistidas para o pesquisador", example = "6")
    private long totalBancas;

    @Schema(description = "Total de producoes tecnicas persistidas para o pesquisador", example = "2")
    private long totalProducoesTecnicas;

    @Schema(description = "Total de trabalhos em eventos persistidos para o pesquisador", example = "15")
    private long totalTrabalhosEventos;

    @Schema(description = "Total de livros publicados, organizados ou editados", example = "2")
    private long totalLivros;

    @Schema(description = "Total de capitulos de livros publicados", example = "5")
    private long totalCapitulos;

    @Schema(description = "Total de itens de producao bibliografica", example = "34")
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
