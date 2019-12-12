package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AvtaleRepository extends JpaRepository<Avtale, UUID> {
    @Override
    List<Avtale> findAllById(Iterable<UUID> ids);

    @Override
    List<Avtale> findAll();
}

