package com.lattesvision.backend.service;

import com.lattesvision.backend.dto.ResultadoClassificacaoQualisDTO;
import com.lattesvision.backend.dto.ResultadoImportacaoQualisDTO;
import com.lattesvision.backend.repository.ClassificacaoQualisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualisImportRunnerTest {

    @Mock
    private QualisImportService importService;

    @Mock
    private QualisService qualisService;

    @Mock
    private ClassificacaoQualisRepository repository;

    @TempDir
    private Path tempDir;

    @Test
    void deveIgnorarImportacaoQuandoBaseJaEstaCompleta() {
        when(repository.countByCicloInicioAndCicloFim(2021, 2024))
                .thenReturn((long) QualisImportService.TOTAL_PERIODICOS_ESPERADO);
        QualisImportRunner runner = new QualisImportRunner(
                importService,
                qualisService,
                repository,
                "arquivo-inexistente.xlsx"
        );

        runner.run(null);

        verify(importService, never()).importar(org.mockito.ArgumentMatchers.any());
        verify(qualisService, never()).reclassificarArtigosExistentes();
    }

    @Test
    void deveImportarAutomaticamenteQuandoBaseEstaVazia() throws Exception {
        Path arquivo = Files.createFile(tempDir.resolve("qualis.xlsx"));
        when(repository.countByCicloInicioAndCicloFim(2021, 2024)).thenReturn(0L);
        when(importService.importar(arquivo)).thenReturn(
                new ResultadoImportacaoQualisDTO(171_154, 33_347, 33_347, 0, 0, 0, 34)
        );
        when(qualisService.reclassificarArtigosExistentes()).thenReturn(
                new ResultadoClassificacaoQualisDTO(0, 0, 0, 0)
        );
        QualisImportRunner runner = new QualisImportRunner(
                importService,
                qualisService,
                repository,
                arquivo.toString()
        );

        runner.run(null);

        verify(importService).importar(arquivo);
        verify(qualisService).reclassificarArtigosExistentes();
    }
}
