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

    @Test
    void deveExtrairDadosEstruturadosDoCvuri() {
        Document doc = Jsoup.parse("""
                <html>
                  <body>
                    <div class="artigo-completo">
                      <span class="informacao-artigo" data-tipo-ordenacao="autor">SOARES, R. A.</span>
                      <span class="informacao-artigo" data-tipo-ordenacao="ano">2024</span>
                      1. SOARES, R. A. LattesVision 2019. Revista Brasileira de Software, 2024.
                      <span cvuri="https://buscatextual.cnpq.br/buscatextual/visualizacv.do?metodo=detalhar&amp;titulo=LattesVision%3A+an%C3%A1lise+acad%C3%AAmica&amp;nomePeriodico=Revista+Brasileira+de+Software&amp;issn=1234567X"></span>
                    </div>
                  </body>
                </html>
                """);

        List<Artigo> artigos = ArtigoParser.parse(doc);

        assertThat(artigos).singleElement().satisfies(artigo -> {
            assertThat(artigo.getAno()).isEqualTo(2024);
            assertThat(artigo.getTitulo()).isEqualTo("LattesVision: análise acadêmica");
            assertThat(artigo.getPeriodico()).isEqualTo("Revista Brasileira de Software");
            assertThat(artigo.getIssn()).isEqualTo("1234-567X");
            assertThat(artigo.getCitacaoCompleta())
                    .isEqualTo("SOARES, R. A. LattesVision 2019. Revista Brasileira de Software, 2024.");
        });
    }

    @Test
    void deveManterFallbackParaArtigoSemCvuri() {
        Document doc = Jsoup.parse("""
                <div class="artigo-completo">
                  1. SOARES, R. A. Artigo legado. Revista, 2022.
                </div>
                """);

        List<Artigo> artigos = ArtigoParser.parse(doc);

        assertThat(artigos).singleElement().satisfies(artigo -> {
            assertThat(artigo.getAno()).isEqualTo(2022);
            assertThat(artigo.getCitacaoCompleta()).isEqualTo("SOARES, R. A. Artigo legado. Revista, 2022.");
            assertThat(artigo.getTitulo()).isNull();
            assertThat(artigo.getPeriodico()).isNull();
            assertThat(artigo.getIssn()).isNull();
        });
    }

    @Test
    void deveNormalizarIssnComHifenEIgnorarValorMalformado() {
        Document doc = Jsoup.parse("""
                <div class="artigo-completo">
                  1. Primeiro artigo, 2021.
                  <span cvuri="?titulo=Primeiro&amp;nomePeriodico=Revista+A&amp;issn=1234-567x"></span>
                </div>
                <div class="artigo-completo">
                  2. Segundo artigo, 2022.
                  <span cvuri="?titulo=Segundo&amp;nomePeriodico=Revista+B&amp;issn=invalido"></span>
                </div>
                """);

        List<Artigo> artigos = ArtigoParser.parse(doc);

        assertThat(artigos).hasSize(2);
        assertThat(artigos.get(0).getIssn()).isEqualTo("1234-567X");
        assertThat(artigos.get(1).getIssn()).isNull();
    }
}
