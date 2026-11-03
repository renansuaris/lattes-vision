package com.lattesvision.backend.controller;

import com.lattesvision.backend.dto.ArticlePageDTO;
import com.lattesvision.backend.dto.BancaPageDTO;
import com.lattesvision.backend.dto.EventWorkPageDTO;
import com.lattesvision.backend.dto.BookChapterPageDTO;
import com.lattesvision.backend.dto.BookPageDTO;
import com.lattesvision.backend.dto.ProjectPageDTO;
import com.lattesvision.backend.dto.TechnicalProductionPageDTO;
import com.lattesvision.backend.dto.OrientationPageDTO;
import com.lattesvision.backend.dto.CategoriaQuantidadeDTO;
import com.lattesvision.backend.dto.GrupoAdhocRequestDTO;
import com.lattesvision.backend.dto.RankingPesquisadorDTO;
import com.lattesvision.backend.dto.ResumoGrupoDTO;
import com.lattesvision.backend.dto.ResumoPesquisadorDTO;
import com.lattesvision.backend.dto.ResumoQualisDTO;
import com.lattesvision.backend.dto.SerieGrupoDTO;
import com.lattesvision.backend.dto.SerieOrientacaoStatusDTO;
import com.lattesvision.backend.dto.SerieTemporalDTO;
import com.lattesvision.backend.dto.YearRange;
import com.lattesvision.backend.service.DashboardService;
import com.lattesvision.backend.service.CsvExportService;
import com.lattesvision.backend.model.EstratoQualis;
import com.lattesvision.backend.model.StatusClassificacaoQualis;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private static final Pattern PADRAO_ID_LATTES = Pattern.compile("^\\d{16}$");

    private final DashboardService dashboardService;
    private final CsvExportService csvExportService;

    public DashboardController(DashboardService dashboardService, CsvExportService csvExportService) {
        this.dashboardService = dashboardService;
        this.csvExportService = csvExportService;
    }

    @GetMapping("/pesquisadores/{id}/resumo")
    public ResponseEntity<ResumoPesquisadorDTO> buscarResumoPesquisador(
            @PathVariable String id,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarResumoPesquisador(id, criarRecorteAnual(anoInicial, anoFinal)));
    }

    @PostMapping("/pesquisadores/{id}/carregar")
    public ResponseEntity<ResumoPesquisadorDTO> carregarResumoPesquisador(
            @PathVariable String id,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.carregarResumoPesquisador(id, criarRecorteAnual(anoInicial, anoFinal)));
    }

    @GetMapping(value = "/pesquisadores/{id}/exportacao.csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportarPesquisadorCsv(
            @PathVariable String id,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdLattes(id);
        YearRange yearRange = criarRecorteAnual(anoInicial, anoFinal);
        csvExportService.validarPesquisadorDisponivel(id);
        return criarRespostaCsv(nomeArquivoExportacao("pesquisador-" + id, yearRange),
                writer -> csvExportService.exportarPesquisador(id, yearRange, writer));
    }

    @PostMapping("/grupo-adhoc/carregar")
    public ResponseEntity<ResumoGrupoDTO> carregarResumoGrupoAdhoc(
            @RequestBody GrupoAdhocRequestDTO request,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdsGrupo(request);
        return ResponseEntity.ok(dashboardService.carregarResumoGrupoAdhoc(request.getIdsLattes(), criarRecorteAnual(anoInicial, anoFinal)));
    }

    @PostMapping(value = "/grupo-adhoc/exportacao.csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportarGrupoAdhocCsv(
            @RequestBody GrupoAdhocRequestDTO request,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdsGrupo(request);
        YearRange yearRange = criarRecorteAnual(anoInicial, anoFinal);
        csvExportService.validarGrupoDisponivel(request.getIdsLattes());
        return criarRespostaCsv(nomeArquivoExportacao("grupo", yearRange),
                writer -> csvExportService.exportarGrupo(request.getIdsLattes(), yearRange, writer));
    }

    @PostMapping("/grupo-adhoc/series")
    public ResponseEntity<List<SerieGrupoDTO>> buscarSeriesGrupoAdhoc(
            @RequestBody GrupoAdhocRequestDTO request,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdsGrupo(request);
        return ResponseEntity.ok(dashboardService.buscarSeriesGrupoAdhoc(request.getIdsLattes(), criarRecorteAnual(anoInicial, anoFinal)));
    }

    @PostMapping("/grupo-adhoc/qualis")
    public ResponseEntity<ResumoQualisDTO> buscarResumoQualisGrupoAdhoc(
            @RequestBody GrupoAdhocRequestDTO request,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdsGrupo(request);
        return ResponseEntity.ok(dashboardService.buscarResumoQualisGrupoAdhoc(request.getIdsLattes(), criarRecorteAnual(anoInicial, anoFinal)));
    }

    @PostMapping("/grupo-adhoc/ranking")
    public ResponseEntity<List<RankingPesquisadorDTO>> buscarRankingGrupoAdhoc(
            @RequestBody GrupoAdhocRequestDTO request,
            @RequestParam(defaultValue = "total") String metrica,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdsGrupo(request);
        return ResponseEntity.ok(dashboardService.buscarRankingGrupoAdhoc(request.getIdsLattes(), metrica, criarRecorteAnual(anoInicial, anoFinal)));
    }

    @GetMapping("/pesquisadores/{id}/series")
    public ResponseEntity<List<SerieTemporalDTO>> buscarSeriesTemporais(
            @PathVariable String id,
            @RequestParam String tipo,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarSeriesTemporais(id, tipo, criarRecorteAnual(anoInicial, anoFinal)));
    }

    @GetMapping("/pesquisadores/{id}/artigos")
    public ResponseEntity<ArticlePageDTO> buscarArtigos(
            @PathVariable String id,
             @RequestParam(required = false) Integer ano,
             @RequestParam(required = false) String estrato,
            @RequestParam(required = false) String statusQualis,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal,
             @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validarIdLattes(id);
        validarPaginacao(page, size);
        return ResponseEntity.ok(dashboardService.buscarArtigos(
                id,
                ano,
                criarRecorteAnual(anoInicial, anoFinal),
                converterEstrato(estrato),
                converterStatusQualis(statusQualis),
                page,
                size
        ));
    }

    @GetMapping("/pesquisadores/{id}/trabalhos-eventos")
    public ResponseEntity<EventWorkPageDTO> buscarTrabalhosEventos(
            @PathVariable String id,
             @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal,
             @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validarIdLattes(id);
        validarPaginacao(page, size);
        return ResponseEntity.ok(dashboardService.buscarTrabalhosEventos(
                id,
                ano,
                criarRecorteAnual(anoInicial, anoFinal),
                page,
                size
        ));
    }

    @GetMapping("/pesquisadores/{id}/capitulos")
    public ResponseEntity<BookChapterPageDTO> buscarCapitulos(
            @PathVariable String id,
             @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal,
             @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validarIdLattes(id);
        validarPaginacao(page, size);
        if (ano != null && (ano < 1000 || ano > 9999)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ano de publicacao deve ter quatro digitos.");
        }
        return ResponseEntity.ok(dashboardService.buscarCapitulos(
                id,
                ano,
                criarRecorteAnual(anoInicial, anoFinal),
                page,
                size
        ));
    }

    @GetMapping("/pesquisadores/{id}/livros")
    public ResponseEntity<BookPageDTO> buscarLivros(
            @PathVariable String id,
             @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal,
             @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validarIdLattes(id);
        validarPaginacao(page, size);
        if (ano != null && (ano < 1000 || ano > 9999)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ano de publicacao deve ter quatro digitos.");
        }
        return ResponseEntity.ok(dashboardService.buscarLivros(
                id,
                ano,
                criarRecorteAnual(anoInicial, anoFinal),
                page,
                size
        ));
    }

    @GetMapping("/pesquisadores/{id}/orientacoes")
    public ResponseEntity<OrientationPageDTO> buscarOrientacoes(
            @PathVariable String id,
             @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal,
             @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validarIdLattes(id);
        validarPaginacao(page, size);
        return ResponseEntity.ok(dashboardService.buscarOrientacoes(
                id,
                ano,
                criarRecorteAnual(anoInicial, anoFinal),
                converterStatusOrientacao(status),
                tipo,
                page,
                size
        ));
    }

    @GetMapping("/pesquisadores/{id}/bancas")
    public ResponseEntity<BancaPageDTO> buscarBancas(
            @PathVariable String id,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal,
             @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validarIdLattes(id);
        validarPaginacao(page, size);
        return ResponseEntity.ok(dashboardService.buscarBancas(
                id,
                criarRecorteAnual(anoInicial, anoFinal),
                tipo,
                page,
                size
        ));
    }

    @GetMapping("/pesquisadores/{id}/bancas/tipos")
    public ResponseEntity<List<CategoriaQuantidadeDTO>> buscarBancasPorTipo(
            @PathVariable String id,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarBancasPorTipo(id, criarRecorteAnual(anoInicial, anoFinal)));
    }

    @GetMapping("/pesquisadores/{id}/orientacoes/status-series")
    public ResponseEntity<List<SerieOrientacaoStatusDTO>> buscarOrientacoesPorStatusAno(
            @PathVariable String id,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarOrientacoesPorStatusAno(id, criarRecorteAnual(anoInicial, anoFinal)));
    }

    @GetMapping("/pesquisadores/{id}/qualis")
    public ResponseEntity<ResumoQualisDTO> buscarResumoQualis(
            @PathVariable String id,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarResumoQualis(id, criarRecorteAnual(anoInicial, anoFinal)));
    }

    @GetMapping("/pesquisadores/{id}/projetos")
    public ResponseEntity<ProjectPageDTO> buscarProjetos(
             @PathVariable String id,
             @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal,
             @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validarIdLattes(id);
        validarPaginacao(page, size);
        if (ano != null && (ano < 1000 || ano > 9999)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ano de inicio do projeto deve ter quatro digitos.");
        }
        return ResponseEntity.ok(dashboardService.buscarProjetos(
                id, ano, criarRecorteAnual(anoInicial, anoFinal), page, size
        ));
    }

    @GetMapping("/pesquisadores/{id}/producoes-tecnicas")
    public ResponseEntity<TechnicalProductionPageDTO> buscarProducoesTecnicas(
             @PathVariable String id,
             @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal,
             @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validarIdLattes(id);
        validarPaginacao(page, size);
        if (ano != null && (ano < 1000 || ano > 9999)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ano da producao tecnica deve ter quatro digitos.");
        }
        return ResponseEntity.ok(dashboardService.buscarProducoesTecnicas(
                id, ano, criarRecorteAnual(anoInicial, anoFinal), page, size
        ));
    }

    private void validarIdLattes(String id) {
        if (id == null || !PADRAO_ID_LATTES.matcher(id).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID Lattes invalido. Use exatamente 16 digitos numericos.");
        }
    }

    private void validarPaginacao(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A pagina deve ser maior ou igual a zero.");
        }
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O tamanho da pagina deve estar entre 1 e 100.");
        }
    }

    private YearRange criarRecorteAnual(Integer anoInicial, Integer anoFinal) {
        if (anoInicial != null && (anoInicial < 1000 || anoInicial > 9999)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ano inicial deve ter quatro digitos.");
        }
        if (anoFinal != null && (anoFinal < 1000 || anoFinal > 9999)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ano final deve ter quatro digitos.");
        }
        if (anoInicial != null && anoFinal != null && anoInicial > anoFinal) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ano inicial nao pode ser maior que o ano final.");
        }
        return new YearRange(anoInicial, anoFinal);
    }

    private ResponseEntity<StreamingResponseBody> criarRespostaCsv(String nomeArquivo, EscritorCsv escritorCsv) {
        StreamingResponseBody corpo = outputStream -> {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            escritorCsv.escrever(writer);
            writer.flush();
        };
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(nomeArquivo, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(corpo);
    }

    private String nomeArquivoExportacao(String escopo, YearRange yearRange) {
        if (yearRange.startYear() != null && yearRange.endYear() != null) {
            return "lattesvision-" + escopo + "-" + yearRange.startYear() + "-" + yearRange.endYear() + ".csv";
        }
        if (yearRange.startYear() != null) {
            return "lattesvision-" + escopo + "-a-partir-de-" + yearRange.startYear() + ".csv";
        }
        if (yearRange.endYear() != null) {
            return "lattesvision-" + escopo + "-ate-" + yearRange.endYear() + ".csv";
        }
        return "lattesvision-" + escopo + "-historico-completo.csv";
    }

    @FunctionalInterface
    private interface EscritorCsv {
        void escrever(java.io.Writer writer) throws java.io.IOException;
    }

    private EstratoQualis converterEstrato(String estrato) {
        return converterEnum(estrato, EstratoQualis.class, "estrato Qualis");
    }

    private StatusClassificacaoQualis converterStatusQualis(String statusQualis) {
        return converterEnum(statusQualis, StatusClassificacaoQualis.class, "status Qualis");
    }

    private String converterStatusOrientacao(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        String statusNormalizado = status.trim().toUpperCase(java.util.Locale.ROOT);
        if (!"CONCLUIDA".equals(statusNormalizado) && !"EM_ANDAMENTO".equals(statusNormalizado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor invalido para status da orientacao.");
        }
        return statusNormalizado;
    }

    private <T extends Enum<T>> T converterEnum(String valor, Class<T> tipo, String nomeParametro) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor invalido para " + nomeParametro + ".");
        }
    }

    private void validarIdsGrupo(GrupoAdhocRequestDTO request) {
        if (request == null || request.getIdsLattes() == null || request.getIdsLattes().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ao menos dois IDs Lattes para analise de grupo.");
        }

        List<String> idsUnicos = request.getIdsLattes().stream()
                .distinct()
                .toList();

        if (idsUnicos.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ao menos dois IDs Lattes distintos para analise de grupo.");
        }

        idsUnicos.forEach(this::validarIdLattes);
        request.setIdsLattes(idsUnicos);
    }
}
