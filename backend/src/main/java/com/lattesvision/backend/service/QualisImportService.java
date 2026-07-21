package com.lattesvision.backend.service;

import com.lattesvision.backend.dto.ResultadoImportacaoQualisDTO;
import com.lattesvision.backend.model.ClassificacaoQualis;
import com.lattesvision.backend.model.EstratoQualis;
import com.lattesvision.backend.repository.ClassificacaoQualisRepository;
import com.lattesvision.backend.util.IssnUtils;
import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class QualisImportService {

    public static final int CICLO_INICIO = 2021;
    public static final int CICLO_FIM = 2024;
    public static final int TOTAL_PERIODICOS_ESPERADO = 33_347;
    public static final String FONTE = "CAPES/SUCUPIRA";

    private static final Logger log = LoggerFactory.getLogger(QualisImportService.class);
    private static final String NOME_ABA = "RelatorioQualis";
    private static final List<String> CABECALHO_ESPERADO =
            List.of("ISSN", "Título", "Área de Avaliação", "Estrato");

    private final ClassificacaoQualisRepository repository;

    public QualisImportService(ClassificacaoQualisRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ResultadoImportacaoQualisDTO importar(Path arquivo) {
        if (!Files.isRegularFile(arquivo)) {
            throw new IllegalArgumentException("Arquivo Qualis nao encontrado: " + arquivo.toAbsolutePath());
        }

        LeituraQualis leitura = lerPlanilha(arquivo);
        Map<String, ClassificacaoQualis> existentesPorIssn = new HashMap<>();
        repository.findAllByCicloInicioAndCicloFim(CICLO_INICIO, CICLO_FIM)
                .forEach(item -> existentesPorIssn.put(item.getIssnNormalizado(), item));

        LocalDateTime dataImportacao = LocalDateTime.now();
        List<ClassificacaoQualis> alteradas = new ArrayList<>();
        int inseridos = 0;
        int atualizados = 0;
        int inalterados = 0;

        for (RegistroQualis registro : leitura.registrosPorIssn().values()) {
            ClassificacaoQualis classificacao = existentesPorIssn.get(registro.issnNormalizado());
            if (classificacao == null) {
                classificacao = new ClassificacaoQualis();
                preencher(classificacao, registro, dataImportacao);
                alteradas.add(classificacao);
                inseridos++;
            } else if (foiAlterada(classificacao, registro)) {
                preencher(classificacao, registro, dataImportacao);
                alteradas.add(classificacao);
                atualizados++;
            } else {
                inalterados++;
            }
        }

        repository.saveAll(alteradas);
        log.info(
                "Importacao Qualis concluida: {} linhas, {} periodicos, {} inseridos, {} atualizados, {} inalterados, {} rejeitados e {} ISSNs com digito invalido.",
                leitura.linhasLidas(),
                leitura.registrosPorIssn().size(),
                inseridos,
                atualizados,
                inalterados,
                leitura.linhasRejeitadas(),
                leitura.issnsComDigitoInvalido()
        );

        return new ResultadoImportacaoQualisDTO(
                leitura.linhasLidas(),
                leitura.registrosPorIssn().size(),
                inseridos,
                atualizados,
                inalterados,
                leitura.linhasRejeitadas(),
                leitura.issnsComDigitoInvalido()
        );
    }

    private LeituraQualis lerPlanilha(Path arquivo) {
        try (OPCPackage pacote = OPCPackage.open(arquivo.toFile(), PackageAccess.READ)) {
            ReadOnlySharedStringsTable textosCompartilhados = new ReadOnlySharedStringsTable(pacote);
            XSSFReader leitor = new XSSFReader(pacote);
            StylesTable estilos = leitor.getStylesTable();
            XSSFReader.SheetIterator abas = (XSSFReader.SheetIterator) leitor.getSheetsData();

            while (abas.hasNext()) {
                try (InputStream conteudoAba = abas.next()) {
                    if (NOME_ABA.equals(abas.getSheetName())) {
                        return processarAba(estilos, textosCompartilhados, conteudoAba);
                    }
                }
            }
            throw new IllegalArgumentException("Aba obrigatoria nao encontrada no XLSX: " + NOME_ABA);
        } catch (Exception ex) {
            if (ex instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }
            throw new IllegalStateException("Falha ao ler a planilha Qualis: " + ex.getMessage(), ex);
        }
    }

    private LeituraQualis processarAba(
            StylesTable estilos,
            ReadOnlySharedStringsTable textosCompartilhados,
            InputStream conteudoAba
    ) throws Exception {
        PlanilhaHandler planilhaHandler = new PlanilhaHandler();
        DataFormatter formatador = new DataFormatter(new Locale("pt", "BR"));
        ContentHandler conteudo = new XSSFSheetXMLHandler(
                estilos,
                null,
                textosCompartilhados,
                planilhaHandler,
                formatador,
                false
        );
        XMLReader parser = SAXHelper.newXMLReader();
        parser.setContentHandler(conteudo);
        parser.parse(new InputSource(conteudoAba));
        return planilhaHandler.resultado();
    }

    private void preencher(
            ClassificacaoQualis classificacao,
            RegistroQualis registro,
            LocalDateTime dataImportacao
    ) {
        classificacao.setIssnNormalizado(registro.issnNormalizado());
        classificacao.setIssnFormatado(registro.issnFormatado());
        classificacao.setTituloPeriodico(registro.titulo());
        classificacao.setEstrato(registro.estrato());
        classificacao.setCicloInicio(CICLO_INICIO);
        classificacao.setCicloFim(CICLO_FIM);
        classificacao.setFonte(FONTE);
        classificacao.setDataImportacao(dataImportacao);
    }

    private boolean foiAlterada(ClassificacaoQualis atual, RegistroQualis novo) {
        return !Objects.equals(atual.getIssnFormatado(), novo.issnFormatado())
                || !Objects.equals(atual.getTituloPeriodico(), novo.titulo())
                || atual.getEstrato() != novo.estrato()
                || !Objects.equals(atual.getFonte(), FONTE);
    }

    private record RegistroQualis(
            String issnNormalizado,
            String issnFormatado,
            String titulo,
            EstratoQualis estrato
    ) {
    }

    private record LeituraQualis(
            long linhasLidas,
            int linhasRejeitadas,
            int issnsComDigitoInvalido,
            Map<String, RegistroQualis> registrosPorIssn
    ) {
    }

    private static final class PlanilhaHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

        private final Map<String, RegistroQualis> registrosPorIssn = new LinkedHashMap<>();
        private final String[] valoresLinha = new String[4];
        private long linhasLidas;
        private int linhasRejeitadas;
        private boolean cabecalhoValidado;

        @Override
        public void startRow(int rowNum) {
            for (int i = 0; i < valoresLinha.length; i++) {
                valoresLinha[i] = "";
            }
        }

        @Override
        public void endRow(int rowNum) {
            if (rowNum == 0) {
                validarCabecalho();
                cabecalhoValidado = true;
                return;
            }
            if (!cabecalhoValidado) {
                throw new IllegalArgumentException("Cabecalho Qualis ausente ou fora da primeira linha.");
            }

            linhasLidas++;
            String issnNormalizado = IssnUtils.normalizar(valoresLinha[0]);
            String titulo = valoresLinha[1].trim();
            String estratoTexto = valoresLinha[3].trim().toUpperCase(Locale.ROOT);
            if (issnNormalizado == null || titulo.isBlank() || estratoTexto.isBlank()) {
                linhasRejeitadas++;
                return;
            }

            EstratoQualis estrato;
            try {
                estrato = EstratoQualis.valueOf(estratoTexto);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Estrato Qualis desconhecido na linha " + (rowNum + 1) + ": " + estratoTexto,
                        ex
                );
            }

            RegistroQualis novo = new RegistroQualis(
                    issnNormalizado,
                    IssnUtils.formatar(issnNormalizado),
                    titulo,
                    estrato
            );
            RegistroQualis anterior = registrosPorIssn.putIfAbsent(issnNormalizado, novo);
            if (anterior != null
                    && (!anterior.titulo().equals(novo.titulo()) || anterior.estrato() != novo.estrato())) {
                throw new IllegalArgumentException(
                        "Conflito de titulo ou estrato para o ISSN " + novo.issnFormatado()
                                + " na linha " + (rowNum + 1)
                );
            }
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            int coluna = new CellReference(cellReference).getCol();
            if (coluna < valoresLinha.length) {
                valoresLinha[coluna] = formattedValue == null ? "" : formattedValue;
            }
        }

        private void validarCabecalho() {
            for (int i = 0; i < CABECALHO_ESPERADO.size(); i++) {
                if (!CABECALHO_ESPERADO.get(i).equals(valoresLinha[i].trim())) {
                    throw new IllegalArgumentException(
                            "Cabecalho Qualis invalido na coluna " + (i + 1)
                                    + ". Esperado: " + CABECALHO_ESPERADO.get(i)
                    );
                }
            }
        }

        private LeituraQualis resultado() {
            int invalidos = (int) registrosPorIssn.values().stream()
                    .filter(registro -> !IssnUtils.possuiDigitoVerificadorValido(registro.issnNormalizado()))
                    .count();
            return new LeituraQualis(
                    linhasLidas,
                    linhasRejeitadas,
                    invalidos,
                    registrosPorIssn
            );
        }
    }
}
