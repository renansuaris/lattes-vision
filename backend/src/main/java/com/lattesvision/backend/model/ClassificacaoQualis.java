package com.lattesvision.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "classificacoes_qualis",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_qualis_issn_ciclo",
                columnNames = {"issn_normalizado", "ciclo_inicio", "ciclo_fim"}
        ),
        indexes = @Index(name = "idx_qualis_issn_normalizado", columnList = "issn_normalizado")
)
public class ClassificacaoQualis {

    @Id
    @SequenceGenerator(
            name = "classificacao_qualis_sequence",
            sequenceName = "classificacao_qualis_sequence",
            allocationSize = 500
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "classificacao_qualis_sequence")
    private Long id;

    @Column(name = "issn_normalizado", nullable = false, length = 8)
    private String issnNormalizado;

    @Column(name = "issn_formatado", nullable = false, length = 9)
    private String issnFormatado;

    @Column(name = "titulo_periodico", nullable = false, columnDefinition = "TEXT")
    private String tituloPeriodico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private EstratoQualis estrato;

    @Column(name = "ciclo_inicio", nullable = false)
    private Integer cicloInicio;

    @Column(name = "ciclo_fim", nullable = false)
    private Integer cicloFim;

    @Column(nullable = false, length = 100)
    private String fonte;

    @Column(name = "data_importacao", nullable = false)
    private LocalDateTime dataImportacao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIssnNormalizado() {
        return issnNormalizado;
    }

    public void setIssnNormalizado(String issnNormalizado) {
        this.issnNormalizado = issnNormalizado;
    }

    public String getIssnFormatado() {
        return issnFormatado;
    }

    public void setIssnFormatado(String issnFormatado) {
        this.issnFormatado = issnFormatado;
    }

    public String getTituloPeriodico() {
        return tituloPeriodico;
    }

    public void setTituloPeriodico(String tituloPeriodico) {
        this.tituloPeriodico = tituloPeriodico;
    }

    public EstratoQualis getEstrato() {
        return estrato;
    }

    public void setEstrato(EstratoQualis estrato) {
        this.estrato = estrato;
    }

    public Integer getCicloInicio() {
        return cicloInicio;
    }

    public void setCicloInicio(Integer cicloInicio) {
        this.cicloInicio = cicloInicio;
    }

    public Integer getCicloFim() {
        return cicloFim;
    }

    public void setCicloFim(Integer cicloFim) {
        this.cicloFim = cicloFim;
    }

    public String getFonte() {
        return fonte;
    }

    public void setFonte(String fonte) {
        this.fonte = fonte;
    }

    public LocalDateTime getDataImportacao() {
        return dataImportacao;
    }

    public void setDataImportacao(LocalDateTime dataImportacao) {
        this.dataImportacao = dataImportacao;
    }
}
