package com.lattesvision.backend.service;

import com.lattesvision.backend.dto.ResultadoImportacaoQualisDTO;
import com.lattesvision.backend.model.ClassificacaoQualis;
import com.lattesvision.backend.model.EstratoQualis;
import com.lattesvision.backend.repository.ClassificacaoQualisRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualisImportServiceTest {

    @Mock
    private ClassificacaoQualisRepository repository;

    @TempDir
    private Path tempDir;

    @Test
    void deveImportarDeduplicarEManterIssnOficialComDigitoInvalido() throws Exception {
        Path arquivo = tempDir.resolve("qualis.xlsx");
        criarPlanilha(arquivo);
        when(repository.findAllByCicloInicioAndCicloFim(2021, 2024)).thenReturn(List.of());

        QualisImportService service = new QualisImportService(repository);
        ResultadoImportacaoQualisDTO resultado = service.importar(arquivo);

        assertThat(resultado.linhasLidas()).isEqualTo(3);
        assertThat(resultado.periodicosDistintos()).isEqualTo(2);
        assertThat(resultado.inseridos()).isEqualTo(2);
        assertThat(resultado.issnsComDigitoInvalido()).isEqualTo(1);
        assertThat(resultado.linhasRejeitadas()).isZero();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ClassificacaoQualis>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertThat(captor.getValue())
                .extracting(ClassificacaoQualis::getIssnNormalizado)
                .containsExactlyInAnyOrder("00010782", "21258165");
        assertThat(captor.getValue())
                .filteredOn(item -> item.getIssnNormalizado().equals("00010782"))
                .singleElement()
                .extracting(ClassificacaoQualis::getEstrato)
                .isEqualTo(EstratoQualis.A1);
    }

    @Test
    void deveAtualizarSomenteRegistroAlterado() throws Exception {
        Path arquivo = tempDir.resolve("qualis.xlsx");
        criarPlanilha(arquivo);

        ClassificacaoQualis existenteInalterada = classificacao("00010782", "COMMUNICATIONS OF THE ACM", EstratoQualis.A1);
        ClassificacaoQualis existenteAlterada = classificacao("21258165", "TITULO ANTIGO", EstratoQualis.C);
        when(repository.findAllByCicloInicioAndCicloFim(2021, 2024))
                .thenReturn(List.of(existenteInalterada, existenteAlterada));

        QualisImportService service = new QualisImportService(repository);
        ResultadoImportacaoQualisDTO resultado = service.importar(arquivo);

        assertThat(resultado.inseridos()).isZero();
        assertThat(resultado.atualizados()).isEqualTo(1);
        assertThat(resultado.inalterados()).isEqualTo(1);
        verify(repository).saveAll(anyList());
        assertThat(existenteAlterada.getTituloPeriodico()).isEqualTo("CADERNOS DE ECONOMIA");
    }

    @Test
    void deveProcessarIntegralmenteABaseOficialDoProjeto() {
        Path arquivo = Path.of("data/qualis/raw/qualis_periodicos_2021_2024_oficial.xlsx");
        when(repository.findAllByCicloInicioAndCicloFim(2021, 2024)).thenReturn(List.of());

        QualisImportService service = new QualisImportService(repository);
        ResultadoImportacaoQualisDTO resultado = service.importar(arquivo);

        assertThat(resultado.linhasLidas()).isEqualTo(171_154);
        assertThat(resultado.periodicosDistintos()).isEqualTo(33_347);
        assertThat(resultado.inseridos()).isEqualTo(33_347);
        assertThat(resultado.linhasRejeitadas()).isZero();
        assertThat(resultado.issnsComDigitoInvalido()).isEqualTo(34);
    }

    private void criarPlanilha(Path arquivo) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("RelatorioQualis");
            adicionarLinha(sheet, 0, "ISSN", "Título", "Área de Avaliação", "Estrato");
            adicionarLinha(sheet, 1, "0001-0782", "COMMUNICATIONS OF THE ACM", "COMPUTAÇÃO", "A1");
            adicionarLinha(sheet, 2, "0001-0782", "COMMUNICATIONS OF THE ACM", "INTERDISCIPLINAR", "A1");
            adicionarLinha(sheet, 3, "2125-8165", "CADERNOS DE ECONOMIA", "ECONOMIA", "C");
            try (OutputStream output = Files.newOutputStream(arquivo)) {
                workbook.write(output);
            }
        }
    }

    private void adicionarLinha(Sheet sheet, int indice, String... valores) {
        Row row = sheet.createRow(indice);
        for (int i = 0; i < valores.length; i++) {
            row.createCell(i).setCellValue(valores[i]);
        }
    }

    private ClassificacaoQualis classificacao(String issn, String titulo, EstratoQualis estrato) {
        ClassificacaoQualis classificacao = new ClassificacaoQualis();
        classificacao.setIssnNormalizado(issn);
        classificacao.setIssnFormatado(issn.substring(0, 4) + "-" + issn.substring(4));
        classificacao.setTituloPeriodico(titulo);
        classificacao.setEstrato(estrato);
        classificacao.setCicloInicio(2021);
        classificacao.setCicloFim(2024);
        classificacao.setFonte(QualisImportService.FONTE);
        return classificacao;
    }
}
