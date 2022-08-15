package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface TilskuddPeriodeRepository extends JpaRepository<TilskuddPeriode, UUID>, JpaSpecificationExecutor {

    @Override
    Optional<TilskuddPeriode> findById(UUID id);

}
