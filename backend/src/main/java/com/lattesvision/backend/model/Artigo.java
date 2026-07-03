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

    public Pesquisador getPesquisador() { return pesquisador; }
    public void setPesquisador(Pesquisador pesquisador) { this.pesquisador = pesquisador; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
}
