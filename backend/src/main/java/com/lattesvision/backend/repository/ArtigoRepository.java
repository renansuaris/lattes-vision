package com.lattesvision.backend.repository;

import com.lattesvision.backend.model.Artigo;
import com.lattesvision.backend.model.EstratoQualis;
import com.lattesvision.backend.model.StatusClassificacaoQualis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtigoRepository extends JpaRepository<Artigo, Long> {

    @EntityGraph(attributePaths = "classificacaoQualis")
    @Query("""
            select artigo
            from Artigo artigo
            left join artigo.classificacaoQualis classificacaoQualis
            where artigo.pesquisador.idLattes = :idLattes
              and (:ano is null or artigo.ano = :ano)
              and (:anoInicial is null or artigo.ano >= :anoInicial)
              and (:anoFinal is null or artigo.ano <= :anoFinal)
              and (:estrato is null or classificacaoQualis.estrato = :estrato)
              and (:statusQualis is null or artigo.statusQualis = :statusQualis)
            """)
    Page<Artigo> findForDashboard(
            @Param("idLattes") String idLattes,
            @Param("ano") Integer ano,
            @Param("anoInicial") Integer anoInicial,
            @Param("anoFinal") Integer anoFinal,
            @Param("estrato") EstratoQualis estrato,
            @Param("statusQualis") StatusClassificacaoQualis statusQualis,
            Pageable pageable
    );
}
