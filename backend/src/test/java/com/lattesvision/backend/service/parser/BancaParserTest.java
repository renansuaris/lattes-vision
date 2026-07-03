package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.Banca;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BancaParserTest {

    @Test
    void deveExtrairBancaComTipoCandidatoTituloEAno() {
        Document doc = Jsoup.parse("""
                <html>
                  <body>
                    <h1>Bancas</h1>
                    <div class="cita-artigos">Teses de doutorado</div>
                    <div class="layout-cell-11">1. Prof. Avaliador. Participacao em banca de Ana Lima. Arquiteturas para analise bibliometrica. 2024.</div>
                  </body>
                </html>
                """);

        List<Banca> bancas = BancaParser.parse(doc);

        assertThat(bancas).hasSize(1);
        assertThat(bancas.get(0).getTipo()).isEqualTo("Doutorado");
        assertThat(bancas.get(0).getAno()).isEqualTo(2024);
        assertThat(bancas.get(0).getNomeCandidato()).isEqualTo("Ana Lima");
        assertThat(bancas.get(0).getTituloTrabalho())
                .contains("Arquiteturas para analise bibliometrica");
    }

    @Test
    void deveClassificarBancaDeQualificacaoDeMestrado() {
        Document doc = Jsoup.parse("""
                <html>
                  <body>
                    <h1>Bancas</h1>
                    <div class="cita-artigos">Qualificacao de mestrado</div>
                    <div class="layout-cell-pad-5">1. Participacao em banca de Carlos Souza. Modelo de extracao de dados. 2023.</div>
                  </body>
                </html>
                """);

        List<Banca> bancas = BancaParser.parse(doc);

        assertThat(bancas).hasSize(1);
        assertThat(bancas.get(0).getTipo()).isEqualTo("Qualificacao de Mestrado");
        assertThat(bancas.get(0).getAno()).isEqualTo(2023);
    }
}
