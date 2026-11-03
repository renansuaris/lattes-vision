package com.lattesvision.backend.dto;

import java.util.List;

public class ResumoQualisDTO {

    private String baseReferencia;
    private String fonte;
    private CoberturaQualisDTO cobertura;
    private List<EstratoQuantidadeDTO> distribuicao;
    private List<SerieQualisAnoDTO> serieAnual;

    public ResumoQualisDTO() {
    }

    public ResumoQualisDTO(
            String baseReferencia,
            String fonte,
            CoberturaQualisDTO cobertura,
            List<EstratoQuantidadeDTO> distribuicao,
            List<SerieQualisAnoDTO> serieAnual
    ) {
        this.baseReferencia = baseReferencia;
        this.fonte = fonte;
        this.cobertura = cobertura;
        this.distribuicao = distribuicao;
        this.serieAnual = serieAnual;
    }

    public String getBaseReferencia() { return baseReferencia; }
    public void setBaseReferencia(String baseReferencia) { this.baseReferencia = baseReferencia; }
    public String getFonte() { return fonte; }
    public void setFonte(String fonte) { this.fonte = fonte; }
    public CoberturaQualisDTO getCobertura() { return cobertura; }
    public void setCobertura(CoberturaQualisDTO cobertura) { this.cobertura = cobertura; }
    public List<EstratoQuantidadeDTO> getDistribuicao() { return distribuicao; }
    public void setDistribuicao(List<EstratoQuantidadeDTO> distribuicao) { this.distribuicao = distribuicao; }
    public List<SerieQualisAnoDTO> getSerieAnual() { return serieAnual; }
    public void setSerieAnual(List<SerieQualisAnoDTO> serieAnual) { this.serieAnual = serieAnual; }
}
