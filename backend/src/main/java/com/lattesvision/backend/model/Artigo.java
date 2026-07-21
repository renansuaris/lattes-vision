package com.lattesvision.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "artigos")
public class Artigo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String citacaoCompleta;

    @Column(columnDefinition = "TEXT")
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String periodico;

    @Column(length = 9)
    private String issn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_qualis", length = 20)
    private StatusClassificacaoQualis statusQualis;

    @ManyToOne
    @JoinColumn(name = "classificacao_qualis_id")
    private ClassificacaoQualis classificacaoQualis;

    @ManyToOne // Vários artigos pertencem a UM pesquisador
    @JoinColumn(name = "id_lattes_pesquisador")
    @JsonIgnore // ESCUDO: Impede que o JSON fique buscando "Artigo -> Pesquisador -> Artigo..." infinitamente
    private Pesquisador pesquisador;

    @Column(name = "ano_publicacao")
    private Integer ano;

    public Artigo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCitacaoCompleta() { return citacaoCompleta; }
    public void setCitacaoCompleta(String citacaoCompleta) { this.citacaoCompleta = citacaoCompleta; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getPeriodico() { return periodico; }
    public void setPeriodico(String periodico) { this.periodico = periodico; }

    public String getIssn() { return issn; }
    public void setIssn(String issn) { this.issn = issn; }

    public StatusClassificacaoQualis getStatusQualis() { return statusQualis; }
    public void setStatusQualis(StatusClassificacaoQualis statusQualis) { this.statusQualis = statusQualis; }

    public ClassificacaoQualis getClassificacaoQualis() { return classificacaoQualis; }
    public void setClassificacaoQualis(ClassificacaoQualis classificacaoQualis) { this.classificacaoQualis = classificacaoQualis; }

    public Pesquisador getPesquisador() { return pesquisador; }
    public void setPesquisador(Pesquisador pesquisador) { this.pesquisador = pesquisador; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
}
