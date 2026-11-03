package com.lattesvision.backend.dto;

import com.lattesvision.backend.model.EstratoQualis;

public class EstratoQuantidadeDTO {

    private EstratoQualis estrato;
    private long quantidade;

    public EstratoQuantidadeDTO() {
    }

    public EstratoQuantidadeDTO(EstratoQualis estrato, long quantidade) {
        this.estrato = estrato;
        this.quantidade = quantidade;
    }

    public EstratoQualis getEstrato() { return estrato; }
    public void setEstrato(EstratoQualis estrato) { this.estrato = estrato; }
    public long getQuantidade() { return quantidade; }
    public void setQuantidade(long quantidade) { this.quantidade = quantidade; }
}
