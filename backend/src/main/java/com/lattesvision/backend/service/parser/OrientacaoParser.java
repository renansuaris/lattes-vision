package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.Orientacao;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OrientacaoParser {

    private static final Pattern PADRAO_ANO = Pattern.compile("\\b(19|20)\\d{2}\\b");

    private OrientacaoParser() {
    }

    public static List<Orientacao> parse(Document doc) {
        List<Orientacao> orientacoes = new ArrayList<>();
        Elements elementos = doc.select("h1, h2, div.layout-cell-11, div.layout-cell-pad-5, div.layout-cell-pad-6");

        String statusAtual = null; // null significa que o motor de extração está DESLIGADO

        for (Element el : elementos) {
            String texto = el.text().trim();

            // 1. Controle da Máquina de Estados (Interruptor de Seção)
            if (isTitulo(el, texto)) {
                String titulo = texto.toLowerCase(Locale.ROOT);

                if (titulo.contains("orientações") || titulo.contains("orientacoes")) {
                    if (titulo.contains("em andamento")) {
                        statusAtual = "EM_ANDAMENTO";
                    } else {
                        // Se não tem "em andamento", assumimos "concluída"
                        statusAtual = "CONCLUIDA";
                    }
                } else if (!titulo.isEmpty()) {
                    statusAtual = null; // Encontrou outra seção (Ex: Produções), desliga o motor!
                }
                continue;
            }

            // 2. Extração dos Dados (Só funciona se o motor estiver ligado)
            if (statusAtual == null || texto.length() < 15) {
                continue;
            }

            // Escudo extra: ignora linhas de formação acadêmica caso escapem (ex: "1999 - 2000")
            if (texto.matches("^(19|20)\\d{2}\\s*-.*")) {
                continue;
            }

            Orientacao ori = new Orientacao();
            ori.setStatus(normalizarParaBusca(texto).contains("inicio:") ? "EM_ANDAMENTO" : "CONCLUIDA");// "EM_ANDAMENTO" ou "CONCLUIDA"
            ori.setTipo(detectarTipo(texto));
            ori.setAno(extrairUltimoAno(texto));

            // 3. Limpeza e Separação (Nome do Aluno vs Título)
            String citacaoLimpada = texto.replaceAll("^\\d+\\.\\s*", "").trim();
            String[] partes = citacaoLimpada.split("\\.\\s+", 3);

            if (partes.length >= 2) {
                ori.setNomeOrientado(partes[0].trim());
                // O resto da string fica no título
                ori.setTituloTrabalho(citacaoLimpada.substring(partes[0].length() + 1).trim());
            } else {
                ori.setTituloTrabalho(citacaoLimpada);
            }

            orientacoes.add(ori);
        }

        return orientacoes;
    }

    private static boolean isTitulo(Element el, String texto) {
        // Títulos no Lattes podem ser h1/h2 ou divs curtas com a palavra chave
        return "h1".equals(el.tagName()) || "h2".equals(el.tagName()) ||
                (texto.length() < 60 && texto.toLowerCase().contains("orientações e supervisões"));
    }

    private static String detectarTipo(String texto) {
        String lower = normalizarParaBusca(texto);

        if (lower.contains("mestrado")) {
            return "Mestrado";
        } else if (lower.contains("doutorado")) {
            return "Doutorado";
        } else if (lower.contains("trabalho de conclusao") || lower.contains("tcc")) {
            return "TCC";
        } else if (lower.contains("iniciacao cientifica")) {
            return "Iniciacao Cientifica";
        } else if (lower.contains("iniciacao a docencia")) {
            // Correção do Falso Positivo! Docência cai em "Outra"
            return "Outra";
        } else if (lower.contains("especializacao")) {
            return "Especializacao";
        }

        return "Outra";
    }

    private static Integer extrairUltimoAno(String texto) {
        Matcher matcher = PADRAO_ANO.matcher(texto);
        Integer ano = null;
        // Pega sempre a última ocorrência de 4 dígitos no texto
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
