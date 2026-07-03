package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.Artigo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ArtigoParser {

    private static final Pattern PADRAO_ANO = Pattern.compile("\\b(19|20)\\d{2}\\b");

    private ArtigoParser() {
    }

    public static List<Artigo> parse(Document doc) {
        List<Artigo> artigos = new ArrayList<>();
        Elements elementosArtigos = doc.select("div.artigo-completo");

        for (Element elArtigo : elementosArtigos) {
            String textoBruto = elArtigo.text();
            String citacaoLimpada = limparCitacao(textoBruto);

            Artigo novoArtigo = new Artigo();
            Integer ano = extrairUltimoAno(textoBruto);
            novoArtigo.setAno(ano);
            novoArtigo.setCitacaoCompleta(citacaoLimpada);

            if (!isDuplicado(artigos, citacaoLimpada)) {
                artigos.add(novoArtigo);
            }
        }

        return artigos;
    }

    private static String limparCitacao(String textoBruto) {
        return textoBruto.replaceAll("^\\d+\\.\\s*", "").trim();
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
