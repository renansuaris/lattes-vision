package com.lattesvision.backend.repository;

import com.lattesvision.backend.model.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {

    @Query("""
            select item
            from Livro item
            where item.pesquisador.idLattes = :idLattes
              and (:ano is null or item.ano = :ano)
              and (:anoInicial is null or item.ano >= :anoInicial)
              and (:anoFinal is null or item.ano <= :anoFinal)
            """)
    Page<Livro> findForDashboard(
            @Param("idLattes") String idLattes,
            @Param("ano") Integer ano,
            @Param("anoInicial") Integer anoInicial,
            @Param("anoFinal") Integer anoFinal,
            Pageable pageable
    );
}
