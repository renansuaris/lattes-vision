package com.lattesvision.backend.repository;

import com.lattesvision.backend.model.Orientacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrientacaoRepository extends JpaRepository<Orientacao, Long>, JpaSpecificationExecutor<Orientacao> {
}
