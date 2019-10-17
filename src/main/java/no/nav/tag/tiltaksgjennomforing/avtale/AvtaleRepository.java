package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AvtaleRepository extends CrudRepository<Avtale, UUID> {

    @Query("select a.id from avtale a where a.journalpost_id is null and a.godkjent_av_veileder is not null")
    List<UUID> finnAvtaleIdTilJournalfoering();

    @Query("select a.Id from avtale a")
    List<Avtale> fetchAvtaler(/*@Param("base_avtale_id") UUID baseAvtaleId*/);
    @Override
    List<Avtale> findAllById(Iterable<UUID> idAvtaleTilJournalfoering);

    List<Avtale> findByVersjon(int versjon);
/*
    List<Avtale> findAllByBaseAvtaleIdAndGodkjentAvVeileder(UUID baseAvtaleId, LocalDateTime godkjentAvVeileder);
    List<Avtale> findAllByBaseAvtaleId(UUID baseAvtaleId);
*/
}

