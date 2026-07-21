package com.lattesvision.backend.service;

import com.lattesvision.backend.dto.ResultadoClassificacaoQualisDTO;
import com.lattesvision.backend.model.Artigo;
import com.lattesvision.backend.model.ClassificacaoQualis;
import com.lattesvision.backend.model.StatusClassificacaoQualis;
import com.lattesvision.backend.repository.ArtigoRepository;
import com.lattesvision.backend.repository.ClassificacaoQualisRepository;
import com.lattesvision.backend.util.IssnUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QualisService {

    private final ClassificacaoQualisRepository classificacaoRepository;
    private final ArtigoRepository artigoRepository;

    public QualisService(
            ClassificacaoQualisRepository classificacaoRepository,
            ArtigoRepository artigoRepository
    ) {
        this.classificacaoRepository = classificacaoRepository;
        this.artigoRepository = artigoRepository;
    }

    public ResultadoClassificacaoQualisDTO classificarArtigos(Collection<Artigo> artigos) {
        Set<String> issns = artigos.stream()
                .map(Artigo::getIssn)
                .map(IssnUtils::normalizar)
                .filter(issn -> issn != null)
                .collect(Collectors.toSet());

        Map<String, ClassificacaoQualis> classificacoesPorIssn = buscarClassificacoes(issns);
        int classificados = 0;
        int semIssn = 0;
        int naoClassificados = 0;

        for (Artigo artigo : artigos) {
            String issnNormalizado = IssnUtils.normalizar(artigo.getIssn());
            if (issnNormalizado == null) {
                artigo.setStatusQualis(StatusClassificacaoQualis.SEM_ISSN);
                artigo.setClassificacaoQualis(null);
                semIssn++;
                continue;
            }

            ClassificacaoQualis classificacao = classificacoesPorIssn.get(issnNormalizado);
            if (classificacao == null) {
                artigo.setStatusQualis(StatusClassificacaoQualis.NAO_CLASSIFICADO);
                artigo.setClassificacaoQualis(null);
                naoClassificados++;
            } else {
                artigo.setStatusQualis(StatusClassificacaoQualis.CLASSIFICADO);
                artigo.setClassificacaoQualis(classificacao);
                classificados++;
            }
        }

        return new ResultadoClassificacaoQualisDTO(
                artigos.size(),
                classificados,
                semIssn,
                naoClassificados
        );
    }

    @Transactional
    public ResultadoClassificacaoQualisDTO reclassificarArtigosExistentes() {
        var artigos = artigoRepository.findAll();
        ResultadoClassificacaoQualisDTO resultado = classificarArtigos(artigos);
        artigoRepository.saveAll(artigos);
        return resultado;
    }

    private Map<String, ClassificacaoQualis> buscarClassificacoes(Set<String> issns) {
        if (issns.isEmpty()) {
            return Collections.emptyMap();
        }

        return classificacaoRepository
                .findAllByCicloInicioAndCicloFimAndIssnNormalizadoIn(
                        QualisImportService.CICLO_INICIO,
                        QualisImportService.CICLO_FIM,
                        issns
                )
                .stream()
                .collect(Collectors.toMap(
                        ClassificacaoQualis::getIssnNormalizado,
                        Function.identity()
                ));
    }
}
