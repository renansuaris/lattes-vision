package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.Projeto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProjetoParserTest {

    @Test
    void deveExtrairProjetosComTipoAnoEBlocosConcatenados() {
        Document doc = Jsoup.parse("""
                <html>
                  <body>
                    <h1>Projetos de pesquisa</h1>
                    <div class="layout-cell-11">2021 - Atual</div>
                    <div class="layout-cell-pad-5">LattesVision: dashboards para curriculos academicos</div>
                    <div class="layout-cell-pad-6">Descricao: coleta e analise de indicadores.</div>
                    <h1>Producoes bibliograficas</h1>
                    <div class="layout-cell-11">Texto fora da secao</div>
                  </body>
                </html>
                """);

        List<Projeto> projetos = ProjetoParser.parse(doc);

        assertThat(projetos).hasSize(1);
        assertThat(projetos.get(0).getTipo()).isEqualTo("Pesquisa");
        assertThat(projetos.get(0).getAno()).isEqualTo(2021);
        assertThat(projetos.get(0).getTitulo())
                .contains("LattesVision: dashboards para curriculos academicos")
                .contains("Descricao: coleta e analise de indicadores.");
    }

    @Test
    void deveDetectarProjetoDeExtensao() {
        Document doc = Jsoup.parse("""
                <html>
                  <body>
                    <h1>Projetos de extensao</h1>
                    <div class="layout-cell-11">2020 - 2022</div>
                    <div class="layout-cell-pad-5">Capacitacao em analise de dados academicos</div>
                  </body>
                </html>
                """);

        List<Projeto> projetos = ProjetoParser.parse(doc);

        assertThat(projetos).hasSize(1);
        assertThat(projetos.get(0).getTipo()).isEqualTo("Extensao");
    }
}
