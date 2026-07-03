package com.lattesvision.backend.service.parser;

import com.lattesvision.backend.model.Projeto;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProjetoParser {

    // Padrão que identifica o INÍCIO de um projeto (Ex: "2018 - 2021", "2020 - Atual", "2015 - 2015")
    private static final Pattern PADRAO_INICIO_PROJETO = Pattern.compile("^(19|20)\\d{2}\\s*-.*");

    private ProjetoParser() {
    }

    public static List<Projeto> parse(Document doc) {
        List<Projeto> projetos = new ArrayList<>();
        // O Jsoup lê de cima para baixo na ordem exata da tela
        Elements elementos = doc.select("h1, h2, div.layout-cell-11, div.layout-cell-pad-5, div.layout-cell-pad-6");

        boolean dentroDaSecao = false;
        String tipoAtual = "Projeto";

        Projeto projetoAtual = null; // <-- O nosso ponteiro Acumulador!

        for (Element el : elementos) {
            String texto = el.text().trim();

            // Controle da Máquina de Estados (Seções)
            if (isTitulo(el, texto)) {
                String titulo = texto.toLowerCase(Locale.ROOT);

                if (titulo.contains("projetos de")) {
                    dentroDaSecao = true;
                    tipoAtual = detectarTipoPeloTitulo(titulo);
                    projetoAtual = null; // Reseta ao mudar de seção
                } else if (dentroDaSecao && !titulo.isEmpty()) {
                    dentroDaSecao = false;
                    projetoAtual = null; // Saiu da seção de projetos
                }
                continue;
            }

            if (!dentroDaSecao || texto.length() < 5) {
                continue;
            }

            // 1. Identifica se é a "Cabeça" de um projeto novo pela data inicial
            if (PADRAO_INICIO_PROJETO.matcher(texto).matches()) {
                projetoAtual = new Projeto();
                projetoAtual.setTipo(tipoAtual);
                projetoAtual.setAno(extrairAnoInicial(texto));
                projetoAtual.setTitulo(""); // Vai ser preenchido na próxima iteração
                projetos.add(projetoAtual);
            }
            // 2. Se já temos um projeto aberto, os blocos seguintes pertencem-lhe!
            else if (projetoAtual != null) {
                if (projetoAtual.getTitulo().isEmpty()) {
                    // O primeiro bloco logo após a data é sempre o Título do Projeto
                    projetoAtual.setTitulo(texto);
                } else {
                    // Os blocos seguintes (Descrição, Integrantes, Financiadores) nós concatenamos
                    // Adicionamos duas quebras de linha para ficar um texto bonito no Banco de Dados
                    projetoAtual.setTitulo(projetoAtual.getTitulo() + "\n\n" + texto);
                }
            }
        }

        return projetos;
    }

    private static boolean isTitulo(Element el, String texto) {
        // Melhorei a heurística: Títulos podem ser h1/h2 ou divs curtas com a palavra "projetos de"
        return "h1".equals(el.tagName()) || "h2".equals(el.tagName()) ||
                (texto.length() < 60 && texto.toLowerCase().contains("projetos de"));
    }

    private static String detectarTipoPeloTitulo(String titulo) {
        if (titulo.contains("pesquisa")) return "Pesquisa";
        if (titulo.contains("extens")) return "Extensao";
        if (titulo.contains("desenvolvimento")) return "Desenvolvimento";
        if (titulo.contains("ensino")) return "Ensino"; // <-- A NOSSA NOVA LINHA!
        return "Projeto";
    }

    private static Integer extrairAnoInicial(String texto) {
        // Agora só olhamos para os 4 primeiros dígitos do texto, ignorando datas nos editais
        Matcher matcher = Pattern.compile("^(19|20)\\d{2}").matcher(texto);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return null;
    }
}
