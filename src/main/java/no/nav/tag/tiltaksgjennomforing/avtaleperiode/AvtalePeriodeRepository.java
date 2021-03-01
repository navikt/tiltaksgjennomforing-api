package no.nav.tag.tiltaksgjennomforing.avtaleperiode;

import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface AvtalePeriodeRepository extends JpaRepository<AvtalePeriode, UUID>, JpaSpecificationExecutor {
    List<AvtalePeriode> findAllByAvtaleId(UUID avtaleId);
}

