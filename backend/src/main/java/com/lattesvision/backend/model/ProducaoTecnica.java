package com.lattesvision.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "producoes_tecnicas")
public class ProducaoTecnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaProducaoTecnica categoria;

    @Column(columnDefinition = "TEXT")
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String autoresTexto;

    private Integer ano;

    private String natureza;

    @Column(columnDefinition = "TEXT")
    private String descricaoBruta;

    @ManyToOne
    @JoinColumn(name = "id_lattes_pesquisador")
    @JsonIgnore
    private Pesquisador pesquisador;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CategoriaProducaoTecnica getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProducaoTecnica categoria) {
        this.categoria = categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutoresTexto() {
        return autoresTexto;
    }

    public void setAutoresTexto(String autoresTexto) {
        this.autoresTexto = autoresTexto;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getNatureza() {
        return natureza;
    }

    public void setNatureza(String natureza) {
        this.natureza = natureza;
    }

    public String getDescricaoBruta() {
        return descricaoBruta;
    }

    public void setDescricaoBruta(String descricaoBruta) {
        this.descricaoBruta = descricaoBruta;
    }

    public Pesquisador getPesquisador() {
        return pesquisador;
    }

    public void setPesquisador(Pesquisador pesquisador) {
        this.pesquisador = pesquisador;
    }
}
