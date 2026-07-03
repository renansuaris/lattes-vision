package com.lattesvision.backend.service;

import com.lattesvision.backend.dto.ResumoPesquisadorDTO;
import com.lattesvision.backend.dto.RankingPesquisadorDTO;
import com.lattesvision.backend.dto.ResumoGrupoDTO;
import com.lattesvision.backend.dto.SerieTemporalDTO;
import com.lattesvision.backend.dto.SerieGrupoDTO;
import com.lattesvision.backend.model.*;
import com.lattesvision.backend.repository.PesquisadorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final String STATUS_CONCLUIDA = "CONCLUIDA";
    private static final String STATUS_EM_ANDAMENTO = "EM_ANDAMENTO";

    private final PesquisadorRepository pesquisadorRepository;
    private final ScraperService scraperService;

    public DashboardService(PesquisadorRepository pesquisadorRepository, ScraperService scraperService) {
        this.pesquisadorRepository = pesquisadorRepository;
        this.scraperService = scraperService;
    }

    public ResumoPesquisadorDTO buscarResumoPesquisador(String idLattes) {
        return toResumoDTO(buscarPorId(idLattes));
    }

    public ResumoPesquisadorDTO carregarResumoPesquisador(String idLattes) {
        return toResumoDTO(carregarOuSincronizar(idLattes));
    }

    public ResumoGrupoDTO carregarResumoGrupoAdhoc(List<String> idsLattes) {
        List<ResumoPesquisadorDTO> pesquisadores = idsLattes.stream()
                .distinct()
                .map(this::carregarOuSincronizar)
                .map(this::toResumoDTO)
                .collect(Collectors.toList());

        ResumoGrupoDTO resumo = new ResumoGrupoDTO();
        resumo.setTotalPesquisadores(pesquisadores.size());
        resumo.setPesquisadores(pesquisadores);
        resumo.setTotalArtigos(somar(pesquisadores, ResumoPesquisadorDTO::getTotalArtigos));
        resumo.setTotalOrientacoes(somar(pesquisadores, ResumoPesquisadorDTO::getTotalOrientacoes));
        resumo.setTotalOrientacoesConcluidas(somar(pesquisadores, ResumoPesquisadorDTO::getTotalOrientacoesConcluidas));
        resumo.setTotalOrientacoesEmAndamento(somar(pesquisadores, ResumoPesquisadorDTO::getTotalOrientacoesEmAndamento));
        resumo.setTotalProjetos(somar(pesquisadores, ResumoPesquisadorDTO::getTotalProjetos));
        resumo.setTotalBancas(somar(pesquisadores, ResumoPesquisadorDTO::getTotalBancas));
        resumo.setTotalProducoesTecnicas(somar(pesquisadores, ResumoPesquisadorDTO::getTotalProducoesTecnicas));
        resumo.setTotalTrabalhosEventos(somar(pesquisadores, ResumoPesquisadorDTO::getTotalTrabalhosEventos));
        return resumo;
    }

    public List<SerieGrupoDTO> buscarSeriesGrupoAdhoc(List<String> idsLattes) {
        Map<Integer, SerieGrupoDTO> seriesPorAno = new TreeMap<>();

        idsLattes.stream()
                .distinct()
                .map(this::carregarOuSincronizar)
                .forEach(pesquisador -> agregarSeriesPesquisador(pesquisador, seriesPorAno));

        return List.copyOf(seriesPorAno.values());
    }

    public List<RankingPesquisadorDTO> buscarRankingGrupoAdhoc(List<String> idsLattes, String metrica) {
        String metricaNormalizada = normalizarMetricaRanking(metrica);

        return idsLattes.stream()
                .distinct()
                .map(this::carregarOuSincronizar)
                .map(this::toResumoDTO)
                .map(resumo -> new RankingPesquisadorDTO(
                        resumo.getIdLattes(),
                        resumo.getNomeCompleto(),
                        metricaNormalizada,
                        calcularQuantidadeRanking(resumo, metricaNormalizada)
                ))
                .sorted(Comparator
                        .comparingLong(RankingPesquisadorDTO::getQuantidade)
                        .reversed()
                        .thenComparing(RankingPesquisadorDTO::getNomeCompleto, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    private Pesquisador carregarOuSincronizar(String idLattes) {
        Optional<Pesquisador> pesquisadorExistente = pesquisadorRepository.findById(idLattes);
        return pesquisadorExistente.orElseGet(() -> scraperService.sincronizarPesquisador(idLattes));
    }

    private ResumoPesquisadorDTO toResumoDTO(Pesquisador pesquisador) {
        ResumoPesquisadorDTO resumo = new ResumoPesquisadorDTO();
        resumo.setIdLattes(pesquisador.getIdLattes());
        resumo.setNomeCompleto(pesquisador.getNomeCompleto());
        resumo.setDataUltimaAtualizacao(pesquisador.getDataUltimaAtualizacao());
        resumo.setDataSincronizacao(pesquisador.getDataSincronizacao());
        resumo.setTotalArtigos(pesquisador.getArtigos().size());
        resumo.setTotalOrientacoes(pesquisador.getOrientacoes().size());
        resumo.setTotalOrientacoesConcluidas(contarOrientacoesPorStatus(pesquisador, STATUS_CONCLUIDA));
        resumo.setTotalOrientacoesEmAndamento(contarOrientacoesPorStatus(pesquisador, STATUS_EM_ANDAMENTO));
        resumo.setTotalProjetos(pesquisador.getProjetos().size());
        resumo.setTotalBancas(pesquisador.getBancas().size());
        resumo.setTotalProducoesTecnicas(pesquisador.getProducoesTecnicas().size());
        resumo.setTotalTrabalhosEventos(pesquisador.getTrabalhosEventos().size());
        return resumo;
    }

    public List<SerieTemporalDTO> buscarSeriesTemporais(String idLattes, String tipo) {
        Pesquisador pesquisador = buscarPorId(idLattes);

        Map<Integer, Long> contagemPorAno;

        switch (tipo.toLowerCase()) {
            case "artigos":
                contagemPorAno = pesquisador.getArtigos().stream()
                        .filter(a -> a.getAno() != null)
                        .collect(Collectors.groupingBy(Artigo::getAno, Collectors.counting()));
                break;
            case "orientacoes":
                contagemPorAno = pesquisador.getOrientacoes().stream()
                        .filter(o -> o.getAno() != null)
                        .collect(Collectors.groupingBy(Orientacao::getAno, Collectors.counting()));
                break;
            case "projetos":
                contagemPorAno = pesquisador.getProjetos().stream()
                        .filter(p -> p.getAno() != null)
                        .collect(Collectors.groupingBy(Projeto::getAno, Collectors.counting()));
                break;
            case "bancas":
                contagemPorAno = pesquisador.getBancas().stream()
                        .filter(b -> b.getAno() != null)
                        .collect(Collectors.groupingBy(Banca::getAno, Collectors.counting()));
                break;
            case "producoes_tecnicas":
                contagemPorAno = pesquisador.getProducoesTecnicas().stream()
                        .filter(pt -> pt.getAno() != null)
                        .collect(Collectors.groupingBy(ProducaoTecnica::getAno, Collectors.counting()));
                break;
            case "trabalhos_eventos":
                contagemPorAno = pesquisador.getTrabalhosEventos().stream()
                        .filter(te -> te.getAno() != null)
                        .collect(Collectors.groupingBy(TrabalhoEvento::getAno, Collectors.counting()));
                break;
            default:
                contagemPorAno = Collections.emptyMap();
        }

        // Usamos TreeMap para garantir a ordenação por ano (chave)
        return new TreeMap<>(contagemPorAno).entrySet().stream()
                .map(entry -> new SerieTemporalDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private Pesquisador buscarPorId(String idLattes) {
        return pesquisadorRepository.findById(idLattes)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pesquisador nao encontrado para o ID informado."
                ));
    }

    private long contarOrientacoesPorStatus(Pesquisador pesquisador, String status) {
        return pesquisador.getOrientacoes().stream()
                .map(Orientacao::getStatus)
                .filter(status::equals)
                .count();
    }

    private long somar(List<ResumoPesquisadorDTO> pesquisadores, java.util.function.ToLongFunction<ResumoPesquisadorDTO> campo) {
        return pesquisadores.stream()
                .mapToLong(campo)
                .sum();
    }

    private String normalizarMetricaRanking(String metrica) {
        if (metrica == null || metrica.isBlank()) {
            return "total";
        }

        String metricaNormalizada = metrica.trim().toLowerCase(Locale.ROOT);
        return switch (metricaNormalizada) {
            case "total", "artigos", "orientacoes", "projetos", "bancas", "producoes_tecnicas", "trabalhos_eventos" -> metricaNormalizada;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metrica de ranking invalida.");
        };
    }

    private long calcularQuantidadeRanking(ResumoPesquisadorDTO resumo, String metrica) {
        return switch (metrica) {
            case "artigos" -> resumo.getTotalArtigos();
            case "orientacoes" -> resumo.getTotalOrientacoes();
            case "projetos" -> resumo.getTotalProjetos();
            case "bancas" -> resumo.getTotalBancas();
            case "producoes_tecnicas" -> resumo.getTotalProducoesTecnicas();
            case "trabalhos_eventos" -> resumo.getTotalTrabalhosEventos();
            case "total" -> resumo.getTotalArtigos()
                    + resumo.getTotalOrientacoes()
                    + resumo.getTotalProjetos()
                    + resumo.getTotalBancas()
                    + resumo.getTotalProducoesTecnicas()
                    + resumo.getTotalTrabalhosEventos();
            default -> 0;
        };
    }

    private void agregarSeriesPesquisador(Pesquisador pesquisador, Map<Integer, SerieGrupoDTO> seriesPorAno) {
        pesquisador.getArtigos().stream()
                .map(Artigo::getAno)
                .filter(java.util.Objects::nonNull)
                .forEach(ano -> obterSerieAno(seriesPorAno, ano).adicionarArtigo());

        pesquisador.getTrabalhosEventos().stream()
                .map(TrabalhoEvento::getAno)
                .filter(java.util.Objects::nonNull)
                .forEach(ano -> obterSerieAno(seriesPorAno, ano).adicionarTrabalhoEvento());

        pesquisador.getOrientacoes().stream()
                .map(Orientacao::getAno)
                .filter(java.util.Objects::nonNull)
                .forEach(ano -> obterSerieAno(seriesPorAno, ano).adicionarOrientacao());

        pesquisador.getProjetos().stream()
                .map(Projeto::getAno)
                .filter(java.util.Objects::nonNull)
                .forEach(ano -> obterSerieAno(seriesPorAno, ano).adicionarProjeto());

        pesquisador.getBancas().stream()
                .map(Banca::getAno)
                .filter(java.util.Objects::nonNull)
                .forEach(ano -> obterSerieAno(seriesPorAno, ano).adicionarBanca());

        pesquisador.getProducoesTecnicas().stream()
                .map(ProducaoTecnica::getAno)
                .filter(java.util.Objects::nonNull)
                .forEach(ano -> obterSerieAno(seriesPorAno, ano).adicionarProducaoTecnica());
    }

    private SerieGrupoDTO obterSerieAno(Map<Integer, SerieGrupoDTO> seriesPorAno, Integer ano) {
        return seriesPorAno.computeIfAbsent(ano, SerieGrupoDTO::new);
    }
}
