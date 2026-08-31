package com.lattesvision.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pesquisadores")
public class Pesquisador {

    @Id
    @Column(name = "id_lattes", length = 16)
    private String idLattes;

    @Column(name = "id_interno", length = 10)
    private String idInterno;

    private String nomeCompleto;

    @Column(columnDefinition = "TEXT")
    private String resumoProfissional;

    private LocalDate dataUltimaAtualizacao;

    private LocalDateTime dataSincronizacao;

    @OneToMany(mappedBy = "pesquisador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Artigo> artigos = new ArrayList<>();

    @OneToMany(mappedBy = "pesquisador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orientacao> orientacoes = new ArrayList<>();

    @OneToMany(mappedBy = "pesquisador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Projeto> projetos = new ArrayList<>();

    @OneToMany(mappedBy = "pesquisador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Banca> bancas = new ArrayList<>();

    @OneToMany(mappedBy = "pesquisador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProducaoTecnica> producoesTecnicas = new ArrayList<>();

    @OneToMany(mappedBy = "pesquisador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrabalhoEvento> trabalhosEventos = new ArrayList<>();

    @OneToMany(mappedBy = "pesquisador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Livro> livros = new ArrayList<>();

    @OneToMany(mappedBy = "pesquisador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CapituloLivro> capitulosLivros = new ArrayList<>();

    public Pesquisador() {
    }

    public String getIdLattes() {
        return idLattes;
    }

    public void setIdLattes(String idLattes) {
        this.idLattes = idLattes;
    }

    public String getIdInterno() {
        return idInterno;
    }

    public void setIdInterno(String idInterno) {
        this.idInterno = idInterno;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getResumoProfissional() {
        return resumoProfissional;
    }

    public void setResumoProfissional(String resumoProfissional) {
        this.resumoProfissional = resumoProfissional;
    }

    public LocalDate getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }

    public void setDataUltimaAtualizacao(LocalDate dataUltimaAtualizacao) {
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
    }

    public LocalDateTime getDataSincronizacao() {
        return dataSincronizacao;
    }

    public void setDataSincronizacao(LocalDateTime dataSincronizacao) {
        this.dataSincronizacao = dataSincronizacao;
    }

    public List<Artigo> getArtigos() {
        return artigos;
    }

    public void setArtigos(List<Artigo> artigos) {
        this.artigos = artigos;
    }

    public void addArtigo(Artigo artigo) {
        artigos.add(artigo);
        artigo.setPesquisador(this);
    }

    public List<Orientacao> getOrientacoes() {
        return orientacoes;
    }

    public void setOrientacoes(List<Orientacao> orientacoes) {
        this.orientacoes = orientacoes;
    }

    public void addOrientacao(Orientacao orientacao) {
        orientacoes.add(orientacao);
        orientacao.setPesquisador(this);
    }

    public List<Projeto> getProjetos() {
        return projetos;
    }

    public void setProjetos(List<Projeto> projetos) {
        this.projetos = projetos;
    }

    public void addProjeto(Projeto projeto) {
        projetos.add(projeto);
        projeto.setPesquisador(this);
    }

    public List<Banca> getBancas() {
        return bancas;
    }

    public void setBancas(List<Banca> bancas) {
        this.bancas = bancas;
    }

    public void addBanca(Banca banca) {
        bancas.add(banca);
        banca.setPesquisador(this);
    }

    public List<ProducaoTecnica> getProducoesTecnicas() {
        return producoesTecnicas;
    }

    public void setProducoesTecnicas(List<ProducaoTecnica> producoesTecnicas) {
        this.producoesTecnicas = producoesTecnicas;
    }

    public void addProducaoTecnica(ProducaoTecnica producaoTecnica) {
        producoesTecnicas.add(producaoTecnica);
        producaoTecnica.setPesquisador(this);
    }

    public List<TrabalhoEvento> getTrabalhosEventos() {
        return trabalhosEventos;
    }

    public void setTrabalhosEventos(List<TrabalhoEvento> trabalhosEventos) {
        this.trabalhosEventos = trabalhosEventos;
    }

    public void addTrabalhoEvento(TrabalhoEvento trabalhoEvento) {
        trabalhosEventos.add(trabalhoEvento);
        trabalhoEvento.setPesquisador(this);
    }

    public List<Livro> getLivros() {
        return livros;
    }

    public void setLivros(List<Livro> livros) {
        this.livros = livros;
    }

    public void addLivro(Livro livro) {
        livros.add(livro);
        livro.setPesquisador(this);
    }

    public List<CapituloLivro> getCapitulosLivros() {
        return capitulosLivros;
    }

    public void setCapitulosLivros(List<CapituloLivro> capitulosLivros) {
        this.capitulosLivros = capitulosLivros;
    }

    public void addCapituloLivro(CapituloLivro capituloLivro) {
        capitulosLivros.add(capituloLivro);
        capituloLivro.setPesquisador(this);
    }
}
