package com.lattesvision.backend.service;

import com.lattesvision.backend.dto.ResultadoClassificacaoQualisDTO;
import com.lattesvision.backend.model.Artigo;
import com.lattesvision.backend.model.ClassificacaoQualis;
import com.lattesvision.backend.model.StatusClassificacaoQualis;
import com.lattesvision.backend.repository.ArtigoRepository;
import com.lattesvision.backend.repository.ClassificacaoQualisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualisServiceTest {

    @Mock
    private ClassificacaoQualisRepository classificacaoRepository;

    @Mock
    private ArtigoRepository artigoRepository;

    private QualisService service;

    @BeforeEach
    void setUp() {
        service = new QualisService(classificacaoRepository, artigoRepository);
    }

    @Test
    void deveClassificarArtigosPorIssnEDistinguirCobertura() {
        ClassificacaoQualis classificacao = new ClassificacaoQualis();
        classificacao.setIssnNormalizado("00010782");

        when(classificacaoRepository.findAllByCicloInicioAndCicloFimAndIssnNormalizadoIn(
                eq(QualisImportService.CICLO_INICIO),
                eq(QualisImportService.CICLO_FIM),
                anyCollection()
        )).thenReturn(List.of(classificacao));

        Artigo classificado = artigo("0001-0782");
        Artigo naoClassificado = artigo("9999-9999");
        Artigo semIssn = artigo(null);

        ResultadoClassificacaoQualisDTO resultado = service.classificarArtigos(
                List.of(classificado, naoClassificado, semIssn)
        );

        assertThat(classificado.getStatusQualis()).isEqualTo(StatusClassificacaoQualis.CLASSIFICADO);
        assertThat(classificado.getClassificacaoQualis()).isSameAs(classificacao);
        assertThat(naoClassificado.getStatusQualis()).isEqualTo(StatusClassificacaoQualis.NAO_CLASSIFICADO);
        assertThat(naoClassificado.getClassificacaoQualis()).isNull();
        assertThat(semIssn.getStatusQualis()).isEqualTo(StatusClassificacaoQualis.SEM_ISSN);
        assertThat(resultado).isEqualTo(new ResultadoClassificacaoQualisDTO(3, 1, 1, 1));
    }

    @Test
    void deveReclassificarEPersistirArtigosExistentes() {
        Artigo artigo = artigo(null);
        when(artigoRepository.findAll()).thenReturn(List.of(artigo));

        ResultadoClassificacaoQualisDTO resultado = service.reclassificarArtigosExistentes();

        assertThat(resultado.semIssn()).isEqualTo(1);
        verify(artigoRepository).saveAll(anyCollection());
    }

    private Artigo artigo(String issn) {
        Artigo artigo = new Artigo();
        artigo.setIssn(issn);
        return artigo;
    }
}
