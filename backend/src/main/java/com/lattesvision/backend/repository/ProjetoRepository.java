package com.lattesvision.backend.repository;

import com.lattesvision.backend.model.Projeto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    @Query("""
            select projeto
            from Projeto projeto
            where projeto.pesquisador.idLattes = :idLattes
              and (:ano is null or projeto.ano = :ano)
              and (:anoInicial is null or projeto.ano >= :anoInicial)
              and (:anoFinal is null or projeto.ano <= :anoFinal)
            """)
    Page<Projeto> findForDashboard(
            @Param("idLattes") String idLattes,
            @Param("ano") Integer ano,
            @Param("anoInicial") Integer anoInicial,
            @Param("anoFinal") Integer anoFinal,
            Pageable pageable
    );
}
