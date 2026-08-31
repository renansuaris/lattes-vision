package com.lattesvision.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "livros")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String autores;

    @Column(columnDefinition = "TEXT")
    private String titulo;

    private Integer ano;

    @Column(columnDefinition = "TEXT")
    private String editora;

    @Column(length = 24)
    private String isbn;

    private String edicao;

    private String volume;

    private String paginas;

    @Column(columnDefinition = "TEXT")
    private String descricaoBruta;

    @ManyToOne
    @JoinColumn(name = "id_lattes_pesquisador")
    @JsonIgnore
    private Pesquisador pesquisador;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAutores() { return autores; }
    public void setAutores(String autores) { this.autores = autores; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public String getEditora() { return editora; }
    public void setEditora(String editora) { this.editora = editora; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getEdicao() { return edicao; }
    public void setEdicao(String edicao) { this.edicao = edicao; }
    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
    public String getPaginas() { return paginas; }
    public void setPaginas(String paginas) { this.paginas = paginas; }
    public String getDescricaoBruta() { return descricaoBruta; }
    public void setDescricaoBruta(String descricaoBruta) { this.descricaoBruta = descricaoBruta; }
    public Pesquisador getPesquisador() { return pesquisador; }
    public void setPesquisador(Pesquisador pesquisador) { this.pesquisador = pesquisador; }
}
