package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AvtaleInnholdRepository extends JpaRepository<AvtaleInnhold, UUID> {

    @Query(value = "select ai from AvtaleInnhold ai where ai.journalpostId is null and ai.godkjentAvVeileder is not null")
    List<AvtaleInnhold> finnAvtaleVersjonerTilJournalfoering();
}

