package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.Orientacao;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrientacaoParserTest {

    @Test
    void deveExtrairOrientacoesConcluidasAteProximaSecao() {
        Document doc = Jsoup.parse("""
                <html>
                  <body>
                    <h1>Orientacoes e supervisoes concluidas</h1>
                    <div class="layout-cell-11">1. Maria Silva. Mineracao de dados em curriculos Lattes. Trabalho de conclusao de curso, 2022.</div>
                    <div class="layout-cell-pad-5">2. Joao Souza. Visualizacao bibliometrica. Dissertacao de mestrado, 2023.</div>
                    <h1>Projetos de pesquisa</h1>
                    <div class="layout-cell-11">2024 - Atual</div>
                  </body>
                </html>
                """);

        List<Orientacao> orientacoes = OrientacaoParser.parse(doc);

        assertThat(orientacoes).hasSize(2);
        assertThat(orientacoes.get(0).getStatus()).isEqualTo("CONCLUIDA");
        assertThat(orientacoes.get(0).getTipo()).isEqualTo("TCC");
        assertThat(orientacoes.get(0).getNomeOrientado()).isEqualTo("Maria Silva");
        assertThat(orientacoes.get(0).getAno()).isEqualTo(2022);
        assertThat(orientacoes.get(1).getTipo()).isEqualTo("Mestrado");
    }

    @Test
    void deveExtrairOrientacaoEmAndamento() {
        Document doc = Jsoup.parse("""
                <html>
                  <body>
                    <h1>Orientacoes e supervisoes em andamento</h1>
                    <div class="layout-cell-11">1. Ana Lima. Inicio: 2024. Tese de doutorado em Engenharia de Software.</div>
                  </body>
                </html>
                """);

        List<Orientacao> orientacoes = OrientacaoParser.parse(doc);

        assertThat(orientacoes).hasSize(1);
        assertThat(orientacoes.get(0).getStatus()).isEqualTo("EM_ANDAMENTO");
        assertThat(orientacoes.get(0).getTipo()).isEqualTo("Doutorado");
        assertThat(orientacoes.get(0).getAno()).isEqualTo(2024);
    }
}
