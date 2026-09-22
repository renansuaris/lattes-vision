package com.lattesvision.backend.controller;

import com.lattesvision.backend.dto.ApiErrorResponse;
import com.lattesvision.backend.dto.ArticlePageDTO;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Dashboard", description = "Endpoints agregados para visualizacao bibliometrica")
public class DashboardController {

    private static final Pattern PADRAO_ID_LATTES = Pattern.compile("^\\d{16}$");

    private final DashboardService dashboardService;
    private final CsvExportService csvExportService;

    public DashboardController(DashboardService dashboardService, CsvExportService csvExportService) {
        this.dashboardService = dashboardService;
        this.csvExportService = csvExportService;
    }

    @Operation(
            summary = "Resumo agregado de um pesquisador",
            description = "Retorna contagens e metadados principais de um pesquisador ja sincronizado, sem expor a entidade JPA completa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumo encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ResumoPesquisadorDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID Lattes invalido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pesquisador nao encontrado localmente",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/pesquisadores/{id}/resumo")
    public ResponseEntity<ResumoPesquisadorDTO> buscarResumoPesquisador(
            @Parameter(description = "ID de 16 digitos do Curriculo Lattes", example = "7364915463901013")
            @PathVariable String id,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarResumoPesquisador(id, criarRecorteAnual(anoInicial, anoFinal)));
    }

    @Operation(
            summary = "Carrega o dashboard individual de um pesquisador",
            description = "Consulta o pesquisador localmente e, caso ele ainda nao exista no banco, executa a sincronizacao antes de retornar o resumo agregado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumo carregado com sucesso",
                    content = @Content(schema = @Schema(implementation = ResumoPesquisadorDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID Lattes invalido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Curriculo Lattes nao encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno durante carregamento ou sincronizacao",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/pesquisadores/{id}/carregar")
    public ResponseEntity<ResumoPesquisadorDTO> carregarResumoPesquisador(
            @Parameter(description = "ID de 16 digitos do Curriculo Lattes", example = "7364915463901013")
            @PathVariable String id,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.carregarResumoPesquisador(id, criarRecorteAnual(anoInicial, anoFinal)));
    }

    @Operation(
            summary = "Exporta os dados de um pesquisador em CSV",
            description = "Gera um arquivo CSV a partir dos dados ja persistidos, respeitando o periodo informado e sem executar scraping."
    )
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

    @Operation(
            summary = "Carrega o dashboard ad-hoc de um grupo de pesquisadores",
            description = "Recebe uma lista de IDs Lattes, usa os pesquisadores ja persistidos quando existirem e sincroniza apenas os IDs ainda ausentes antes de retornar um resumo agregado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumo de grupo carregado com sucesso",
                    content = @Content(schema = @Schema(implementation = ResumoGrupoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Lista vazia, lista com menos de dois IDs ou ID Lattes invalido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Um dos curriculos Lattes nao foi encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno durante carregamento ou sincronizacao",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/grupo-adhoc/carregar")
    public ResponseEntity<ResumoGrupoDTO> carregarResumoGrupoAdhoc(
            @RequestBody GrupoAdhocRequestDTO request,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdsGrupo(request);
        return ResponseEntity.ok(dashboardService.carregarResumoGrupoAdhoc(request.getIdsLattes(), criarRecorteAnual(anoInicial, anoFinal)));
    }

    @Operation(
            summary = "Exporta os dados de um grupo ad-hoc em CSV",
            description = "Gera um arquivo CSV consolidado a partir dos curriculos ja persistidos, respeitando o periodo informado e sem executar scraping."
    )
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

    @Operation(
            summary = "Series anuais agregadas de um grupo ad-hoc",
            description = "Recebe uma lista de IDs Lattes e retorna series agregadas por ano para graficos comparativos do grupo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Series de grupo encontradas com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SerieGrupoDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Lista vazia, lista com menos de dois IDs ou ID Lattes invalido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/grupo-adhoc/series")
    public ResponseEntity<List<SerieGrupoDTO>> buscarSeriesGrupoAdhoc(
            @RequestBody GrupoAdhocRequestDTO request,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdsGrupo(request);
        return ResponseEntity.ok(dashboardService.buscarSeriesGrupoAdhoc(request.getIdsLattes(), criarRecorteAnual(anoInicial, anoFinal)));
    }

    @Operation(
            summary = "Resumo Qualis agregado de um grupo ad-hoc",
            description = "Recebe uma lista de IDs Lattes e consolida cobertura, distribuicao por estrato e evolucao anual usando a base de referencia CAPES 2021-2024."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumo Qualis do grupo encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ResumoQualisDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Lista vazia, lista com menos de dois IDs ou ID Lattes invalido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/grupo-adhoc/qualis")
    public ResponseEntity<ResumoQualisDTO> buscarResumoQualisGrupoAdhoc(
            @RequestBody GrupoAdhocRequestDTO request,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdsGrupo(request);
        return ResponseEntity.ok(dashboardService.buscarResumoQualisGrupoAdhoc(request.getIdsLattes(), criarRecorteAnual(anoInicial, anoFinal)));
    }

    @Operation(
            summary = "Ranking de pesquisadores de um grupo ad-hoc",
            description = "Recebe uma lista de IDs Lattes e retorna os pesquisadores ordenados por uma metrica agregada."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ranking de grupo encontrado com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RankingPesquisadorDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Lista vazia, lista com menos de dois IDs, ID Lattes invalido ou metrica invalida",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/grupo-adhoc/ranking")
    public ResponseEntity<List<RankingPesquisadorDTO>> buscarRankingGrupoAdhoc(
            @RequestBody GrupoAdhocRequestDTO request,
            @Parameter(description = "Metrica do ranking (total, artigos, orientacoes, projetos, bancas, producoes_tecnicas, trabalhos_eventos)", example = "total")
            @RequestParam(defaultValue = "total") String metrica,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdsGrupo(request);
        return ResponseEntity.ok(dashboardService.buscarRankingGrupoAdhoc(request.getIdsLattes(), metrica, criarRecorteAnual(anoInicial, anoFinal)));
    }

    @Operation(
            summary = "Series temporais de producao",
            description = "Retorna a contagem de producoes agrupada por ano para um tipo especifico (artigos, orientacoes, projetos, bancas, producoes_tecnicas)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Series encontradas com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SerieTemporalDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parametros invalidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pesquisador nao encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/pesquisadores/{id}/series")
    public ResponseEntity<List<SerieTemporalDTO>> buscarSeriesTemporais(
            @Parameter(description = "ID de 16 digitos do Curriculo Lattes", example = "7364915463901013")
            @PathVariable String id,
            @Parameter(description = "Tipo de producao (artigos, orientacoes, projetos, bancas, producoes_tecnicas, trabalhos_eventos)", example = "artigos")
            @RequestParam String tipo,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarSeriesTemporais(id, tipo, criarRecorteAnual(anoInicial, anoFinal)));
    }

    @Operation(
            summary = "Artigos paginados para auditoria do dashboard",
            description = "Retorna artigos ja persistidos para um pesquisador, com filtros opcionais por ano, estrato e cobertura Qualis. Nao executa scraping."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pagina de artigos encontrada com sucesso",
                    content = @Content(schema = @Schema(implementation = ArticlePageDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID, filtros ou parametros de paginacao invalidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pesquisador nao encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/pesquisadores/{id}/artigos")
    public ResponseEntity<ArticlePageDTO> buscarArtigos(
            @Parameter(description = "ID de 16 digitos do Curriculo Lattes", example = "7364915463901013")
            @PathVariable String id,
            @Parameter(description = "Ano de publicacao") @RequestParam(required = false) Integer ano,
            @Parameter(description = "Estrato Qualis") @RequestParam(required = false) String estrato,
            @Parameter(description = "Status da cobertura Qualis")
            @RequestParam(required = false) String statusQualis,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal,
            @Parameter(description = "Pagina baseada em zero") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por pagina, entre 1 e 100")
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

    @Operation(
            summary = "Orientacoes paginadas para auditoria do dashboard",
            description = "Retorna orientacoes ja persistidas para um pesquisador, com filtros opcionais por ano, status e tipo. Nao executa scraping."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pagina de orientacoes encontrada com sucesso",
                    content = @Content(schema = @Schema(implementation = OrientationPageDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID, filtros ou parametros de paginacao invalidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pesquisador nao encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/pesquisadores/{id}/orientacoes")
    public ResponseEntity<OrientationPageDTO> buscarOrientacoes(
            @Parameter(description = "ID de 16 digitos do Curriculo Lattes", example = "7364915463901013")
            @PathVariable String id,
            @Parameter(description = "Ano da orientacao") @RequestParam(required = false) Integer ano,
            @Parameter(description = "Status da orientacao: CONCLUIDA ou EM_ANDAMENTO")
            @RequestParam(required = false) String status,
            @Parameter(description = "Tipo da orientacao, por exemplo Mestrado ou Doutorado")
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal,
            @Parameter(description = "Pagina baseada em zero") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por pagina, entre 1 e 100")
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

    @Operation(
            summary = "Participacoes em bancas por tipo",
            description = "Retorna a quantidade de participacoes em bancas agrupada por tipo para graficos de distribuicao."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Distribuicao de bancas encontrada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoriaQuantidadeDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID Lattes invalido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pesquisador nao encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/pesquisadores/{id}/bancas/tipos")
    public ResponseEntity<List<CategoriaQuantidadeDTO>> buscarBancasPorTipo(
            @Parameter(description = "ID de 16 digitos do Curriculo Lattes", example = "7364915463901013")
            @PathVariable String id,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarBancasPorTipo(id, criarRecorteAnual(anoInicial, anoFinal)));
    }

    @Operation(
            summary = "Orientacoes por status e ano",
            description = "Retorna orientacoes concluidas e em andamento agrupadas por ano para grafico de barras lado a lado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Series de orientacoes encontradas com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SerieOrientacaoStatusDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID Lattes invalido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pesquisador nao encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/pesquisadores/{id}/orientacoes/status-series")
    public ResponseEntity<List<SerieOrientacaoStatusDTO>> buscarOrientacoesPorStatusAno(
            @Parameter(description = "ID de 16 digitos do Curriculo Lattes", example = "7364915463901013")
            @PathVariable String id,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarOrientacoesPorStatusAno(id, criarRecorteAnual(anoInicial, anoFinal)));
    }

    @Operation(
            summary = "Resumo Qualis dos artigos de um pesquisador",
            description = "Retorna cobertura, distribuicao por estrato e serie anual usando a base de referencia CAPES 2021-2024. A mesma base e aplicada aos artigos independentemente do ano de publicacao."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumo Qualis encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ResumoQualisDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID Lattes invalido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pesquisador nao encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/pesquisadores/{id}/qualis")
    public ResponseEntity<ResumoQualisDTO> buscarResumoQualis(
            @Parameter(description = "ID de 16 digitos do Curriculo Lattes", example = "7364915463901013")
            @PathVariable String id,
            @RequestParam(required = false) Integer anoInicial,
            @RequestParam(required = false) Integer anoFinal
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarResumoQualis(id, criarRecorteAnual(anoInicial, anoFinal)));
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
