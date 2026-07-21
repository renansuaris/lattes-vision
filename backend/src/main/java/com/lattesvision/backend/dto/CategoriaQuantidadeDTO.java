package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Quantidade agregada por categoria")
public class CategoriaQuantidadeDTO {

    @Schema(description = "Nome da categoria agregada", example = "Mestrado")
    private String categoria;

    @Schema(description = "Quantidade de itens na categoria", example = "5")
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
