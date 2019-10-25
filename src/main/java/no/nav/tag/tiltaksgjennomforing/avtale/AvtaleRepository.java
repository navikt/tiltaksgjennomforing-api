package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface AvtaleRepository extends CrudRepository<Avtale, UUID> {

    @Query(value = "select a.id from Avtale a where a.journalpostId is null and a.godkjentAvVeileder is not null")
    List<UUID> finnAvtaleIdTilJournalfoering();

    @Override
    List<Avtale> findAllById(Iterable<UUID> idAvtaleTilJournalfoering);

    @Override
    List<Avtale> findAll();
}

