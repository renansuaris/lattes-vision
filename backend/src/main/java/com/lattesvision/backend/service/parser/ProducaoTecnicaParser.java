package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.CategoriaProducaoTecnica;
import com.lattesvision.backend.model.ProducaoTecnica;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProducaoTecnicaParser {

    private static final Pattern PADRAO_ANO = Pattern.compile("\\b(19|20)\\d{2}\\b");
    private static final Pattern PADRAO_NATUREZA = Pattern.compile("\\(([^()]*)\\)");

    private ProducaoTecnicaParser() {
    }

    public static List<ProducaoTecnica> parse(Document doc) {
        List<ProducaoTecnica> producoes = new ArrayList<>();
        Elements elementos = doc.select("h1, h2, div.inst_back, div.cita-artigos, div.layout-cell-11, div.layout-cell-pad-5, div.layout-cell-pad-6");

        boolean dentroDaSecao = false;
        CategoriaProducaoTecnica categoriaAtual = null;

        for (Element el : elementos) {
            String texto = el.text().trim();
            String textoBusca = normalizarParaBusca(texto);
            String tagName = el.tagName().toLowerCase(Locale.ROOT);
            String className = el.className();

            if (isTitulo(tagName, className, texto)) {
                if (textoBusca.contains("producao tecnica")) {
                    dentroDaSecao = true;
                    categoriaAtual = null;
                    continue;
                }

                CategoriaProducaoTecnica categoriaDetectada = detectarCategoria(textoBusca);
                if (dentroDaSecao && categoriaDetectada != null) {
                    categoriaAtual = categoriaDetectada;
                    continue;
                }

                if (dentroDaSecao && isTituloDeOutraSecao(textoBusca)) {
                    dentroDaSecao = false;
                    categoriaAtual = null;
                }
                continue;
            }

            if (!dentroDaSecao || categoriaAtual == null || !isItem(className) || texto.length() < 10) {
                continue;
            }

            producoes.add(parseItem(texto, categoriaAtual));
        }

        return producoes;
    }

    private static boolean isTitulo(String tagName, String className, String texto) {
        return "h1".equals(tagName)
                || "h2".equals(tagName)
                || className.contains("inst_back")
                || className.contains("cita-artigos")
                || texto.length() < 80 && detectarCategoria(normalizarParaBusca(texto)) != null;
    }

    private static boolean isItem(String className) {
        return className.contains("layout-cell-11")
                || className.contains("layout-cell-pad-5")
                || className.contains("layout-cell-pad-6");
    }

    private static boolean isTituloDeOutraSecao(String textoBusca) {
        return textoBusca.contains("producao bibliografica")
                || textoBusca.contains("producao artistica")
                || textoBusca.contains("orientacoes")
                || textoBusca.contains("bancas")
                || textoBusca.contains("eventos")
                || textoBusca.contains("projetos")
                || textoBusca.contains("patentes")
                || textoBusca.contains("demais trabalhos");
    }

    private static CategoriaProducaoTecnica detectarCategoria(String textoBusca) {
        if (textoBusca.contains("programas de computador com registro")
                || textoBusca.contains("software com registro")
                || textoBusca.contains("softwares com registro")) {
            return CategoriaProducaoTecnica.SOFTWARE_COM_REGISTRO;
        }
        if (textoBusca.contains("programas de computador sem registro")
                || textoBusca.contains("software sem registro")
                || textoBusca.contains("softwares sem registro")) {
            return CategoriaProducaoTecnica.SOFTWARE_SEM_REGISTRO;
        }
        if (textoBusca.contains("produtos tecnologicos")
                || textoBusca.contains("produto tecnologico")) {
            return CategoriaProducaoTecnica.PRODUTO_TECNOLOGICO;
        }
        if (textoBusca.contains("processos ou tecnicas")
                || textoBusca.contains("processo ou tecnica")) {
            return CategoriaProducaoTecnica.PROCESSO_OU_TECNICA;
        }
        if (textoBusca.contains("trabalhos tecnicos")
                || textoBusca.contains("trabalho tecnico")) {
            return CategoriaProducaoTecnica.TRABALHO_TECNICO;
        }
        if (textoBusca.contains("demais tipos de producao tecnica")
                || textoBusca.contains("entrevistas, mesas redondas")) {
            return CategoriaProducaoTecnica.OUTRA;
        }
        return null;
    }

    private static ProducaoTecnica parseItem(String textoBruto, CategoriaProducaoTecnica categoria) {
        String descricao = textoBruto.replaceAll("^\\d+\\.\\s*", "").trim();
        String[] partes = separarAutoresEDescricao(descricao);
        String restante = partes[1].trim();

        Integer ano = extrairUltimoAno(restante);
        if (ano != null) {
            restante = removerTextoAPartirDoUltimoAno(restante);
        }

        String natureza = null;
        if (deveExtrairNatureza(categoria)) {
            natureza = extrairUltimaNatureza(restante);
            if (natureza != null) {
                restante = removerUltimaNatureza(restante);
            }
        }

        ProducaoTecnica producao = new ProducaoTecnica();
        producao.setCategoria(categoria);
        producao.setAutoresTexto(partes[0].trim());
        producao.setTitulo(limparPontuacaoFinal(restante));
        producao.setAno(ano);
        producao.setNatureza(natureza);
        producao.setDescricaoBruta(descricao);
        return producao;
    }

    private static String[] separarAutoresEDescricao(String descricao) {
        int idx = indiceSeparador(descricao, " . ");
        if (idx < 0) {
            idx = indiceSeparador(descricao, ".. ");
        }
        if (idx < 0) {
            idx = indiceSeparador(descricao, ". ");
        }
        if (idx < 0) {
            return new String[]{"", descricao};
        }

        String separador = descricao.startsWith(".. ", idx) ? ".. " : descricao.startsWith(" . ", idx) ? " . " : ". ";
        return new String[]{
                descricao.substring(0, idx).trim(),
                descricao.substring(idx + separador.length()).trim()
        };
    }

    private static int indiceSeparador(String texto, String separador) {
        int idx = texto.indexOf(separador);
        if (idx < 0) {
            return -1;
        }
        String restante = texto.substring(idx + separador.length());
        return restante.length() >= 10 ? idx : -1;
    }

    private static Integer extrairUltimoAno(String texto) {
        Matcher matcher = PADRAO_ANO.matcher(texto);
        Integer ano = null;
        while (matcher.find()) {
            ano = Integer.parseInt(matcher.group());
        }
        return ano;
    }

    private static String removerTextoAPartirDoUltimoAno(String texto) {
        Matcher matcher = PADRAO_ANO.matcher(texto);
        int inicioUltimoAno = -1;
        while (matcher.find()) {
            inicioUltimoAno = matcher.start();
        }
        return inicioUltimoAno >= 0 ? texto.substring(0, inicioUltimoAno).trim() : texto;
    }

    private static boolean deveExtrairNatureza(CategoriaProducaoTecnica categoria) {
        return categoria == CategoriaProducaoTecnica.PROCESSO_OU_TECNICA
                || categoria == CategoriaProducaoTecnica.OUTRA;
    }

    private static String extrairUltimaNatureza(String texto) {
        Matcher matcher = PADRAO_NATUREZA.matcher(texto);
        String natureza = null;
        while (matcher.find()) {
            natureza = matcher.group(1).trim();
        }
        return natureza == null || natureza.isBlank() ? null : natureza;
    }

    private static String removerUltimaNatureza(String texto) {
        Matcher matcher = PADRAO_NATUREZA.matcher(texto);
        int inicio = -1;
        int fim = -1;
        while (matcher.find()) {
            inicio = matcher.start();
            fim = matcher.end();
        }
        if (inicio < 0) {
            return texto;
        }
        return (texto.substring(0, inicio) + texto.substring(fim)).trim();
    }

    private static String limparPontuacaoFinal(String texto) {
        return texto == null ? null : texto.trim().replaceAll("[\\s.,;]+$", "");
    }

    private static String normalizarParaBusca(String texto) {
        String lower = texto == null ? "" : texto.toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
