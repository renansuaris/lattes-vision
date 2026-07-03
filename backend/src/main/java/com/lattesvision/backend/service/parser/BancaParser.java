package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.Banca;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BancaParser {

    private BancaParser() {
    }

    public static List<Banca> parse(Document doc) {
        List<Banca> bancas = new ArrayList<>();

        // Buscamos h1/h2 (Títulos maiores), inst_back (Títulos médios), cita-artigos (Subtipos) e layout-cell-11 (Dados)
        Elements elementos = doc.select("h1, h2, div.inst_back, div.cita-artigos, div.layout-cell-11, div.layout-cell-pad-5, div.layout-cell-pad-6");

        boolean isSecaoBancas = false;
        String tipoBancaAtual = "Outra";

        for (Element el : elementos) {
            String texto = el.text().trim();
            String className = el.className();
            String tagName = el.tagName().toLowerCase();

            // 1. Controle da Máquina de Estados (Liga/Desliga a extração de bancas)
            if (tagName.equals("h1") || tagName.equals("h2") || className.contains("inst_back")) {
                String tituloLower = normalizarParaBusca(texto);
                if (tituloLower.contains("bancas")) {
                    isSecaoBancas = true;
                } else if (!tituloLower.isEmpty() && !tituloLower.contains("participacao em")) {
                    isSecaoBancas = false; // Achou outra seção grande do Lattes, desliga o motor!
                }
                continue;
            }

            if (!isSecaoBancas) continue;

            // 2. Captura o Subtipo da Banca (Ex: "Teses de doutorado", "Concurso público")
            if (className.contains("cita-artigos")) {
                tipoBancaAtual = classificarTipoBanca(texto);
                continue;
            }

            // 3. Extração dos Dados da Banca
            if (className.contains("layout-cell-11") || className.contains("layout-cell-pad")) {
                if (texto.length() < 15) continue;

                Banca banca = new Banca();
                banca.setTipo(tipoBancaAtual);
                banca.setAno(extrairUltimoAno(texto));

                // Limpa o número inicial da lista ("1. ", "2. ")
                String citacaoLimpada = texto.replaceAll("^\\d+\\.\\s*", "").trim();

                // Procura a ancora de texto padrao do Lattes, tolerando acentos.
                String marcador = "Participacao em banca de ";
                int idxMarcador = normalizarParaBusca(citacaoLimpada).indexOf(marcador.toLowerCase(Locale.ROOT));

                if (idxMarcador != -1) {
                    // Corta tudo o que vem antes do marcador (geralmente os nomes dos avaliadores)
                    String resto = citacaoLimpada.substring(idxMarcador + marcador.length());

                    // Separa o nome do candidato (fica antes do primeiro ponto) do título do trabalho
                    String[] partes = resto.split("\\.", 2);
                    banca.setNomeCandidato(partes[0].trim());

                    if (partes.length > 1) {
                        banca.setTituloTrabalho(partes[1].trim());
                    } else {
                        banca.setTituloTrabalho(resto.trim());
                    }
                } else {
                    // Se não tiver o marcador (Ex: Concursos, Comissões julgadoras), o nome é nulo
                    // e guardamos a citação completa no título para preservar a informação
                    banca.setNomeCandidato(null);
                    banca.setTituloTrabalho(citacaoLimpada);
                }

                bancas.add(banca);
            }
        }
        return bancas;
    }

    private static String classificarTipoBanca(String texto) {
        String lower = normalizarParaBusca(texto);
        if (lower.contains("mestrado")) {
            return lower.contains("qualifica") ? "Qualificacao de Mestrado" : "Mestrado";
        }
        if (lower.contains("doutorado")) {
            return lower.contains("qualifica") ? "Qualificacao de Doutorado" : "Doutorado";
        }
        if (lower.contains("conclusao de curso") || lower.contains("tcc")) {
            return "TCC";
        }
        if (lower.contains("concurso")) {
            return "Concurso Publico";
        }
        if (lower.contains("livre-docencia") || lower.contains("livre docencia")) {
            return "Livre-Docencia";
        }
        if (lower.contains("avaliacao")) {
            return "Avaliacao de Curso";
        }
        return "Outra";
    }

    private static Integer extrairUltimoAno(String texto) {
        Matcher matcher = Pattern.compile("\\b(19|20)\\d{2}\\b").matcher(texto);
        Integer ano = null;
        while (matcher.find()) {
            ano = Integer.parseInt(matcher.group()); // Pega sempre o ano mais próximo do final
        }
        return ano;
    }

    private static String normalizarParaBusca(String texto) {
        String lower = texto == null ? "" : texto.toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
