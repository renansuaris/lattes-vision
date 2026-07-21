package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.Artigo;
import com.lattesvision.backend.util.IssnUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ArtigoParser {

    private static final Pattern PADRAO_ANO = Pattern.compile("\\b(19|20)\\d{2}\\b");
    private static final String SELETOR_ANO_ESTRUTURADO =
            "span.informacao-artigo[data-tipo-ordenacao=ano]";

    private ArtigoParser() {
    }

    public static List<Artigo> parse(Document doc) {
        List<Artigo> artigos = new ArrayList<>();
        Elements elementosArtigos = doc.select("div.artigo-completo");

        for (Element elArtigo : elementosArtigos) {
            String textoBruto = elArtigo.text();
            String citacaoLimpada = extrairCitacao(elArtigo);
            Map<String, String> dadosEstruturados = extrairDadosEstruturados(elArtigo);

            Artigo novoArtigo = new Artigo();
            novoArtigo.setAno(extrairAno(elArtigo, textoBruto));
            novoArtigo.setCitacaoCompleta(citacaoLimpada);
            novoArtigo.setTitulo(valorOuNull(dadosEstruturados.get("titulo")));
            novoArtigo.setPeriodico(valorOuNull(dadosEstruturados.get("nomePeriodico")));
            novoArtigo.setIssn(IssnUtils.formatar(dadosEstruturados.get("issn")));

            if (!isDuplicado(artigos, citacaoLimpada)) {
                artigos.add(novoArtigo);
            }
        }

        return artigos;
    }

    private static Map<String, String> extrairDadosEstruturados(Element artigo) {
        Element elementoComCvuri = artigo.selectFirst("[cvuri]");
        if (elementoComCvuri == null) {
            return Map.of();
        }

        String cvuri = elementoComCvuri.attr("cvuri")
                .replaceAll("(?i)</?monospace>", "");
        int inicioQuery = cvuri.indexOf('?');
        String query = inicioQuery >= 0 ? cvuri.substring(inicioQuery + 1) : cvuri;

        Map<String, String> parametros = new HashMap<>();
        for (String parametro : query.split("&(?=[A-Za-z][A-Za-z0-9_]*=)")) {
            int separador = parametro.indexOf('=');
            if (separador <= 0) {
                continue;
            }

            String chave = parametro.substring(0, separador);
            String valor = parametro.substring(separador + 1);
            parametros.put(chave, decodificar(valor));
        }
        return parametros;
    }

    private static String decodificar(String valor) {
        try {
            return URLDecoder.decode(valor, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return valor;
        }
    }

    private static Integer extrairAno(Element artigo, String textoBruto) {
        Element elementoAno = artigo.selectFirst(SELETOR_ANO_ESTRUTURADO);
        if (elementoAno != null) {
            Matcher matcher = PADRAO_ANO.matcher(elementoAno.text());
            if (matcher.find()) {
                return Integer.parseInt(matcher.group());
            }
        }
        return extrairUltimoAno(textoBruto);
    }

    private static String valorOuNull(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private static String limparCitacao(String textoBruto) {
        return textoBruto.replaceAll("^\\d+\\.\\s*", "").trim();
    }

    private static String extrairCitacao(Element artigo) {
        Element copia = artigo.clone();
        copia.select(".informacao-artigo[data-tipo-ordenacao]").remove();
        return limparCitacao(copia.text());
    }

    private static Integer extrairUltimoAno(String texto) {
        Matcher matcher = PADRAO_ANO.matcher(texto);
        Integer ano = null;
        while (matcher.find()) {
            ano = Integer.parseInt(matcher.group());
        }
        return ano;
    }

    private static boolean isDuplicado(List<Artigo> artigos, String citacao) {
        String normalizada = normalizar(citacao);
        for (Artigo artigoExistente : artigos) {
            String existente = normalizar(artigoExistente.getCitacaoCompleta());
            if (existente.contains(normalizada) || normalizada.contains(existente)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}
