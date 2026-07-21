package com.lattesvision.backend.service;

import com.lattesvision.backend.dto.CategoriaQuantidadeDTO;
import com.lattesvision.backend.dto.ResumoPesquisadorDTO;
import com.lattesvision.backend.dto.ResumoQualisDTO;
import com.lattesvision.backend.dto.RankingPesquisadorDTO;
import com.lattesvision.backend.dto.ResumoGrupoDTO;
import com.lattesvision.backend.dto.SerieGrupoDTO;
import com.lattesvision.backend.dto.SerieOrientacaoStatusDTO;
import com.lattesvision.backend.model.Artigo;
import com.lattesvision.backend.model.Banca;
import com.lattesvision.backend.model.ClassificacaoQualis;
import com.lattesvision.backend.model.EstratoQualis;
import com.lattesvision.backend.model.Orientacao;
import com.lattesvision.backend.model.Pesquisador;
import com.lattesvision.backend.model.TrabalhoEvento;
import com.lattesvision.backend.model.StatusClassificacaoQualis;
import com.lattesvision.backend.repository.PesquisadorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private PesquisadorRepository pesquisadorRepository;

    @Mock
    private ScraperService scraperService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void deveRetornarResumoSemSincronizarQuandoPesquisadorJaExiste() {
        Pesquisador pesquisador = criarPesquisador("7364915463901013", "Pesquisador Local");
        when(pesquisadorRepository.findById("7364915463901013")).thenReturn(Optional.of(pesquisador));

        ResumoPesquisadorDTO resumo = dashboardService.carregarResumoPesquisador("7364915463901013");

        assertThat(resumo.getIdLattes()).isEqualTo("7364915463901013");
        assertThat(resumo.getNomeCompleto()).isEqualTo("Pesquisador Local");
        assertThat(resumo.getTotalArtigos()).isEqualTo(1);
        verify(scraperService, never()).sincronizarPesquisador("7364915463901013");
    }

    @Test
    void deveSincronizarQuandoPesquisadorNaoExisteLocalmente() {
        Pesquisador pesquisador = criarPesquisador("7364915463901013", "Pesquisador Sincronizado");
        when(pesquisadorRepository.findById("7364915463901013")).thenReturn(Optional.empty());
        when(scraperService.sincronizarPesquisador("7364915463901013")).thenReturn(pesquisador);

        ResumoPesquisadorDTO resumo = dashboardService.carregarResumoPesquisador("7364915463901013");

        assertThat(resumo.getIdLattes()).isEqualTo("7364915463901013");
        assertThat(resumo.getNomeCompleto()).isEqualTo("Pesquisador Sincronizado");
        assertThat(resumo.getTotalArtigos()).isEqualTo(1);
        verify(scraperService).sincronizarPesquisador("7364915463901013");
    }

    @Test
    void deveCarregarGrupoSincronizandoApenasPesquisadoresAusentes() {
        Pesquisador pesquisadorLocal = criarPesquisador("7364915463901013", "Pesquisador Local");
        Pesquisador pesquisadorSincronizado = criarPesquisador("6255476538629622", "Pesquisador Sincronizado");
        when(pesquisadorRepository.findById("7364915463901013")).thenReturn(Optional.of(pesquisadorLocal));
        when(pesquisadorRepository.findById("6255476538629622")).thenReturn(Optional.empty());
        when(scraperService.sincronizarPesquisador("6255476538629622")).thenReturn(pesquisadorSincronizado);

        ResumoGrupoDTO resumo = dashboardService.carregarResumoGrupoAdhoc(List.of(
                "7364915463901013",
                "6255476538629622"
        ));

        assertThat(resumo.getTotalPesquisadores()).isEqualTo(2);
        assertThat(resumo.getTotalArtigos()).isEqualTo(2);
        assertThat(resumo.getPesquisadores())
                .extracting(ResumoPesquisadorDTO::getIdLattes)
                .containsExactly("7364915463901013", "6255476538629622");
        verify(scraperService, never()).sincronizarPesquisador("7364915463901013");
        verify(scraperService).sincronizarPesquisador("6255476538629622");
    }

    @Test
    void deveRetornarSeriesAgregadasDoGrupoPorAno() {
        Pesquisador pesquisadorLocal = criarPesquisador("7364915463901013", "Pesquisador Local");
        Pesquisador pesquisadorSincronizado = criarPesquisador("6255476538629622", "Pesquisador Sincronizado");
        TrabalhoEvento trabalhoEvento = new TrabalhoEvento();
        trabalhoEvento.setAno(2024);
        trabalhoEvento.setTitulo("Trabalho em evento de teste");
        pesquisadorSincronizado.addTrabalhoEvento(trabalhoEvento);
        when(pesquisadorRepository.findById("7364915463901013")).thenReturn(Optional.of(pesquisadorLocal));
        when(pesquisadorRepository.findById("6255476538629622")).thenReturn(Optional.empty());
        when(scraperService.sincronizarPesquisador("6255476538629622")).thenReturn(pesquisadorSincronizado);

        List<SerieGrupoDTO> series = dashboardService.buscarSeriesGrupoAdhoc(List.of(
                "7364915463901013",
                "6255476538629622"
        ));

        assertThat(series).hasSize(1);
        assertThat(series.get(0).getAno()).isEqualTo(2024);
        assertThat(series.get(0).getArtigos()).isEqualTo(2);
        assertThat(series.get(0).getTrabalhosEventos()).isEqualTo(1);
    }

    @Test
    void deveRetornarResumoQualisAgregadoDoGrupo() {
        Pesquisador primeiro = criarPesquisador("7364915463901013", "Pesquisador A");
        primeiro.getArtigos().clear();
        primeiro.addArtigo(criarArtigoQualis(2024, EstratoQualis.A1));

        Artigo semIssn = new Artigo();
        semIssn.setAno(2024);
        semIssn.setStatusQualis(StatusClassificacaoQualis.SEM_ISSN);
        primeiro.addArtigo(semIssn);

        Pesquisador segundo = criarPesquisador("6255476538629622", "Pesquisador B");
        segundo.getArtigos().clear();
        segundo.addArtigo(criarArtigoQualis(2023, EstratoQualis.B2));

        when(pesquisadorRepository.findById("7364915463901013")).thenReturn(Optional.of(primeiro));
        when(pesquisadorRepository.findById("6255476538629622")).thenReturn(Optional.of(segundo));

        ResumoQualisDTO resumo = dashboardService.buscarResumoQualisGrupoAdhoc(List.of(
                "7364915463901013",
                "6255476538629622",
                "7364915463901013"
        ));

        assertThat(resumo.getCobertura().getTotalArtigos()).isEqualTo(3);
        assertThat(resumo.getCobertura().getClassificados()).isEqualTo(2);
        assertThat(resumo.getCobertura().getSemIssn()).isEqualTo(1);
        assertThat(resumo.getCobertura().getPercentualClassificados()).isEqualTo(66.67);
        assertThat(resumo.getDistribuicao())
                .filteredOn(item -> item.getQuantidade() > 0)
                .extracting(item -> item.getEstrato(), item -> item.getQuantidade())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(EstratoQualis.A1, 1L),
                        org.assertj.core.groups.Tuple.tuple(EstratoQualis.B2, 1L)
                );
        assertThat(resumo.getSerieAnual())
                .extracting(item -> item.getAno())
                .containsExactly(2023, 2024);
        verify(scraperService, never()).sincronizarPesquisador("7364915463901013");
        verify(scraperService, never()).sincronizarPesquisador("6255476538629622");
    }

    @Test
    void deveRetornarRankingDoGrupoOrdenadoPorMetrica() {
        Pesquisador pesquisadorComMaisArtigos = criarPesquisador("7364915463901013", "Pesquisador A");
        Pesquisador pesquisadorComMenosArtigos = criarPesquisador("6255476538629622", "Pesquisador B");

        Artigo artigoExtra = new Artigo();
        artigoExtra.setAno(2025);
        artigoExtra.setCitacaoCompleta("Artigo extra de teste");
        pesquisadorComMaisArtigos.addArtigo(artigoExtra);

        when(pesquisadorRepository.findById("7364915463901013")).thenReturn(Optional.of(pesquisadorComMaisArtigos));
        when(pesquisadorRepository.findById("6255476538629622")).thenReturn(Optional.of(pesquisadorComMenosArtigos));

        List<RankingPesquisadorDTO> ranking = dashboardService.buscarRankingGrupoAdhoc(List.of(
                "6255476538629622",
                "7364915463901013"
        ), "artigos");

        assertThat(ranking).hasSize(2);
        assertThat(ranking)
                .extracting(RankingPesquisadorDTO::getIdLattes)
                .containsExactly("7364915463901013", "6255476538629622");
        assertThat(ranking)
                .extracting(RankingPesquisadorDTO::getQuantidade)
                .containsExactly(2L, 1L);
        assertThat(ranking)
                .extracting(RankingPesquisadorDTO::getMetrica)
                .containsOnly("artigos");
    }

    @Test
    void deveRetornarParticipacoesEmBancasAgrupadasPorTipo() {
        Pesquisador pesquisador = criarPesquisador("7364915463901013", "Pesquisador Local");
        pesquisador.addBanca(criarBanca("Mestrado"));
        pesquisador.addBanca(criarBanca("Doutorado"));
        pesquisador.addBanca(criarBanca("Mestrado"));
        pesquisador.addBanca(criarBanca("TCC"));
        pesquisador.addBanca(criarBanca(null));
        when(pesquisadorRepository.findById("7364915463901013")).thenReturn(Optional.of(pesquisador));

        List<CategoriaQuantidadeDTO> bancasPorTipo = dashboardService.buscarBancasPorTipo("7364915463901013");

        assertThat(bancasPorTipo)
                .extracting(CategoriaQuantidadeDTO::getCategoria)
                .containsExactly("Mestrado", "Doutorado", "Graduacao", "Outra");
        assertThat(bancasPorTipo)
                .extracting(CategoriaQuantidadeDTO::getQuantidade)
                .containsExactly(2L, 1L, 1L, 1L);
    }

    @Test
    void deveRetornarOrientacoesAgrupadasPorAnoEStatus() {
        Pesquisador pesquisador = criarPesquisador("7364915463901013", "Pesquisador Local");
        pesquisador.addOrientacao(criarOrientacao(2023, "CONCLUIDA"));
        pesquisador.addOrientacao(criarOrientacao(2024, "CONCLUIDA"));
        pesquisador.addOrientacao(criarOrientacao(2024, "EM_ANDAMENTO"));
        when(pesquisadorRepository.findById("7364915463901013")).thenReturn(Optional.of(pesquisador));

        List<SerieOrientacaoStatusDTO> series = dashboardService.buscarOrientacoesPorStatusAno("7364915463901013");

        assertThat(series).hasSize(2);
        assertThat(series)
                .extracting(SerieOrientacaoStatusDTO::getAno)
                .containsExactly(2023, 2024);
        assertThat(series)
                .extracting(SerieOrientacaoStatusDTO::getConcluidas)
                .containsExactly(1L, 1L);
        assertThat(series)
                .extracting(SerieOrientacaoStatusDTO::getEmAndamento)
                .containsExactly(0L, 1L);
    }

    @Test
    void deveRetornarCoberturaDistribuicaoESerieAnualQualis() {
        Pesquisador pesquisador = new Pesquisador();
        pesquisador.setIdLattes("7364915463901013");
        pesquisador.setNomeCompleto("Pesquisador Local");
        pesquisador.addArtigo(criarArtigoQualis(2023, EstratoQualis.A2));
        pesquisador.addArtigo(criarArtigoQualis(2024, EstratoQualis.A2));
        pesquisador.addArtigo(criarArtigoQualis(2024, EstratoQualis.B1));

        Artigo semIssn = new Artigo();
        semIssn.setAno(2024);
        semIssn.setStatusQualis(StatusClassificacaoQualis.SEM_ISSN);
        pesquisador.addArtigo(semIssn);

        Artigo naoClassificado = new Artigo();
        naoClassificado.setAno(2022);
        naoClassificado.setStatusQualis(StatusClassificacaoQualis.NAO_CLASSIFICADO);
        pesquisador.addArtigo(naoClassificado);

        when(pesquisadorRepository.findById("7364915463901013")).thenReturn(Optional.of(pesquisador));

        ResumoQualisDTO resumo = dashboardService.buscarResumoQualis("7364915463901013");

        assertThat(resumo.getBaseReferencia()).isEqualTo("2021-2024");
        assertThat(resumo.getFonte()).isEqualTo("CAPES/SUCUPIRA");
        assertThat(resumo.getCobertura().getTotalArtigos()).isEqualTo(5);
        assertThat(resumo.getCobertura().getClassificados()).isEqualTo(3);
        assertThat(resumo.getCobertura().getSemIssn()).isEqualTo(1);
        assertThat(resumo.getCobertura().getNaoClassificados()).isEqualTo(1);
        assertThat(resumo.getCobertura().getPercentualClassificados()).isEqualTo(60.0);
        assertThat(resumo.getDistribuicao()).hasSize(EstratoQualis.values().length);
        assertThat(resumo.getDistribuicao())
                .filteredOn(item -> item.getEstrato() == EstratoQualis.A2)
                .singleElement()
                .extracting(item -> item.getQuantidade())
                .isEqualTo(2L);
        assertThat(resumo.getSerieAnual())
                .extracting(item -> item.getAno())
                .containsExactly(2023, 2024);
        assertThat(resumo.getSerieAnual().get(1).getEstratos().get(EstratoQualis.A2)).isEqualTo(1L);
        assertThat(resumo.getSerieAnual().get(1).getEstratos().get(EstratoQualis.B1)).isEqualTo(1L);
    }

    private Pesquisador criarPesquisador(String idLattes, String nomeCompleto) {
        Pesquisador pesquisador = new Pesquisador();
        pesquisador.setIdLattes(idLattes);
        pesquisador.setNomeCompleto(nomeCompleto);

        Artigo artigo = new Artigo();
        artigo.setAno(2024);
        artigo.setCitacaoCompleta("Artigo de teste");
        pesquisador.addArtigo(artigo);

        return pesquisador;
    }

    private Banca criarBanca(String tipo) {
        Banca banca = new Banca();
        banca.setAno(2024);
        banca.setTipo(tipo);
        banca.setTituloTrabalho("Banca de teste");
        return banca;
    }

    private Orientacao criarOrientacao(Integer ano, String status) {
        Orientacao orientacao = new Orientacao();
        orientacao.setAno(ano);
        orientacao.setStatus(status);
        orientacao.setTipo("Mestrado");
        orientacao.setTituloTrabalho("Orientacao de teste");
        return orientacao;
    }

    private Artigo criarArtigoQualis(Integer ano, EstratoQualis estrato) {
        ClassificacaoQualis classificacao = new ClassificacaoQualis();
        classificacao.setEstrato(estrato);

        Artigo artigo = new Artigo();
        artigo.setAno(ano);
        artigo.setStatusQualis(StatusClassificacaoQualis.CLASSIFICADO);
        artigo.setClassificacaoQualis(classificacao);
        return artigo;
    }
}
