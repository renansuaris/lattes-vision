package com.lattesvision.backend.controller;

import com.lattesvision.backend.model.Pesquisador;
import com.lattesvision.backend.repository.PesquisadorRepository;
import com.lattesvision.backend.service.ScraperService;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/lattes")
@CrossOrigin(origins = "*")
@Tag(name = "Pesquisador", description = "Endpoints para gerenciamento e extracao de dados de pesquisadores da Plataforma Lattes")
public class PesquisadorController {

    private static final Pattern PADRAO_ID_LATTES = Pattern.compile("^\\d{16}$");

    @Autowired
    private ScraperService scraperService;

    @Autowired
    private PesquisadorRepository repository;

    @Operation(summary = "Busca pesquisador por ID Lattes", description = "Retorna os dados completos de um pesquisador (artigos, orientacoes, projetos e bancas) que ja foram persistidos no banco de dados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pesquisador encontrado com sucesso"),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID Lattes invalido",
                    content = @Content(schema = @Schema(implementation = com.lattesvision.backend.dto.ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pesquisador nao encontrado localmente",
                    content = @Content(schema = @Schema(implementation = com.lattesvision.backend.dto.ApiErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Pesquisador> buscarPesquisador(
            @Parameter(description = "ID de 16 digitos do Curriculo Lattes", example = "7364915463901013")
            @PathVariable String id) {
        validarIdLattes(id);
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pesquisador nao encontrado para o ID informado."
                ));
    }

    @Operation(summary = "Lista todos os pesquisadores cadastrados", description = "Recupera a lista completa de pesquisadores que ja passaram pelo processo de sincronizacao e estao no banco de dados.")
    @GetMapping
    public ResponseEntity<List<Pesquisador>> listarTodos() {
        return ResponseEntity.ok(repository.findAll());
    }

    @Operation(summary = "Sincroniza um pesquisador (Web Scraping)", description = "Aciona o motor Playwright para acessar a Plataforma Lattes, realizar o scraping completo e atualizar/inserir os dados no banco de dados local.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sincronizacao concluida com sucesso"),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID Lattes invalido",
                    content = @Content(schema = @Schema(implementation = com.lattesvision.backend.dto.ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Curriculo Lattes nao encontrado",
                    content = @Content(schema = @Schema(implementation = com.lattesvision.backend.dto.ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno ao processar o scraping do Lattes",
                    content = @Content(schema = @Schema(implementation = com.lattesvision.backend.dto.ApiErrorResponse.class))
            )
    })
    @PostMapping("/sincronizar/{id}")
    public ResponseEntity<Pesquisador> sincronizarUm(
            @Parameter(description = "ID de 16 digitos para processamento", example = "7364915463901013")
            @PathVariable String id) {
        validarIdLattes(id);
        Pesquisador pesquisadorSalvo = scraperService.sincronizarPesquisador(id);
        return ResponseEntity.ok(pesquisadorSalvo);
    }

    @Operation(summary = "Sincroniza uma lista de pesquisadores em lote", description = "Recebe um array de IDs Lattes e processa a sincronizacao sequencial de cada um, aplicando o tempo de espera (jitter) configurado no servico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sincronizacao em lote concluida com sucesso"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Lista vazia ou contem ID Lattes invalido",
                    content = @Content(schema = @Schema(implementation = com.lattesvision.backend.dto.ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno ao processar o lote",
                    content = @Content(schema = @Schema(implementation = com.lattesvision.backend.dto.ApiErrorResponse.class))
            )
    })
    @PostMapping("/sincronizar-lote")
    public ResponseEntity<List<Pesquisador>> sincronizarLote(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Lista de strings contendo os IDs Lattes",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(type = "string", example = "7364915463901013")))
            )
            @RequestBody List<String> ids) {
        validarLote(ids);
        List<Pesquisador> resultados = scraperService.sincronizarLote(ids);
        return ResponseEntity.ok(resultados);
    }

    private void validarLote(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A lista de IDs Lattes nao pode ser vazia.");
        }

        ids.forEach(this::validarIdLattes);
    }

    private void validarIdLattes(String id) {
        if (id == null || !PADRAO_ID_LATTES.matcher(id).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID Lattes invalido. Use exatamente 16 digitos numericos.");
        }
    }
}
