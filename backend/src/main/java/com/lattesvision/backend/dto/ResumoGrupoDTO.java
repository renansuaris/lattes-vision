package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resumo agregado de um grupo ad-hoc de pesquisadores")
public class ResumoGrupoDTO {

    @Schema(description = "Quantidade de pesquisadores analisados", example = "4")
    private long totalPesquisadores;

    @Schema(description = "Resumo individual de cada pesquisador incluido no grupo")
    private List<ResumoPesquisadorDTO> pesquisadores;

    @Schema(description = "Total agregado de artigos", example = "48")
    private long totalArtigos;

    @Schema(description = "Total agregado de orientacoes", example = "20")
    private long totalOrientacoes;

    @Schema(description = "Total agregado de orientacoes concluidas", example = "15")
    private long totalOrientacoesConcluidas;

    @Schema(description = "Total agregado de orientacoes em andamento", example = "5")
    private long totalOrientacoesEmAndamento;

    @Schema(description = "Total agregado de projetos", example = "8")
    private long totalProjetos;

    @Schema(description = "Total agregado de bancas", example = "30")
    private long totalBancas;

    @Schema(description = "Total agregado de producoes tecnicas", example = "12")
    private long totalProducoesTecnicas;

    @Schema(description = "Total agregado de trabalhos em eventos", example = "42")
    private long totalTrabalhosEventos;

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
}
