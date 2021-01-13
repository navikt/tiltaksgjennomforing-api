package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TilskuddPeriodeRepository extends JpaRepository<TilskuddPeriode, UUID>, JpaSpecificationExecutor {

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    Optional<TilskuddPeriode> findById(UUID id);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    List<TilskuddPeriode> findAllById(Iterable<UUID> ids);


    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    List<TilskuddPeriode> findAll();

    List<TilskuddPeriode> findAllByStatus(TilskuddPeriodeStatus status);

}

