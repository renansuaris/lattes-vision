package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.CapituloLivro;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public final class CapituloLivroParser {

    private static final String CABECALHO_CAPITULOS = "capitulos de livros publicados";

    private CapituloLivroParser() {
    }

    public static List<CapituloLivro> parse(Document doc) {
        List<CapituloLivro> capitulos = new ArrayList<>();
        boolean dentroDaSubsecao = false;

        for (Element elemento : doc.select("h1, h2, h3, div.inst_back, div.cita-artigos, div.layout-cell-11, div.layout-cell-pad-5, div.layout-cell-pad-6")) {
            if (ProducaoBibliograficaParserSupport.isCabecalho(elemento)) {
                dentroDaSubsecao = CABECALHO_CAPITULOS.equals(
                        ProducaoBibliograficaParserSupport.normalizarCabecalho(elemento.text())
                );
                continue;
            }
            if (!dentroDaSubsecao || !ProducaoBibliograficaParserSupport.isItem(elemento)) {
                continue;
            }

            String descricao = ProducaoBibliograficaParserSupport.limparNumeracao(elemento.text());
            String[] partes = ProducaoBibliograficaParserSupport.separarAutoresERestante(descricao);
            String titulo = ProducaoBibliograficaParserSupport.extrairTitulo(partes[1]);
            if (titulo == null || titulo.length() < 2 || isDuplicado(capitulos, titulo)) {
                continue;
            }

            CapituloLivro capitulo = new CapituloLivro();
            capitulo.setAutores(partes[0]);
            capitulo.setTitulo(titulo);
            capitulo.setTituloLivro(ProducaoBibliograficaParserSupport.extrairTituloLivro(partes[1]));
            capitulo.setAno(ProducaoBibliograficaParserSupport.extrairUltimoAno(descricao));
            capitulo.setEditora(ProducaoBibliograficaParserSupport.extrairEditora(descricao));
            capitulo.setIsbn(ProducaoBibliograficaParserSupport.extrairIsbn(descricao));
            capitulo.setEdicao(ProducaoBibliograficaParserSupport.extrairEdicao(descricao));
            capitulo.setVolume(ProducaoBibliograficaParserSupport.extrairVolume(descricao));
            capitulo.setPaginas(ProducaoBibliograficaParserSupport.extrairPaginas(descricao));
            capitulo.setDescricaoBruta(descricao);
            capitulos.add(capitulo);
        }

        return capitulos;
    }

    private static boolean isDuplicado(List<CapituloLivro> capitulos, String titulo) {
        String tituloNormalizado = normalizar(titulo);
        return capitulos.stream().anyMatch(capitulo -> normalizar(capitulo.getTitulo()).equals(tituloNormalizado));
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.replaceAll("\\s+", "").trim().toLowerCase(java.util.Locale.ROOT);
    }
}
