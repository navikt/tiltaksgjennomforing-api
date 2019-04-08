package no.nav.tag.tiltaksgjennomforing.domene;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface AvtaleRepository extends CrudRepository<Avtale, UUID> {
}
