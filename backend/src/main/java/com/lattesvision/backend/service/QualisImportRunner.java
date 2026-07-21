package com.lattesvision.backend.service;

import com.lattesvision.backend.dto.ResultadoClassificacaoQualisDTO;
import com.lattesvision.backend.dto.ResultadoImportacaoQualisDTO;
import com.lattesvision.backend.repository.ClassificacaoQualisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
@ConditionalOnProperty(name = "qualis.import.enabled", havingValue = "true", matchIfMissing = true)
public class QualisImportRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(QualisImportRunner.class);

    private final QualisImportService importService;
    private final QualisService qualisService;
    private final ClassificacaoQualisRepository repository;
    private final String caminhoConfigurado;

    public QualisImportRunner(
            QualisImportService importService,
            QualisService qualisService,
            ClassificacaoQualisRepository repository,
            @Value("${qualis.import.file}") String arquivo
    ) {
        this.importService = importService;
        this.qualisService = qualisService;
        this.repository = repository;
        this.caminhoConfigurado = arquivo;
    }

    @Override
    public void run(ApplicationArguments args) {
        long totalExistente = repository.countByCicloInicioAndCicloFim(
                QualisImportService.CICLO_INICIO,
                QualisImportService.CICLO_FIM
        );
        if (totalExistente == QualisImportService.TOTAL_PERIODICOS_ESPERADO) {
            log.info("Base Qualis 2021-2024 ja carregada com {} periodicos. Importacao ignorada.", totalExistente);
            return;
        }

        Path arquivo = localizarArquivo();
        log.info("Base Qualis ausente ou incompleta. Iniciando importacao de: {}", arquivo.toAbsolutePath());
        if (totalExistente > 0) {
            log.warn(
                    "Base Qualis parcial ou divergente: {} de {} periodicos. A importacao tentara reparar os dados.",
                    totalExistente,
                    QualisImportService.TOTAL_PERIODICOS_ESPERADO
            );
        }
        ResultadoImportacaoQualisDTO importacao = importService.importar(arquivo);
        ResultadoClassificacaoQualisDTO classificacao = qualisService.reclassificarArtigosExistentes();
        log.info("Resultado da importacao Qualis: {}", importacao);
        log.info("Resultado da reclassificacao dos artigos existentes: {}", classificacao);
    }

    private Path localizarArquivo() {
        Path configurado = Path.of(caminhoConfigurado);
        List<Path> candidatos = List.of(
                configurado,
                Path.of("backend").resolve(configurado)
        );
        return candidatos.stream()
                .map(Path::normalize)
                .filter(Files::isRegularFile)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Arquivo oficial do Qualis nao encontrado. Caminhos verificados: " + candidatos
                ));
    }
}
