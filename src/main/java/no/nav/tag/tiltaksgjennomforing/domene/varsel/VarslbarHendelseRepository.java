package no.nav.tag.tiltaksgjennomforing.domene.varsel;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface VarslbarHendelseRepository extends CrudRepository<VarslbarHendelse, UUID> {
}
