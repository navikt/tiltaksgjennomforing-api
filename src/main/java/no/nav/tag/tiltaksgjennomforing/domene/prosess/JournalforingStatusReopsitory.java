package no.nav.tag.tiltaksgjennomforing.domene.prosess;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface JournalforingStatusReopsitory extends CrudRepository<StatusJournalforing, UUID> {
}
