package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArenaRyddeAvtaleRepository extends JpaRepository<ArenaRyddeAvtale, UUID>, JpaSpecificationExecutor {
    @Override
    Optional<ArenaRyddeAvtale> findById(UUID id);

}