package com.lattesvision.backend.dto;

public class CategoriaQuantidadeDTO {

    private String categoria;

    private long quantidade;

    public CategoriaQuantidadeDTO() {
    }

    public CategoriaQuantidadeDTO(String categoria, long quantidade) {
        this.categoria = categoria;
        this.quantidade = quantidade;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(long quantidade) {
        this.quantidade = quantidade;
    }
}
