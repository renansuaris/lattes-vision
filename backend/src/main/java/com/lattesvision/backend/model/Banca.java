package com.lattesvision.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "bancas")
public class Banca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer ano;
    private String tipo; // Ex: "Mestrado", "Doutorado", "TCC", "Concurso Publico"

    private String nomeCandidato;

    @Column(columnDefinition = "TEXT")
    private String tituloTrabalho;

    @ManyToOne
    @JoinColumn(name = "id_lattes_pesquisador")
    @JsonIgnore
    private Pesquisador pesquisador;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getNomeCandidato() { return nomeCandidato; }
    public void setNomeCandidato(String nomeCandidato) { this.nomeCandidato = nomeCandidato; }

    public String getTituloTrabalho() { return tituloTrabalho; }
    public void setTituloTrabalho(String tituloTrabalho) { this.tituloTrabalho = tituloTrabalho; }

    public Pesquisador getPesquisador() { return pesquisador; }
    public void setPesquisador(Pesquisador pesquisador) { this.pesquisador = pesquisador; }
}
