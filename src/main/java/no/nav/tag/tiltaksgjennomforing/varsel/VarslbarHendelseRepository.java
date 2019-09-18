package no.nav.tag.tiltaksgjennomforing.varsel;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface VarslbarHendelseRepository extends CrudRepository<VarslbarHendelse, UUID> {
}
