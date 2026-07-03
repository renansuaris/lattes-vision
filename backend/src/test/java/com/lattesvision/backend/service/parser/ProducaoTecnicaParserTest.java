package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.CategoriaProducaoTecnica;
import com.lattesvision.backend.model.ProducaoTecnica;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProducaoTecnicaParserTest {

    @Test
    void deveExtrairProducoesTecnicasPorCategoria() {
        Document doc = Jsoup.parse("""
                <html>
                  <body>
                    <h1>Producoes</h1>
                    <h2>Producao tecnica</h2>
                    <div class="cita-artigos">Programas de computador com registro</div>
                    <div class="layout-cell-11">1. SOARES, R. A.; COUTINHO, E. F.. LattesVision Coletor. 2024.</div>
                    <div class="cita-artigos">Produtos tecnologicos</div>
                    <div class="layout-cell-pad-5">2. SOARES, R. A.. Dashboard bibliometrico para PPGs. 2023.</div>
                    <h2>Producao artistica/cultural</h2>
                    <div class="layout-cell-11">Texto fora da producao tecnica. 2022.</div>
                  </body>
                </html>
                """);

        List<ProducaoTecnica> producoes = ProducaoTecnicaParser.parse(doc);

        assertThat(producoes).hasSize(2);
        assertThat(producoes.get(0).getCategoria()).isEqualTo(CategoriaProducaoTecnica.SOFTWARE_COM_REGISTRO);
        assertThat(producoes.get(0).getAutoresTexto()).isEqualTo("SOARES, R. A.; COUTINHO, E. F");
        assertThat(producoes.get(0).getTitulo()).isEqualTo("LattesVision Coletor");
        assertThat(producoes.get(0).getAno()).isEqualTo(2024);
        assertThat(producoes.get(1).getCategoria()).isEqualTo(CategoriaProducaoTecnica.PRODUTO_TECNOLOGICO);
    }

    @Test
    void deveExtrairNaturezaDeProcessoOuTecnica() {
        Document doc = Jsoup.parse("""
                <html>
                  <body>
                    <h2>Produção técnica</h2>
                    <div class="cita-artigos">Processos ou técnicas</div>
                    <div class="layout-cell-11">1. BARCELLOS, M. P.. Instrumento de Avaliação de Repositórios de Medição (IARM). 2015.</div>
                  </body>
                </html>
                """);

        List<ProducaoTecnica> producoes = ProducaoTecnicaParser.parse(doc);

        assertThat(producoes).hasSize(1);
        assertThat(producoes.get(0).getCategoria()).isEqualTo(CategoriaProducaoTecnica.PROCESSO_OU_TECNICA);
        assertThat(producoes.get(0).getTitulo()).isEqualTo("Instrumento de Avaliação de Repositórios de Medição");
        assertThat(producoes.get(0).getNatureza()).isEqualTo("IARM");
        assertThat(producoes.get(0).getAno()).isEqualTo(2015);
    }

    @Test
    void deveMapearEntrevistasParaOutra() {
        Document doc = Jsoup.parse("""
                <html>
                  <body>
                    <h2>Produção técnica</h2>
                    <div class="cita-artigos">Entrevistas, mesas redondas, programas e comentários na mídia</div>
                    <div class="layout-cell-11">1. SOARES, R. A.. Entrevista sobre LattesVision (Radio Universitaria). 2024.</div>
                  </body>
                </html>
                """);

        List<ProducaoTecnica> producoes = ProducaoTecnicaParser.parse(doc);

        assertThat(producoes).hasSize(1);
        assertThat(producoes.get(0).getCategoria()).isEqualTo(CategoriaProducaoTecnica.OUTRA);
        assertThat(producoes.get(0).getNatureza()).isEqualTo("Radio Universitaria");
    }
}
