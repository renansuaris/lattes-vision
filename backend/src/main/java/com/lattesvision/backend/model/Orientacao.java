package com.lattesvision.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "orientacoes")
public class Orientacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;

    private String tipo; // Ex: Mestrado, Doutorado, TCC

    private String nomeOrientado;

    @Column(columnDefinition = "TEXT")
    private String tituloTrabalho;

    private Integer ano;

    @ManyToOne
    @JoinColumn(name = "id_lattes_pesquisador")
    @JsonIgnore
    private Pesquisador pesquisador;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getNomeOrientado() { return nomeOrientado; }
    public void setNomeOrientado(String nomeOrientado) { this.nomeOrientado = nomeOrientado; }
    public String getTituloTrabalho() { return tituloTrabalho; }
    public void setTituloTrabalho(String tituloTrabalho) { this.tituloTrabalho = tituloTrabalho; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public Pesquisador getPesquisador() { return pesquisador; }
    public void setPesquisador(Pesquisador pesquisador) { this.pesquisador = pesquisador; }
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
}
