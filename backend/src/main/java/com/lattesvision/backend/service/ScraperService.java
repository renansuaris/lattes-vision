package com.lattesvision.backend.service;

import com.lattesvision.backend.model.*;
import com.lattesvision.backend.repository.PesquisadorRepository;
import com.lattesvision.backend.service.parser.ArtigoParser;
import com.lattesvision.backend.service.parser.BancaParser;
import com.lattesvision.backend.service.parser.OrientacaoParser;
import com.lattesvision.backend.service.parser.ProducaoTecnicaParser;
import com.lattesvision.backend.service.parser.ProjetoParser;
import com.lattesvision.backend.service.parser.TrabalhoEventoParser;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScraperService {
    private static final Logger log = LoggerFactory.getLogger(ScraperService.class);

    private static final String URL_LATTES_16 = "https://lattes.cnpq.br/";
    private static final String URL_PREVIEW = "http://buscatextual.cnpq.br/buscatextual/preview.do?metodo=apresentar&id=";
    private static final int MAX_TENTATIVAS = 3;
    private static final long RETRY_BACKOFF_MS = 2000L;
    private static final Pattern PADRAO_DATA = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})");

    private Playwright playwright;
    private Browser browser;

    @Autowired
    private PesquisadorRepository repository;

    @PostConstruct
    public void init() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        log.info("Motor Playwright iniciado no LattesVision.");
    }

    @PreDestroy
    public void destroy() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    public Pesquisador sincronizarPesquisador(String idLattes) {
        Pesquisador pesquisadorExistente = repository.findById(idLattes).orElse(null);
        String id10 = pesquisadorExistente != null ? pesquisadorExistente.getIdInterno() : null;

        try (BrowserContext context = browser.newContext()) {
            Page pagePrincipal = context.newPage();
            id10 = resolverIdInterno(idLattes, id10, pagePrincipal);
            String htmlCompleto = baixarHtmlComRetry(id10, context, pagePrincipal);
            return parseESalvar(idLattes, id10, htmlCompleto);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao extrair dados do Lattes: " + e.getMessage(), e);
        }
    }

    public List<Pesquisador> sincronizarLote(List<String> idsLattes) {
        List<Pesquisador> processados = new ArrayList<>();
        log.info("Iniciando processamento em lote ({} perfis)", idsLattes.size());

        for (int i = 0; i < idsLattes.size(); i++) {
            String id = idsLattes.get(i);
            log.info("==================================================");
            log.info("Processando [{}/{}] - Lattes ID: {}", i + 1, idsLattes.size(), id);

            try {
                processados.add(sincronizarPesquisador(id));
            } catch (Exception e) {
                log.error("Erro ao processar ID {}: {}", id, e.getMessage(), e);
            }

            if (i < idsLattes.size() - 1) {
                int tempoEspera = ThreadLocalRandom.current().nextInt(3000, 6000);
                log.info("Aguardando {}ms entre requisicoes...", tempoEspera);
                sleep(tempoEspera);
            }
        }

        log.info("Lote finalizado.");
        return processados;
    }

    private String resolverIdInterno(String idLattes, String id10Cache, Page pagePrincipal) {
        if (id10Cache != null && id10Cache.length() == 10) {
            log.info("Usando ID interno em cache: {}", id10Cache);
            return id10Cache;
        }

        log.info("Descobrindo ID interno para: {}", idLattes);
        pagePrincipal.navigate(URL_LATTES_16 + idLattes);
        String id10 = extrairParametroId(pagePrincipal.url());

        if (id10 == null || id10.length() != 10) {
            throw new RuntimeException("Falha ao obter ID interno para " + idLattes);
        }
        return id10;
    }

    private String baixarHtmlComRetry(String id10, BrowserContext context, Page pagePrincipal) {
        RuntimeException ultimoErro = null;

        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            try {
                pagePrincipal.navigate(URL_PREVIEW + id10);
                Page novaAbaCv = context.waitForPage(() -> pagePrincipal.evaluate("abreCV()"));
                novaAbaCv.waitForSelector("h2.nome", new Page.WaitForSelectorOptions().setTimeout(15000));
                String htmlCompleto = novaAbaCv.content();
                novaAbaCv.close();
                return htmlCompleto;
            } catch (Exception e) {
                ultimoErro = new RuntimeException("Tentativa " + tentativa + " falhou: " + e.getMessage(), e);
                if (tentativa < MAX_TENTATIVAS) {
                    sleep(RETRY_BACKOFF_MS * tentativa);
                }
            }
        }

        throw ultimoErro != null ? ultimoErro : new RuntimeException("Falha ao baixar HTML do curriculo.");
    }

    private Pesquisador parseESalvar(String idLattes, String id10, String html) {
        Document doc = Jsoup.parse(html);
        Pesquisador pesquisador = repository.findById(idLattes).orElse(new Pesquisador());

        pesquisador.setIdLattes(idLattes);
        pesquisador.setIdInterno(id10);
        pesquisador.setDataSincronizacao(LocalDateTime.now());
        pesquisador.setDataUltimaAtualizacao(extrairDataAtualizacao(doc));

        Element nome = doc.selectFirst("h2.nome");
        pesquisador.setNomeCompleto(nome != null ? nome.ownText() : "Nome nao encontrado");
        pesquisador.setResumoProfissional(doc.select("p.resumo").text());

        List<Artigo> artigos = ArtigoParser.parse(doc);
        List<Orientacao> orientacoes = OrientacaoParser.parse(doc);
        List<Projeto> projetos = ProjetoParser.parse(doc);
        List<Banca> bancas = BancaParser.parse(doc);
        List<ProducaoTecnica> producoesTecnicas = ProducaoTecnicaParser.parse(doc);
        List<TrabalhoEvento> trabalhosEventos = TrabalhoEventoParser.parse(doc);

        pesquisador.getArtigos().clear();
        artigos.forEach(pesquisador::addArtigo);

        pesquisador.getOrientacoes().clear();
        orientacoes.forEach(pesquisador::addOrientacao);

        pesquisador.getProjetos().clear();
        projetos.forEach(pesquisador::addProjeto);

        pesquisador.getBancas().clear();
        bancas.forEach(pesquisador::addBanca);

        pesquisador.getProducoesTecnicas().clear();
        producoesTecnicas.forEach(pesquisador::addProducaoTecnica);

        pesquisador.getTrabalhosEventos().clear();
        trabalhosEventos.forEach(pesquisador::addTrabalhoEvento);

        log.info("Salvando pesquisador: {} | Artigos: {} | Orientacoes: {} | Projetos: {} | Bancas: {} | Producoes tecnicas: {} | Trabalhos em eventos: {}",
                pesquisador.getNomeCompleto(),
                pesquisador.getArtigos().size(),
                pesquisador.getOrientacoes().size(),
                pesquisador.getProjetos().size(),
                pesquisador.getBancas().size(),
                pesquisador.getProducoesTecnicas().size(),
                pesquisador.getTrabalhosEventos().size());

        return repository.save(pesquisador);
    }

    private LocalDate extrairDataAtualizacao(Document doc) {
        String atualizacao = doc.select("ul.informacoes-autor li:contains(Atualização do currículo)").text();
        Matcher matcher = PADRAO_DATA.matcher(atualizacao);
        if (!matcher.find()) {
            return null;
        }
        int dia = Integer.parseInt(matcher.group(1));
        int mes = Integer.parseInt(matcher.group(2));
        int ano = Integer.parseInt(matcher.group(3));
        return LocalDate.of(ano, mes, dia);
    }

    private String extrairParametroId(String url) {
        int idx = url.indexOf("id=");
        if (idx < 0) {
            return null;
        }
        String id = url.substring(idx + 3);
        int amp = id.indexOf('&');
        return amp > -1 ? id.substring(0, amp) : id;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrompida durante espera.", e);
        }
    }
}
