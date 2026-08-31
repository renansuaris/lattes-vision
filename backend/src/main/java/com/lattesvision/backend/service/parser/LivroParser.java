package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.Livro;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public final class LivroParser {

    private static final String CABECALHO_LIVROS = "livros publicados organizados ou edicoes";

    private LivroParser() {
    }

    public static List<Livro> parse(Document doc) {
        List<Livro> livros = new ArrayList<>();
        boolean dentroDaSubsecao = false;

        for (Element elemento : doc.select("h1, h2, h3, div.inst_back, div.cita-artigos, div.layout-cell-11, div.layout-cell-pad-5, div.layout-cell-pad-6")) {
            if (ProducaoBibliograficaParserSupport.isCabecalho(elemento)) {
                dentroDaSubsecao = CABECALHO_LIVROS.equals(
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
            if (titulo == null || titulo.length() < 2 || isDuplicado(livros, titulo)) {
                continue;
            }

            Livro livro = new Livro();
            livro.setAutores(partes[0]);
            livro.setTitulo(titulo);
            livro.setAno(ProducaoBibliograficaParserSupport.extrairUltimoAno(descricao));
            livro.setEditora(ProducaoBibliograficaParserSupport.extrairEditora(descricao));
            livro.setIsbn(ProducaoBibliograficaParserSupport.extrairIsbn(descricao));
            livro.setEdicao(ProducaoBibliograficaParserSupport.extrairEdicao(descricao));
            livro.setVolume(ProducaoBibliograficaParserSupport.extrairVolume(descricao));
            livro.setPaginas(ProducaoBibliograficaParserSupport.extrairPaginas(descricao));
            livro.setDescricaoBruta(descricao);
            livros.add(livro);
        }

        return livros;
    }

    private static boolean isDuplicado(List<Livro> livros, String titulo) {
        String tituloNormalizado = normalizar(titulo);
        return livros.stream().anyMatch(livro -> normalizar(livro.getTitulo()).equals(tituloNormalizado));
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.replaceAll("\\s+", "").trim().toLowerCase(java.util.Locale.ROOT);
    }
}
