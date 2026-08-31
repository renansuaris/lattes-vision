package com.lattesvision.backend.service.parser;

import org.jsoup.nodes.Element;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ProducaoBibliograficaParserSupport {

    private static final Pattern PADRAO_ANO = Pattern.compile("\\b(19|20)\\d{2}\\b");
    private static final Pattern PADRAO_ISBN = Pattern.compile("(?i)\\bISBN\\s*:?\\s*([0-9Xx-]{10,24})");
    private static final Pattern PADRAO_EDICAO = Pattern.compile("(?i)\\b(\\d+(?:ª|a|o)?\\.?\\s*ed\\.)");
    private static final Pattern PADRAO_VOLUME = Pattern.compile("(?i)\\bv\\.\\s*([^,.]+)");
    private static final Pattern PADRAO_PAGINAS_PREFIXO = Pattern.compile("(?i)\\b(?:p\\.|pp\\.)\\s*([0-9]+(?:\\s*[-–]\\s*[0-9]+)?)");
    private static final Pattern PADRAO_PAGINAS_SUFIXO = Pattern.compile("(?i)\\b([0-9]+(?:\\s*[-–]\\s*[0-9]+)?)\\s*(?:p\\.|pp\\.)");

    private ProducaoBibliograficaParserSupport() {
    }

    static boolean isCabecalho(Element elemento) {
        String tagName = elemento.tagName().toLowerCase(Locale.ROOT);
        return tagName.matches("h[1-3]")
                || elemento.hasClass("inst_back")
                || elemento.hasClass("cita-artigos");
    }

    static boolean isItem(Element elemento) {
        return elemento.hasClass("layout-cell-11")
                || elemento.hasClass("layout-cell-pad-5")
                || elemento.hasClass("layout-cell-pad-6");
    }

    static String normalizarCabecalho(String texto) {
        String semAcentos = Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    static String limparNumeracao(String texto) {
        return texto == null ? "" : texto.replaceFirst("^\\s*\\d+\\.\\s*", "").trim();
    }

    static String[] separarAutoresERestante(String descricao) {
        int separador = descricao.indexOf(" . ");
        if (separador < 0) {
            separador = descricao.indexOf(".. ");
        }
        if (separador < 0) {
            return new String[]{null, descricao};
        }
        return new String[]{
                valorOuNull(descricao.substring(0, separador)),
                descricao.substring(separador + (descricao.startsWith(".. ", separador) ? 3 : 3)).trim()
        };
    }

    static String extrairTitulo(String restante) {
        if (restante == null || restante.isBlank()) {
            return null;
        }

        int fim = restante.length();
        for (String marcador : new String[]{". In:", ". Em:", ". ISBN"}) {
            int indice = restante.indexOf(marcador);
            if (indice > 0) {
                fim = Math.min(fim, indice);
            }
        }
        Matcher edicao = Pattern.compile("(?i)\\.\\s*\\d+(?:ª|a|o)?\\.?\\s*ed\\.").matcher(restante);
        if (edicao.find()) {
            fim = Math.min(fim, edicao.start());
        }
        Matcher editoraEAno = Pattern.compile("(?i)\\.\\s*(?:[^.]+:\\s*)?[^,.]+,\\s*(?:19|20)\\d{2}").matcher(restante);
        if (editoraEAno.find()) {
            fim = Math.min(fim, editoraEAno.start());
        }
        return valorOuNull(restante.substring(0, fim).replaceFirst("\\.$", ""));
    }

    static String extrairTituloLivro(String restante) {
        if (restante == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("(?i)\\.\\s+(?:In|Em):\\s*(.+)").matcher(restante);
        if (!matcher.find()) {
            return null;
        }
        String aposMarcador = matcher.group(1);
        Matcher editoraEAno = Pattern.compile("(?i)\\.\\s*(?:[^.]+:\\s*)?[^,.]+,\\s*(?:19|20)\\d{2}")
                .matcher(aposMarcador);
        int fim = editoraEAno.find() ? editoraEAno.start() : aposMarcador.length();
        return valorOuNull(aposMarcador.substring(0, fim).replaceFirst("\\.$", ""));
    }

    static Integer extrairUltimoAno(String descricao) {
        Matcher matcher = PADRAO_ANO.matcher(descricao == null ? "" : descricao);
        Integer ano = null;
        while (matcher.find()) {
            ano = Integer.parseInt(matcher.group());
        }
        return ano;
    }

    static String extrairIsbn(String descricao) {
        Matcher matcher = PADRAO_ISBN.matcher(descricao == null ? "" : descricao);
        return matcher.find() ? valorOuNull(matcher.group(1).replaceAll("\\s+", "")) : null;
    }

    static String extrairEdicao(String descricao) {
        return extrairGrupo(PADRAO_EDICAO, descricao);
    }

    static String extrairVolume(String descricao) {
        return extrairGrupo(PADRAO_VOLUME, descricao);
    }

    static String extrairPaginas(String descricao) {
        String paginas = extrairGrupo(PADRAO_PAGINAS_PREFIXO, descricao);
        return paginas != null ? paginas : extrairGrupo(PADRAO_PAGINAS_SUFIXO, descricao);
    }

    static String extrairEditora(String descricao) {
        Integer ano = extrairUltimoAno(descricao);
        if (ano == null) {
            return null;
        }
        int indiceAno = descricao.lastIndexOf(String.valueOf(ano));
        String antesDoAno = descricao.substring(0, indiceAno).replaceFirst("[,.\\s]+$", "");
        int separador = Math.max(antesDoAno.lastIndexOf(":"), antesDoAno.lastIndexOf(" . "));
        return separador >= 0 ? valorOuNull(antesDoAno.substring(separador + 1).replaceFirst("^[.\\s]+", "")) : null;
    }

    static String valorOuNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static String extrairGrupo(Pattern padrao, String descricao) {
        Matcher matcher = padrao.matcher(descricao == null ? "" : descricao);
        return matcher.find() ? valorOuNull(matcher.group(1)) : null;
    }
}
