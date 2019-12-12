package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AvtaleInnholdRepository extends JpaRepository<AvtaleInnhold, UUID> {
    @Override
    List<AvtaleInnhold> findAllById(Iterable<UUID> ids);

    @Query(value = "select distinct ai.avtale from AvtaleInnhold ai where ai.journalpostId is null and ai.godkjentAvVeileder is not null")
    List<UUID> finnAvtaleIdTilJournalfoering();
}

