package com.lattesvision.backend.dto;

import java.util.List;

public class GrupoAdhocRequestDTO {

    private List<String> idsLattes;

    public List<String> getIdsLattes() {
        return idsLattes;
    }

    public void setIdsLattes(List<String> idsLattes) {
        this.idsLattes = idsLattes;
    }
}
