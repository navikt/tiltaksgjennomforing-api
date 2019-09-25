package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface AvtaleRepository extends CrudRepository<Avtale, UUID> {

    @Query("select a.id from avtale a where a.journalpost_id is null and a.godkjent_av_veileder is not null")
    List<UUID> finnAvtaleIdTilJournalfoering();

    @Override
    List<Avtale> findAllById(Iterable<UUID> idAvtaleTilJournalfoering);
}

