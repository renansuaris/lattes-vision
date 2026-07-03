package com.lattesvision.backend.repository;

import com.lattesvision.backend.model.Banca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BancaRepository extends JpaRepository<Banca, Long> {
}
