package com.lattesvision.backend.controller;

import com.lattesvision.backend.dto.ApiErrorResponse;
import com.lattesvision.backend.dto.CategoriaQuantidadeDTO;
import com.lattesvision.backend.dto.GrupoAdhocRequestDTO;
import com.lattesvision.backend.dto.RankingPesquisadorDTO;
import com.lattesvision.backend.dto.ResumoGrupoDTO;
import com.lattesvision.backend.dto.ResumoPesquisadorDTO;
import com.lattesvision.backend.dto.ResumoQualisDTO;
import com.lattesvision.backend.dto.SerieGrupoDTO;
import com.lattesvision.backend.dto.SerieOrientacaoStatusDTO;
import com.lattesvision.backend.dto.SerieTemporalDTO;
import com.lattesvision.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "Endpoints agregados para visualizacao bibliometrica")
public class DashboardController {

    private static final Pattern PADRAO_ID_LATTES = Pattern.compile("^\\d{16}$");

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
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
            @PathVariable String id
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarResumoPesquisador(id));
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
                    responseCode = "500",
                    description = "Erro interno durante carregamento ou sincronizacao",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/pesquisadores/{id}/carregar")
    public ResponseEntity<ResumoPesquisadorDTO> carregarResumoPesquisador(
            @Parameter(description = "ID de 16 digitos do Curriculo Lattes", example = "7364915463901013")
            @PathVariable String id
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.carregarResumoPesquisador(id));
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
                    responseCode = "500",
                    description = "Erro interno durante carregamento ou sincronizacao",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/grupo-adhoc/carregar")
    public ResponseEntity<ResumoGrupoDTO> carregarResumoGrupoAdhoc(@RequestBody GrupoAdhocRequestDTO request) {
        validarIdsGrupo(request);
        return ResponseEntity.ok(dashboardService.carregarResumoGrupoAdhoc(request.getIdsLattes()));
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
    public ResponseEntity<List<SerieGrupoDTO>> buscarSeriesGrupoAdhoc(@RequestBody GrupoAdhocRequestDTO request) {
        validarIdsGrupo(request);
        return ResponseEntity.ok(dashboardService.buscarSeriesGrupoAdhoc(request.getIdsLattes()));
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
            @RequestBody GrupoAdhocRequestDTO request
    ) {
        validarIdsGrupo(request);
        return ResponseEntity.ok(dashboardService.buscarResumoQualisGrupoAdhoc(request.getIdsLattes()));
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
            @RequestParam(defaultValue = "total") String metrica
    ) {
        validarIdsGrupo(request);
        return ResponseEntity.ok(dashboardService.buscarRankingGrupoAdhoc(request.getIdsLattes(), metrica));
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
            @RequestParam String tipo
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarSeriesTemporais(id, tipo));
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
            @PathVariable String id
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarBancasPorTipo(id));
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
            @PathVariable String id
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarOrientacoesPorStatusAno(id));
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
            @PathVariable String id
    ) {
        validarIdLattes(id);
        return ResponseEntity.ok(dashboardService.buscarResumoQualis(id));
    }

    private void validarIdLattes(String id) {
        if (id == null || !PADRAO_ID_LATTES.matcher(id).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID Lattes invalido. Use exatamente 16 digitos numericos.");
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
