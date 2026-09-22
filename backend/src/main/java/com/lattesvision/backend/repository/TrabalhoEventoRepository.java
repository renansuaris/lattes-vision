package com.lattesvision.backend.repository;

import com.lattesvision.backend.model.TrabalhoEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrabalhoEventoRepository extends JpaRepository<TrabalhoEvento, Long> {

    @Query("""
            select trabalho
            from TrabalhoEvento trabalho
            where trabalho.pesquisador.idLattes = :idLattes
              and (:ano is null or trabalho.ano = :ano)
              and (:anoInicial is null or trabalho.ano >= :anoInicial)
              and (:anoFinal is null or trabalho.ano <= :anoFinal)
            """)
    Page<TrabalhoEvento> findForDashboard(
            @Param("idLattes") String idLattes,
            @Param("ano") Integer ano,
            @Param("anoInicial") Integer anoInicial,
            @Param("anoFinal") Integer anoFinal,
            Pageable pageable
    );
}
