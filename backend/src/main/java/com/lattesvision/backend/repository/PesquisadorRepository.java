package com.lattesvision.backend.repository;

import com.lattesvision.backend.model.Pesquisador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PesquisadorRepository extends JpaRepository<Pesquisador, String> {
}
