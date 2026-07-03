package com.lattesvision.backend.service;

import com.lattesvision.backend.dto.ResumoPesquisadorDTO;
import com.lattesvision.backend.dto.RankingPesquisadorDTO;
import com.lattesvision.backend.dto.ResumoGrupoDTO;
import com.lattesvision.backend.dto.SerieGrupoDTO;
import com.lattesvision.backend.model.Artigo;
import com.lattesvision.backend.model.Pesquisador;
import com.lattesvision.backend.model.TrabalhoEvento;
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
}
