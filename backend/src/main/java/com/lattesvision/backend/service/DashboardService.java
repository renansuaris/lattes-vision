package com.lattesvision.backend.service;

import com.lattesvision.backend.dto.CategoriaQuantidadeDTO;
import com.lattesvision.backend.dto.ArticleDetailDTO;
import com.lattesvision.backend.dto.ArticlePageDTO;
import com.lattesvision.backend.dto.BancaDetailDTO;
import com.lattesvision.backend.dto.BancaPageDTO;
import com.lattesvision.backend.dto.EventWorkDetailDTO;
import com.lattesvision.backend.dto.EventWorkPageDTO;
import com.lattesvision.backend.dto.BookChapterDetailDTO;
import com.lattesvision.backend.dto.BookChapterPageDTO;
import com.lattesvision.backend.repository.CapituloLivroRepository;
import com.lattesvision.backend.dto.BookDetailDTO;
import com.lattesvision.backend.dto.BookPageDTO;
import com.lattesvision.backend.repository.LivroRepository;
import com.lattesvision.backend.dto.ProjectDetailDTO;
import com.lattesvision.backend.dto.ProjectPageDTO;
import com.lattesvision.backend.dto.TechnicalProductionDetailDTO;
import com.lattesvision.backend.dto.TechnicalProductionPageDTO;
import com.lattesvision.backend.dto.OrientationDetailDTO;
import com.lattesvision.backend.dto.OrientationPageDTO;
import com.lattesvision.backend.dto.ResumoPesquisadorDTO;
import com.lattesvision.backend.dto.CoberturaQualisDTO;
import com.lattesvision.backend.dto.EstratoQuantidadeDTO;
import com.lattesvision.backend.dto.ResumoQualisDTO;
import com.lattesvision.backend.dto.RankingPesquisadorDTO;
import com.lattesvision.backend.dto.ResumoGrupoDTO;
import com.lattesvision.backend.dto.SerieTemporalDTO;
import com.lattesvision.backend.dto.SerieGrupoDTO;
import com.lattesvision.backend.dto.SerieOrientacaoStatusDTO;
import com.lattesvision.backend.dto.SerieQualisAnoDTO;
import com.lattesvision.backend.dto.YearRange;
import com.lattesvision.backend.model.*;
import com.lattesvision.backend.repository.ArtigoRepository;
import com.lattesvision.backend.repository.BancaRepository;
import com.lattesvision.backend.repository.PesquisadorRepository;
import com.lattesvision.backend.repository.OrientacaoRepository;
import com.lattesvision.backend.repository.TrabalhoEventoRepository;
import com.lattesvision.backend.repository.ProjetoRepository;
import com.lattesvision.backend.repository.ProducaoTecnicaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
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
    private static final YearRange ALL_YEARS = new YearRange(null, null);

    private final PesquisadorRepository pesquisadorRepository;
    private final ArtigoRepository artigoRepository;
    private final BancaRepository bancaRepository;
    private final OrientacaoRepository orientacaoRepository;
    private final TrabalhoEventoRepository trabalhoEventoRepository;
    private final ProjetoRepository projetoRepository;
    private final ProducaoTecnicaRepository producaoTecnicaRepository;
    private final LivroRepository bookRepository;
    private final CapituloLivroRepository bookChapterRepository;
    private final ScraperService scraperService;

    public DashboardService(
            PesquisadorRepository pesquisadorRepository,
            ArtigoRepository artigoRepository,
            BancaRepository bancaRepository,
            OrientacaoRepository orientacaoRepository,
            TrabalhoEventoRepository trabalhoEventoRepository,
            ProjetoRepository projetoRepository,
            ProducaoTecnicaRepository producaoTecnicaRepository,
            LivroRepository bookRepository,
            CapituloLivroRepository bookChapterRepository,
            ScraperService scraperService
    ) {
        this.bookRepository = bookRepository;
        this.bookChapterRepository = bookChapterRepository;
        this.pesquisadorRepository = pesquisadorRepository;
        this.artigoRepository = artigoRepository;
        this.bancaRepository = bancaRepository;
        this.orientacaoRepository = orientacaoRepository;
        this.trabalhoEventoRepository = trabalhoEventoRepository;
        this.projetoRepository = projetoRepository;
        this.producaoTecnicaRepository = producaoTecnicaRepository;
        this.scraperService = scraperService;
    }

    public ResumoPesquisadorDTO buscarResumoPesquisador(String idLattes, YearRange yearRange) {
        return toResumoDTO(buscarPorId(idLattes), yearRange);
    }

    public ResumoPesquisadorDTO buscarResumoPesquisador(String idLattes) {
        return buscarResumoPesquisador(idLattes, ALL_YEARS);
    }

    public ResumoPesquisadorDTO carregarResumoPesquisador(String idLattes, YearRange yearRange) {
        return toResumoDTO(carregarOuSincronizar(idLattes), yearRange);
    }

    public ResumoPesquisadorDTO carregarResumoPesquisador(String idLattes) {
        return carregarResumoPesquisador(idLattes, ALL_YEARS);
    }

    public ResumoGrupoDTO carregarResumoGrupoAdhoc(List<String> idsLattes, YearRange yearRange) {
        List<ResumoPesquisadorDTO> pesquisadores = idsLattes.stream()
                .distinct()
                .map(this::carregarOuSincronizar)
                .map(pesquisador -> toResumoDTO(pesquisador, yearRange))
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
        resumo.setTotalLivros(somar(pesquisadores, ResumoPesquisadorDTO::getTotalLivros));
        resumo.setTotalCapitulos(somar(pesquisadores, ResumoPesquisadorDTO::getTotalCapitulos));
        resumo.setTotalProducaoBibliografica(somar(pesquisadores, ResumoPesquisadorDTO::getTotalProducaoBibliografica));
        return resumo;
    }

    public ResumoGrupoDTO carregarResumoGrupoAdhoc(List<String> idsLattes) {
        return carregarResumoGrupoAdhoc(idsLattes, ALL_YEARS);
    }

    public List<SerieGrupoDTO> buscarSeriesGrupoAdhoc(List<String> idsLattes, YearRange yearRange) {
        Map<Integer, SerieGrupoDTO> seriesPorAno = new TreeMap<>();

        idsLattes.stream()
                .distinct()
                .map(this::carregarOuSincronizar)
                .forEach(pesquisador -> agregarSeriesPesquisador(pesquisador, seriesPorAno, yearRange));

        return List.copyOf(seriesPorAno.values());
    }

    public List<SerieGrupoDTO> buscarSeriesGrupoAdhoc(List<String> idsLattes) {
        return buscarSeriesGrupoAdhoc(idsLattes, ALL_YEARS);
    }

    public List<RankingPesquisadorDTO> buscarRankingGrupoAdhoc(List<String> idsLattes, String metrica, YearRange yearRange) {
        String metricaNormalizada = normalizarMetricaRanking(metrica);

        return idsLattes.stream()
                .distinct()
                .map(this::carregarOuSincronizar)
                .map(pesquisador -> toResumoDTO(pesquisador, yearRange))
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

    public List<RankingPesquisadorDTO> buscarRankingGrupoAdhoc(List<String> idsLattes, String metrica) {
        return buscarRankingGrupoAdhoc(idsLattes, metrica, ALL_YEARS);
    }

    public ResumoQualisDTO buscarResumoQualisGrupoAdhoc(List<String> idsLattes, YearRange yearRange) {
        List<Pesquisador> pesquisadores = idsLattes.stream()
                .distinct()
                .map(this::carregarOuSincronizar)
                .toList();
        return montarResumoQualis(pesquisadores, yearRange);
    }

    public ResumoQualisDTO buscarResumoQualisGrupoAdhoc(List<String> idsLattes) {
        return buscarResumoQualisGrupoAdhoc(idsLattes, ALL_YEARS);
    }

    private Pesquisador carregarOuSincronizar(String idLattes) {
        Optional<Pesquisador> pesquisadorExistente = pesquisadorRepository.findById(idLattes);
        return pesquisadorExistente.orElseGet(() -> scraperService.sincronizarPesquisador(idLattes));
    }

    private ResumoPesquisadorDTO toResumoDTO(Pesquisador pesquisador, YearRange yearRange) {
        ResumoPesquisadorDTO resumo = new ResumoPesquisadorDTO();
        resumo.setIdLattes(pesquisador.getIdLattes());
        resumo.setNomeCompleto(pesquisador.getNomeCompleto());
        resumo.setDataUltimaAtualizacao(pesquisador.getDataUltimaAtualizacao());
        resumo.setDataSincronizacao(pesquisador.getDataSincronizacao());
        resumo.setTotalArtigos(contarNoPeriodo(pesquisador.getArtigos().stream().map(Artigo::getAno), yearRange));
        resumo.setTotalOrientacoes(contarNoPeriodo(pesquisador.getOrientacoes().stream().map(Orientacao::getAno), yearRange));
        resumo.setTotalOrientacoesConcluidas(contarOrientacoesPorStatus(pesquisador, STATUS_CONCLUIDA, yearRange));
        resumo.setTotalOrientacoesEmAndamento(contarOrientacoesPorStatus(pesquisador, STATUS_EM_ANDAMENTO, yearRange));
        resumo.setTotalProjetos(contarNoPeriodo(pesquisador.getProjetos().stream().map(Projeto::getAno), yearRange));
        resumo.setTotalBancas(contarNoPeriodo(pesquisador.getBancas().stream().map(Banca::getAno), yearRange));
        resumo.setTotalProducoesTecnicas(contarNoPeriodo(pesquisador.getProducoesTecnicas().stream().map(ProducaoTecnica::getAno), yearRange));
        resumo.setTotalTrabalhosEventos(contarNoPeriodo(pesquisador.getTrabalhosEventos().stream().map(TrabalhoEvento::getAno), yearRange));
        resumo.setTotalLivros(contarNoPeriodo(pesquisador.getLivros().stream().map(Livro::getAno), yearRange));
        resumo.setTotalCapitulos(contarNoPeriodo(pesquisador.getCapitulosLivros().stream().map(CapituloLivro::getAno), yearRange));
        resumo.setTotalProducaoBibliografica(
                resumo.getTotalArtigos()
                        + resumo.getTotalTrabalhosEventos()
                        + resumo.getTotalLivros()
                        + resumo.getTotalCapitulos()
        );
        return resumo;
    }

    public List<SerieTemporalDTO> buscarSeriesTemporais(String idLattes, String tipo, YearRange yearRange) {
        Pesquisador pesquisador = buscarPorId(idLattes);

        Map<Integer, Long> contagemPorAno;

        switch (tipo.toLowerCase()) {
            case "artigos":
                contagemPorAno = pesquisador.getArtigos().stream()
                        .filter(a -> a.getAno() != null && yearRange.includes(a.getAno()))
                        .collect(Collectors.groupingBy(Artigo::getAno, Collectors.counting()));
                break;
            case "orientacoes":
                contagemPorAno = pesquisador.getOrientacoes().stream()
                        .filter(o -> o.getAno() != null && yearRange.includes(o.getAno()))
                        .collect(Collectors.groupingBy(Orientacao::getAno, Collectors.counting()));
                break;
            case "projetos":
                contagemPorAno = pesquisador.getProjetos().stream()
                        .filter(p -> p.getAno() != null && yearRange.includes(p.getAno()))
                        .collect(Collectors.groupingBy(Projeto::getAno, Collectors.counting()));
                break;
            case "bancas":
                contagemPorAno = pesquisador.getBancas().stream()
                        .filter(b -> b.getAno() != null && yearRange.includes(b.getAno()))
                        .collect(Collectors.groupingBy(Banca::getAno, Collectors.counting()));
                break;
            case "producoes_tecnicas":
                contagemPorAno = pesquisador.getProducoesTecnicas().stream()
                        .filter(pt -> pt.getAno() != null && yearRange.includes(pt.getAno()))
                        .collect(Collectors.groupingBy(ProducaoTecnica::getAno, Collectors.counting()));
                break;
            case "trabalhos_eventos":
                contagemPorAno = pesquisador.getTrabalhosEventos().stream()
                        .filter(te -> te.getAno() != null && yearRange.includes(te.getAno()))
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

    public List<SerieTemporalDTO> buscarSeriesTemporais(String idLattes, String tipo) {
        return buscarSeriesTemporais(idLattes, tipo, ALL_YEARS);
    }

    public ArticlePageDTO buscarArtigos(
            String idLattes,
            Integer ano,
            YearRange yearRange,
            EstratoQualis estrato,
            StatusClassificacaoQualis statusQualis,
            int page,
            int size
    ) {
        buscarPorId(idLattes);

        Sort sort = Sort.by(
                Sort.Order.desc("ano").nullsLast(),
                Sort.Order.asc("titulo").nullsLast(),
                Sort.Order.asc("id")
        );
        Page<Artigo> resultado = artigoRepository.findForDashboard(
                idLattes,
                ano,
                yearRange.startYear(),
                yearRange.endYear(),
                estrato,
                statusQualis,
                PageRequest.of(page, size, sort)
        );

        List<ArticleDetailDTO> items = resultado.getContent().stream()
                .map(this::toArticleDetailDTO)
                .toList();
        return new ArticlePageDTO(
                items,
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalElements(),
                resultado.getTotalPages()
        );
    }

    public ArticlePageDTO buscarArtigos(
            String idLattes,
            Integer ano,
            EstratoQualis estrato,
            StatusClassificacaoQualis statusQualis,
            int page,
            int size
    ) {
        return buscarArtigos(idLattes, ano, ALL_YEARS, estrato, statusQualis, page, size);
    }

    public OrientationPageDTO buscarOrientacoes(
            String idLattes,
            Integer ano,
            YearRange yearRange,
            String status,
            String tipo,
            int page,
            int size
    ) {
        buscarPorId(idLattes);

        Sort sort = Sort.by(
                Sort.Order.desc("ano"),
                Sort.Order.asc("status"),
                Sort.Order.asc("nomeOrientado"),
                Sort.Order.asc("id")
        );
        Specification<Orientacao> filtros = (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("pesquisador").get("idLattes"), idLattes));

            if (ano != null) {
                predicates.add(criteriaBuilder.equal(root.get("ano"), ano));
            }
            if (yearRange.startYear() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ano"), yearRange.startYear()));
            }
            if (yearRange.endYear() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("ano"), yearRange.endYear()));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (tipo != null && !tipo.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("tipo")),
                        tipo.trim().toLowerCase(Locale.ROOT)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Page<Orientacao> resultado = orientacaoRepository.findAll(filtros, PageRequest.of(page, size, sort));

        List<OrientationDetailDTO> items = resultado.getContent().stream()
                .map(this::toOrientationDetailDTO)
                .toList();
        return new OrientationPageDTO(
                items,
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalElements(),
                resultado.getTotalPages()
        );
    }

    public OrientationPageDTO buscarOrientacoes(
            String idLattes,
            Integer ano,
            String status,
            String tipo,
            int page,
            int size
    ) {
        return buscarOrientacoes(idLattes, ano, ALL_YEARS, status, tipo, page, size);
    }

    public BancaPageDTO buscarBancas(
            String idLattes,
            YearRange yearRange,
            String tipo,
            int page,
            int size
    ) {
        buscarPorId(idLattes);

        Sort sort = Sort.by(
                Sort.Order.desc("ano").nullsLast(),
                Sort.Order.asc("tipo").nullsLast(),
                Sort.Order.asc("nomeCandidato").nullsLast(),
                Sort.Order.asc("tituloTrabalho").nullsLast(),
                Sort.Order.asc("id")
        );
        Page<Banca> resultado = bancaRepository.findForDashboard(
                idLattes,
                yearRange.startYear(),
                yearRange.endYear(),
                normalizarTipoBancaFiltro(tipo),
                PageRequest.of(page, size, sort)
        );

        List<BancaDetailDTO> items = resultado.getContent().stream()
                .map(this::toBancaDetailDTO)
                .toList();
        return new BancaPageDTO(
                items,
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalElements(),
                resultado.getTotalPages()
        );
    }

    public EventWorkPageDTO buscarTrabalhosEventos(
            String idLattes,
            Integer ano,
            YearRange yearRange,
            int page,
            int size
    ) {
        buscarPorId(idLattes);

        Sort sort = Sort.by(
                Sort.Order.desc("ano").nullsLast(),
                Sort.Order.asc("titulo").nullsLast(),
                Sort.Order.asc("id")
        );
        Page<TrabalhoEvento> resultado = trabalhoEventoRepository.findForDashboard(
                idLattes,
                ano,
                yearRange.startYear(),
                yearRange.endYear(),
                PageRequest.of(page, size, sort)
        );

        List<EventWorkDetailDTO> items = resultado.getContent().stream()
                .map(this::toEventWorkDetailDTO)
                .toList();
        return new EventWorkPageDTO(
                items,
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalElements(),
                resultado.getTotalPages()
        );
    }

    public BookPageDTO buscarLivros(String idLattes, Integer ano, YearRange yearRange, int page, int size) {
        buscarPorId(idLattes);
        Sort sort = Sort.by(Sort.Order.desc("ano").nullsLast(),
                Sort.Order.asc("titulo").nullsLast(), Sort.Order.asc("id"));
        Page<Livro> result = bookRepository.findForDashboard(
                idLattes, ano, yearRange.startYear(), yearRange.endYear(), PageRequest.of(page, size, sort));
        List<BookDetailDTO> items = result.getContent().stream()
                .map(item -> new BookDetailDTO(item.getId(), item.getAno(), item.getTitulo(), item.getAutores(), item.getEditora(), item.getIsbn(), item.getEdicao(), item.getVolume(), item.getPaginas(), item.getDescricaoBruta())).toList();
        return new BookPageDTO(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public BookChapterPageDTO buscarCapitulos(String idLattes, Integer ano, YearRange yearRange, int page, int size) {
        buscarPorId(idLattes);
        Sort sort = Sort.by(Sort.Order.desc("ano").nullsLast(),
                Sort.Order.asc("titulo").nullsLast(), Sort.Order.asc("id"));
        Page<CapituloLivro> result = bookChapterRepository.findForDashboard(
                idLattes, ano, yearRange.startYear(), yearRange.endYear(), PageRequest.of(page, size, sort));
        List<BookChapterDetailDTO> items = result.getContent().stream()
                .map(item -> new BookChapterDetailDTO(item.getId(), item.getAno(), item.getTitulo(), item.getAutores(), item.getEditora(), item.getIsbn(), item.getEdicao(), item.getVolume(), item.getPaginas(), item.getDescricaoBruta(), item.getTituloLivro(), item.getOrganizadores())).toList();
        return new BookChapterPageDTO(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public ProjectPageDTO buscarProjetos(String idLattes, Integer ano, YearRange yearRange, int page, int size) {
        buscarPorId(idLattes);

        Sort sort = Sort.by(
                Sort.Order.desc("ano").nullsLast(),
                Sort.Order.asc("titulo").nullsLast(),
                Sort.Order.asc("id")
        );
        Page<Projeto> resultado = projetoRepository.findForDashboard(
                idLattes, ano, yearRange.startYear(), yearRange.endYear(), PageRequest.of(page, size, sort)
        );
        List<ProjectDetailDTO> items = resultado.getContent().stream()
                .map(projeto -> new ProjectDetailDTO(
                        projeto.getId(), projeto.getAno(), projeto.getTipo(), projeto.getTitulo()
                ))
                .toList();
        return new ProjectPageDTO(items, resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages());
    }

    public TechnicalProductionPageDTO buscarProducoesTecnicas(
            String idLattes, Integer ano, YearRange yearRange, int page, int size
    ) {
        buscarPorId(idLattes);

        Sort sort = Sort.by(
                Sort.Order.desc("ano").nullsLast(),
                Sort.Order.asc("titulo").nullsLast(),
                Sort.Order.asc("id")
        );
        Page<ProducaoTecnica> resultado = producaoTecnicaRepository.findForDashboard(
                idLattes, ano, yearRange.startYear(), yearRange.endYear(), PageRequest.of(page, size, sort)
        );
        List<TechnicalProductionDetailDTO> items = resultado.getContent().stream()
                .map(producao -> new TechnicalProductionDetailDTO(
                        producao.getId(), producao.getAno(), producao.getCategoria(), producao.getTitulo(),
                        producao.getAutoresTexto(), producao.getNatureza(), producao.getDescricaoBruta()
                ))
                .toList();
        return new TechnicalProductionPageDTO(items, resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages());
    }

    public List<CategoriaQuantidadeDTO> buscarBancasPorTipo(String idLattes, YearRange yearRange) {
        Pesquisador pesquisador = buscarPorId(idLattes);

        return pesquisador.getBancas().stream()
                .filter(banca -> yearRange.includes(banca.getAno()))
                .collect(Collectors.groupingBy(
                        banca -> normalizarCategoria(banca.getTipo()),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> new CategoriaQuantidadeDTO(entry.getKey(), entry.getValue()))
                .sorted(Comparator
                        .comparingLong(CategoriaQuantidadeDTO::getQuantidade)
                        .reversed()
                        .thenComparing(CategoriaQuantidadeDTO::getCategoria, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<CategoriaQuantidadeDTO> buscarBancasPorTipo(String idLattes) {
        return buscarBancasPorTipo(idLattes, ALL_YEARS);
    }

    public List<SerieOrientacaoStatusDTO> buscarOrientacoesPorStatusAno(String idLattes, YearRange yearRange) {
        Pesquisador pesquisador = buscarPorId(idLattes);
        Map<Integer, SerieOrientacaoStatusDTO> seriesPorAno = new TreeMap<>();

        pesquisador.getOrientacoes().stream()
                .filter(orientacao -> orientacao.getAno() != null && yearRange.includes(orientacao.getAno()))
                .forEach(orientacao -> {
                    SerieOrientacaoStatusDTO serie = seriesPorAno.computeIfAbsent(
                            orientacao.getAno(),
                            SerieOrientacaoStatusDTO::new
                    );

                    if (STATUS_CONCLUIDA.equals(orientacao.getStatus())) {
                        serie.adicionarConcluida();
                    } else if (STATUS_EM_ANDAMENTO.equals(orientacao.getStatus())) {
                        serie.adicionarEmAndamento();
                    }
                });

        return List.copyOf(seriesPorAno.values());
    }

    public List<SerieOrientacaoStatusDTO> buscarOrientacoesPorStatusAno(String idLattes) {
        return buscarOrientacoesPorStatusAno(idLattes, ALL_YEARS);
    }

    public ResumoQualisDTO buscarResumoQualis(String idLattes, YearRange yearRange) {
        return montarResumoQualis(List.of(buscarPorId(idLattes)), yearRange);
    }

    public ResumoQualisDTO buscarResumoQualis(String idLattes) {
        return buscarResumoQualis(idLattes, ALL_YEARS);
    }

    private ResumoQualisDTO montarResumoQualis(List<Pesquisador> pesquisadores, YearRange yearRange) {
        Map<EstratoQualis, Long> distribuicao = new EnumMap<>(EstratoQualis.class);
        Map<Integer, SerieQualisAnoDTO> seriePorAno = new TreeMap<>();
        long classificados = 0;
        long semIssn = 0;
        long total = 0;

        for (EstratoQualis estrato : EstratoQualis.values()) {
            distribuicao.put(estrato, 0L);
        }

        for (Pesquisador pesquisador : pesquisadores) {
            for (Artigo artigo : pesquisador.getArtigos()) {
                if (!yearRange.includes(artigo.getAno())) {
                    continue;
                }
                total++;
                if (artigo.getStatusQualis() == StatusClassificacaoQualis.SEM_ISSN) {
                    semIssn++;
                }

                ClassificacaoQualis classificacao = artigo.getClassificacaoQualis();
                if (artigo.getStatusQualis() != StatusClassificacaoQualis.CLASSIFICADO
                        || classificacao == null
                        || classificacao.getEstrato() == null) {
                    continue;
                }

                EstratoQualis estrato = classificacao.getEstrato();
                classificados++;
                distribuicao.merge(estrato, 1L, Long::sum);

                if (artigo.getAno() != null) {
                    seriePorAno.computeIfAbsent(artigo.getAno(), SerieQualisAnoDTO::new)
                            .adicionar(estrato);
                }
            }
        }

        long naoClassificados = total - classificados - semIssn;
        List<EstratoQuantidadeDTO> distribuicaoOrdenada = java.util.Arrays.stream(EstratoQualis.values())
                .map(estrato -> new EstratoQuantidadeDTO(estrato, distribuicao.get(estrato)))
                .toList();

        return new ResumoQualisDTO(
                "2021-2024",
                "CAPES/SUCUPIRA",
                new CoberturaQualisDTO(total, classificados, semIssn, naoClassificados),
                distribuicaoOrdenada,
                List.copyOf(seriePorAno.values())
        );
    }

    private ArticleDetailDTO toArticleDetailDTO(Artigo artigo) {
        ClassificacaoQualis classificacao = artigo.getClassificacaoQualis();
        EstratoQualis estrato = classificacao != null ? classificacao.getEstrato() : null;
        String titulo = artigo.getTitulo();
        if (titulo == null || titulo.isBlank()) {
            titulo = artigo.getCitacaoCompleta();
        }

        return new ArticleDetailDTO(
                artigo.getId(),
                artigo.getAno(),
                titulo,
                artigo.getPeriodico(),
                artigo.getIssn(),
                estrato,
                artigo.getStatusQualis()
        );
    }

    private OrientationDetailDTO toOrientationDetailDTO(Orientacao orientacao) {
        return new OrientationDetailDTO(
                orientacao.getId(),
                orientacao.getAno(),
                orientacao.getNomeOrientado(),
                orientacao.getTituloTrabalho(),
                orientacao.getTipo(),
                orientacao.getStatus()
        );
    }

    private BancaDetailDTO toBancaDetailDTO(Banca banca) {
        return new BancaDetailDTO(
                banca.getId(),
                banca.getAno(),
                normalizarCategoria(banca.getTipo()),
                banca.getNomeCandidato(),
                banca.getTituloTrabalho()
        );
    }

    private EventWorkDetailDTO toEventWorkDetailDTO(TrabalhoEvento trabalho) {
        return new EventWorkDetailDTO(
                trabalho.getId(),
                trabalho.getAno(),
                trabalho.getTitulo(),
                trabalho.getNomeEvento(),
                trabalho.getAutores(),
                trabalho.getTipo()
        );
    }

    private Pesquisador buscarPorId(String idLattes) {
        return pesquisadorRepository.findById(idLattes)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pesquisador nao encontrado para o ID informado."
                ));
    }

    private long contarOrientacoesPorStatus(Pesquisador pesquisador, String status, YearRange yearRange) {
        return pesquisador.getOrientacoes().stream()
                .filter(orientacao -> yearRange.includes(orientacao.getAno()))
                .map(Orientacao::getStatus)
                .filter(status::equals)
                .count();
    }

    private long contarNoPeriodo(java.util.stream.Stream<Integer> anos, YearRange yearRange) {
        return anos.filter(yearRange::includes).count();
    }

    private String normalizarCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return "Outra";
        }

        String categoriaNormalizada = categoria.trim();
        if ("TCC".equalsIgnoreCase(categoriaNormalizada)) {
            return "Graduacao";
        }

        return categoriaNormalizada;
    }

    private String normalizarTipoBancaFiltro(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return null;
        }

        String tipoNormalizado = java.text.Normalizer.normalize(tipo.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        if ("graduacao".equalsIgnoreCase(tipoNormalizado)) {
            return "Graduacao";
        }
        if ("outra".equalsIgnoreCase(tipoNormalizado)) {
            return "Outra";
        }
        return tipo.trim();
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
                    + resumo.getTotalTrabalhosEventos()
                    + resumo.getTotalLivros()
                    + resumo.getTotalCapitulos();
            default -> 0;
        };
    }

    private void agregarSeriesPesquisador(Pesquisador pesquisador, Map<Integer, SerieGrupoDTO> seriesPorAno, YearRange yearRange) {
        pesquisador.getArtigos().stream()
                .map(Artigo::getAno)
                .filter(java.util.Objects::nonNull)
                .filter(yearRange::includes)
                .forEach(ano -> obterSerieAno(seriesPorAno, ano).adicionarArtigo());

        pesquisador.getTrabalhosEventos().stream()
                .map(TrabalhoEvento::getAno)
                .filter(java.util.Objects::nonNull)
                .filter(yearRange::includes)
                .forEach(ano -> obterSerieAno(seriesPorAno, ano).adicionarTrabalhoEvento());

        pesquisador.getOrientacoes().stream()
                .map(Orientacao::getAno)
                .filter(java.util.Objects::nonNull)
                .filter(yearRange::includes)
                .forEach(ano -> obterSerieAno(seriesPorAno, ano).adicionarOrientacao());

        pesquisador.getProjetos().stream()
                .map(Projeto::getAno)
                .filter(java.util.Objects::nonNull)
                .filter(yearRange::includes)
                .forEach(ano -> obterSerieAno(seriesPorAno, ano).adicionarProjeto());

        pesquisador.getBancas().stream()
                .map(Banca::getAno)
                .filter(java.util.Objects::nonNull)
                .filter(yearRange::includes)
                .forEach(ano -> obterSerieAno(seriesPorAno, ano).adicionarBanca());

        pesquisador.getProducoesTecnicas().stream()
                .map(ProducaoTecnica::getAno)
                .filter(java.util.Objects::nonNull)
                .filter(yearRange::includes)
                .forEach(ano -> obterSerieAno(seriesPorAno, ano).adicionarProducaoTecnica());
    }

    private SerieGrupoDTO obterSerieAno(Map<Integer, SerieGrupoDTO> seriesPorAno, Integer ano) {
        return seriesPorAno.computeIfAbsent(ano, SerieGrupoDTO::new);
    }
}
