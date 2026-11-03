package com.lattesvision.backend.dto;

public class CoberturaQualisDTO {

    private long totalArtigos;
    private long classificados;
    private long semIssn;
    private long naoClassificados;
    private double percentualClassificados;

    public CoberturaQualisDTO() {
    }

    public CoberturaQualisDTO(long totalArtigos, long classificados, long semIssn, long naoClassificados) {
        this.totalArtigos = totalArtigos;
        this.classificados = classificados;
        this.semIssn = semIssn;
        this.naoClassificados = naoClassificados;
        this.percentualClassificados = totalArtigos == 0
                ? 0
                : Math.round((classificados * 10000.0) / totalArtigos) / 100.0;
    }

    public long getTotalArtigos() { return totalArtigos; }
    public void setTotalArtigos(long totalArtigos) { this.totalArtigos = totalArtigos; }
    public long getClassificados() { return classificados; }
    public void setClassificados(long classificados) { this.classificados = classificados; }
    public long getSemIssn() { return semIssn; }
    public void setSemIssn(long semIssn) { this.semIssn = semIssn; }
    public long getNaoClassificados() { return naoClassificados; }
    public void setNaoClassificados(long naoClassificados) { this.naoClassificados = naoClassificados; }
    public double getPercentualClassificados() { return percentualClassificados; }
    public void setPercentualClassificados(double percentualClassificados) { this.percentualClassificados = percentualClassificados; }
}
