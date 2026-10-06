package com.lattesvision.backend.repository;

import com.lattesvision.backend.model.ProducaoTecnica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProducaoTecnicaRepository extends JpaRepository<ProducaoTecnica, Long> {

    @Query("""
            select producao
            from ProducaoTecnica producao
            where producao.pesquisador.idLattes = :idLattes
              and (:ano is null or producao.ano = :ano)
              and (:anoInicial is null or producao.ano >= :anoInicial)
              and (:anoFinal is null or producao.ano <= :anoFinal)
            """)
    Page<ProducaoTecnica> findForDashboard(
            @Param("idLattes") String idLattes,
            @Param("ano") Integer ano,
            @Param("anoInicial") Integer anoInicial,
            @Param("anoFinal") Integer anoFinal,
            Pageable pageable
    );
}
