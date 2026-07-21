package com.lattesvision.backend.dto;

import com.lattesvision.backend.model.EstratoQualis;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.EnumMap;
import java.util.Map;

@Schema(description = "Distribuicao anual de artigos por estrato Qualis")
public class SerieQualisAnoDTO {

    private Integer ano;
    private Map<EstratoQualis, Long> estratos = criarEstratosZerados();

    public SerieQualisAnoDTO() {
    }

    public SerieQualisAnoDTO(Integer ano) {
        this.ano = ano;
    }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public Map<EstratoQualis, Long> getEstratos() { return estratos; }
    public void setEstratos(Map<EstratoQualis, Long> estratos) { this.estratos = estratos; }

    public void adicionar(EstratoQualis estrato) {
        estratos.merge(estrato, 1L, Long::sum);
    }

    private static Map<EstratoQualis, Long> criarEstratosZerados() {
        Map<EstratoQualis, Long> valores = new EnumMap<>(EstratoQualis.class);
        for (EstratoQualis estrato : EstratoQualis.values()) {
            valores.put(estrato, 0L);
        }
        return valores;
    }
}
