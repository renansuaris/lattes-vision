package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.Artigo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArtigoParserTest {

    @Test
    void deveExtrairArtigosComAnoELimparNumeracaoInicial() {
        Document doc = Jsoup.parse("""
                <html>
                  <body>
                    <div class="artigo-completo">1. SOARES, R. A.; COUTINHO, E. F. LattesVision. Revista Brasileira de Software, 2024.</div>
                    <div class="artigo-completo">2. SOARES, R. A. Analise de Curriculos Lattes. Anais do Evento, 2023.</div>
                  </body>
                </html>
                """);

        List<Artigo> artigos = ArtigoParser.parse(doc);

        assertThat(artigos).hasSize(2);
        assertThat(artigos.get(0).getCitacaoCompleta())
                .isEqualTo("SOARES, R. A.; COUTINHO, E. F. LattesVision. Revista Brasileira de Software, 2024.");
        assertThat(artigos.get(0).getAno()).isEqualTo(2024);
        assertThat(artigos.get(1).getAno()).isEqualTo(2023);
    }

    @Test
    void deveIgnorarArtigosDuplicadosNormalizados() {
        Document doc = Jsoup.parse("""
                <html>
                  <body>
                    <div class="artigo-completo">1. SOARES, R. A. LattesVision. Revista, 2024.</div>
                    <div class="artigo-completo">2. SOARES,R.A.LattesVision.Revista,2024.</div>
                  </body>
                </html>
                """);

        List<Artigo> artigos = ArtigoParser.parse(doc);

        assertThat(artigos).hasSize(1);
    }
}
