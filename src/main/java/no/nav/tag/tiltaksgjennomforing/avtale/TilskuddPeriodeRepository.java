package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TilskuddPeriodeRepository extends JpaRepository<TilskuddPeriode, UUID>, JpaSpecificationExecutor {

    @Override
    Optional<TilskuddPeriode> findById(UUID id);

    List<TilskuddPeriode> findAllByAvtaleAndSluttDatoBefore(Avtale avtale, LocalDate sluttDato);

    @Query("""
          select t from TilskuddPeriode t join fetch t.avtale av
          where av.tiltakstype = 'VTAO'
          and t.status = 'UBEHANDLET'
          and t.beløp is null and year(t.startDato) in (:aar)
        """)
    List<TilskuddPeriode> ubehandledeVtaoTilskuddUtenBelopForAar(Collection<Integer> aar);
}
