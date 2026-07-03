package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.TrabalhoEvento;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TrabalhoEventoParser {

    private static final Pattern PADRAO_ANO = Pattern.compile("\\b(19|20)\\d{2}\\b");

    private TrabalhoEventoParser() {
    }

    public static List<TrabalhoEvento> parse(Document doc) {
        List<TrabalhoEvento> trabalhos = new ArrayList<>();
        
        // Seleção mais ampla de elementos que podem conter títulos ou itens
        Elements elementos = doc.select("h1, h2, h3, div.inst_back, div.cita-artigos, div.layout-cell-11, div.layout-cell-pad-5, div.layout-cell-pad-6");

        boolean dentroDaProducaoBibliografica = false;
        String tipoAtual = null;

        for (Element el : elementos) {
            String texto = el.text().trim();
            if (texto.isEmpty()) continue;

            String textoBusca = normalizarParaBusca(texto);
            String tagName = el.tagName().toLowerCase(Locale.ROOT);
            String className = el.className();

            // Detectar início da seção de Produção Bibliográfica (pode ser h1 ou h2)
            if (isTituloPrincipal(tagName, textoBusca)) {
                dentroDaProducaoBibliografica = true;
                tipoAtual = null;
                continue;
            }

            // Se mudou para outra seção principal, sai da bibliográfica
            if (isTituloDeOutraSecao(tagName, textoBusca) && dentroDaProducaoBibliografica) {
                // Se for um título de outra seção, mas não for as subseções que queremos
                if (!isTituloSubsecao(tagName, className, textoBusca)) {
                    dentroDaProducaoBibliografica = false;
                    tipoAtual = null;
                    continue;
                }
            }

            // Detectar subseções de eventos (Trabalhos completos, Resumos, etc.)
            if (isTituloSubsecao(tagName, className, textoBusca)) {
                tipoAtual = detectarTipo(textoBusca);
                continue;
            }

            // SEGURANÇA: Se encontrar qualquer outro título (h1, h2, h3 ou barra de título) 
            // que NÃO seja de eventos, para de capturar itens imediatamente.
            if (isQualquerOutroTitulo(tagName, className, textoBusca)) {
                tipoAtual = null;
                continue;
            }

            // Se estamos dentro de uma subseção de eventos e é um item da lista
            if (tipoAtual != null && isItem(className) && texto.length() > 15) {
                TrabalhoEvento novoTrabalho = parseItem(texto, tipoAtual);
                if (novoTrabalho != null && !isDuplicado(trabalhos, novoTrabalho.getTitulo())) {
                    trabalhos.add(novoTrabalho);
                }
            }
        }

        return trabalhos;
    }

    private static boolean isTituloPrincipal(String tagName, String textoBusca) {
        return (tagName.equals("h1") || tagName.equals("h2")) && textoBusca.equals("producao bibliografica");
    }

    private static boolean isQualquerOutroTitulo(String tagName, String className, String textoBusca) {
        // Se for um cabeçalho HTML
        if (tagName.matches("h[1-3]")) return true;
        
        // Se for uma daquelas barras cinzas do Lattes (inst_back ou cita-artigos)
        if (className.contains("inst_back") || className.contains("cita-artigos")) {
            // Só é outro título se não for um dos títulos de subseção de eventos
            return !isTituloSubsecao(tagName, className, textoBusca);
        }

        return false;
    }

    private static boolean isTituloDeOutraSecao(String tagName, String textoBusca) {
        if (!tagName.equals("h1") && !tagName.equals("h2")) return false;
        return textoBusca.contains("producao tecnica") 
                || textoBusca.contains("producao artistica")
                || textoBusca.contains("orientacoes")
                || textoBusca.contains("bancas")
                || textoBusca.contains("projetos")
                || textoBusca.contains("outras informacoes")
                || textoBusca.contains("dados gerais");
    }

    private static boolean isTituloSubsecao(String tagName, String className, String textoBusca) {
        boolean isHeader = tagName.matches("h[1-3]") || className.contains("inst_back") || className.contains("cita-artigos");
        return isHeader && textoBusca.contains("trabalhos completos publicados em anais de congressos");
    }

    private static String detectarTipo(String textoBusca) {
        if (textoBusca.contains("trabalhos completos")) return "Completo";
        return null;
    }

    private static boolean isItem(String className) {
        return className.contains("layout-cell-11")
                || className.contains("layout-cell-pad-5")
                || className.contains("layout-cell-pad-6");
    }

    private static TrabalhoEvento parseItem(String textoBruto, String tipo) {
        // Limpar número inicial (ex: "1. ")
        String descricao = textoBruto.replaceAll("^\\d+\\.\\s*", "").trim();
        
        // Tentar separar autores por " . " ou ". "
        String[] partes = separarAutoresERestante(descricao);
        String autores = partes[0];
        String restante = partes[1];

        // Extrair título (geralmente vai até o ". In:" ou até o ano)
        String titulo = extrairTitulo(restante);
        String nomeEvento = extrairEvento(restante);
        Integer ano = extrairUltimoAno(textoBruto);

        if (titulo == null || titulo.length() < 5) return null;

        TrabalhoEvento evento = new TrabalhoEvento();
        evento.setTitulo(titulo);
        evento.setAno(ano);
        evento.setAutores(autores);
        evento.setNomeEvento(nomeEvento);
        evento.setTipo(tipo);
        return evento;
    }

    private static String[] separarAutoresERestante(String descricao) {
        int idx = descricao.indexOf(" . ");
        if (idx < 0) idx = descricao.indexOf(". ");
        
        if (idx > 0 && idx < descricao.length() / 2) { // Autores geralmente no início
            return new String[]{
                descricao.substring(0, idx).trim(),
                descricao.substring(idx + 2).trim()
            };
        }
        return new String[]{"", descricao};
    }

    private static String extrairTitulo(String restante) {
        int idxIn = restante.indexOf(". In:");
        if (idxIn > 0) {
            return restante.substring(0, idxIn).trim();
        }
        // Se não tiver ". In:", tenta pegar até o ponto final antes do ano
        int idxPonto = restante.lastIndexOf(".");
        if (idxPonto > 10) {
            return restante.substring(0, idxPonto).trim();
        }
        return restante;
    }

    private static String extrairEvento(String restante) {
        int idxIn = restante.indexOf(". In:");
        if (idxIn > 0) {
            String aposIn = restante.substring(idxIn + 5).trim();
            int idxVirgula = aposIn.indexOf(",");
            if (idxVirgula > 0) {
                return aposIn.substring(0, idxVirgula).trim();
            }
            int idxPonto = aposIn.indexOf(".");
            if (idxPonto > 0) {
                return aposIn.substring(0, idxPonto).trim();
            }
            return aposIn;
        }
        return null;
    }

    private static boolean isDuplicado(List<TrabalhoEvento> lista, String titulo) {
        if (titulo == null) return false;
        String normalizado = normalizarParaBusca(titulo);
        return lista.stream().anyMatch(t -> normalizarParaBusca(t.getTitulo()).equals(normalizado));
    }

    private static Integer extrairUltimoAno(String texto) {
        Matcher matcher = PADRAO_ANO.matcher(texto);
        Integer ano = null;
        while (matcher.find()) {
            ano = Integer.parseInt(matcher.group());
        }
        return ano;
    }

    private static String normalizarParaBusca(String texto) {
        String lower = texto == null ? "" : texto.toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
