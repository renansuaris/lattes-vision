package com.lattesvision.backend.repository;

import com.lattesvision.backend.model.ClassificacaoQualis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ClassificacaoQualisRepository extends JpaRepository<ClassificacaoQualis, Long> {

    long countByCicloInicioAndCicloFim(Integer cicloInicio, Integer cicloFim);

    List<ClassificacaoQualis> findAllByCicloInicioAndCicloFim(Integer cicloInicio, Integer cicloFim);

    List<ClassificacaoQualis> findAllByCicloInicioAndCicloFimAndIssnNormalizadoIn(
            Integer cicloInicio,
            Integer cicloFim,
            Collection<String> issnsNormalizados
    );
}
