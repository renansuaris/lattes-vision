package com.lattesvision.backend.exception;

public class CurriculoLattesNaoEncontradoException extends RuntimeException {

    private final String idLattes;

    public CurriculoLattesNaoEncontradoException(String idLattes) {
        super("Não foi encontrado um currículo Lattes para o ID informado. Verifique os 16 dígitos e tente novamente.");
        this.idLattes = idLattes;
    }

    public String getIdLattes() {
        return idLattes;
    }
}
