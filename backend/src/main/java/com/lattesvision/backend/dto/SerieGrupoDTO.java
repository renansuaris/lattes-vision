package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Serie anual agregada de um grupo de pesquisadores")
public class SerieGrupoDTO {

    @Schema(description = "Ano de referencia", example = "2024")
    private Integer ano;

    @Schema(description = "Total de artigos no ano", example = "10")
    private long artigos;

    @Schema(description = "Total de trabalhos em eventos no ano", example = "6")
    private long trabalhosEventos;

    @Schema(description = "Total de orientacoes no ano", example = "4")
    private long orientacoes;

    @Schema(description = "Total de projetos no ano", example = "2")
    private long projetos;

    @Schema(description = "Total de bancas no ano", example = "5")
    private long bancas;

    @Schema(description = "Total de producoes tecnicas no ano", example = "3")
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
