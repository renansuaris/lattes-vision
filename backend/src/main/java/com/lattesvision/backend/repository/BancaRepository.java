package com.lattesvision.backend.repository;

import com.lattesvision.backend.model.Banca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BancaRepository extends JpaRepository<Banca, Long> {

    @Query("""
            select banca
            from Banca banca
            where banca.pesquisador.idLattes = :idLattes
              and (:anoInicial is null or banca.ano >= :anoInicial)
              and (:anoFinal is null or banca.ano <= :anoFinal)
              and (
                    :tipo is null
                    or (:tipo = 'Graduacao' and lower(trim(coalesce(banca.tipo, ''))) in ('tcc', 'graduacao', 'graduação'))
                    or (:tipo = 'Outra' and (banca.tipo is null or trim(banca.tipo) = '' or lower(trim(banca.tipo)) = 'outra'))
                    or lower(trim(banca.tipo)) = lower(trim(:tipo))
              )
            """)
    Page<Banca> findForDashboard(
            @Param("idLattes") String idLattes,
            @Param("anoInicial") Integer anoInicial,
            @Param("anoFinal") Integer anoFinal,
            @Param("tipo") String tipo,
            Pageable pageable
    );
}
